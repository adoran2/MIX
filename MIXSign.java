//: MIXSign.java
//  A component of a MIX1009 'word'

/** MIXSign simply holds either '+' or '-', denoting the sign of
 *  a MIX1009 word
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class MIXSign
{
   private char sign;
   
   /** Sets up the initial state of the MIXSign.
    *  @return no return value
   */
   MIXSign()
   {
      sign = '+';                                 // We always start with a positive sign
   }
   
   /** Sets the Sign to either '+' or '-'.
    *  @param newSign char value of '+' or '-'.
    *  @return No return value
    *  @exception CharNotASignException Thrown if the character supplied is not a valid sign.
   */
   void setSign( char newSign ) throws CharNotASignException
   {
      if (( newSign != '+' ) && ( newSign != '-' ))
         throw new CharNotASignException();
      else sign = newSign;
   }
   
   /** Used to retrieve the value of the sign.
    *  @return char denoting current sign.
   */
   char getSign()
   {
      return sign;
   }
}

///:~
