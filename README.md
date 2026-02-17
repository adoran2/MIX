# MIX 1009 Simulator

A graphical simulator of Donald Knuth's **MIX computer** from *The Art of Computer Programming* (TAOCP), featuring an interactive assembler, debugger, and visual representation of the MIX architecture.

**Live demo:** [https://adoran2.github.io/MIX/](https://adoran2.github.io/MIX/)

## History

This project was originally created as a **third-year university project** (1998-99) at the Department of Computer Science, University of Warwick, by Andrew Doran. It was written in **Java** as an AWT applet, designed to run inside a web browser via the now-defunct `<applet>` tag.

Since Java applets were removed from modern browsers and JDKs, the simulator could no longer run in its original form. In 2026, the application was **rebuilt from scratch as a modern web application** using React and TypeScript by [Claude](https://claude.ai), Anthropic's AI assistant, and deployed to GitHub Pages.

The original Java source code is preserved in the repository root for historical reference.

## Features

- **MIXAL assembler** — write MIX assembly language programs and assemble them into memory
- **Step-through debugger** — execute one instruction at a time with full visibility of registers, memory, and indicators
- **Adjustable execution speed** — run programs from 1 instruction/sec up to maximum speed
- **Visual architecture** — registers (A, X, I1-I6, J), comparison and overflow indicators, and a 4000-cell memory display
- **Line printer output** — see printed output from MIX I/O instructions
- **Sample programs** — includes Program P (First Five Hundred Primes) and Algorithm M (Maximum) from TAOCP

## Running locally

```bash
cd mix-sim
npm install
npm run dev
```

Then open [http://localhost:5173/MIX/](http://localhost:5173/MIX/) in your browser.

## Project structure

```
MIX/
├── mix-sim/                  # Modern web simulator (React + TypeScript + Vite)
│   ├── src/
│   │   ├── core/             # MIX machine engine, assembler, instruction set
│   │   ├── components/       # React UI components
│   │   └── styles/           # CSS stylesheets
│   └── public/               # Static assets (original graphics)
├── .github/workflows/        # GitHub Pages deployment
├── *.java                    # Original Java source (1999, preserved)
├── graphics/                 # Original image assets
├── MIX_report.pdf            # Original university project report
└── notes.txt                 # Original developer notes
```

## Original project

- **Author:** Andrew Doran ([andrewdoran.uk](http://andrewdoran.uk/))
- **Version:** 0.11 (30 March 1999)
- **Language:** Java (JDK 1.1 / AWT)
- **Reference:** Donald E. Knuth, *The Art of Computer Programming*, Volume 1, Section 1.3
