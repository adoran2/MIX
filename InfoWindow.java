//: InfoWindow.java
//  Displays a 'dialogue box' information window
import java.awt.*;
import java.awt.event.*;
import ImageLabel;

/** @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
public class InfoWindow extends Frame
{  private Panel bottomPanel = new Panel();                 // This panel holds the
   private Button dismissButton = new Button( "Dismiss" );  // Button to close the InfoWindow
   
   /** InfoWindow constructor
    *  @param legend ImageLabel to be displayed
    *  @param windowTitle String containing requested window title
   */
   public InfoWindow( ImageLabel legend, String windowTitle )
   {  setLayout( new BorderLayout() );
      setBackground( Color.white );                                     
      setResizable( false );                                 // No resizing as it is an Info box
      setTitle( windowTitle );                               // Assign the proper title
      setSize( legend.getWidth(), legend.getHeight() + 50 ); // Make the window a viewable size
      bottomPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );  
      bottomPanel.setBackground( Color.white );
      dismissButton.addActionListener( new DismissActionL() );
      dismissButton.setBackground( Color.lightGray );        // Give the button a 'pushy' feel
      bottomPanel.add( dismissButton );
      add( "South", bottomPanel );                           // Add the Dismiss button panel
      add( "Center", legend );                               // Add the ImageLabel
   }
   
   /** Closes the window and thus invokes a componentHidden event
   */
   private class DismissActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  setVisible( false );
      }
   }
}
///:~
