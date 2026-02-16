//: InputConsole.java
//  Window for editing MIXAL programs
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;

/** InputConsole allows the user to edit and prepare MIXAL programs
 *  for the MIX1009.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class InputConsole extends Frame
{  private static final String SIGMA = ( "" + ((char)0x03A3)),
                               DELTA = ( "" + ((char)0x0394)),
                               PI    = ( "" + ((char)0x03A0));
                               
   private static final Font MONOSPACEDFONT = new Font( "Monospaced", Font.PLAIN, 12 );

   private Button sigma,                                    // These are for character input...
                  delta,
                  pi,
                  cut,                                      // ...and these are for window actions.
                  copy,
                  paste;

   private TextArea textinput;                              // Main window for editing programs
      
   private Panel greekPanel,                                // Panel to hold greek character buttons
                 utilPanel,                                 // Panel to hold action buttons
                 buttonBar;                                 // Holds both of the above panels
                    
   private MenuBar mb;                                      // Our menu bar
      
   private Menu insertMenu,
                editMenu;
      
   private MenuItem[] greekItems = { new MenuItem( "Delta", new MenuShortcut( KeyEvent.VK_D ) ),
                                     new MenuItem( "Sigma", new MenuShortcut( KeyEvent.VK_S ) ),
                                     new MenuItem( "Pi"   , new MenuShortcut( KeyEvent.VK_P ) ) },
                      editItems =  { new MenuItem( "Cut"  , new MenuShortcut( KeyEvent.VK_X ) ),
                                     new MenuItem( "Copy" , new MenuShortcut( KeyEvent.VK_C ) ),
                                     new MenuItem( "Paste", new MenuShortcut( KeyEvent.VK_V ) ),
                                     new MenuItem( "Clear All" ) };
                                                  // An item to clear the entire window

   private Clipboard clipbd;                                // For cut and paste operations
   
   private InsertDeltaL deltaL;                             // 'ActionListener' objects, created
   private InsertSigmaL sigmaL;                             // this way so that they can be attached
   private InsertPiL    piL;                                // to both the menu items and the button 
   private CutL         cutL;                               // bar.
   private CopyL        copyL;
   private PasteL        pasteL;

   /** Sole constructor for the ImageConsole
    *  @return No return value.
   */
   InputConsole()
   {  greekPanel  = new Panel();
      utilPanel   = new Panel();
      buttonBar   = new Panel();
   
      delta = new Button( DELTA );
      sigma = new Button( SIGMA );
      pi    = new Button( PI );
      cut   = new Button( "Cut" );
      copy  = new Button( "Copy" );
      paste = new Button( "Paste" );

      delta.setBackground( Color.lightGray );               // The buttons should be grey to make
      sigma.setBackground( Color.lightGray );               // them look 'pushable' on the white
      pi.setBackground( Color.lightGray );		    // background
      cut.setBackground( Color.lightGray );
      copy.setBackground( Color.lightGray );
      paste.setBackground( Color.lightGray );                
      
      textinput = new TextArea(1, 70);                      // 70 Characters wide is enough
      textinput.setFont( MONOSPACEDFONT );                  // Keep all the buttons and the text
      delta.setFont( MONOSPACEDFONT );                      // area in our monospaced font for
      sigma.setFont( MONOSPACEDFONT );                      // consistency.
      pi.setFont( MONOSPACEDFONT );
      
      // This section sets up the display
      setLayout( new BorderLayout() );
      greekPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
      greekPanel.add( delta );
      greekPanel.add( sigma );
      greekPanel.add( pi );
      
      utilPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
      utilPanel.add( cut );
      utilPanel.add( copy );
      utilPanel.add( paste );
      
      buttonBar.setLayout( new GridLayout(1,2) );
      buttonBar.add( greekPanel );
      buttonBar.add( utilPanel );
      
      buttonBar.setBackground( Color.white );

      add( "North", buttonBar );
      add( "Center", textinput );

      mb = new MenuBar();
      insertMenu = new Menu( "Insert" );
      editMenu   = new Menu( "Edit" );

      for ( int i = 0; i < greekItems.length; i++ )
         insertMenu.add( greekItems[i] );
      for ( int i = 0; i < editItems.length; i++ )
         editMenu.add( editItems[i] );

      mb.add( insertMenu );
      mb.add( editMenu );
      setMenuBar( mb );
      textinput.requestFocus();                             // Start with the input focus in the
      setTitle( "MIX 1009 Program" );                       // text window
      // End of display setup section
      
      clipbd = new Clipboard( "InputConsoleClipboard" );    // We cannot access the system clipboard
      deltaL = new InsertDeltaL();                          // from an applet
      sigmaL = new InsertSigmaL();
      piL    = new InsertPiL();
      cutL   = new CutL();
      copyL  = new CopyL();
      pasteL = new PasteL();
      
      delta.addActionListener( deltaL );
      greekItems[0].addActionListener( deltaL );
      sigma.addActionListener( sigmaL );
      greekItems[1].addActionListener( sigmaL );
      pi.addActionListener( piL );
      greekItems[2].addActionListener( piL );
      cut.addActionListener( cutL );
      editItems[0].addActionListener( cutL );
      copy.addActionListener( copyL );
      editItems[1].addActionListener( copyL );
      paste.addActionListener( pasteL );
      editItems[2].addActionListener( pasteL );

      editItems[3].addActionListener( new ClearAllL() );
   }

   /** Lets the calling object obtain the program from the console
    *  @return String returned by getText()
   */
   String getProgram()
   {  return textinput.getText();
   }
   
   private boolean weHaveSelectedText()
   {  if ( textinput.getSelectionStart() == textinput.getSelectionEnd() )
        return false;
      else return true;
   }
   
   private class InsertDeltaL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  if ( weHaveSelectedText() )
            textinput.replaceRange(DELTA,textinput.getSelectionStart(),textinput.getSelectionEnd());
         else textinput.insert( DELTA, textinput.getCaretPosition() );
      }
   }
   
   private class InsertSigmaL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  if ( weHaveSelectedText() )
            textinput.replaceRange(SIGMA,textinput.getSelectionStart(),textinput.getSelectionEnd());
         else textinput.insert( SIGMA, textinput.getCaretPosition() );
      }
   }
   
   private class InsertPiL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  if ( weHaveSelectedText() )
            textinput.replaceRange(PI, textinput.getSelectionStart(), textinput.getSelectionEnd());
         else textinput.insert( PI, textinput.getCaretPosition() );
      }
   }

   /* Originally, with the following classes, I wanted to allow access
    * to the system clipboard so that programs could be cut and pasted into
    * the window after being downloaded from the web site.  However, due to
    * security reasons, applets are not allowed access to the system clipboard
    * so this has been modified to used an 'internal' clipboard.
   */
   
   private class CutL implements ActionListener                // From Eckel pp. 702
   {  public void actionPerformed( ActionEvent e )
      {  String selection = textinput.getSelectedText();
         StringSelection clipString = new StringSelection( selection );
         clipbd.setContents( clipString, clipString );
         textinput.replaceRange( "", textinput.getSelectionStart(), textinput.getSelectionEnd() );
      }
   }
   
   private class CopyL implements ActionListener        // From Eckel pp. 702
   {  public void actionPerformed( ActionEvent e )
      {  String selection = textinput.getSelectedText();
         StringSelection clipString = new StringSelection( selection );
         clipbd.setContents( clipString, clipString );
      }
   }
   
   private class PasteL implements ActionListener        // From Eckel pp. 702
   {  public void actionPerformed( ActionEvent e )
      {  Transferable clipData = clipbd.getContents( InputConsole.this );
         try
         {  String clipString = (String)clipData.getTransferData( DataFlavor.stringFlavor );
            if ( weHaveSelectedText() )
               textinput.replaceRange( clipString, textinput.getSelectionStart(),
                                                   textinput.getSelectionEnd() );
            else textinput.insert( clipString, textinput.getCaretPosition() );
         }
         catch( Exception ex )
         {  System.out.println( "Clipboard doesn't contain pastable text." );
         }
      }
   }

   private class ClearAllL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  System.out.println("Clear All selected...");
      }
   }

   // Test code
   public static void main( String args[] )
   {  InputConsole win = new InputConsole();
      win.setSize( 500, 300 );
      win.show();
   }
}
