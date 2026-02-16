import '../styles/LinePrinterDisplay.css';

interface Props {
  output: string[];
}

export function LinePrinterDisplay({ output }: Props) {
  return (
    <div className="line-printer">
      <h3 className="line-printer__title">Line Printer Output</h3>
      <div className="line-printer__paper">
        {output.length === 0 ? (
          <div className="line-printer__empty">No output yet</div>
        ) : (
          output.map((line, i) => (
            <div key={i} className="line-printer__line">{line}</div>
          ))
        )}
      </div>
    </div>
  );
}
