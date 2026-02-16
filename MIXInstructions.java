//: MIXInstructions.java
//  Sets up a HashSet of standard MIX Operators
import com.objectspace.jgl.*;

/** MIXInstructions creates a HashSet containing the standard Operator
 *  objects derived from TAOCP pp.129 onwards.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.1 - 27 February 1999
*/
class MIXInstructions
{  private static HashSet inst;

   static final HashSet getHashSet()
   {  inst = new HashSet();
      inst.add( new Operator(  "NOP",  0, 0 ) );
      inst.add( new Operator(  "ADD",  1, 5 ) );
      inst.add( new Operator(  "SUB",  2, 5 ) );
      inst.add( new Operator(  "MUL",  3, 5 ) );
      inst.add( new Operator(  "DIV",  4, 5 ) );
      inst.add( new Operator(  "NUM",  5, 0 ) );
      inst.add( new Operator( "CHAR",  5, 1 ) );
      inst.add( new Operator(  "HLT",  5, 2 ) );
      inst.add( new Operator(  "SLA",  6, 2 ) );
      inst.add( new Operator(  "SRA",  6, 1 ) );
      inst.add( new Operator( "SLAX",  6, 2 ) );
      inst.add( new Operator( "SRAX",  6, 3 ) );
      inst.add( new Operator(  "SLC",  6, 4 ) );
      inst.add( new Operator(  "SRC",  6, 5 ) );
      inst.add( new Operator( "MOVE",  7, 1 ) );
      inst.add( new Operator(  "LDA",  8, 5 ) );
      inst.add( new Operator(  "LD1",  9, 5 ) );
      inst.add( new Operator(  "LD2", 10, 5 ) );
      inst.add( new Operator(  "LD3", 11, 5 ) );
      inst.add( new Operator(  "LD4", 12, 5 ) );
      inst.add( new Operator(  "LD5", 13, 5 ) );
      inst.add( new Operator(  "LD6", 14, 5 ) );
      inst.add( new Operator(  "LDX", 15, 5 ) );
      inst.add( new Operator( "LDAN", 16, 5 ) );
      inst.add( new Operator( "LD1N", 17, 5 ) );
      inst.add( new Operator( "LD2N", 18, 5 ) );
      inst.add( new Operator( "LD3N", 19, 5 ) );
      inst.add( new Operator( "LD4N", 20, 5 ) );
      inst.add( new Operator( "LD5N", 21, 5 ) );
      inst.add( new Operator( "LD6N", 22, 5 ) );
      inst.add( new Operator( "LDXN", 23, 5 ) );
      inst.add( new Operator(  "STA", 24, 5 ) );
      inst.add( new Operator(  "ST1", 25, 5 ) );
      inst.add( new Operator(  "ST2", 26, 5 ) );
      inst.add( new Operator(  "ST3", 27, 5 ) );
      inst.add( new Operator(  "ST4", 28, 5 ) );
      inst.add( new Operator(  "ST5", 29, 5 ) );
      inst.add( new Operator(  "ST6", 30, 5 ) );
      inst.add( new Operator(  "STX", 31, 5 ) );
      inst.add( new Operator(  "STJ", 32, 2 ) );
      inst.add( new Operator(  "STZ", 33, 5 ) );
      inst.add( new Operator( "JBUS", 34, 0 ) );
      inst.add( new Operator(  "IOC", 35, 0 ) );
      inst.add( new Operator(   "IN", 36, 0 ) );
      inst.add( new Operator(  "OUT", 37, 0 ) );
      inst.add( new Operator( "JRED", 38, 0 ) );
      inst.add( new Operator(  "JMP", 39, 0 ) );
      inst.add( new Operator(  "JSJ", 39, 1 ) );
      inst.add( new Operator(  "JOV", 39, 2 ) );
      inst.add( new Operator( "JNOV", 39, 3 ) );
      inst.add( new Operator(   "JL", 39, 4 ) );
      inst.add( new Operator(   "JE", 39, 5 ) );
      inst.add( new Operator(   "JG", 39, 6 ) );
      inst.add( new Operator(  "JGE", 39, 7 ) );
      inst.add( new Operator(  "JNE", 39, 8 ) );
      inst.add( new Operator(  "JLE", 39, 9 ) );
      inst.add( new Operator(  "JAN", 40, 0 ) );
      inst.add( new Operator(  "JAZ", 40, 1 ) );
      inst.add( new Operator(  "JAP", 40, 2 ) );
      inst.add( new Operator( "JANN", 40, 3 ) );
      inst.add( new Operator( "JANZ", 40, 4 ) );
      inst.add( new Operator( "JANP", 40, 5 ) );
      inst.add( new Operator(  "J1N", 41, 0 ) );
      inst.add( new Operator(  "J1Z", 41, 1 ) );
      inst.add( new Operator(  "J1P", 41, 2 ) );
      inst.add( new Operator( "J1NN", 41, 3 ) );
      inst.add( new Operator( "J1NZ", 41, 4 ) );
      inst.add( new Operator( "J1NP", 41, 5 ) );
      inst.add( new Operator(  "J2N", 42, 0 ) );
      inst.add( new Operator(  "J2Z", 42, 1 ) );
      inst.add( new Operator(  "J2P", 42, 2 ) );
      inst.add( new Operator( "J2NN", 42, 3 ) );
      inst.add( new Operator( "J2NZ", 42, 4 ) );
      inst.add( new Operator( "J2NP", 42, 5 ) );
      inst.add( new Operator(  "J3N", 43, 0 ) );
      inst.add( new Operator(  "J3Z", 43, 1 ) );
      inst.add( new Operator(  "J3P", 43, 2 ) );
      inst.add( new Operator( "J3NN", 43, 3 ) );
      inst.add( new Operator( "J3NZ", 43, 4 ) );
      inst.add( new Operator( "J3NP", 43, 5 ) );
      inst.add( new Operator(  "J4N", 44, 0 ) );
      inst.add( new Operator(  "J4Z", 44, 1 ) );
      inst.add( new Operator(  "J4P", 44, 2 ) );
      inst.add( new Operator( "J4NN", 44, 3 ) );
      inst.add( new Operator( "J4NZ", 44, 4 ) );
      inst.add( new Operator( "J4NP", 44, 5 ) );
      inst.add( new Operator(  "J5N", 45, 0 ) );
      inst.add( new Operator(  "J5Z", 45, 1 ) );
      inst.add( new Operator(  "J5P", 45, 2 ) );
      inst.add( new Operator( "J5NN", 45, 3 ) );
      inst.add( new Operator( "J5NZ", 45, 4 ) );
      inst.add( new Operator( "J5NP", 45, 5 ) );
      inst.add( new Operator(  "J6N", 46, 0 ) );
      inst.add( new Operator(  "J6Z", 46, 1 ) );
      inst.add( new Operator(  "J6P", 46, 2 ) );
      inst.add( new Operator( "J6NN", 46, 3 ) );
      inst.add( new Operator( "J6NZ", 46, 4 ) );
      inst.add( new Operator( "J6NP", 46, 5 ) );
      inst.add( new Operator(  "JXN", 47, 0 ) );
      inst.add( new Operator(  "JXZ", 47, 1 ) );
      inst.add( new Operator(  "JXP", 47, 2 ) );
      inst.add( new Operator( "JXNN", 47, 3 ) );
      inst.add( new Operator( "JXNZ", 47, 4 ) );
      inst.add( new Operator( "JXNP", 47, 5 ) );
      inst.add( new Operator( "INCA", 48, 0 ) );
      inst.add( new Operator( "DECA", 48, 1 ) );
      inst.add( new Operator( "ENTA", 48, 2 ) );
      inst.add( new Operator( "ENNA", 48, 3 ) );
      inst.add( new Operator( "INC1", 49, 0 ) );
      inst.add( new Operator( "DEC1", 49, 1 ) );
      inst.add( new Operator( "ENT1", 49, 2 ) );
      inst.add( new Operator( "ENN1", 49, 3 ) );
      inst.add( new Operator( "INC2", 50, 0 ) );
      inst.add( new Operator( "DEC2", 50, 1 ) );
      inst.add( new Operator( "ENT2", 50, 2 ) );
      inst.add( new Operator( "ENN2", 50, 3 ) );
      inst.add( new Operator( "INC3", 51, 0 ) );
      inst.add( new Operator( "DEC3", 51, 1 ) );
      inst.add( new Operator( "ENT3", 51, 2 ) );
      inst.add( new Operator( "ENN3", 51, 3 ) );
      inst.add( new Operator( "INC4", 52, 0 ) );
      inst.add( new Operator( "DEC4", 52, 1 ) );
      inst.add( new Operator( "ENT4", 52, 2 ) );
      inst.add( new Operator( "ENN4", 52, 3 ) );
      inst.add( new Operator( "INC5", 53, 0 ) );
      inst.add( new Operator( "DEC5", 53, 1 ) );
      inst.add( new Operator( "ENT5", 53, 2 ) );
      inst.add( new Operator( "ENN5", 53, 3 ) );
      inst.add( new Operator( "INC6", 54, 0 ) );
      inst.add( new Operator( "DEC6", 54, 1 ) );
      inst.add( new Operator( "ENT6", 54, 2 ) );
      inst.add( new Operator( "ENN6", 54, 3 ) );
      inst.add( new Operator( "INCX", 55, 0 ) );
      inst.add( new Operator( "DECX", 55, 1 ) );
      inst.add( new Operator( "ENTX", 55, 2 ) );
      inst.add( new Operator( "ENNX", 55, 3 ) );
      inst.add( new Operator( "CMPA", 56, 5 ) );
      inst.add( new Operator( "CMP1", 57, 5 ) );
      inst.add( new Operator( "CMP2", 58, 5 ) );
      inst.add( new Operator( "CMP3", 59, 5 ) );
      inst.add( new Operator( "CMP4", 60, 5 ) );
      inst.add( new Operator( "CMP5", 61, 5 ) );
      inst.add( new Operator( "CMP6", 62, 5 ) );
      inst.add( new Operator( "CMPX", 63, 5 ) );
      return inst;
   }

   // Test code here - TO BE REMOVED
   public static void main( String args[] )
   {  HashSet a = MIXInstructions.getHashSet();
      System.out.println( a );
   }
}
