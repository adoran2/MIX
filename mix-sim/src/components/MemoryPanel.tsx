import { useState } from 'react';
import { MIXWord } from '../core/MIXWord';
import { MIXWordDisplay } from './MIXWordDisplay';
import { MEMORY_SIZE } from '../core/types';
import '../styles/MemoryPanel.css';

interface Props {
  memory: MIXWord[];
  pc: number;
}

const VISIBLE_ROWS = 16;

export function MemoryPanel({ memory, pc }: Props) {
  const [scrollPos, setScrollPos] = useState(0);

  const handleScroll = (e: React.ChangeEvent<HTMLInputElement>) => {
    setScrollPos(parseInt(e.target.value));
  };

  const jumpToPC = () => {
    setScrollPos(Math.max(0, Math.min(pc, MEMORY_SIZE - VISIBLE_ROWS)));
  };

  return (
    <div className="memory-panel">
      <div className="memory-panel__header">
        <h3 className="memory-panel__title">Memory</h3>
        <button className="memory-panel__goto-pc" onClick={jumpToPC} title="Jump to PC">
          Go to PC ({pc})
        </button>
      </div>
      <div className="memory-panel__content">
        <div className="memory-panel__rows">
          {Array.from({ length: VISIBLE_ROWS }, (_, i) => {
            const addr = scrollPos + i;
            if (addr >= MEMORY_SIZE) return null;
            const isPC = addr === pc;
            return (
              <div key={addr} className={`memory-panel__row ${isPC ? 'memory-panel__row--pc' : ''}`}>
                <span className="memory-panel__addr">
                  {isPC ? '\u25B6' : '\u00A0'}{addr.toString().padStart(4, '0')}
                </span>
                <MIXWordDisplay word={memory[addr]} compact />
              </div>
            );
          })}
        </div>
        <input
          type="range"
          className="memory-panel__scrollbar"
          min={0}
          max={MEMORY_SIZE - VISIBLE_ROWS}
          value={scrollPos}
          onChange={handleScroll}
          orient="vertical"
        />
      </div>
    </div>
  );
}
