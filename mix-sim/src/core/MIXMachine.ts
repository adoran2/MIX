import { MIXWord } from './MIXWord';
import { ComparisonState, Sign, MEMORY_SIZE, BASE, IREGISTER_SIZE, MIX_CHAR_TABLE, CHAR_TO_MIX } from './types';
import { instructionsByName } from './MIXInstructions';

export interface MachineState {
  memory: MIXWord[];
  aRegister: MIXWord;
  xRegister: MIXWord;
  iRegisters: MIXWord[];  // I1-I6 (index 0 = I1)
  jRegister: MIXWord;     // Always positive
  comparisonIndicator: ComparisonState;
  overflowFlag: boolean;
  pc: number;
  clock: number;
  halted: boolean;
  output: string[];       // Line printer output lines
  error: string | null;
}

export type MachineEvent = 'step' | 'halt' | 'error' | 'output' | 'reset';
export type MachineListener = (event: MachineEvent, state: MachineState) => void;

/**
 * MIXMachine — the core MIX1009 simulation engine.
 * Pure logic, no GUI dependencies.
 */
export class MIXMachine {
  memory: MIXWord[];
  aRegister: MIXWord;
  xRegister: MIXWord;
  iRegisters: MIXWord[];
  jRegister: MIXWord;
  comparisonIndicator: ComparisonState;
  overflowFlag: boolean;
  pc: number;
  clock: number;
  halted: boolean;
  output: string[];
  error: string | null;
  private listeners: MachineListener[] = [];

  constructor() {
    this.memory = [];
    for (let i = 0; i < MEMORY_SIZE; i++) {
      this.memory.push(new MIXWord());
    }
    this.aRegister = new MIXWord();
    this.xRegister = new MIXWord();
    this.iRegisters = [];
    for (let i = 0; i < 6; i++) {
      this.iRegisters.push(new MIXWord(IREGISTER_SIZE));
    }
    this.jRegister = new MIXWord(IREGISTER_SIZE);
    this.comparisonIndicator = ComparisonState.OFF;
    this.overflowFlag = false;
    this.pc = 0;
    this.clock = 0;
    this.halted = false;
    this.output = [];
    this.error = null;
  }

  addListener(listener: MachineListener): void {
    this.listeners.push(listener);
  }

  removeListener(listener: MachineListener): void {
    this.listeners = this.listeners.filter(l => l !== listener);
  }

  private emit(event: MachineEvent): void {
    const state = this.getState();
    for (const l of this.listeners) l(event, state);
  }

  getState(): MachineState {
    return {
      memory: this.memory,
      aRegister: this.aRegister,
      xRegister: this.xRegister,
      iRegisters: this.iRegisters,
      jRegister: this.jRegister,
      comparisonIndicator: this.comparisonIndicator,
      overflowFlag: this.overflowFlag,
      pc: this.pc,
      clock: this.clock,
      halted: this.halted,
      output: this.output,
      error: this.error,
    };
  }

  reset(): void {
    for (let i = 0; i < MEMORY_SIZE; i++) this.memory[i].clear();
    this.aRegister.clear();
    this.xRegister.clear();
    for (let i = 0; i < 6; i++) this.iRegisters[i].clear();
    this.jRegister.clear();
    this.comparisonIndicator = ComparisonState.OFF;
    this.overflowFlag = false;
    this.pc = 0;
    this.clock = 0;
    this.halted = false;
    this.output = [];
    this.error = null;
    this.emit('reset');
  }

  // ═══════════════════════════════════════════════
  //  ASSEMBLER
  // ═══════════════════════════════════════════════

