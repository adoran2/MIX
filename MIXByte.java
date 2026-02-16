//: MIXByte.java
//  A component of a MIX1009 'word'

/** MIXByte holds a value between 0 and 63 inclusive.  It is used to construct
 *  MIX 'words' in conjunction with MIXSign.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class MIXByte
{  private int value;
   private boolean packed;
   
   /** Sets up the initial state of the MIXByte.
    *  @return No return value.
   */
   MIXByte()
   {  value = 0;                        // This is dependent on the notion that each MIX
      packed = true;                    // word starts with the value of +         0 (i.e. packed)
   }
   
   /** Sets the value of the MIXByte (must be between 0 and 63 inclusive).
    *  @param newValue int value between 0 and 63.
    *  @return No return value.
    *  @exception ValueOutOfBoundsException Thrown if the value specified does not lie between
    *                                       0 and 63 inclusive.
   */
   void setValue(int newValue) throws ValueOutOfBoundsException
   {  if (( newValue < 0 ) || ( newValue > 63 ))
         throw new ValueOutOfBoundsException();
      else value = newValue;
   }
   
   /** Retrieves the value of the MIXByte
    *  @return int value
   */
   int getValue()
   {  return value;
   }
   
   /** Sets the status of the 'packed' flag for the MIXByte.
    *  @param p boolean where ( true == packed ) and ( false == unpacked ).
    *  @return No return value.
   */
   void setPacked( boolean p )
   {  packed = p;
   }
   
   /** Retrieves the status of the 'packed' flag for the MIXByte.
    *  @return boolean packed status.
   */
   boolean isPacked()
   {  return packed;
   }
}

///:~

