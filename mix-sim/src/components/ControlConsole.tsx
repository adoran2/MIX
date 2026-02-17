import '../styles/ControlConsole.css';

interface Props {
  onStep: () => void;
  onRun: () => void;
  onReset: () => void;
  halted: boolean;
  pc: number;
  clock: number;
}

export function ControlConsole({ onStep, onRun, onReset, halted, pc, clock }: Props) {
  return (
    <div className="control-console">
      <h3 className="control-console__title">Control Console</h3>
      <div className="control-console__buttons">
        <button className="control-console__btn control-console__btn--step" onClick={onStep} disabled={halted}>
          Step
        </button>
        <button className="control-console__btn control-console__btn--go" onClick={onRun} disabled={halted}>
          Go
        </button>
        <button className="control-console__btn control-console__btn--reset" onClick={onReset}>
          Reset
        </button>
      </div>
      <div className="control-console__status">
        <span>PC: {pc}</span>
        <span>Clock: {clock}u</span>
        {halted && <span className="control-console__halted">HALTED</span>}
      </div>
    </div>
  );
}