  /**
   * Assemble a MIXAL program into memory.
   * Returns the start address (from END directive) or 0.
   */
  assemble(source: string): { startAddress: number; errors: string[] } {
    const errors: string[] = [];
    const symbols = new Map<string, number>();
    const lines = source.split('\n');
    let locCounter = 0;
    let startAddress = 0;

    // Forward reference patches: address to resolve later
    const patches: { address: number; symbol: string; lineNum: number }[] = [];

    // Assembler directives
    const directives = new Set(['ORIG', 'EQU', 'CON', 'ALF', 'END']);

    // First pass: collect labels and determine addresses
    const parsed: {
      label: string;
      op: string;
      operand: string;
      loc: number;
      lineNum: number;
    }[] = [];

    for (let lineNum = 0; lineNum < lines.length; lineNum++) {
      const raw = lines[lineNum];
      const line = raw.trimEnd();
      if (line.length === 0) continue;
      if (line[0] === '*') continue; // Comment line

      // Parse MIXAL format: LABEL  OP  OPERAND
      // Fields are separated by whitespace
      const tokens = line.split(/\s+/).filter(t => t.length > 0);
      if (tokens.length === 0) continue;

      let label = '';
      let op = '';
      let operand = '';

      // Detect if line starts with a label (first char is not space)
      if (line[0] !== ' ' && line[0] !== '\t') {
        label = tokens[0];
        op = tokens.length > 1 ? tokens[1] : '';
        operand = tokens.length > 2 ? tokens.slice(2).join(' ') : '';
      } else {
        op = tokens[0];
        operand = tokens.length > 1 ? tokens.slice(1).join(' ') : '';
      }

      // Strip comments (anything after double-space in operand)
      const commentIdx = operand.indexOf('  ');
      if (commentIdx >= 0) operand = operand.substring(0, commentIdx).trim();

      op = op.toUpperCase();

      if (label) {
        symbols.set(label, locCounter);
      }

      if (op === 'ORIG') {
        locCounter = this.evaluateExpression(operand, symbols, errors, lineNum);
        continue;
      }

      if (op === 'EQU') {
        if (label) {
          symbols.set(label, this.evaluateExpression(operand, symbols, errors, lineNum));
        }
        continue;
      }

      if (op === 'END') {
        startAddress = this.evaluateExpression(operand, symbols, errors, lineNum);
        parsed.push({ label, op, operand, loc: locCounter, lineNum });
        break;
      }

      parsed.push({ label, op, operand, loc: locCounter, lineNum });
      locCounter++;
    }

    // Second pass: generate code
    for (const entry of parsed) {
      const { op, operand, loc, lineNum } = entry;

      if (op === 'END') continue;

      if (op === 'CON') {
        const val = this.evaluateExpression(operand, symbols, errors, lineNum);
        this.memory[loc].fromLong(val);
        continue;
      }

      if (op === 'ALF') {
        // ALF stores 5 characters as MIX character codes
        let chars = operand;
        // Handle quoted format: ALF "HELLO" or ALF  HELLO
        if (chars.startsWith('"') && chars.endsWith('"')) {
          chars = chars.slice(1, -1);
        }
        // Pad or truncate to 5 characters
        chars = chars.padEnd(5, ' ').substring(0, 5);
        this.memory[loc].sign = Sign.PLUS;
        for (let i = 0; i < 5; i++) {
          const code = CHAR_TO_MIX.get(chars[i]);
          if (code !== undefined) {
            this.memory[loc].setValue(i, code);
          } else {
            this.memory[loc].setValue(i, 0); // space for unknown
            errors.push(`Line ${lineNum + 1}: Unknown character '${chars[i]}'`);
          }
          this.memory[loc].setPacked(i, false);
        }
        continue;
      }

      // Regular instruction
      const instrDef = instructionsByName.get(op);
      if (!instrDef) {
        errors.push(`Line ${lineNum + 1}: Unknown instruction '${op}'`);
        continue;
      }

      // Parse operand: ADDRESS,INDEX(FIELD)
      let address = 0;
      let index = 0;
      let field = instrDef.fValue;
      let addrSign: Sign = Sign.PLUS;

      if (operand.length > 0) {
        let remaining = operand;

        // Extract field spec (L:R) in parentheses
        const fieldMatch = remaining.match(/\((\d+):(\d+)\)$/);
        if (fieldMatch) {
          const L = parseInt(fieldMatch[1]);
          const R = parseInt(fieldMatch[2]);
          field = 8 * L + R;
          remaining = remaining.substring(0, remaining.indexOf('('));
        }

        // Extract index register ,N
        const indexMatch = remaining.match(/,(\d+)$/);
        if (indexMatch) {
          index = parseInt(indexMatch[1]);
          remaining = remaining.substring(0, remaining.indexOf(','));
        }

        // Evaluate address expression
        if (remaining.length > 0) {
          address = this.evaluateExpression(remaining, symbols, errors, lineNum);
          if (address < 0) {
            addrSign = Sign.MINUS;
            address = -address;
          }
        }
      }

      // Encode instruction into memory word:
      // Sign | Byte0 (addr high) | Byte1 (addr low) | Byte2 (index) | Byte3 (field) | Byte4 (opcode)
      if (loc >= 0 && loc < MEMORY_SIZE) {
        const word = this.memory[loc];
        word.sign = addrSign;
        word.setValue(0, Math.floor(address / BASE) % BASE);
        word.setValue(1, address % BASE);
        word.setValue(2, index);
        word.setValue(3, field);
        word.setValue(4, instrDef.cValue);
        for (let i = 0; i < 5; i++) word.setPacked(i, false);
      }
    }

    this.pc = startAddress;
    return { startAddress, errors };
  }

