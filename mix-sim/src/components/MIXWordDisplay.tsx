import { MIXWord } from '../core/MIXWord';
import '../styles/MIXWordDisplay.css';

interface Props {
  word: MIXWord;
  label?: string;
  compact?: boolean;
}

export function MIXWordDisplay({ word, label, compact }: Props) {
  return (
    <div className={`mix-word ${compact ? 'mix-word--compact' : ''}`}>
      {label && <span className="mix-word__label">{label}</span>}
      <div className="mix-word__cells">
        <span className="mix-word__sign">{word.sign}</span>
        {word.bytes.map((byte, i) => {
          const isPacked = word.isPacked(i);
          return (
            <span
              key={i}
              className={`mix-word__byte ${isPacked ? 'mix-word__byte--packed' : ''}`}
            >
              {byte.toString().padStart(2, '\u00A0')}
            </span>
          );
        })}
      </div>
      <span className="mix-word__value">{word.toLong()}</span>
    </div>
  );
}
