//: MIXClock.java
//  Gives program timing information to the user.
import java.awt.*;
import java.awt.event.*;

/** MIXClock gives the user a readout of how long a program has
 *  taken to execute.  Measured in u, the time is mainly used
 *  for comparative purposes between algorithms and optimization.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class MIXClock extends Frame
{  private int clock = 0;                        // Zero to start with...

   private TextField clockDisplay;

   private Label timeElapsed,
                 u;

   private MenuBar mb;
   private Menu clockMenu;
   private MenuItem resetClock;
   private Panel clockPanel;

   /** Sole constructor for the MIXClock.
    *  @return No return value.
   */
   MIXClock()
   {   clockDisplay = new TextField( 10 );
       timeElapsed  = new Label( "Time elapsed: ", Label.RIGHT );
       u            = new Label( "u", Label.LEFT );

       clockDisplay.setEditable( false );
       clockDisplay.setText( "0000000000" );

       clockPanel = new Panel();
       setLayout( new BorderLayout() );
       setTitle( "MIX 1009 Clock" );

       mb = new MenuBar();
       clockMenu = new Menu( "Clock" );
       mb.add( clockMenu );
       resetClock = new MenuItem( "Reset", new MenuShortcut( KeyEvent.VK_X ) );
       resetClock.addActionListener( new ResetClockActionL() );
       clockMenu.add( resetClock );

       setMenuBar( mb );

       clockPanel.setLayout( new FlowLayout() );
       clockPanel.setBackground( Color.white );

       clockPanel.add( timeElapsed );
       clockPanel.add( clockDisplay );
       clockPanel.add( u );

       add( BorderLayout.CENTER, clockPanel );
   }

   /** Updates the clock display to hold the correct number of
    *  digits accounting for changes in the length of the number.
    *  @return No return value.
   */
   private void updateClock()
   {  int digitCount = 1,
          divisions = clock;

      String clockText = "";

      do
      {  divisions = ( divisions / 10 );
         digitCount++;
      }
      while( divisions != 0 );

      for ( int i = 1; i < (12 - digitCount); i++ )
         clockText += "0";

      clockText += clock;
      clockDisplay.setText( clockText );
   }

   /** Increments the clock by the specified number and updates the
    *  display accordingly.
    *  @param int value to increment the clock by.
    *  @return No return value.
   */
   void incrementClock( int increment )
   {  clock += increment;
      updateClock();
   }

   /** Resets the clock to zero.
    *  @return No return value.
   */
   void resetClock()
   {  clock = 0;
      updateClock();
   }

   private class ResetClockActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  System.out.println( "Reset Clock selected..." );
         resetClock();
      }
   }
   
   // Test code
   public static void main( String args[] )
   {  MIXClock testClock = new MIXClock();
      testClock.setSize( 300, 85 );
      testClock.setVisible( true );
      for ( int i = 0; i < 10008; i++ )
         testClock.incrementClock( 1 );
      testClock.resetClock();
   }
}