  private evaluateExpression(
    expr: string,
    symbols: Map<string, number>,
    errors: string[],
    lineNum: number
  ): number {
    expr = expr.trim();
    if (expr.length === 0) return 0;

    // Handle simple numeric literal
    if (/^[+-]?\d+$/.test(expr)) return parseInt(expr, 10);

    // Handle asterisk (current location counter is complex; just use 0 placeholder)
    if (expr === '*') return 0;

    // Handle symbol lookup
    const symVal = symbols.get(expr);
    if (symVal !== undefined) return symVal;

    // Handle simple arithmetic: A+B, A-B
    const addIdx = expr.lastIndexOf('+');
    if (addIdx > 0) {
      const left = this.evaluateExpression(expr.substring(0, addIdx), symbols, errors, lineNum);
      const right = this.evaluateExpression(expr.substring(addIdx + 1), symbols, errors, lineNum);
      return left + right;
    }
    const subIdx = expr.lastIndexOf('-');
    if (subIdx > 0) {
      const left = this.evaluateExpression(expr.substring(0, subIdx), symbols, errors, lineNum);
      const right = this.evaluateExpression(expr.substring(subIdx + 1), symbols, errors, lineNum);
      return left - right;
    }

    // Handle multiplication
    const mulIdx = expr.lastIndexOf('*');
    if (mulIdx > 0) {
      const left = this.evaluateExpression(expr.substring(0, mulIdx), symbols, errors, lineNum);
      const right = this.evaluateExpression(expr.substring(mulIdx + 1), symbols, errors, lineNum);
      return left * right;
    }

    // Handle division
    const divIdx = expr.lastIndexOf('/');
    if (divIdx > 0) {
      const left = this.evaluateExpression(expr.substring(0, divIdx), symbols, errors, lineNum);
      const right = this.evaluateExpression(expr.substring(divIdx + 1), symbols, errors, lineNum);
      return right !== 0 ? Math.floor(left / right) : 0;
    }

    errors.push(`Line ${lineNum + 1}: Cannot resolve expression '${expr}'`);
    return 0;
  }

  // ═══════════════════════════════════════════════
  //  EXECUTION ENGINE
  // ═══════════════════════════════════════════════

