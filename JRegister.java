//: JRegister.java
//  A positive-only version of an I-Register.

/** JRegister is a positive-only form of IRegister
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class JRegister extends IRegister
{
   /** We disallow the setting of the J-Register's sign to negative
    *  @param sign char denoting desired sign.
    *  @return No return value.
    *  @exception JRegisterMustBePositiveException Thrown if an attempt is made
                  to set the sign of the J-Register to anything but '+'
   */
   void setSign( char sign ) throws JRegisterMustBePositiveException
   {
      if ( sign != '+' )
         throw new JRegisterMustBePositiveException();
   }
}

///:~
