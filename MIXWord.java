//: MIXWord.java
//  A MIX1009 'word' constructed from arbitrary MIXBytes and a MIXSign.
import java.awt.*;
import java.awt.image.*;

/** MIXWord is a basic unit of memory for the MIX1009.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.1 - 24 January 1999
*/
class MIXWord extends Canvas
{  private MIXSign   wordSign;
   private MIXByte[] wordBytes;
   private int       displayWidth;
   private static final Font SMLMONOSPACEDFONT = new Font( "Monospaced", Font.PLAIN, 12 );
   private static final Font BIGMONOSPACEDFONT = new Font( "Monospaced", Font.PLAIN, 16 );
   
   /** Standard word in the MIX1009 consists of five MIXBytes and a MIXSign.
    *  @return No return value.
   */
   MIXWord()
   {  this( 5 );
   }
   
   /** Can have a MIX1009 'word' constructed with an arbitrary number of MIXBytes and a MIXSign.
    *  @param wordLength int value denoting number of bytes in the word.
    *  @return No return value.
   */
   MIXWord( int wordLength )
   {  wordSign  = new MIXSign();
      wordBytes = new MIXByte[ wordLength ];
      for( int i = wordLength - 1; i >= 0; i--)
         wordBytes[i] = new MIXByte();
      displayWidth = (( wordLength + 1 ) * 30) - 1;
      setSize( displayWidth, 29 );
   }
   
   /** Indicates whether the byte array index specified is out of range.
    *  @param index int value of index, starting at zero for LSB.
    *  @return boolean true if value specified is out of range.
   */
   boolean indexOutOfRange( int index )
   {  if (( index < 0 ) || ( index > ( wordBytes.length -1 )))
         return true;
      return false;
   }
   
   /** Discovers if a certain byte in the word is packed.
    *  @param index int value of index, starting at zero for LSB.
    *  @return boolean value denoting status of 'packed' for byte.
    *  @exception IndexOutOfRangeException Thrown if the byte referenced does not exist in the word.
   */
   boolean isPacked( int index ) throws IndexOutOfRangeException
   {  if ( indexOutOfRange( index ) )
         throw new IndexOutOfRangeException();
      else return wordBytes[ index ].isPacked();
   }
   
   /** Sets the 'packed' status for an individual byte in the word.
    *  @param index int value of index, starting at zero for LSB.
    *  @param p boolean value denoting 'packed' status to be set.
    *  @return No return value.
    *  @exception IndexOutOfRangeException Thrown if the byte referenced does not exist in the word.
   */
   void setPacked( int index, boolean p ) throws IndexOutOfRangeException
   {  if ( indexOutOfRange( index ) )
         throw new IndexOutOfRangeException();
      else wordBytes[ index ].setPacked( p );
   }
   
   /** Retrieves the value of a specified byte in the word.
    *  @param index int vale of index, starting at zero for LSB.
    *  @return int value of specified byte.
    *  @exception IndexOutOfRangeException Thrown if the byte referenced does not exist in the word.
   */
   int getValue( int index ) throws IndexOutOfRangeException
   {  if ( indexOutOfRange( index ) )
         throw new IndexOutOfRangeException();
      else return wordBytes[ index ].getValue();
   }
   
   /** Sets the value of a specified byte in the word.
    *  @param index int value of index, starting at zero for LSB.
    *  @param newValue int value between 0 and 63 inclusive.
    *  @return No return value.
    *  @exception IndexOutOfRangeException Thrown if the byte referenced does not exist in the word.
    *  @exception ValueOutOfBoundsException Thrown if the value specified does not lie between
    *                                       0 and 63 inclusive.
   */
   void setValue( int index, int newValue ) throws IndexOutOfRangeException,
                                                   ValueOutOfBoundsException
   {  if ( indexOutOfRange( index ) )
         throw new IndexOutOfRangeException();
      else wordBytes[ index ].setValue( newValue );
      repaint();
   }
   
   /** Sets the sign of the word.
    *  @param newSign char value of desired sign.
    *  @return No return value.
    *  @exception CharNotASignException Thrown if the character supplied is not a valid sign.
   */
   void setSign( char newSign ) throws CharNotASignException
   {  wordSign.setSign( newSign );
      repaint();
   }
   
