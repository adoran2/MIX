import { useState, useCallback, useRef } from 'react';
import { MIXMachine } from './core/MIXMachine';
import { RegisterPanel } from './components/RegisterPanel';
import { MemoryPanel } from './components/MemoryPanel';
import { ComparisonIndicator, OverflowIndicator } from './components/Indicators';
import { InputConsole } from './components/InputConsole';
import { LinePrinterDisplay } from './components/LinePrinterDisplay';
import { ControlConsole } from './components/ControlConsole';
import './App.css';

const base = import.meta.env.BASE_URL;

function App() {
  const machineRef = useRef(new MIXMachine());
  const [, forceUpdate] = useState(0);
  const [source, setSource] = useState('');
  const [assembleErrors, setAssembleErrors] = useState<string[]>([]);
  const [showInfo, setShowInfo] = useState(false);

  const rerender = useCallback(() => forceUpdate(n => n + 1), []);

  const machine = machineRef.current;

  const handleAssemble = useCallback(() => {
    machine.reset();
    const result = machine.assemble(source);
    setAssembleErrors(result.errors);
    rerender();
  }, [machine, source, rerender]);

  const handleStep = useCallback(() => {
    machine.step();
    rerender();
  }, [machine, rerender]);

  const handleRun = useCallback(() => {
    machine.run(100000);
    rerender();
  }, [machine, rerender]);

  const handleReset = useCallback(() => {
    machine.reset();
    setAssembleErrors([]);
    rerender();
  }, [machine, rerender]);

  return (
    <div className="app">
      <header className="app__header">
        <img src={`${base}MIX.jpg`} alt="MIX 1009" className="app__splash" />
        <div className="app__header-controls">
          <img src={`${base}MIXLogo.jpg`} alt="MIX Logo" className="app__logo" />
          <div className="app__header-text">
            <h1>MIX 1009 Simulator</h1>
            <p>A web-based simulator of Knuth's MIX computer from TAOCP</p>
          </div>
          <button className="app__info-btn" onClick={() => setShowInfo(true)}>Info</button>
        </div>
      </header>

      <div className="app__main">
        <div className="app__left">
          <InputConsole
            source={source}
            onSourceChange={setSource}
            onAssemble={handleAssemble}
            assembleErrors={assembleErrors}
          />
          <LinePrinterDisplay output={machine.output} />
        </div>

        <div className="app__center">
          <ControlConsole
            onStep={handleStep}
            onRun={handleRun}
            onReset={handleReset}
            halted={machine.halted}
            pc={machine.pc}
            clock={machine.clock}
          />
          <div className="app__indicators">
            <OverflowIndicator overflow={machine.overflowFlag} />
            <ComparisonIndicator state={machine.comparisonIndicator} />
          </div>
          <RegisterPanel
            aRegister={machine.aRegister}
            xRegister={machine.xRegister}
            iRegisters={machine.iRegisters}
            jRegister={machine.jRegister}
          />
          {machine.error && (
            <div className="app__error">
              Runtime Error: {machine.error}
            </div>
          )}
        </div>

        <div className="app__right">
          <MemoryPanel memory={machine.memory} pc={machine.pc} />
        </div>
      </div>

      <footer className="app__footer">
        MIX 1009 Simulator &mdash; Originally by Andrew Doran (1999) &mdash; Web port using React + TypeScript
      </footer>

      {showInfo && (
        <div className="app__modal-overlay" onClick={() => setShowInfo(false)}>
          <div className="app__modal" onClick={(e) => e.stopPropagation()}>
            <img src={`${base}MIXInfo.jpg`} alt="MIX 1009 Information" className="app__info-img" />
            <button className="app__modal-close" onClick={() => setShowInfo(false)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
