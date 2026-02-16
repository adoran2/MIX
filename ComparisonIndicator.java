//: ComparisonIndicator.java
//  A four-state indicator that is set upon a MIX comparison
import java.awt.*;

/** ComparisonIndicator maintains the state of the comparison indicator in the MIX1009.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class ComparisonIndicator extends Canvas
{
   static final int OFF = 0;
   static final int LESSTHAN = 1;
   static final int GREATERTHAN = 2;
   static final int EQUALTO = 3;

   private int state;

   private static final Font CFONT  = new Font( "Monospaced", Font.PLAIN,  12 );
   private static final Font CFONTI = new Font( "Monospaced", Font.ITALIC, 12 );
   private static final int CINDICATORWIDTH = 130;
   private static final int CINDICATORHEIGHT = 35;
   
   /** Loads the comparison indicator images and puts the machine
    *  into an initial 'indicator off' state.
    *  @return No return value.
   */
   ComparisonIndicator()
   {  state = OFF;
      setSize( CINDICATORWIDTH, CINDICATORHEIGHT );
   }
   
   /** Sets the state of the comparison indicator
    *  @param newState a valid integer state value.
    *  @return No return value.
    *  @exception NotAValidStateException thrown if the newState is not a valid state.
   */
   void setState( int newState ) throws NotAValidStateException
   {  if (( newState < 0 ) || ( newState > 3 ))
         throw new NotAValidStateException();
      else if ( state != newState )
           {  state = newState;
              repaint();
           }
   }

   /** Returns the numeric state of the comparison indicator
    *  @return int value representing state of the comparison indicator.
   */
   int getState()
   {  return state;
   }
   

   /** (Re)draws the comparison indicator, with the current state
    *  highlighted, or every indicator off if we have not been used
    *  yet.
    *  @param g Graphics object
    *  @return No return value.
   */
   public void paint( Graphics g )
   {  g.setColor( Color.white );
      g.fillRect( 0, 0, CINDICATORWIDTH, CINDICATORHEIGHT );
      g.setColor( Color.green );
      if ( state == LESSTHAN )
         g.fillArc( 10, 18, 15, 15, 0, 360 );
      if ( state == EQUALTO )
         g.fillArc( 20,  2, 15, 15, 0, 360 );
      if ( state == GREATERTHAN )
         g.fillArc( 30, 18, 15, 15, 0, 360 );
      g.setColor( Color.black );
      g.setFont( CFONT );
      g.drawString( "Comparison", 55, 15 );
      g.drawString( "indicator", 57, 26 );
      g.setFont( CFONTI );
      g.drawString( "E", 23, 14 );
      g.drawString( "L", 13, 30 );
      g.drawString( "G", 33, 30 );
      g.drawArc( 20,  2, 15, 15, 0, 360 );
      g.drawArc( 10, 18, 15, 15, 0, 360 );
      g.drawArc( 30, 18, 15, 15, 0, 360 );
   }

   // Eliminates flicker (from Eckel p.686)
   public void update( Graphics g )
   {  paint(g);
   }
}

///:~
