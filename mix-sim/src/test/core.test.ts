import { describe, it, expect, beforeEach } from 'vitest';
import { MIXWord } from '../core/MIXWord';
import { MIXMachine } from '../core/MIXMachine';
import { Sign, ComparisonState, BASE } from '../core/types';
import { SAMPLE_PROGRAMS } from '../core/samplePrograms';

describe('MIXWord', () => {
  let word: MIXWord;

  beforeEach(() => {
    word = new MIXWord();
  });

  it('initializes to +0|0|0|0|0', () => {
    expect(word.sign).toBe(Sign.PLUS);
    expect(word.toLong()).toBe(0);
    for (let i = 0; i < 5; i++) {
      expect(word.getValue(i)).toBe(0);
    }
  });

  it('sets and gets individual byte values', () => {
    word.setValue(0, 10);
    word.setValue(4, 63);
    expect(word.getValue(0)).toBe(10);
    expect(word.getValue(4)).toBe(63);
  });

  it('throws on out-of-range byte values', () => {
    expect(() => word.setValue(0, 64)).toThrow();
    expect(() => word.setValue(0, -1)).toThrow();
  });

  it('throws on out-of-range index', () => {
    expect(() => word.getValue(5)).toThrow();
    expect(() => word.getValue(-1)).toThrow();
  });

  it('converts to/from long correctly', () => {
    word.fromLong(12345);
    expect(word.toLong()).toBe(12345);
    expect(word.sign).toBe(Sign.PLUS);
  });

  it('handles negative values', () => {
    word.fromLong(-999);
    expect(word.toLong()).toBe(-999);
    expect(word.sign).toBe(Sign.MINUS);
  });

  it('handles zero correctly', () => {
    word.fromLong(0);
    expect(word.toLong()).toBe(0);
    expect(word.sign).toBe(Sign.PLUS);
  });

  it('handles max value (64^5 - 1 = 1073741823)', () => {
    const maxVal = Math.pow(64, 5) - 1;
    word.fromLong(maxVal);
    expect(word.toLong()).toBe(maxVal);
    for (let i = 0; i < 5; i++) {
      expect(word.getValue(i)).toBe(63);
    }
  });

  it('copies from another word', () => {
    const other = new MIXWord();
    other.fromLong(-42);
    other.setPacked(2, false);
    word.copyFrom(other);
    expect(word.toLong()).toBe(-42);
    expect(word.sign).toBe(Sign.MINUS);
    expect(word.isPacked(2)).toBe(false);
  });

  it('clears to zero', () => {
    word.fromLong(99999);
    word.clear();
    expect(word.toLong()).toBe(0);
    expect(word.sign).toBe(Sign.PLUS);
  });

  it('creates 2-byte word for index registers', () => {
    const iReg = new MIXWord(2);
    iReg.fromLong(100);
    expect(iReg.toLong()).toBe(100);
    expect(iReg.size).toBe(2);
  });
});

describe('MIXMachine - Basic Operations', () => {
  let machine: MIXMachine;

  beforeEach(() => {
    machine = new MIXMachine();
  });

  it('initializes with all zeros', () => {
    expect(machine.pc).toBe(0);
    expect(machine.clock).toBe(0);
    expect(machine.halted).toBe(false);
    expect(machine.aRegister.toLong()).toBe(0);
    expect(machine.xRegister.toLong()).toBe(0);
    expect(machine.comparisonIndicator).toBe(ComparisonState.OFF);
    expect(machine.overflowFlag).toBe(false);
  });

  it('resets to initial state', () => {
    machine.aRegister.fromLong(123);
    machine.pc = 50;
    machine.halted = true;
    machine.reset();
    expect(machine.aRegister.toLong()).toBe(0);
    expect(machine.pc).toBe(0);
    expect(machine.halted).toBe(false);
  });
});

