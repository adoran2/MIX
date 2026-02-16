//: Operator.java
//  A MIX operator

/** Operator is a MIX operator, composed of its name, its C-value and
 *  its normal F-value.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.1 - 27 February 1999
*/
class Operator
{  private String name;
   private int cValue;
   private int fValue;

   /** Sole constructor of Operator.
    *  @param nameOf A String that identifies the Operator.
    *  @param commandNumber The C-value of the Operator.
    *  @return fieldValue The normal F-value of the Operator.
   */
   Operator( String nameOf, int commandNumber, int fieldValue )
   {  name   = nameOf;
      cValue = commandNumber;
      fValue = fieldValue;
   }

   /** Gets the name of the Operator.
    *  @return String identifier of the Operator.
   */
   public String getName()
   {  return name;
   }

   /** Gets the c-value of the Operator.
    *  @return int c-value of the Operator.
   */
   public int getCValue()
   {  return cValue;
   }

   /** Gets the normal f-value of the Operator.
    *  @return int normal f-value of the Operator.
   */
   public int getNormalFValue()
   {  return fValue;
   }

   /** Returns the hash code of the Operator.  This is simply the same
    *  hash code as returned by the String identifier for the Symbol.
    *  @return int hash code value
   */
   public int hashCode()
   {  return name.hashCode();
   }

   /** Checks to see if this object is equivalent to another.
    *  Equivalence here is simply based on whether the String identifiers
    *  are equal.
    *  @param o Object to be compared with
    *  @return boolean denoting equivalence
   */
   public boolean equals( Object o )
   {  if (( o != null) && ( o instanceof Operator ))
         return name.equals( ((Operator)o).getName() );
      else
	 return false;
   }
}