  /** Execute a single instruction at the current PC. Returns new PC. */
  step(): number {
    if (this.halted) return this.pc;
    if (this.pc < 0 || this.pc >= MEMORY_SIZE) {
      this.error = `PC out of range: ${this.pc}`;
      this.emit('error');
      return this.pc;
    }

    const word = this.memory[this.pc];
    const code = word.getValue(4);      // opcode C
    const field = word.getValue(3);     // field F
    const fStart = Math.floor(field / 8); // L
    const fEnd = field % 8;               // R
    const fOurStart = Math.max(fStart - 1, 0);
    const indexCode = word.getValue(2);

    // Compute effective address
    let addr = word.getValue(0) * BASE + word.getValue(1);
    if (word.sign === Sign.MINUS) addr = -addr;

    if (indexCode > 0 && indexCode <= 6) {
      const iReg = this.iRegisters[indexCode - 1];
      let iVal = iReg.getValue(0) * BASE + iReg.getValue(1);
      if (iReg.sign === Sign.MINUS) iVal = -iVal;
      addr += iVal;
    }

    this.error = null;

    switch (code) {
      case 0: // NOP
        this.pc++;
        this.clock++;
        break;

      case 1: // ADD
        this._execAdd(addr, fStart, fEnd, fOurStart);
        break;

      case 2: // SUB
        this._execSub(addr, fStart, fEnd, fOurStart);
        break;

      case 3: // MUL
        this._execMul(addr, fStart, fEnd, fOurStart);
        break;

      case 4: // DIV
        this._execDiv(addr, fStart, fEnd, fOurStart);
        break;

      case 5: // NUM/CHAR/HLT
        if (field === 0) this._execNUM();
        else if (field === 1) this._execCHAR();
        else if (field === 2) {
          this.halted = true;
          this.pc++;
          this.clock++;
          this.emit('halt');
          return this.pc;
        }
        break;

      case 6: // Shifts
        this._execShift(field, addr);
        break;

      case 7: // MOVE
        this._execMove(addr, field);
        break;

      case 8: // LDA
        this._execLoad(this.aRegister, addr, fStart, fEnd, fOurStart, false);
        this.pc++;
        this.clock += 2;
        break;

      case 9: case 10: case 11: case 12: case 13: case 14: // LD1-LD6
        this._execLoadIndex(code - 9, addr, fStart, fEnd, fOurStart, false);
        this.pc++;
        this.clock += 2;
        break;

      case 15: // LDX
        this._execLoad(this.xRegister, addr, fStart, fEnd, fOurStart, false);
        this.pc++;
        this.clock += 2;
        break;

      case 16: // LDAN
        this._execLoad(this.aRegister, addr, fStart, fEnd, fOurStart, true);
        this.pc++;
        this.clock += 2;
        break;

      case 17: case 18: case 19: case 20: case 21: case 22: // LD1N-LD6N
        this._execLoadIndex(code - 17, addr, fStart, fEnd, fOurStart, true);
        this.pc++;
        this.clock += 2;
        break;

      case 23: // LDXN
        this._execLoad(this.xRegister, addr, fStart, fEnd, fOurStart, true);
        this.pc++;
        this.clock += 2;
        break;

      case 24: // STA
        this._execStore(this.aRegister, addr, fStart, fEnd, fOurStart);
        this.pc++;
        this.clock += 2;
        break;

      case 25: case 26: case 27: case 28: case 29: case 30: // ST1-ST6
        this._execStore(this.iRegisters[code - 25], addr, fStart, fEnd, fOurStart);
        this.pc++;
        this.clock += 2;
        break;

      case 31: // STX
        this._execStore(this.xRegister, addr, fStart, fEnd, fOurStart);
        this.pc++;
        this.clock += 2;
        break;

      case 32: // STJ
        this._execStore(this.jRegister, addr, fStart, fEnd, fOurStart);
        this.pc++;
        this.clock += 2;
        break;

      case 33: // STZ
        this._execStoreZero(addr, fStart, fEnd, fOurStart);
        this.pc++;
        this.clock += 2;
        break;

      case 34: // JBUS - all our peripherals are always ready
        this.pc++;
        this.clock++;
        break;

      case 35: // IOC
        this.pc++;
        this.clock++;
        break;

      case 36: // IN - not implemented
        this.pc++;
        this.clock++;
        break;

      case 37: // OUT
        this._execOut(addr, field);
        this.pc++;
        break;

      case 38: // JRED - all peripherals ready, so always jump
        this._setJ(this.pc + 1);
        this.pc = addr;
        this.clock++;
        break;

      case 39: // Jump instructions
        this._execJump(field, addr);
        break;

      case 40: // JAN/JAZ/JAP/JANN/JANZ/JANP
        this._execRegJump(this.aRegister, field, addr);
        break;

      case 41: case 42: case 43: case 44: case 45: case 46: // J1-J6 jumps
        this._execRegJump(this.iRegisters[code - 41], field, addr);
        break;

      case 47: // JXN/JXZ/JXP/JXNN/JXNZ/JXNP
        this._execRegJump(this.xRegister, field, addr);
        break;

      case 48: // INCA/DECA/ENTA/ENNA
        this._execIncDecEnt(this.aRegister, field, addr);
        break;

      case 49: case 50: case 51: case 52: case 53: case 54: // INC/DEC/ENT/ENN for I1-I6
        this._execIncDecEnt(this.iRegisters[code - 49], field, addr);
        break;

      case 55: // INCX/DECX/ENTX/ENNX
        this._execIncDecEnt(this.xRegister, field, addr);
        break;

      case 56: // CMPA
        this._execCompare(this.aRegister, addr, fStart, fEnd, fOurStart);
        break;

      case 57: case 58: case 59: case 60: case 61: case 62: // CMP1-CMP6
        this._execCompare(this.iRegisters[code - 57], addr, fStart, fEnd, fOurStart);
        break;

      case 63: // CMPX
        this._execCompare(this.xRegister, addr, fStart, fEnd, fOurStart);
        break;

      default:
        this.error = `Unknown opcode ${code} at address ${this.pc}`;
        this.emit('error');
        return this.pc;
    }

    this.emit('step');
    return this.pc;
  }

