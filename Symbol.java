//: Symbol.java
//  An object that has a symbolic name and an associated value,
//  used throughought the assembly procedure.

/** Symbol is an object that has a String name and an associated
 *  value.  The object can be put into hashtables of various forms
 *  as its hashcode is the same as that of its name.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.1 - 25 February 1999
*/
class Symbol
{  private String identifier;
   private int value;

   /** Sole constructor of object.
    *  @param identifierOf A String that identifies the Symbol
    *  @param valueOf The initial value of the Symbol
    *  @return No return value
   */
   Symbol( String identifierOf, int valueOf )
   {  identifier = identifierOf;
      value      = valueOf;
   }

   /** Gets the identifier of the Symbol.
    *  @return String identifier of the Symbol.
   */
   public String getIdentifier()
   {  return identifier;
   }

   /** Gets the value of the Symbol.
    *  @return int value of the Symbol.
   */
   public int getValue()
   {  return value;
   }

   /** Sets the value of the symbol.
    *  @param newValue An integer denoting the new value
    *  @return No return value
   */
   public void setValue( int newValue )
   {  value = newValue;
   }

   /** Returns the hash code of the Symbol.  This is simply the same
    *  hash code as returned by the String identifier for the Symbol.
    *  @return int hash code value
   */
   public int hashCode()
   {  return identifier.hashCode();
   }

   /** Checks to see if this object is equivalent to another.
    *  Equivalence here is simply based on whether the String identifiers
    *  are equal.
    *  @param o Object to be compared with
    *  @return boolean denoting equivalence
   */
   public boolean equals( Object o )
   {  if (( o != null) && ( o instanceof Symbol ))
         return identifier.equals( ((Symbol)o).getIdentifier() );
      else
	 return false;
   }
}
