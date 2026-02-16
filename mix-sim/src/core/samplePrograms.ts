/** Sample MIXAL programs for demonstration. */

export const SAMPLE_PROGRAMS: { name: string; description: string; source: string }[] = [
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
  {
    name: 'Count Down',
    description: 'Counts down from 10 to 0 using a loop',
    source: `* COUNT DOWN FROM 10
         ORIG 3000
START    ENT1 10
LOOP     DEC1 1
         J1P  LOOP
         HLT
         END  START`,
  },
  {
    name: 'Maximum',
    description: 'Find the maximum of a list of numbers (from TAOCP)',
    source: `* FIND MAXIMUM - TAOCP Algorithm M
         ORIG 3000
INIT     ENT3 0
         JMP  CHANGEM
LOOP     CMPA X,3
         JGE  NOCHANGE
CHANGEM  LDA  X,3
         ENT2 0,3
NOCHANGE DEC3 1
         J3NN LOOP
         HLT
X        CON  100
         CON  50
         CON  200
         CON  150
         CON  75
         END  INIT`,
  },
];
