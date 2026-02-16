//: OverFlowIndicator.java
//  Maintains the state of the 'overflow' flag in the MIX1009
import java.awt.*;

/** OverFlowIndicator maintains the state of the 'overflow' flag in the MIX1009.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.1 - 26 January 1999
*/
class OverFlowIndicator extends Canvas
{  private boolean overFlow;
   private int width,
               height;
   private static final Font OFONT = new Font( "Monospaced", Font.PLAIN, 12 );
   
   /** Puts the indicator into an initial 'off' state.
    *  @return No return value
   */
   OverFlowIndicator()
   {   overFlow = false;
       width  = 90;
       height = 35;
       setSize( width, height );
   }
   
   /** Sets the state of the overflow indicator.
    *  @param newState where true == on, false == off.
    *  @return No return value.
   */
   void setState( boolean newState )
   {  if ( overFlow != newState )
      {  overFlow = newState;
         repaint();
      }
   }
   
   /** Retrieves the state of the overflow indicator.
    *  @return boolean where true == on, false == off.
   */
   boolean getState()
   {
      return overFlow;
   }
   
   public void paint( Graphics g )
   {  g.setColor( Color.white );
      g.fillRect( 0, 0, width, height );
      g.setColor( Color.black );
      g.setFont( OFONT );
      g.drawString( "Overflow", 30, 15 );
      g.drawString(  "toggle", 35, 26 );
      if ( overFlow )
      {  g.setColor( Color.red );
         g.fillArc( 8, 8, 15, 15, 0, 360 );
         g.setColor( Color.black );
      }
      g.drawArc( 8, 8, 15, 15, 0, 360 );
      g.drawArc( 12, 12, 1, 1, 0, 360 );
      g.drawArc( 18, 12, 1, 1, 0, 360 );
      g.drawArc( 18, 18, 1, 1, 0, 360 );
      g.drawArc( 12, 18, 1, 1, 0, 360 );
   }

   // Eliminates flicker (from Eckel p.686)
   public void update( Graphics g )
   {  paint(g);
   }
}

///:~
