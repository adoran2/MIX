import '../styles/ControlConsole.css';

interface Props {
  onStep: () => void;
  onRun: () => void;
  onStop: () => void;
  onReset: () => void;
  halted: boolean;
  running: boolean;
  pc: number;
  clock: number;
  speed: number;
  onSpeedChange: (speed: number) => void;
}

const SPEED_LABELS: Record<number, string> = {
  0: '1/sec',
  1: '5/sec',
  2: '20/sec',
  3: '100/sec',
  4: '500/sec',
  5: 'Max',
};

export function ControlConsole({ onStep, onRun, onStop, onReset, halted, running, pc, clock, speed, onSpeedChange }: Props) {
  return (
    <div className="control-console">
      <h3 className="control-console__title">Control Console</h3>
      <div className="control-console__buttons">
        <button className="control-console__btn control-console__btn--step" onClick={onStep} disabled={halted || running}>
          Step
        </button>
        {!running ? (
          <button className="control-console__btn control-console__btn--go" onClick={onRun} disabled={halted}>
            Go
          </button>
        ) : (
          <button className="control-console__btn control-console__btn--stop" onClick={onStop}>
            Stop
          </button>
        )}
        <button className="control-console__btn control-console__btn--reset" onClick={onReset} disabled={running}>
          Reset
        </button>
      </div>
      <div className="control-console__speed">
        <label className="control-console__speed-label">Speed:</label>
        <input
          type="range"
          className="control-console__speed-slider"
          min={0}
          max={5}
          step={1}
          value={speed}
          onChange={(e) => onSpeedChange(parseInt(e.target.value))}
          disabled={running}
        />
        <span className="control-console__speed-value">{SPEED_LABELS[speed]}</span>
      </div>
      <div className="control-console__status">
        <span>PC: {pc}</span>
        <span>Clock: {clock}u</span>
        {running && <span className="control-console__running">RUNNING</span>}
        {halted && <span className="control-console__halted">HALTED</span>}
      </div>
    </div>
  );
}