  /** Run until halt or error, with an optional step limit. */
  run(maxSteps: number = 1000000): void {
    let steps = 0;
    while (!this.halted && !this.error && steps < maxSteps) {
      this.step();
      steps++;
    }
  }

  // ═══════════════════════════════════════════════
  //  INSTRUCTION IMPLEMENTATIONS
  // ═══════════════════════════════════════════════

  /** Extract a numeric value from memory[addr] using field spec (fStart..fEnd). */
  private _extractValue(addr: number, fStart: number, fEnd: number, fOurStart: number): number {
    const mem = this.memory[addr];
    let value = 0;
    let power = 0;
    for (let i = fEnd - 1; i >= fOurStart; i--) {
      value += Math.pow(BASE, power) * mem.getValue(i);
      power++;
    }
    if (fStart === 0 && mem.sign === Sign.MINUS) value = -value;
    return value;
  }

  private _execAdd(addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const value = this._extractValue(addr, fStart, fEnd, fOurStart);
    this._mADD(this.aRegister, value);
    this.pc++;
    this.clock += 2;
  }

  private _execSub(addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const value = this._extractValue(addr, fStart, fEnd, fOurStart);
    this._mADD(this.aRegister, -value);
    this.pc++;
    this.clock += 2;
  }

  private _execMul(addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const value = this._extractValue(addr, fStart, fEnd, fOurStart);
    this._mMUL(value);
    this.pc++;
    this.clock += 10;
  }

  private _execDiv(addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const value = this._extractValue(addr, fStart, fEnd, fOurStart);
    this._mDIV(value);
    this.pc++;
    this.clock += 12;
  }

  private _execNUM(): void {
    this._mNUM();
    this.pc++;
    this.clock += 10;
  }

  private _execCHAR(): void {
    this._mCHAR();
    this.pc++;
    this.clock += 10;
  }

  private _execShift(field: number, shiftAmount: number): void {
    switch (field) {
      case 0: this._mSLA(shiftAmount); break;
      case 1: this._mSRA(shiftAmount); break;
      case 2: this._mSLAX(shiftAmount, false); break;
      case 3: this._mSRAX(shiftAmount, false); break;
      case 4: this._mSLAX(shiftAmount, true); break;  // SLC
      case 5: this._mSRAX(shiftAmount, true); break;   // SRC
    }
    this.pc++;
    this.clock += 2;
  }

  private _execMove(addr: number, field: number): void {
    if (field === 0) { this.pc++; this.clock++; return; }
    const iReg = this.iRegisters[0]; // I1
    let dest = iReg.getValue(0) * BASE + iReg.getValue(1);
    if (iReg.sign === Sign.MINUS) dest = -dest;

    for (let i = 0; i < field; i++) {
      if (addr + i >= 0 && addr + i < MEMORY_SIZE && dest + i >= 0 && dest + i < MEMORY_SIZE) {
        this.memory[dest + i].copyFrom(this.memory[addr + i]);
      }
    }

    // Increment I1 by field
    let newI1 = dest + field;
    iReg.sign = newI1 < 0 ? Sign.MINUS : Sign.PLUS;
    newI1 = Math.abs(newI1);
    iReg.setValue(0, Math.floor(newI1 / BASE) % BASE);
    iReg.setValue(1, newI1 % BASE);

    this.pc++;
    this.clock += 1 + 2 * field;
  }