describe('MIXMachine - Instruction Execution', () => {
  let machine: MIXMachine;

  beforeEach(() => {
    machine = new MIXMachine();
  });

  /** Helper to encode an instruction word into memory */
  function encode(loc: number, sign: Sign, addr: number, index: number, field: number, opcode: number): void {
    const w = machine.memory[loc];
    w.sign = sign;
    w.setValue(0, Math.floor(Math.abs(addr) / BASE) % BASE);
    w.setValue(1, Math.abs(addr) % BASE);
    w.setValue(2, index);
    w.setValue(3, field);
    w.setValue(4, opcode);
    for (let i = 0; i < 5; i++) w.setPacked(i, false);
  }

  describe('NOP', () => {
    it('advances PC by 1', () => {
      encode(0, Sign.PLUS, 0, 0, 0, 0); // NOP
      machine.step();
      expect(machine.pc).toBe(1);
      expect(machine.clock).toBe(1);
    });
  });

  describe('LDA', () => {
    it('loads full word from memory', () => {
      machine.memory[100].fromLong(-12345);
      encode(0, Sign.PLUS, 100, 0, 5, 8); // LDA 100(0:5)
      machine.step();
      expect(machine.aRegister.toLong()).toBe(-12345);
    });

    it('loads partial field (1:3)', () => {
      machine.memory[100].fromLong(0);
      machine.memory[100].sign = Sign.MINUS;
      machine.memory[100].setValue(0, 1);
      machine.memory[100].setValue(1, 2);
      machine.memory[100].setValue(2, 3);
      machine.memory[100].setValue(3, 4);
      machine.memory[100].setValue(4, 5);

      // LDA 100(1:3) => field = 8*1+3 = 11
      encode(0, Sign.PLUS, 100, 0, 11, 8);
      machine.step();
      // Bytes 0,1,2 of memory (field 1:3) loaded right-aligned into A
      // A should have sign=+, bytes [0,0,1,2,3]
      expect(machine.aRegister.sign).toBe(Sign.PLUS);
      expect(machine.aRegister.getValue(2)).toBe(1);
      expect(machine.aRegister.getValue(3)).toBe(2);
      expect(machine.aRegister.getValue(4)).toBe(3);
    });
  });

  describe('LDX', () => {
    it('loads full word into X register', () => {
      machine.memory[50].fromLong(999);
      encode(0, Sign.PLUS, 50, 0, 5, 15); // LDX 50(0:5)
      machine.step();
      expect(machine.xRegister.toLong()).toBe(999);
    });
  });

  describe('LDAN', () => {
    it('loads negated value', () => {
      machine.memory[100].fromLong(500);
      encode(0, Sign.PLUS, 100, 0, 5, 16); // LDAN 100(0:5)
      machine.step();
      expect(machine.aRegister.toLong()).toBe(-500);
    });
  });

  describe('STA', () => {
    it('stores A register to memory', () => {
      machine.aRegister.fromLong(-777);
      encode(0, Sign.PLUS, 200, 0, 5, 24); // STA 200(0:5)
      machine.step();
      expect(machine.memory[200].toLong()).toBe(-777);
    });
  });

  describe('STZ', () => {
    it('stores zero to memory', () => {
      machine.memory[200].fromLong(999);
      encode(0, Sign.PLUS, 200, 0, 5, 33); // STZ 200(0:5)
      machine.step();
      expect(machine.memory[200].toLong()).toBe(0);
    });
  });

  describe('ADD', () => {
    it('adds memory to A register', () => {
      machine.aRegister.fromLong(100);
      machine.memory[50].fromLong(200);
      encode(0, Sign.PLUS, 50, 0, 5, 1); // ADD 50(0:5)
      machine.step();
      expect(machine.aRegister.toLong()).toBe(300);
    });

    it('handles negative addition', () => {
      machine.aRegister.fromLong(100);
      machine.memory[50].fromLong(-150);
      encode(0, Sign.PLUS, 50, 0, 5, 1); // ADD 50(0:5)
      machine.step();
      expect(machine.aRegister.toLong()).toBe(-50);
    });
  });

  describe('SUB', () => {
    it('subtracts memory from A register', () => {
      machine.aRegister.fromLong(300);
      machine.memory[50].fromLong(100);
      encode(0, Sign.PLUS, 50, 0, 5, 2); // SUB 50(0:5)
      machine.step();
      expect(machine.aRegister.toLong()).toBe(200);
    });
  });

  describe('MUL', () => {
    it('multiplies A by memory value', () => {
      machine.aRegister.fromLong(10);
      machine.memory[50].fromLong(20);
      encode(0, Sign.PLUS, 50, 0, 5, 3); // MUL 50(0:5)
      machine.step();
      // Result = 200, fits in X register, A should be 0
      expect(machine.xRegister.toLong()).toBe(200);
    });
  });

  describe('DIV', () => {
    it('divides A:X by memory value', () => {
      machine.aRegister.fromLong(0);
      machine.xRegister.fromLong(200);
      machine.memory[50].fromLong(10);
      encode(0, Sign.PLUS, 50, 0, 5, 4); // DIV 50(0:5)
      machine.step();
      // quotient in A, remainder in X
      expect(machine.aRegister.toLong()).toBe(20);
      expect(machine.xRegister.toLong()).toBe(0);
    });

    it('sets overflow on division by zero', () => {
      machine.aRegister.fromLong(100);
      machine.memory[50].fromLong(0);
      encode(0, Sign.PLUS, 50, 0, 5, 4); // DIV 50(0:5)
      machine.step();
      expect(machine.overflowFlag).toBe(true);
    });
  });

  describe('HLT', () => {
    it('halts the machine', () => {
      encode(0, Sign.PLUS, 0, 0, 2, 5); // HLT
      machine.step();
      expect(machine.halted).toBe(true);
    });
  });

  describe('ENTA / ENNA', () => {
    it('ENTA sets A to address value', () => {
      encode(0, Sign.PLUS, 42, 0, 2, 48); // ENTA 42
      machine.step();
      expect(machine.aRegister.toLong()).toBe(42);
    });

    it('ENNA sets A to negative address value', () => {
      encode(0, Sign.PLUS, 42, 0, 3, 48); // ENNA 42
      machine.step();
      expect(machine.aRegister.toLong()).toBe(-42);
    });
  });

  describe('INCA / DECA', () => {
    it('INCA increments A', () => {
      machine.aRegister.fromLong(10);
      encode(0, Sign.PLUS, 5, 0, 0, 48); // INCA 5
      machine.step();
      expect(machine.aRegister.toLong()).toBe(15);
    });

    it('DECA decrements A', () => {
      machine.aRegister.fromLong(10);
      encode(0, Sign.PLUS, 3, 0, 1, 48); // DECA 3
      machine.step();
      expect(machine.aRegister.toLong()).toBe(7);
    });
  });

  describe('Index registers', () => {
    it('ENT1 sets I1', () => {
      encode(0, Sign.PLUS, 50, 0, 2, 49); // ENT1 50
      machine.step();
      expect(machine.iRegisters[0].toLong()).toBe(50);
    });

    it('INC1 increments I1', () => {
      machine.iRegisters[0].fromLong(10);
      encode(0, Sign.PLUS, 5, 0, 0, 49); // INC1 5
      machine.step();
      expect(machine.iRegisters[0].toLong()).toBe(15);
    });

    it('DEC1 decrements I1', () => {
      machine.iRegisters[0].fromLong(10);
      encode(0, Sign.PLUS, 3, 0, 1, 49); // DEC1 3
      machine.step();
      expect(machine.iRegisters[0].toLong()).toBe(7);
    });

    it('LD1 loads index register', () => {
      machine.memory[100].fromLong(42);
      encode(0, Sign.PLUS, 100, 0, 5, 9); // LD1 100(0:5)
      machine.step();
      expect(machine.iRegisters[0].toLong()).toBe(42);
    });

    it('uses index register for address modification', () => {
      machine.iRegisters[0].fromLong(10); // I1 = 10
      machine.memory[110].fromLong(999);  // Data at 100+10
      encode(0, Sign.PLUS, 100, 1, 5, 8); // LDA 100,1(0:5)
      machine.step();
      expect(machine.aRegister.toLong()).toBe(999);
    });
  });

  describe('Jumps', () => {
    it('JMP jumps and sets J register', () => {
      encode(0, Sign.PLUS, 100, 0, 0, 39); // JMP 100
      machine.step();
      expect(machine.pc).toBe(100);
      // J register should be 1 (return address)
      const jVal = machine.jRegister.getValue(0) * BASE + machine.jRegister.getValue(1);
      expect(jVal).toBe(1);
    });

    it('JSJ jumps without setting J', () => {
      machine.jRegister.fromLong(0);
      encode(0, Sign.PLUS, 200, 0, 1, 39); // JSJ 200
      machine.step();
      expect(machine.pc).toBe(200);
      expect(machine.jRegister.toLong()).toBe(0);
    });

    it('JOV jumps on overflow', () => {
      machine.overflowFlag = true;
      encode(0, Sign.PLUS, 50, 0, 2, 39); // JOV 50
      machine.step();
      expect(machine.pc).toBe(50);
      expect(machine.overflowFlag).toBe(false);
    });

    it('JOV does not jump without overflow', () => {
      machine.overflowFlag = false;
      encode(0, Sign.PLUS, 50, 0, 2, 39); // JOV 50
      machine.step();
      expect(machine.pc).toBe(1);
    });
  });

  describe('Comparison', () => {
    it('CMPA sets LESS when A < memory', () => {
      machine.aRegister.fromLong(10);
      machine.memory[50].fromLong(20);
      encode(0, Sign.PLUS, 50, 0, 5, 56); // CMPA 50(0:5)
      machine.step();
      expect(machine.comparisonIndicator).toBe(ComparisonState.LESS);
    });

    it('CMPA sets EQUAL when A == memory', () => {
      machine.aRegister.fromLong(42);
      machine.memory[50].fromLong(42);
      encode(0, Sign.PLUS, 50, 0, 5, 56); // CMPA 50(0:5)
      machine.step();
      expect(machine.comparisonIndicator).toBe(ComparisonState.EQUAL);
    });

    it('CMPA sets GREATER when A > memory', () => {
      machine.aRegister.fromLong(100);
      machine.memory[50].fromLong(50);
      encode(0, Sign.PLUS, 50, 0, 5, 56); // CMPA 50(0:5)
      machine.step();
      expect(machine.comparisonIndicator).toBe(ComparisonState.GREATER);
    });
  });

  describe('Conditional jumps on comparison', () => {
    it('JL jumps when LESS', () => {
      machine.comparisonIndicator = ComparisonState.LESS;
      encode(0, Sign.PLUS, 50, 0, 4, 39); // JL 50
      machine.step();
      expect(machine.pc).toBe(50);
    });

    it('JE jumps when EQUAL', () => {
      machine.comparisonIndicator = ComparisonState.EQUAL;
      encode(0, Sign.PLUS, 50, 0, 5, 39); // JE 50
      machine.step();
      expect(machine.pc).toBe(50);
    });

    it('JG jumps when GREATER', () => {
      machine.comparisonIndicator = ComparisonState.GREATER;
      encode(0, Sign.PLUS, 50, 0, 6, 39); // JG 50
      machine.step();
      expect(machine.pc).toBe(50);
    });

    it('JGE jumps when GREATER or EQUAL', () => {
      machine.comparisonIndicator = ComparisonState.EQUAL;
      encode(0, Sign.PLUS, 50, 0, 7, 39); // JGE 50
      machine.step();
      expect(machine.pc).toBe(50);
    });
  });

  describe('Register conditional jumps', () => {
    it('JAN jumps when A is negative', () => {
      machine.aRegister.fromLong(-5);
      encode(0, Sign.PLUS, 50, 0, 0, 40); // JAN 50
      machine.step();
      expect(machine.pc).toBe(50);
    });

    it('JAZ jumps when A is zero', () => {
      machine.aRegister.fromLong(0);
      encode(0, Sign.PLUS, 50, 0, 1, 40); // JAZ 50
      machine.step();
      expect(machine.pc).toBe(50);
    });

    it('JAP jumps when A is positive', () => {
      machine.aRegister.fromLong(10);
      encode(0, Sign.PLUS, 50, 0, 2, 40); // JAP 50
      machine.step();
      expect(machine.pc).toBe(50);
    });

    it('J1P jumps when I1 is positive', () => {
      machine.iRegisters[0].fromLong(5);
      encode(0, Sign.PLUS, 50, 0, 2, 41); // J1P 50
      machine.step();
      expect(machine.pc).toBe(50);
    });
  });

  describe('Shifts', () => {
    it('SLA shifts A left', () => {
      machine.aRegister.setValue(0, 1);
      machine.aRegister.setValue(1, 2);
      machine.aRegister.setValue(2, 3);
      machine.aRegister.setValue(3, 4);
      machine.aRegister.setValue(4, 5);
      encode(0, Sign.PLUS, 1, 0, 0, 6); // SLA 1
      machine.step();
      expect(machine.aRegister.getValue(0)).toBe(2);
      expect(machine.aRegister.getValue(1)).toBe(3);
      expect(machine.aRegister.getValue(2)).toBe(4);
      expect(machine.aRegister.getValue(3)).toBe(5);
      expect(machine.aRegister.getValue(4)).toBe(0);
    });

    it('SRA shifts A right', () => {
      machine.aRegister.setValue(0, 1);
      machine.aRegister.setValue(1, 2);
      machine.aRegister.setValue(2, 3);
      machine.aRegister.setValue(3, 4);
      machine.aRegister.setValue(4, 5);
      encode(0, Sign.PLUS, 1, 0, 1, 6); // SRA 1
      machine.step();
      expect(machine.aRegister.getValue(0)).toBe(0);
      expect(machine.aRegister.getValue(1)).toBe(1);
      expect(machine.aRegister.getValue(2)).toBe(2);
      expect(machine.aRegister.getValue(3)).toBe(3);
      expect(machine.aRegister.getValue(4)).toBe(4);
    });
  });

  describe('MOVE', () => {
    it('copies words and increments I1', () => {
      machine.memory[10].fromLong(100);
      machine.memory[11].fromLong(200);
      machine.memory[12].fromLong(300);
      machine.iRegisters[0].fromLong(20); // I1 = 20 (destination)
      encode(0, Sign.PLUS, 10, 0, 3, 7); // MOVE 10(3) - move 3 words
      machine.step();
      expect(machine.memory[20].toLong()).toBe(100);
      expect(machine.memory[21].toLong()).toBe(200);
      expect(machine.memory[22].toLong()).toBe(300);
      // I1 should be incremented to 23
      expect(machine.iRegisters[0].toLong()).toBe(23);
    });
  });
});

