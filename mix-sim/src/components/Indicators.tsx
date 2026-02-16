import { ComparisonState } from '../core/types';
import '../styles/Indicators.css';

interface ComparisonProps {
  state: ComparisonState;
}

export function ComparisonIndicator({ state }: ComparisonProps) {
  return (
    <div className="indicator comparison-indicator">
      <div className="indicator__lights">
        <div className={`indicator__light indicator__light--e ${state === ComparisonState.EQUAL ? 'indicator__light--on' : ''}`}>
          <span>E</span>
        </div>
        <div className={`indicator__light indicator__light--l ${state === ComparisonState.LESS ? 'indicator__light--on' : ''}`}>
          <span>L</span>
        </div>
        <div className={`indicator__light indicator__light--g ${state === ComparisonState.GREATER ? 'indicator__light--on' : ''}`}>
          <span>G</span>
        </div>
      </div>
      <div className="indicator__label">
        Comparison
        <br />
        indicator
      </div>
    </div>
  );
}

interface OverflowProps {
  overflow: boolean;
}

export function OverflowIndicator({ overflow }: OverflowProps) {
  return (
    <div className="indicator overflow-indicator">
      <div className={`indicator__light indicator__light--overflow ${overflow ? 'indicator__light--overflow-on' : ''}`}>
      </div>
      <div className="indicator__label">
        Overflow
        <br />
        toggle
      </div>
    </div>
  );
}
