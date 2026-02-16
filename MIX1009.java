//: MIX1009.java
//  An Implementation of Donald Knuth's MIX
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.URL;
import ImageLabel;

/** An Implementation of Donald Knuth's MIX
 *  Third Year Project 1998-99
 *  Department of Computer Science, University of Warwick
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
public class MIX1009 extends Applet
{  static final boolean OPEN       = true,                  // Set up some English definitions
                        CLOSED     = false;

   private static Button beginButton = new Button( "Start" ),
                         infoButton  = new Button( "Info" );
   private static Panel  bottomPanel = new Panel();         // Holds the buttons
   private static ImageLabel MIXLogo,                       // Main applet window logo
                             infoImage;                     // Info window contents

   private static ImageLabel logo;

   private static boolean begun      = CLOSED,              // We start with no windows open
                          infoWindow = CLOSED;
                                   
   /** Gets us into a valid working state on web page.
    *  @return No return value.
   */
   public void init()
   {  showStatus( "Loading images..." );                                       // Let the user know 
                                                                               //   what's happening
      MIXLogo   = new ImageLabel( getDocumentBase(), "graphics/MIX.jpg" );     // Load the MIX logo
      MIXLogo.waitForImage( true );
      infoImage = new ImageLabel( getDocumentBase(), "graphics/MIXInfo.jpg" ); // Load the Info
                                                                               // window information
      infoImage.waitForImage( true );
      logo      = new ImageLabel( getDocumentBase(), "graphics/MIXLogo.jpg" );
      showStatus( "" );
         
      beginButton.addActionListener( new BeginActionL() );  // Tie the buttons to their respective
      infoButton.addActionListener( new InfoActionL() );    //   actions
      beginButton.setBackground( Color.lightGray );         // Colour them grey so they look
      infoButton.setBackground( Color.lightGray );          //   'pushable'

      setLayout( new BorderLayout() );                      // Intro window layout
      setBackground( Color.white );                         // Make the background the same color as
                                                            //   the image
      bottomPanel.setLayout( new GridLayout(1,2,60,0) );    // Button bar layout
      bottomPanel.setBackground( Color.white );
      bottomPanel.add( beginButton );
      bottomPanel.add( infoButton  );
      add( "South", bottomPanel );                          // Put the button bar on the bottom
      add( "Center", MIXLogo );                             // Put the image in the center
   }
      
   /** Lets us know whether the Info window is open or closed.
    *  @return boolean representing either OPEN or CLOSED
   */
   private boolean getInfoWindowStatus()
   {  return infoWindow;
   }
   
   /** Sets the Info window status to open or closed.
    *  @param status representing either OPEN or CLOSED
   */
   private void setInfoWindowStatus( boolean status )
   {  infoWindow = status;
   }
   
   /** Creates and displays the Info window.
    *  @return No return value.
   */
   private void displayInfoWindow()
   {  final InfoWindow iw = new InfoWindow( infoImage, "MIX : Info" );
      setInfoWindowStatus( OPEN );
      iw.addComponentListener( new ComponentAdapter()
      {  public void componentHidden( ComponentEvent e )
         {  iw.dispose();
            setInfoWindowStatus( CLOSED );
         }
      } );
      iw.setVisible( true );
   }
   
   /** Will invoke the Info window if it is not already displayed.
   */
   private class InfoActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  if ( getInfoWindowStatus() == CLOSED )
            displayInfoWindow();
      }
   }

   /** Lets us know if the MIX1009 machine window has been opened.
    *  @return boolean representing either OPEN or CLOSED
   */
   private boolean getMIXMachineStatus()
   {  return begun;
   }

   /** Sets the status of the MIX1009 machine window to open or closed.
    *  @param status representing either OPEN or CLOSED
    *  @return No return value.
   */
   private void setMIXMachineStatus( boolean status )
   {  begun = status;
   }

   /** Creates and displays the MIX1009 machine window.
    *  @return No return value.
   */
   private void startMIXMachine()
   {  final MIXMachine mm = new MIXMachine( logo );
      setMIXMachineStatus( OPEN );
      mm.addComponentListener( new ComponentAdapter()
      {  public void componentHidden( ComponentEvent e )
         {   mm.dispose();
             setMIXMachineStatus( CLOSED );
         }
      } );
      mm.setVisible( true );
   }

   /** Starts the MIX1009 machine if it hasn't already been opened. 
   */
   private class BeginActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      { if ( getMIXMachineStatus() == CLOSED )
           startMIXMachine();
      }
   }
}

///:~