describe('MIXMachine - Assembler', () => {
  let machine: MIXMachine;

  beforeEach(() => {
    machine = new MIXMachine();
  });

  it('assembles a simple HLT program', () => {
    const result = machine.assemble(`         ORIG 0
         HLT
         END  0`);
    expect(result.errors).toHaveLength(0);
    expect(machine.memory[0].getValue(4)).toBe(5); // HLT opcode
    expect(machine.memory[0].getValue(3)).toBe(2); // HLT field
  });

  it('assembles ENTA instruction', () => {
    const result = machine.assemble(`         ORIG 0
         ENTA 42
         HLT
         END  0`);
    expect(result.errors).toHaveLength(0);
    // ENTA has opcode 48, field 2
    expect(machine.memory[0].getValue(4)).toBe(48);
    expect(machine.memory[0].getValue(3)).toBe(2);
    // Address should be 42
    const addr = machine.memory[0].getValue(0) * BASE + machine.memory[0].getValue(1);
    expect(addr).toBe(42);
  });

  it('resolves labels', () => {
    const result = machine.assemble(`         ORIG 0
START    ENTA 10
         HLT
         END  START`);
    expect(result.errors).toHaveLength(0);
    expect(result.startAddress).toBe(0);
  });

  it('handles CON directive', () => {
    const result = machine.assemble(`         ORIG 100
         CON  12345
         END  100`);
    expect(result.errors).toHaveLength(0);
    expect(machine.memory[100].toLong()).toBe(12345);
  });

  it('assembles and runs a simple program', () => {
    machine.assemble(`         ORIG 0
         ENTA 42
         HLT
         END  0`);
    machine.run();
    expect(machine.halted).toBe(true);
    expect(machine.aRegister.toLong()).toBe(42);
  });

  it('assembles and runs addition', () => {
    machine.assemble(`         ORIG 0
         LDA  NUM1
         ADD  NUM2
         STA  RESULT
         HLT
NUM1     CON  100
NUM2     CON  200
RESULT   CON  0
         END  0`);
    machine.run();
    expect(machine.halted).toBe(true);
    expect(machine.memory[6].toLong()).toBe(300); // RESULT at location 6
  });

  it('assembles loop with index register', () => {
    machine.assemble(`         ORIG 0
         ENT1 10
LOOP     DEC1 1
         J1P  LOOP
         HLT
         END  0`);
    machine.run();
    expect(machine.halted).toBe(true);
    expect(machine.iRegisters[0].toLong()).toBe(0);
  });
});

