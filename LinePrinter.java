//: LinePrinter.java
//  The visible output of the MIX 1009.
import java.awt.*;
import java.awt.event.*;

/** LinePrinter is the window used for simulated physical output of
 *  the MIX1009 machine, basically consisting of a TextArea.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class LinePrinter extends Frame
{  private static final String SIGMA = ( "" + ((char)0x03A3)),
                               DELTA = ( "" + ((char)0x0394)),
                               PI    = ( "" + ((char)0x03A0));

   private static final Font MONOSPACEDFONT = new Font( "Monospaced", Font.PLAIN, 12 );

   private TextArea textOutput;

   private MenuBar mb;

   private Menu printerMenu;
   
   private MenuItem clearAll;

   private int charCounter,
               MAXLINELENGTH = 120;

   /** Sole constructor for the LinePrinter.
    *  @return No return value.
   */
   LinePrinter()
   {   charCounter = 0;
       textOutput = new TextArea(1, 70);
       textOutput.setFont( MONOSPACEDFONT );
       textOutput.setEditable( false );
       textOutput.setBackground( Color.white );
       textOutput.setForeground( Color.black );

       mb = new MenuBar();
       printerMenu = new Menu( "Printer" );
       mb.add( printerMenu );
       clearAll = new MenuItem( "Clear All", new MenuShortcut( KeyEvent.VK_X ) );       
       clearAll.addActionListener( new ClearAllActionL() );
       printerMenu.add( clearAll );

       setLayout( new BorderLayout() );
       setTitle( "MIX 1009 Line Printer" );
       
       setMenuBar( mb );

       add( "Center", textOutput );
   }

   /** Outputs the character equivalents of a MIX word to the line printer
    *  @param word The MIXWord to be printed
    *  @return No return value.
    *  @exception NotAMIXCharacterException Thrown if the character is not in the range 0-55
   */
   void print( MIXWord word ) throws NotAMIXCharacterException
   {  int characterCode;
      char character = ' ';

      try
      {  for (int i=0; i<5; i++)
         {  characterCode = word.getValue( i );

            switch (characterCode)
            {  case  0 : character = ' '; break;
               case  1 : character = 'A'; break;
               case  2 : character = 'B'; break;
               case  3 : character = 'C'; break;
               case  4 : character = 'D'; break;
               case  5 : character = 'E'; break;
               case  6 : character = 'F'; break;
               case  7 : character = 'G'; break;
               case  8 : character = 'H'; break;
               case  9 : character = 'I'; break;
               case 10 : character = ((char)0x0394); break; // Delta
               case 11 : character = 'J'; break;
               case 12 : character = 'K'; break;
               case 13 : character = 'L'; break;
               case 14 : character = 'M'; break;
               case 15 : character = 'N'; break;
               case 16 : character = 'O'; break;
               case 17 : character = 'P'; break;
               case 18 : character = 'Q'; break;
               case 19 : character = 'R'; break;
               case 20 : character = ((char)0x03A3); break; // Sigma
               case 21 : character = ((char)0x03A0); break; // Pi
               case 22 : character = 'S'; break;
               case 23 : character = 'T'; break;
               case 24 : character = 'U'; break;
               case 25 : character = 'V'; break;
               case 26 : character = 'W'; break;
               case 27 : character = 'X'; break;
               case 28 : character = 'Y'; break;
               case 29 : character = 'Z'; break;
               case 30 : character = '0'; break;
               case 31 : character = '1'; break;
               case 32 : character = '2'; break;
               case 33 : character = '3'; break;
               case 34 : character = '4'; break;
               case 35 : character = '5'; break;
               case 36 : character = '6'; break;
               case 37 : character = '7'; break;
               case 38 : character = '8'; break;
               case 39 : character = '9'; break;
               case 40 : character = '.'; break;
               case 41 : character = ','; break;
               case 42 : character = '('; break;
               case 43 : character = ')'; break;
               case 44 : character = '+'; break;
               case 45 : character = '-'; break;
               case 46 : character = '*'; break;
               case 47 : character = '/'; break;
               case 48 : character = '='; break;
               case 49 : character = '$'; break;
               case 50 : character = '<'; break;
               case 51 : character = '>'; break;
               case 52 : character = '@'; break;
               case 53 : character = ';'; break;
               case 54 : character = ':'; break;
               case 55 : character = '\''; break;
               default : throw new NotAMIXCharacterException();
            }

            textOutput.append( ""+character );
            charCounter++;
            if (charCounter == MAXLINELENGTH)
            {  textOutput.append( ""+'\n' );
               charCounter = 0;
            }
         }
      }
      catch (Exception e)
      { if (e instanceof NotAMIXCharacterException)
           throw (NotAMIXCharacterException)e;
      }
   }

   /** Clears the contents of the LinePrinter by blanking the text area
    *  @return No return value
   */
   void clear()
   {  textOutput.setText("");
      charCounter = 0;
   }

   private class ClearAllActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  System.out.println( "Clear All selected..." );
         textOutput.setText("");
         charCounter = 0;
      }
   }

   // Test code
   public static void main(String args[]) throws NotAMIXCharacterException,
                                                 ValueOutOfBoundsException,
                                                 IndexOutOfRangeException
   {  LinePrinter lpr = new LinePrinter();
      MIXWord word = new MIXWord();
      word.setValue(0, 1);
      word.setValue(1, 15);
      word.setValue(2, 4);
      word.setValue(3, 28);

      lpr.setSize( 550, 300 );
      lpr.setVisible( true );

      while (true)        
         lpr.print( word );

   }
}

///:~