  private _execLoad(reg: MIXWord, addr: number, fStart: number, fEnd: number, fOurStart: number, negate: boolean): void {
    const mem = this.memory[addr];

    // Set sign
    if (fStart === 0) {
      const memSign = mem.sign;
      reg.sign = negate ? (memSign === Sign.PLUS ? Sign.MINUS : Sign.PLUS) : memSign;
    } else {
      reg.sign = negate ? Sign.MINUS : Sign.PLUS;
    }

    // Load bytes right-aligned into register
    let counter = reg.size - 1;
    for (let i = fEnd - 1; i >= fOurStart; i--) {
      if (counter >= 0) {
        reg.setValue(counter, mem.getValue(i));
        reg.setPacked(counter, mem.isPacked(i));
        counter--;
      }
    }
    // Zero remaining high bytes
    for (let i = counter; i >= 0; i--) {
      reg.setValue(i, 0);
      reg.setPacked(i, false);
    }
  }

  private _execLoadIndex(regIdx: number, addr: number, fStart: number, fEnd: number, fOurStart: number, negate: boolean): void {
    // Index registers are 2-byte; _execLoad handles right-alignment,
    // so loading from a wider field naturally takes the lowest 2 bytes.
    this._execLoad(this.iRegisters[regIdx], addr, fStart, fEnd, fOurStart, negate);
  }

  private _execStore(reg: MIXWord, addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const mem = this.memory[addr];

    if (fStart === 0) {
      mem.sign = reg.sign;
    }

    // Store bytes from register right-aligned into memory field
    let counter = reg.size - 1;
    for (let i = fEnd - 1; i >= fOurStart; i--) {
      if (counter >= 0) {
        mem.setValue(i, reg.getValue(counter));
        mem.setPacked(i, reg.isPacked(counter));
        counter--;
      } else {
        mem.setValue(i, 0);
        mem.setPacked(i, false);
      }
    }
  }

  private _execStoreZero(addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const mem = this.memory[addr];
    if (fStart === 0) {
      mem.sign = Sign.PLUS;
    }
    for (let i = fEnd - 1; i >= fOurStart; i--) {
      mem.setValue(i, 0);
      mem.setPacked(i, false);
    }
  }

  private _execOut(addr: number, field: number): void {
    if (field === 18) { // Line printer
      let line = '';
      for (let i = 0; i < 24; i++) {
        if (addr + i < MEMORY_SIZE) {
          const w = this.memory[addr + i];
          for (let b = 0; b < 5; b++) {
            const charCode = w.getValue(b);
            if (charCode >= 0 && charCode < MIX_CHAR_TABLE.length) {
              line += MIX_CHAR_TABLE[charCode];
            } else {
              line += '?';
            }
          }
        }
      }
      this.output.push(line);
      this.clock += 24;
      this.emit('output');
    } else {
      this.clock++;
    }
  }

  private _setJ(returnAddr: number): void {
    this.jRegister.sign = Sign.PLUS;
    this.jRegister.setValue(0, Math.floor(returnAddr / BASE) % BASE);
    this.jRegister.setValue(1, returnAddr % BASE);
  }

  private _execJump(field: number, addr: number): void {
    switch (field) {
      case 0: // JMP
        this._setJ(this.pc + 1);
        this.pc = addr;
        break;
      case 1: // JSJ
        this.pc = addr;
        break;
      case 2: // JOV
        if (this.overflowFlag) {
          this._setJ(this.pc + 1);
          this.pc = addr;
          this.overflowFlag = false;
        } else {
          this.pc++;
        }
        break;
      case 3: // JNOV
        if (this.overflowFlag) {
          this.pc++;
          this.overflowFlag = false;
        } else {
          this._setJ(this.pc + 1);
          this.pc = addr;
        }
        break;
      case 4: // JL
        if (this.comparisonIndicator === ComparisonState.LESS) {
          this._setJ(this.pc + 1);
          this.pc = addr;
        } else this.pc++;
        break;
      case 5: // JE
        if (this.comparisonIndicator === ComparisonState.EQUAL) {
          this._setJ(this.pc + 1);
          this.pc = addr;
        } else this.pc++;
        break;
      case 6: // JG
        if (this.comparisonIndicator === ComparisonState.GREATER) {
          this._setJ(this.pc + 1);
          this.pc = addr;
        } else this.pc++;
        break;
      case 7: // JGE
        if (this.comparisonIndicator === ComparisonState.GREATER || this.comparisonIndicator === ComparisonState.EQUAL) {
          this._setJ(this.pc + 1);
          this.pc = addr;
        } else this.pc++;
        break;
      case 8: // JNE
        if (this.comparisonIndicator !== ComparisonState.EQUAL) {
          this._setJ(this.pc + 1);
          this.pc = addr;
        } else this.pc++;
        break;
      case 9: // JLE
        if (this.comparisonIndicator === ComparisonState.LESS || this.comparisonIndicator === ComparisonState.EQUAL) {
          this._setJ(this.pc + 1);
          this.pc = addr;
        } else this.pc++;
        break;
    }
    this.clock++;
  }

