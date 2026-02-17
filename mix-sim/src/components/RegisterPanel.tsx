import { MIXWord } from '../core/MIXWord';
import { MIXWordDisplay } from './MIXWordDisplay';
import '../styles/RegisterPanel.css';

interface Props {
  aRegister: MIXWord;
  xRegister: MIXWord;
  iRegisters: MIXWord[];
  jRegister: MIXWord;
}

export function RegisterPanel({ aRegister, xRegister, iRegisters, jRegister }: Props) {
  return (
    <div className="register-panel">
      <h3 className="register-panel__title">Registers</h3>
      <div className="register-panel__group">
        <MIXWordDisplay word={aRegister} label="rA" />
        <MIXWordDisplay word={xRegister} label="rX" />
      </div>
      <div className="register-panel__group register-panel__index">
        {iRegisters.map((reg, i) => (
          <MIXWordDisplay key={i} word={reg} label={`rI${i + 1}`} compact />
        ))}
      </div>
      <div className="register-panel__group">
        <MIXWordDisplay word={jRegister} label="rJ" compact />
      </div>
    </div>
  );
}
