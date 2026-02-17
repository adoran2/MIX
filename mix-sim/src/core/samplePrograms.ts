/** Sample MIXAL programs matching the original MIX 1009 simulator. */

export const SAMPLE_PROGRAMS: { name: string; description: string; source: string }[] = [
  {
    name: 'Primes',
    description: 'First Five Hundred Primes — Program P from TAOCP Vol 1, Section 1.3.2',
    source: `* FIRST FIVE HUNDRED PRIMES
* Program P from TAOCP Vol 1, Section 1.3.2
* by Donald E. Knuth
*
L        EQU  500
PRINTER  EQU  18
PRIME    EQU  -1
BUF0     EQU  2000
BUF1     EQU  2025
         ORIG 0
         CON  2
         ORIG 1995
TITLE    ALF "FIRST"
         ALF " FIVE"
         ALF " HUND"
         ALF "RED P"
         ALF "RIMES"
         ORIG 2024
         CON  2035
         ORIG 2049
         CON  2010
LMINUS   CON  -499
THREE    CON  3
         ORIG 3000
START    IOC  0(18)
         LD1  LMINUS
         LD2  THREE
P2       INC1 1
         ST2  499,1
         J1Z  PRINT
P4       INC2 2
         ENT3 2
P5       ENTA 0
         ENTX 0,2
         DIV  -1,3
         JXZ  P4
         CMPA -1,3
         INC3 1
         JG   P5
         JMP  P2
PRINT    OUT  TITLE(18)
         ENT4 2035
         ENT5 -50
NEWLN    INC5 501
PLINE    LDA  -1,5
         CHAR
         STX  0,4(1:4)
         DEC4 1
         DEC5 50
         J5P  PLINE
         OUT  0,4(18)
         LD4  24,4
         J5N  NEWLN
         HLT
         END  START`,
  },
  {
    name: 'Maximum',
    description: 'Find maximum of 100 random values — Algorithm M from TAOCP Vol 1',
    source: `* FIND MAXIMUM - Algorithm M from TAOCP Vol 1
* Finds the maximum value among numbers stored
* in memory locations 1000-1099.
* Result: rA = max value, rI2 = index of max
*
         ORIG 1000
X        CON  183
         CON  542
         CON  57
         CON  891
         CON  405
         CON  713
         CON  268
         CON  934
         CON  126
         CON  677
         CON  351
         CON  809
         CON  42
         CON  598
         CON  765
         CON  100
         CON  483
         CON  250
         CON  637
         CON  19
         ORIG 3000
START    ENT1 19
INIT     LDA  X,1
         ENT2 0,1
LOOP     DEC1 1
         J1N  DONE
         CMPA X,1
         JGE  LOOP
         JMP  INIT
DONE     HLT
         END  START`,
  },
  {
    name: 'Hello World',
    description: 'Prints "HELLO WORLD" to the line printer',
    source: `* HELLO WORLD PROGRAM
         ORIG 3000
START    LDA  MSG
         STA  0
         OUT  0(18)
         HLT
MSG      ALF "HELLO"
         ALF " WORL"
         ALF "D    "
         END  START`,
  },
  {
    name: 'Add Two Numbers',
    description: 'Adds two numbers and stores the result',
    source: `* ADD TWO NUMBERS
         ORIG 3000
START    LDA  NUM1
         ADD  NUM2
         STA  RESULT
         HLT
NUM1     CON  100
NUM2     CON  200
RESULT   CON  0
         END  START`,
  },
];
