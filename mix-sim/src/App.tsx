import { useState, useCallback, useRef, useEffect } from 'react';
import { MIXMachine } from './core/MIXMachine';
import { RegisterPanel } from './components/RegisterPanel';
import { MemoryPanel } from './components/MemoryPanel';
import { ComparisonIndicator, OverflowIndicator } from './components/Indicators';
import { InputConsole } from './components/InputConsole';
import { LinePrinterDisplay } from './components/LinePrinterDisplay';
import { ControlConsole } from './components/ControlConsole';
import './App.css';

const base = import.meta.env.BASE_URL;

// Speed level -> interval in ms (0 = slowest, 5 = max/instant)
const SPEED_INTERVALS = [1000, 200, 50, 10, 2, 0];

function App() {
  const machineRef = useRef(new MIXMachine());
  const [, forceUpdate] = useState(0);
  const [source, setSource] = useState('');
  const [assembleErrors, setAssembleErrors] = useState<string[]>([]);
  const [showInfo, setShowInfo] = useState(false);
  const [running, setRunning] = useState(false);
  const [speed, setSpeed] = useState(2); // default: 20/sec
  const [memScrollPos, setMemScrollPos] = useState(0);
  const [memScrollTo, setMemScrollTo] = useState<number | undefined>(undefined);
  const runningRef = useRef(false);
  const speedRef = useRef(speed);

  const rerender = useCallback(() => forceUpdate(n => n + 1), []);

  const machine = machineRef.current;

  // Keep speedRef in sync
  useEffect(() => {
    speedRef.current = speed;
  }, [speed]);

  const scrollToPC = useCallback(() => {
    setMemScrollTo(machine.pc);
  }, [machine]);

  const handleAssemble = useCallback(() => {
    machine.reset();
    const result = machine.assemble(source);
    setAssembleErrors(result.errors);
    scrollToPC();
    rerender();
  }, [machine, source, rerender, scrollToPC]);

  const handleStep = useCallback(() => {
    machine.step();
    scrollToPC();
    rerender();
  }, [machine, rerender, scrollToPC]);

  const handleStop = useCallback(() => {
    runningRef.current = false;
    setRunning(false);
  }, []);

  const handleRun = useCallback(() => {
    if (machine.halted || machine.error) return;
    runningRef.current = true;
    setRunning(true);

    const tick = () => {
      if (!runningRef.current) return;
      const m = machineRef.current;
      if (m.halted || m.error) {
        runningRef.current = false;
        setRunning(false);
        setMemScrollTo(m.pc);
        forceUpdate(n => n + 1);
        return;
      }

      const interval = SPEED_INTERVALS[speedRef.current];

      if (interval === 0) {
        // Max speed: run in chunks, update UI periodically
        const chunkSize = 5000;
        m.run(chunkSize);
        setMemScrollTo(m.pc);
        forceUpdate(n => n + 1);
        if (!m.halted && !m.error && runningRef.current) {
          requestAnimationFrame(tick);
        } else {
          runningRef.current = false;
          setRunning(false);
        }
      } else {
        m.step();
        setMemScrollTo(m.pc);
        forceUpdate(n => n + 1);
        if (!m.halted && !m.error && runningRef.current) {
          setTimeout(tick, interval);
        } else {
          runningRef.current = false;
          setRunning(false);
        }
      }
    };

    tick();
  }, [machine]);

  const handleReset = useCallback(() => {
    runningRef.current = false;
    setRunning(false);
    machine.reset();
    setAssembleErrors([]);
    setMemScrollTo(0);
    rerender();
  }, [machine, rerender]);

  return (
    <div className="app">
      <header className="app__header">
        <img src={`${base}MIXLogo.jpg`} alt="MIX Logo" className="app__logo" />
        <div className="app__header-text">
          <h1>MIX 1009 Simulator</h1>
          <p>A web-based simulator of Knuth's MIX computer from TAOCP</p>
        </div>
        <button className="app__info-btn" onClick={() => setShowInfo(true)}>Info</button>
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
            onStop={handleStop}
            onReset={handleReset}
            halted={machine.halted}
            running={running}
            pc={machine.pc}
            clock={machine.clock}
            speed={speed}
            onSpeedChange={setSpeed}
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
          <MemoryPanel
            memory={machine.memory}
            pc={machine.pc}
            scrollTo={memScrollTo}
            onScrollChange={setMemScrollPos}
            scrollPos={memScrollPos}
          />
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
