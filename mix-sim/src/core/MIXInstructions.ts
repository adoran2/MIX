import type { Operator } from './Operator';
import { createOperator } from './Operator';

/** All standard MIX operators from TAOCP pp.129+. Keyed by mnemonic name. */
const INSTRUCTIONS: Operator[] = [
  // Miscellaneous
  createOperator('NOP',   0, 0),
  // Arithmetic
  createOperator('ADD',   1, 5),
  createOperator('SUB',   2, 5),
  createOperator('MUL',   3, 5),
  createOperator('DIV',   4, 5),
  // Conversion / Halt
  createOperator('NUM',   5, 0),
  createOperator('CHAR',  5, 1),
  createOperator('HLT',   5, 2),
  // Shifts
  createOperator('SLA',   6, 0),
  createOperator('SRA',   6, 1),
  createOperator('SLAX',  6, 2),
  createOperator('SRAX',  6, 3),
  createOperator('SLC',   6, 4),
  createOperator('SRC',   6, 5),
  // Move
  createOperator('MOVE',  7, 1),
  // Load A
  createOperator('LDA',   8, 5),
  // Load index registers
  createOperator('LD1',   9, 5),
  createOperator('LD2',  10, 5),
  createOperator('LD3',  11, 5),
  createOperator('LD4',  12, 5),
  createOperator('LD5',  13, 5),
  createOperator('LD6',  14, 5),
  // Load X
  createOperator('LDX',  15, 5),
  // Load negative
  createOperator('LDAN', 16, 5),
  createOperator('LD1N', 17, 5),
  createOperator('LD2N', 18, 5),
  createOperator('LD3N', 19, 5),
  createOperator('LD4N', 20, 5),
  createOperator('LD5N', 21, 5),
  createOperator('LD6N', 22, 5),
  createOperator('LDXN', 23, 5),
  // Store
  createOperator('STA',  24, 5),
  createOperator('ST1',  25, 5),
  createOperator('ST2',  26, 5),
  createOperator('ST3',  27, 5),
  createOperator('ST4',  28, 5),
  createOperator('ST5',  29, 5),
  createOperator('ST6',  30, 5),
  createOperator('STX',  31, 5),
  createOperator('STJ',  32, 2),
  createOperator('STZ',  33, 5),
  // I/O
  createOperator('JBUS', 34, 0),
  createOperator('IOC',  35, 0),
  createOperator('IN',   36, 0),
  createOperator('OUT',  37, 0),
  createOperator('JRED', 38, 0),
  // Jumps
  createOperator('JMP',  39, 0),
  createOperator('JSJ',  39, 1),
  createOperator('JOV',  39, 2),
  createOperator('JNOV', 39, 3),
  createOperator('JL',   39, 4),
  createOperator('JE',   39, 5),
  createOperator('JG',   39, 6),
  createOperator('JGE',  39, 7),
  createOperator('JNE',  39, 8),
  createOperator('JLE',  39, 9),
  // A register jumps
  createOperator('JAN',  40, 0),
  createOperator('JAZ',  40, 1),
  createOperator('JAP',  40, 2),
  createOperator('JANN', 40, 3),
  createOperator('JANZ', 40, 4),
  createOperator('JANP', 40, 5),
  // I1 jumps
  createOperator('J1N',  41, 0),
  createOperator('J1Z',  41, 1),
  createOperator('J1P',  41, 2),
  createOperator('J1NN', 41, 3),
  createOperator('J1NZ', 41, 4),
  createOperator('J1NP', 41, 5),
  // I2 jumps
  createOperator('J2N',  42, 0),
  createOperator('J2Z',  42, 1),
  createOperator('J2P',  42, 2),
  createOperator('J2NN', 42, 3),
  createOperator('J2NZ', 42, 4),
  createOperator('J2NP', 42, 5),
  // I3 jumps
  createOperator('J3N',  43, 0),
  createOperator('J3Z',  43, 1),
  createOperator('J3P',  43, 2),
  createOperator('J3NN', 43, 3),
  createOperator('J3NZ', 43, 4),
  createOperator('J3NP', 43, 5),
  // I4 jumps
  createOperator('J4N',  44, 0),
  createOperator('J4Z',  44, 1),
  createOperator('J4P',  44, 2),
  createOperator('J4NN', 44, 3),
  createOperator('J4NZ', 44, 4),
  createOperator('J4NP', 44, 5),
  // I5 jumps
  createOperator('J5N',  45, 0),
  createOperator('J5Z',  45, 1),
  createOperator('J5P',  45, 2),
  createOperator('J5NN', 45, 3),
  createOperator('J5NZ', 45, 4),
  createOperator('J5NP', 45, 5),
  // I6 jumps
  createOperator('J6N',  46, 0),
  createOperator('J6Z',  46, 1),
  createOperator('J6P',  46, 2),
  createOperator('J6NN', 46, 3),
  createOperator('J6NZ', 46, 4),
  createOperator('J6NP', 46, 5),
  // X register jumps
  createOperator('JXN',  47, 0),
  createOperator('JXZ',  47, 1),
  createOperator('JXP',  47, 2),
  createOperator('JXNN', 47, 3),
  createOperator('JXNZ', 47, 4),
  createOperator('JXNP', 47, 5),
  // Inc/Dec/Enter A
  createOperator('INCA', 48, 0),
  createOperator('DECA', 48, 1),
  createOperator('ENTA', 48, 2),
  createOperator('ENNA', 48, 3),
  // Inc/Dec/Enter I1-I6
  createOperator('INC1', 49, 0),
  createOperator('DEC1', 49, 1),
  createOperator('ENT1', 49, 2),
  createOperator('ENN1', 49, 3),
  createOperator('INC2', 50, 0),
  createOperator('DEC2', 50, 1),
  createOperator('ENT2', 50, 2),
  createOperator('ENN2', 50, 3),
  createOperator('INC3', 51, 0),
  createOperator('DEC3', 51, 1),
  createOperator('ENT3', 51, 2),
  createOperator('ENN3', 51, 3),
  createOperator('INC4', 52, 0),
  createOperator('DEC4', 52, 1),
  createOperator('ENT4', 52, 2),
  createOperator('ENN4', 52, 3),
  createOperator('INC5', 53, 0),
  createOperator('DEC5', 53, 1),
  createOperator('ENT5', 53, 2),
  createOperator('ENN5', 53, 3),
  createOperator('INC6', 54, 0),
  createOperator('DEC6', 54, 1),
  createOperator('ENT6', 54, 2),
  createOperator('ENN6', 54, 3),
  // Inc/Dec/Enter X
  createOperator('INCX', 55, 0),
  createOperator('DECX', 55, 1),
  createOperator('ENTX', 55, 2),
  createOperator('ENNX', 55, 3),
  // Compare
  createOperator('CMPA', 56, 5),
  createOperator('CMP1', 57, 5),
  createOperator('CMP2', 58, 5),
  createOperator('CMP3', 59, 5),
  createOperator('CMP4', 60, 5),
  createOperator('CMP5', 61, 5),
  createOperator('CMP6', 62, 5),
  createOperator('CMPX', 63, 5),
];

/** Map from mnemonic name to Operator */
export const instructionsByName: Map<string, Operator> = new Map(
  INSTRUCTIONS.map(op => [op.name, op])
);

/** Lookup by (C, F) pair for decoding. Key = `${c}:${f}` */
export const instructionsByCF: Map<string, Operator> = new Map(
  INSTRUCTIONS.map(op => [`${op.cValue}:${op.fValue}`, op])
);

export function getInstructionByName(name: string): Operator | undefined {
  return instructionsByName.get(name);
}

export { INSTRUCTIONS };
