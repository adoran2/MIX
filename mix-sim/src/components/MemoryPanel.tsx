import { useEffect, useRef } from 'react';
import { MIXWord } from '../core/MIXWord';
import { MIXWordDisplay } from './MIXWordDisplay';
import { MEMORY_SIZE } from '../core/types';
import '../styles/MemoryPanel.css';

interface Props {
  memory: MIXWord[];
  pc: number;
  scrollTo?: number;
  onScrollChange?: (pos: number) => void;
  scrollPos: number;
}

const VISIBLE_ROWS = 16;

export function MemoryPanel({ memory, pc, scrollTo, onScrollChange, scrollPos }: Props) {
  const prevScrollTo = useRef<number | undefined>(undefined);

  useEffect(() => {
    if (scrollTo !== undefined && scrollTo !== prevScrollTo.current) {
      prevScrollTo.current = scrollTo;
      // Centre the target address in the visible window
      const centred = Math.max(0, Math.min(scrollTo - Math.floor(VISIBLE_ROWS / 2), MEMORY_SIZE - 1));
      onScrollChange?.(centred);
    }
  }, [scrollTo, onScrollChange]);

  const handleScroll = (e: React.ChangeEvent<HTMLInputElement>) => {
    onScrollChange?.(parseInt(e.target.value));
  };

  const jumpToPC = () => {
    const centred = Math.max(0, Math.min(pc - Math.floor(VISIBLE_ROWS / 2), MEMORY_SIZE - 1));
    onScrollChange?.(centred);
  };

  // Compute the last valid start so that the last row shows address 3999
  const maxScroll = MEMORY_SIZE - 1;

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
          max={maxScroll}
          value={scrollPos}
          onChange={handleScroll}
        />
      </div>
    </div>
  );
}
