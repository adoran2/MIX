/** Represents a single MIX instruction definition with opcode (C), default field (F), and mnemonic name. */
export interface Operator {
  name: string;
  cValue: number;
  fValue: number;
}

export function createOperator(name: string, cValue: number, fValue: number): Operator {
  return { name, cValue, fValue };
}