describe('MIXMachine - Integration', () => {
  let machine: MIXMachine;

  beforeEach(() => {
    machine = new MIXMachine();
  });

  it('runs count-down program correctly', () => {
    machine.assemble(`         ORIG 0
         ENT1 5
LOOP     DEC1 1
         J1P  LOOP
         HLT
         END  0`);
    machine.run();
    expect(machine.halted).toBe(true);
    expect(machine.iRegisters[0].toLong()).toBe(0);
  });

  it('runs program with comparison and conditional jump', () => {
    machine.assemble(`         ORIG 0
         ENTA 10
         STA  VAL
         ENTA 20
         CMPA VAL
         JG   BIGGER
         ENTA 0
         JMP  DONE
BIGGER   ENTA 1
DONE     HLT
VAL      CON  0
         END  0`);
    machine.run();
    expect(machine.halted).toBe(true);
    expect(machine.aRegister.toLong()).toBe(1); // 20 > 10, so BIGGER branch
  });

  it('assembles single-number field notation for I/O', () => {
    const result = machine.assemble(`         ORIG 0
         OUT  100(18)
         HLT
         END  0`);
    expect(result.errors).toHaveLength(0);
    // OUT opcode = 37, field should be 18
    expect(machine.memory[0].getValue(4)).toBe(37);
    expect(machine.memory[0].getValue(3)).toBe(18);
    const addr = machine.memory[0].getValue(0) * BASE + machine.memory[0].getValue(1);
    expect(addr).toBe(100);
  });

  it('resolves * as current location counter', () => {
    const result = machine.assemble(`         ORIG 10
         ENTA 0
         JMP  *-1
         HLT
         END  10`);
    expect(result.errors).toHaveLength(0);
    // JMP at location 11 should jump to 11-1 = 10
    const addr = machine.memory[11].getValue(0) * BASE + machine.memory[11].getValue(1);
    expect(addr).toBe(10);
  });

  it('assembles Primes sample program without errors', () => {
    const primes = SAMPLE_PROGRAMS.find(p => p.name === 'Primes');
    expect(primes).toBeDefined();
    const result = machine.assemble(primes!.source);
    expect(result.errors).toHaveLength(0);
    expect(result.startAddress).toBe(3000);
  });

  it('handles CHAR and NUM conversion', () => {
    // Set A register to numeric value 12345
    machine.aRegister.fromLong(12345);
    // Put CHAR instruction at 0, HLT at 1
    const charField = 1; // CHAR
    const w0 = machine.memory[0];
    w0.sign = Sign.PLUS;
    w0.setValue(0, 0); w0.setValue(1, 0); w0.setValue(2, 0);
    w0.setValue(3, charField); w0.setValue(4, 5);
    for (let i = 0; i < 5; i++) w0.setPacked(i, false);

    const w1 = machine.memory[1];
    w1.sign = Sign.PLUS;
    w1.setValue(0, 0); w1.setValue(1, 0); w1.setValue(2, 0);
    w1.setValue(3, 2); w1.setValue(4, 5); // HLT
    for (let i = 0; i < 5; i++) w1.setPacked(i, false);

    machine.run();
    expect(machine.halted).toBe(true);
    // X register bytes should contain digit chars (30+digit)
    // 12345 → A gets 00001, X gets 23 45
    // Actually X bytes should be: 30+1, 30+2, 30+3, 30+4, 30+5
    expect(machine.xRegister.getValue(0)).toBe(31); // '1'
    expect(machine.xRegister.getValue(1)).toBe(32); // '2'
    expect(machine.xRegister.getValue(2)).toBe(33); // '3'
    expect(machine.xRegister.getValue(3)).toBe(34); // '4'
    expect(machine.xRegister.getValue(4)).toBe(35); // '5'
  });
});
