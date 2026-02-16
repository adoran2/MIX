export enum ComparisonState {
  OFF = 0,
  LESS = 1,
  GREATER = 2,
  EQUAL = 3,
}

export enum Sign {
  PLUS = '+',
  MINUS = '-',
}

export const MIX_BYTE_MAX = 63;
export const MIX_BYTE_MIN = 0;
export const MEMORY_SIZE = 4000;
export const WORD_SIZE = 5;
export const IREGISTER_SIZE = 2;
export const BASE = 64;

/** MIX character set (56 characters, indices 0-55) per TAOCP */
export const MIX_CHAR_TABLE: string[] = [
  ' ',  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',  // 0-9
  '\u0394', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', // 10-19
  '\u03A3', '\u03A0', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', // 20-29
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',  // 30-39
  '.', ',', '(', ')', '+', '-', '*', '/', '=', '$',    // 40-49
  '<', '>', '@', ';', ':', "'",                          // 50-55
];

/** Reverse lookup: character to MIX code */
export const CHAR_TO_MIX: Map<string, number> = new Map(
  MIX_CHAR_TABLE.map((ch, i) => [ch, i])
);