  private _execRegJump(reg: MIXWord, field: number, addr: number): void {
    const value = reg.toLong();
    let jump = false;

    switch (field) {
      case 0: jump = value < 0; break;        // Negative
      case 1: jump = value === 0; break;       // Zero
      case 2: jump = value > 0; break;         // Positive
      case 3: jump = value >= 0; break;        // Non-negative
      case 4: jump = value !== 0; break;       // Non-zero
      case 5: jump = value <= 0; break;        // Non-positive
    }

    if (jump) {
      this._setJ(this.pc + 1);
      this.pc = addr;
    } else {
      this.pc++;
    }
    this.clock++;
  }

  private _execIncDecEnt(reg: MIXWord, field: number, addr: number): void {
    switch (field) {
      case 0: // INC
        this._mADD(reg, addr);
        break;
      case 1: // DEC
        this._mADD(reg, -addr);
        break;
      case 2: // ENT
        reg.fromLong(addr);
        break;
      case 3: // ENN
        reg.fromLong(-addr);
        break;
    }
    this.pc++;
    this.clock++;
  }

  private _execCompare(reg: MIXWord, addr: number, fStart: number, fEnd: number, fOurStart: number): void {
    const memValue = this._extractValue(addr, fStart, fEnd, fOurStart);

    // Extract same field from register
    let regValue = 0;
    let power = 0;
    const regStart = fOurStart;
    const regEnd = fEnd;
    // For index registers (2-byte), adjust indices
    const maxIdx = reg.size - 1;
    for (let i = Math.min(regEnd - 1, maxIdx); i >= Math.min(regStart, maxIdx); i--) {
      if (i >= 0 && i < reg.size) {
        regValue += Math.pow(BASE, power) * reg.getValue(i);
      }
      power++;
    }
    if (fStart === 0) {
      if (reg.sign === Sign.MINUS) regValue = -regValue;
    }

    if (regValue < memValue) {
      this.comparisonIndicator = ComparisonState.LESS;
    } else if (regValue > memValue) {
      this.comparisonIndicator = ComparisonState.GREATER;
    } else {
      this.comparisonIndicator = ComparisonState.EQUAL;
    }
    this.pc++;
    this.clock += 2;
  }

  // ═══════════════════════════════════════════════
  //  ARITHMETIC HELPERS
  // ═══════════════════════════════════════════════

  private _mADD(reg: MIXWord, value: number): void {
    let current = reg.toLong();
    current += value;

    const maxVal = Math.pow(BASE, reg.size) - 1;
    if (Math.abs(current) > maxVal) {
      this.overflowFlag = true;
    }

    reg.fromLong(current > maxVal ? current % (maxVal + 1) : current < -(maxVal) ? -((-current) % (maxVal + 1)) : current);
  }

  private _mMUL(value: number): void {
    const aVal = this.aRegister.toLong();
    let result = aVal * value;

    if (result < 0) {
      this.aRegister.sign = Sign.MINUS;
      this.xRegister.sign = Sign.MINUS;
      result = -result;
    } else {
      this.aRegister.sign = Sign.PLUS;
      this.xRegister.sign = Sign.PLUS;
    }

    // Pack into X (low bytes)
    for (let i = 4; i >= 0; i--) {
      this.xRegister.setValue(i, result % BASE);
      result = Math.floor(result / BASE);
    }
    // Pack remainder into A (high bytes)
    for (let i = 4; i >= 0; i--) {
      this.aRegister.setValue(i, result % BASE);
      result = Math.floor(result / BASE);
    }
  }