   /** Retrieves the sign of the word.
    *  @return char denoting word sign.
   */
   char getSign()
   {  return wordSign.getSign();
   }

   public String toString()
   {  long value = 0;
      int  power = 0;

      for ( int i = 4; i>=0; i-- )
      {  value += (long)(Math.pow( 64, power ) * wordBytes[ i ].getValue() );
         power++;
      }

      if (wordSign.getSign() == '-')
         value = -value;

      return ( "" + value );
   }

   public long toLong()
   {  long value = 0;
      long power = 0;

      for ( int i = 4; i>=0; i-- )
      {  value += (long)(Math.pow( 64, power ) * wordBytes[ i ].getValue() );
         power++;
      }

      if (wordSign.getSign() == '-')
         value = -value;

      return ( value );
   }

   public void paint( Graphics g )
   {  int currentByte  = displayWidth,
          xValue       = 0,
          placeCounter = 0;
      long valueToShow = 0;
          
      boolean packedFlag = false;

      g.setColor( Color.white );
      g.fillRect( 0, 0, displayWidth, 29 );
      g.setColor( Color.black );
      g.drawRect( 0, 0, displayWidth, 28 );
      g.setFont( SMLMONOSPACEDFONT );

      for( int i = displayWidth - 30; i >= 0; i -= 30 )
         {  currentByte = ((i+1)/30) - 1;
            if ( packedFlag == false )                                // If the last Byte wasn't
               if ( !wordBytes[ currentByte ].isPacked() )            // packed and this one isn't
                                                                      // either...
               {  valueToShow = wordBytes[ currentByte ].getValue();
                  if ( ( "" + valueToShow ).length() == 2 )           // ...then draw it!
                     xValue = i+8;
                  else
                     xValue = i+12;
                  g.drawString( "" + valueToShow, xValue, 19 );
               }
               else                                                   // But if this one is...
               {  packedFlag = true;                                  // ...we're joined to our
                                                                      // neighbour...
                  placeCounter = 0;                                   // ...have a place value
                                                                      // of 64^0...
                  valueToShow = wordBytes[ currentByte ].getValue();  // ...and must start to
                                                                      // establish our value.
                  if ( currentByte == 0 )                             // However, if this is the
                                                                      // last digit we *do*
                  {  if ( ( "" + valueToShow ).length() == 2 )        // need to display our
                                                                      // contents even if we are
                        xValue = i+8;                                 // packed with the sign of
                                                                      // the word.
                     else
                        xValue = i+12;
                     g.drawString( "" + valueToShow, xValue, 19 );
                  }
               }
 
// UNLESS WE ARE ON BYTE ZERO...OTHERWISE NOTHING GETS DISPLAYED!
            else                                                      // If the last Byte was
                                                                      // packed...
            {  if (( wordBytes[ currentByte ].isPacked() )  && (currentByte > 0)) // ...and this one
                                                                                  // is too...
               {  placeCounter++;                                     // ...recognise this position
                                                                      // is 'worth' more...
                  valueToShow += (long)(Math.pow( 64, placeCounter ) *
                                 wordBytes[ currentByte ].getValue());// ...and add it to our
                                                                      // 'running total'.
               }
               else                                                   // But if this one isn't...
               {  placeCounter++;                                     // ...recognise this position
                                                                      // is 'worth' more...
                  valueToShow += (long)(Math.pow( 64, placeCounter ) *
                                 wordBytes[ currentByte ].getValue());// ...add it to our 'running
                                                                      // total'...
                  xValue = (((30 * (placeCounter+1))-( ("" + valueToShow).length() * 7))/2) + i;
                  g.drawString( "" + valueToShow, xValue, 19 );       // ...draw it...
                  packedFlag = false;                                 // ...and let the next Byte
                                                                      // know we weren't packed.
               }
            }

            if ( wordBytes[ currentByte ].isPacked() )
            {  g.drawLine( i,  0, i,  4 );
               g.drawLine( i, 25, i, 29 );
            }
            else
               g.drawLine( i,  0, i, 29 );
            
         }
      g.setFont( BIGMONOSPACEDFONT );
      g.drawString( "" + wordSign.getSign(), 10, 20 );
   }

   // Eliminates flicker (from Eckel p.686)
   public void update( Graphics g )
   {  paint(g);
   }

}

///:~

