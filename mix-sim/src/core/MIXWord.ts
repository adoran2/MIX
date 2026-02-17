import { Sign, MIX_BYTE_MAX, MIX_BYTE_MIN, WORD_SIZE, BASE } from './types';

/**
 * MIXWord is the fundamental storage unit: a sign + N bytes (default 5).
 * Each byte holds a value 0-63. Bytes also carry a "packed" flag for display grouping.
 */
export class MIXWord {
  sign: Sign;
  bytes: number[];
  packed: boolean[];
  readonly size: number;

  constructor(size: number = WORD_SIZE) {
    this.size = size;
    this.sign = Sign.PLUS;
    this.bytes = new Array(size).fill(0);
    this.packed = new Array(size).fill(true);
  }

  getValue(index: number): number {
    if (index < 0 || index >= this.size) throw new RangeError(`Byte index ${index} out of range [0, ${this.size - 1}]`);
    return this.bytes[index];
  }

  setValue(index: number, value: number): void {
    if (index < 0 || index >= this.size) throw new RangeError(`Byte index ${index} out of range [0, ${this.size - 1}]`);
    if (value < MIX_BYTE_MIN || value > MIX_BYTE_MAX) throw new RangeError(`Byte value ${value} out of range [0, 63]`);
    this.bytes[index] = value;
  }

  isPacked(index: number): boolean {
    if (index < 0 || index >= this.size) throw new RangeError(`Byte index ${index} out of range`);
    return this.packed[index];
  }

  setPacked(index: number, p: boolean): void {
    if (index < 0 || index >= this.size) throw new RangeError(`Byte index ${index} out of range`);
    this.packed[index] = p;
  }

  /** Convert word to its numeric (long) value, including sign. */
  toLong(): number {
    let value = 0;
    let power = 0;
    for (let i = this.size - 1; i >= 0; i--) {
      value += Math.pow(BASE, power) * this.bytes[i];
      power++;
    }
    if (this.sign === Sign.MINUS) value = -value;
    return value;
  }

  /** Set word from a numeric value. Sign is set automatically. */
  fromLong(num: number): void {
    if (num < 0) {
      this.sign = Sign.MINUS;
      num = -num;
    } else {
      this.sign = Sign.PLUS;
    }
    for (let i = this.size - 1; i >= 0; i--) {
      this.bytes[i] = num % BASE;
      num = Math.floor(num / BASE);
    }
  }

  /** Copy all data from another word of the same size. */
  copyFrom(other: MIXWord): void {
    this.sign = other.sign;
    for (let i = 0; i < Math.min(this.size, other.size); i++) {
      this.bytes[i] = other.bytes[i];
      this.packed[i] = other.packed[i];
    }
  }

  /** Reset to zero state. */
  clear(): void {
    this.sign = Sign.PLUS;
    this.bytes.fill(0);
    this.packed.fill(true);
  }

  toString(): string {
    return `${this.sign}${this.bytes.map(b => b.toString().padStart(2, ' ')).join('|')}`;
  }
}