  private _mDIV(denominator: number): void {
    if (denominator === 0) {
      this.overflowFlag = true;
      return;
    }

    // Compose 10-byte numerator from A:X
    let numerator = 0;
    let aPower = 5;
    let xPower = 0;
    for (let i = 4; i >= 0; i--) {
      numerator += Math.pow(BASE, aPower) * this.aRegister.getValue(i);
      numerator += Math.pow(BASE, xPower) * this.xRegister.getValue(i);
      aPower++;
      xPower++;
    }
    if (this.aRegister.sign === Sign.MINUS) numerator = -numerator;

    const quotient = Math.trunc(numerator / denominator);
    const remainder = numerator - quotient * denominator;

    const maxVal = Math.pow(BASE, 5) - 1;
    if (Math.abs(quotient) > maxVal) {
      this.overflowFlag = true;
      return;
    }

    // Store sign
    this.xRegister.sign = this.aRegister.sign; // remainder sign = A sign
    this.aRegister.sign = quotient < 0 ? Sign.MINUS : Sign.PLUS;

    // Pack quotient into A
    let q = Math.abs(quotient);
    for (let i = 4; i >= 0; i--) {
      this.aRegister.setValue(i, q % BASE);
      q = Math.floor(q / BASE);
    }

    // Pack remainder into X
    let r = Math.abs(remainder);
    for (let i = 4; i >= 0; i--) {
      this.xRegister.setValue(i, r % BASE);
      r = Math.floor(r / BASE);
    }
  }

  private _mNUM(): void {
    let numVal = 0;
    let aMult = 100000;
    let xMult = 1;

    for (let i = 4; i >= 0; i--) {
      numVal += (this.aRegister.getValue(i) % 10) * aMult;
      numVal += (this.xRegister.getValue(i) % 10) * xMult;
      aMult = Math.floor(aMult / 10);
      xMult *= 10;
    }

    const savedSign = this.aRegister.sign;
    this.aRegister.fromLong(numVal);
    this.aRegister.sign = savedSign;

    const maxVal = Math.pow(BASE, 5) - 1;
    if (numVal > maxVal) this.overflowFlag = true;
  }

  private _mCHAR(): void {
    let numVal = Math.abs(this.aRegister.toLong());

    for (let i = 4; i >= 0; i--) {
      this.xRegister.setValue(i, (numVal % 10) + 30);
      this.xRegister.setPacked(i, false);
      numVal = Math.floor(numVal / 10);
    }
    for (let i = 4; i >= 0; i--) {
      this.aRegister.setValue(i, (numVal % 10) + 30);
      this.aRegister.setPacked(i, false);
      numVal = Math.floor(numVal / 10);
    }
  }

  private _mSLA(shift: number): void {
    for (let s = 0; s < shift; s++) {
      for (let i = 0; i < 4; i++) {
        this.aRegister.setValue(i, this.aRegister.getValue(i + 1));
      }
      this.aRegister.setValue(4, 0);
    }
  }

  private _mSRA(shift: number): void {
    for (let s = 0; s < shift; s++) {
      for (let i = 4; i > 0; i--) {
        this.aRegister.setValue(i, this.aRegister.getValue(i - 1));
      }
      this.aRegister.setValue(0, 0);
    }
  }

  private _mSLAX(shift: number, circular: boolean): void {
    for (let s = 0; s < shift; s++) {
      const saved = this.aRegister.getValue(0);
      for (let i = 0; i < 4; i++) {
        this.aRegister.setValue(i, this.aRegister.getValue(i + 1));
      }
      this.aRegister.setValue(4, this.xRegister.getValue(0));
      for (let i = 0; i < 4; i++) {
        this.xRegister.setValue(i, this.xRegister.getValue(i + 1));
      }
      this.xRegister.setValue(4, circular ? saved : 0);
    }
  }

  private _mSRAX(shift: number, circular: boolean): void {
    for (let s = 0; s < shift; s++) {
      const saved = this.xRegister.getValue(4);
      for (let i = 4; i > 0; i--) {
        this.xRegister.setValue(i, this.xRegister.getValue(i - 1));
      }
      this.xRegister.setValue(0, this.aRegister.getValue(4));
      for (let i = 4; i > 0; i--) {
        this.aRegister.setValue(i, this.aRegister.getValue(i - 1));
      }
      this.aRegister.setValue(0, circular ? saved : 0);
    }
  }
}
