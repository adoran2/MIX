import { SAMPLE_PROGRAMS } from '../core/samplePrograms';
import '../styles/InputConsole.css';

interface Props {
  source: string;
  onSourceChange: (source: string) => void;
  onAssemble: () => void;
  assembleErrors: string[];
}

export function InputConsole({ source, onSourceChange, onAssemble, assembleErrors }: Props) {
  const loadSample = (idx: number) => {
    onSourceChange(SAMPLE_PROGRAMS[idx].source);
  };

  return (
    <div className="input-console">
      <div className="input-console__header">
        <h3 className="input-console__title">MIXAL Program Editor</h3>
        <div className="input-console__actions">
          <select
            className="input-console__sample-select"
            onChange={(e) => {
              if (e.target.value !== '') loadSample(parseInt(e.target.value));
            }}
            defaultValue=""
          >
            <option value="" disabled>Load sample...</option>
            {SAMPLE_PROGRAMS.map((p, i) => (
              <option key={i} value={i}>{p.name}</option>
            ))}
          </select>
          <button className="input-console__assemble-btn" onClick={onAssemble}>
            Assemble
          </button>
        </div>
      </div>
      <textarea
        className="input-console__editor"
        value={source}
        onChange={(e) => onSourceChange(e.target.value)}
        spellCheck={false}
        placeholder={`* Enter MIXAL program here\n* Lines starting with * are comments\n         ORIG 0\n         ENTA 42\n         HLT\n         END  0`}
      />
      {assembleErrors.length > 0 && (
        <div className="input-console__errors">
          {assembleErrors.map((err, i) => (
            <div key={i} className="input-console__error">{err}</div>
          ))}
        </div>
      )}
    </div>
  );
}
