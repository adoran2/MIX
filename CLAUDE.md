# CLAUDE.md — MIX1009 Simulator

## Project Overview

MIX1009 is a Java-based graphical simulator of Donald Knuth's MIX computer, as described in *The Art of Computer Programming* (TAOCP). It was created as a third-year university project (1998–99) at the Department of Computer Science, University of Warwick, by Andrew Doran.

The simulator provides an interactive GUI for writing, assembling, running, and debugging MIXAL (MIX Assembly Language) programs. It includes a visual representation of the MIX architecture: registers, memory cells, I/O peripherals, comparison and overflow indicators, and an execution clock.

- **Author:** Andrew Doran (http://andrew.doran.com/)
- **Version:** 0.11 (30 March 1999)
- **Language:** Java (originally JDK 1.1 / AWT)
- **Runtime:** Currently buildable with OpenJDK 21

## Repository Structure

```
MIX/
├── MIX1009.java                # Entry point — Applet that launches the simulator
├── MIXMachine.java             # Main GUI frame — the core simulator (~3900 lines)
├── MIXInstructions.java        # MIX instruction set definitions and lookup
├── MIXWord.java                # 5-byte + sign word (fundamental MIX data unit)
├── MIXByte.java                # Single MIX byte (value 0–63)
├── MIXSign.java                # Sign representation (+ or -)
├── Operator.java               # MIX operator representation
├── Symbol.java                 # Symbol table entry for the assembler
├── IRegister.java              # Index register (2-byte word)
├── JRegister.java              # Jump register (positive-only)
├── ComparisonIndicator.java    # Visual comparison state (LESS, EQUAL, GREATER)
├── OverFlowIndicator.java      # Visual overflow flag
├── InputConsole.java           # MIXAL program editor window
├── LinePrinter.java            # Output peripheral simulation
├── MIXClock.java               # Execution timing display
├── ImageLabel.java             # Image display component (by Marty Hall)
├── InfoWindow.java             # Info/about dialog
├── *Exception.java (6 files)   # Custom exception classes:
│   ├── CharNotASignException.java
│   ├── IndexOutOfRangeException.java
│   ├── JRegisterMustBePositiveException.java
│   ├── NotAMIXCharacterException.java
│   ├── NotAValidStateException.java
│   └── ValueOutOfBoundsException.java
├── *.class                     # Pre-compiled bytecode (committed)
├── graphics/
│   ├── MIX.jpg                 # Main UI logo
│   ├── MIXInfo.jpg             # Info window image
│   └── MIXLogo.jpg             # Program logo
├── MIXApplet.html              # HTML applet wrapper for browser deployment
├── MIX_report.pdf              # Original project report
└── notes.txt                   # Developer notes and TODOs from 1999
```

**Total:** 23 Java source files, ~6,100 lines of code.

## Architecture

### Core Data Model

- **MIXWord** — The fundamental storage unit: 5 bytes + a sign bit. Extends `Canvas` so it renders itself in the GUI. Represents memory cells and register A/X.
- **MIXByte** — A single byte with value range 0–63.
- **MIXSign** — Represents `+` or `-`.
- **IRegister** — A 2-byte index register (I1–I6). Extends MIXWord.
- **JRegister** — The jump register. Must always be positive. Extends IRegister.

### Instruction Handling

- **MIXInstructions** — Defines all valid MIX opcodes using `com.objectspace.jgl.HashSet` for lookup.
- **Operator** — Represents a single MIX instruction with opcode, field, and address.

### GUI Components

- **MIX1009** — Applet entry point. Displays a splash screen with Start/Info buttons.
- **MIXMachine** — The main simulator frame. Contains the assembler, executor, memory display, register display, control console (step/go), menus, and all peripheral windows. This is the largest file (~3,900 lines) and contains most of the application logic.
- **InputConsole** — MIXAL program editor with cut/copy/paste and Greek character insertion (Σ, Δ, Π).
- **LinePrinter** — Simulates the MIX line printer peripheral.
- **MIXClock** — Displays execution time in Knuth's abstract "u" time units.
- **ComparisonIndicator** / **OverFlowIndicator** — Canvas-based visual indicators.
- **InfoWindow** — Displays project information.
- **ImageLabel** — Utility for displaying images (by Marty Hall, from *Core Web Programming*).

### MIX Machine Specifications

- **Memory:** 4000 cells (MIXWord array)
- **Registers:** A (accumulator), X (extension), I1–I6 (index), J (jump)
- **I/O Peripherals:** Input console, line printer
- **Indicators:** Comparison indicator (LESS/EQUAL/GREATER), overflow toggle
- **No floating-point operations** (noted as intentionally excluded)

## Building and Running

### Prerequisites

- Java Development Kit (JDK 11+; tested with OpenJDK 21)
- The JGL library (`com.objectspace.jgl`) must be on the classpath for `MIXInstructions.java`

### Compile

There is no build system (no Maven, Gradle, or Makefile). Compile manually:

```bash
javac -classpath . *.java
```

Note: Expect deprecation warnings from AWT API usage — these are expected and can be ignored (as noted in `notes.txt`).

### Run

The application was originally designed as an applet. Since applets are removed in modern Java, it would need adaptation to run as a standalone application. The pre-compiled `.class` files in the repository were built with JDK 1.1 (class version 45.3).

## External Dependencies

| Dependency | Usage | Notes |
|---|---|---|
| `com.objectspace.jgl.HashSet` | Instruction set lookup in `MIXInstructions.java` | JGL (Java Generic Library) — third-party collections library. Not managed by any dependency system; requires manual classpath configuration. |
| `java.awt.*` | All GUI components | Standard library (AWT) |
| `java.applet.*` | Entry point | Deprecated/removed in modern JDK |

## Code Conventions

### Naming

- **Classes:** PascalCase (`MIXMachine`, `ComparisonIndicator`, `JRegister`)
- **Methods:** camelCase (`setSign`, `getValue`, `addComponent`)
- **Constants:** UPPERCASE or English words (`OFF`, `LESSTHAN`, `OPEN`, `CLOSED`, `MAXLINELENGTH`)
- **Private fields:** camelCase, sometimes prefixed (`cIndicator`, `oIndicator`)

### Style

- All classes reside in the **default package** (no package declarations)
- JavaDoc comments on public methods and classes
- Development comments marked with `//:` prefix
- Source files end with `///:~` marker
- Inner classes used extensively for AWT event listeners (`ActionListener`, `ItemListener`, `ComponentAdapter`)
- `GridBagLayout` used for complex UI layouts

### Error Handling

Custom exception classes are used for domain-specific validation:
- `ValueOutOfBoundsException` — value exceeds MIX byte/word range
- `IndexOutOfRangeException` — invalid index register reference
- `CharNotASignException` — character is not `+` or `-`
- `NotAMIXCharacterException` — character not in MIX character set (0–55)
- `NotAValidStateException` — invalid indicator state
- `JRegisterMustBePositiveException` — J register received negative value

## Testing

There is **no automated test suite**. No JUnit, TestNG, or other testing framework is configured. Validation has historically been manual.

## CI/CD

No CI/CD pipelines are configured. There are no GitHub Actions workflows, and no `.gitignore` file.

## Key Files to Understand

When working on this codebase, start with these files in order:

1. **`MIX1009.java`** — Entry point; understand how the app bootstraps
2. **`MIXWord.java`** — The fundamental data type; everything builds on this
3. **`MIXMachine.java`** — The core simulator with assembler, executor, and GUI (largest file)
4. **`MIXInstructions.java`** — How the instruction set is defined
5. **`notes.txt`** — Original developer notes with design decisions and known TODOs

## Known Limitations and Historical Notes

- Originally an **Applet** — the `java.applet` API is removed in modern JDK versions; running the application as-is requires adaptation
- Uses **AWT** (not Swing or JavaFX) — pre-dates modern Java GUI frameworks
- Depends on **JGL** (`com.objectspace.jgl`), an obsolete third-party library that could be replaced with `java.util.HashSet`
- **No package structure** — all classes in the default package
- **Compiled `.class` files are committed** — these are from the original JDK 1.1 build
- **No `.gitignore`** — binary files and class files are tracked
- Floating-point MIX operations are **not implemented**
- The assembler has limited support for forward references and `EQU` expressions (see `notes.txt`)

## Development Guidelines

When modifying this codebase:

1. **Preserve the MIX architecture semantics** — refer to Knuth's TAOCP (Volume 1, Section 1.3) for specification details
2. **MIXMachine.java is monolithic** — most logic (assembler, executor, GUI) lives in this single file. Take care when modifying it
3. **No build automation** — compile with `javac *.java` and verify manually
4. **Watch for the JGL dependency** — `MIXInstructions.java` imports `com.objectspace.jgl.HashSet`; if modernizing, replace with `java.util.HashSet`
5. **AWT deprecations are expected** — the codebase intentionally uses deprecated AWT patterns
6. **Character encoding** — MIX uses its own 56-character set (not ASCII); see character mapping in `MIXMachine.java` and `LinePrinter.java`
