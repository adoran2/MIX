//: MIXMachine.java
//  The main MIX frame object that controls the machine
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import ImageLabel;

/** MIXMachine 'controls' the main objects in the Applet and shows the
 *  user the internal states of the MIX1009.
 *  @author Andrew Doran
 *  @author http://andrew.doran.com/
 *  @version 0.11 - 30 March 1999
*/
class MIXMachine extends Frame
{  // ---- Memory and memory indicators
   private MIXWord[]           memory;
   private MIXWord             aRegister,
                               xRegister;
   private IRegister[]         iRegisters;
   private JRegister           jRegister;
   private ComparisonIndicator cIndicator;
   private OverFlowIndicator   oIndicator;

   // ---- Peripherals
   // private MagneticTapeUnit[] tapeUnit;        // see Knuth p. 137
   // private DiskOrDrumUnit[]   diskUnit;
   // private CardReader         cardReader;
   // private CardPunch          cardPunch;
   private LinePrinter         lpr;
   // private PaperTape        paperTape;
   private InputConsole        program;

   // ---- My peripherals
   private ControlConsole      control;
   private MIXClock            clock;
   private ProgramLoader       pLoader;

   // ---- GUI declarations
   private static final Font MIXFont = new Font( "Monospaced", Font.PLAIN, 12 );   
   private MenuBar mb;
   private Menu mixMenu,
                viewMenu,
                helpMenu;
   private MenuItem reset,
                    quit,
                    info;
   private CheckboxMenuItem[] viewOptions;
   private ImageLabel logo;
   private Label regA,
                 regX,
                 regJ,
                 memoryCells;
   private Label[] regIx,
                   memoryLabels;
   private MIXWord[] memoryVisible;
   private Panel memoryPanel,
                 aXRegPanel,
                 mainPanel;
   private Scrollbar memoryScroller;
   private GridBagLayout mGBL,
                         pGBL;
   private GridBagConstraints mGBC,
                              pGBC;

   private int globalPC = 0;                                // SEE CONTROL SECTION - START VALUE
                                                            // OF STEPPED PROGRAM
   private boolean choiceFlag = false;

   /** Sole entry point to machine.  This constructor establishes a MIX1009
    *  in its default state.
    *  @return No return value.
   */
   MIXMachine( ImageLabel logo )
   {  // ---- Instantiate memory and indicator components
      memory     = new MIXWord[ 4000 ];
      for ( int i=0; i < 4000; i++ )
         memory[ i ] = new MIXWord();
      aRegister  = new MIXWord();
      xRegister  = new MIXWord();
      iRegisters = new IRegister[ 6 ];
      for ( int i=0; i < 6; i++ )
         iRegisters[ i ] = new IRegister();
      jRegister  = new JRegister();
      cIndicator = new ComparisonIndicator();
      oIndicator = new OverFlowIndicator();
      
      // ---- Instantiate peripherals
      // tapeUnit = new MagneticTapeUnit[ 8 ];
      // diskUnit = new DiskOrDrumUnit[ 8 ];
      // cardReader = new CardReader();
      // cardPunch = new CardPunch();
      lpr      = new LinePrinter();
      // paperTape = new PaperTape();
      program  = new InputConsole();

      // ---- Instantiate my peripherals
      control  = new ControlConsole();
      clock    = new MIXClock();
      pLoader  = new ProgramLoader();

      // ---- Setup GUI - menus
      mb = new MenuBar();
      mixMenu  = new Menu( "MIX" );
      viewMenu = new Menu( "View" );
      helpMenu = new Menu( "Help" );
      reset    = new MenuItem( "Reset All" );
      quit     = new MenuItem( "Quit" );
      info     = new MenuItem( "Info" );
      viewOptions = new CheckboxMenuItem[ 4 ];
      viewOptions[ 0 ] = new CheckboxMenuItem( "Program" );
      viewOptions[ 1 ] = new CheckboxMenuItem( "Line Printer" );
      viewOptions[ 2 ] = new CheckboxMenuItem( "Control Console" );
      viewOptions[ 3 ] = new CheckboxMenuItem( "Clock" );

      reset.addActionListener( new ResetActionL() );
      quit.addActionListener( new QuitActionL() );
      info.addActionListener( new HelpActionL() );
      viewOptions[0].addItemListener( new ProgramItemActionL() );
      viewOptions[1].addItemListener( new PrinterItemActionL() );
      viewOptions[2].addItemListener( new ControlItemActionL() );
      viewOptions[3].addItemListener( new ClockItemActionL() );

      mixMenu.add( reset );
      mixMenu.add( quit );
      for ( int i = 0; i < viewOptions.length; i++ )
         viewMenu.add( viewOptions[i] );
      helpMenu.add( info );
      mb.add( mixMenu );
      mb.add( viewMenu );
      mb.add( helpMenu );
      setMenuBar( mb );

      // ---- Setup GUI - frame
      setTitle( "MIX 1009 Main Window" );
      setSize( 450, 600 );
      setResizable( false );
      regA = new Label( "Register A", Label.CENTER );
      regX = new Label( "Register X", Label.CENTER );
      regJ = new Label( "Register J", Label.CENTER );
      regA.setFont( MIXFont );
      regX.setFont( MIXFont );
      regJ.setFont( MIXFont );
      regIx = new Label[ 6 ];
      for ( int i = 0; i < 6; i++ )
      {  regIx[ i ] = new Label( "Register I" + (i+1), Label.CENTER );
         regIx[ i ].setFont( MIXFont );
      }

      // ------ Create memory panel
      memoryPanel = new Panel();
      memoryCells = new Label( "Memory cells", Label.CENTER );
      memoryCells.setFont( MIXFont );
      memoryScroller = new Scrollbar( Scrollbar.VERTICAL, 0, 10, 0, 4000 );
      memoryScroller.addAdjustmentListener( new MemoryAdjustL() );
      memoryLabels = new Label[ 10 ];
      memoryVisible = new MIXWord[ 10 ];
      for ( int i = 0; i < 10; i++ )
      {  memoryLabels[ i ] = new Label( "000" + i, Label.RIGHT );
         memoryLabels[ i ].setFont( MIXFont );
         memoryVisible[ i ] = new MIXWord();
         mMOVE( memory[ i ], memoryVisible[ i ] );
      }
      mGBL = new GridBagLayout();
      memoryPanel.setLayout( mGBL );
      mGBC = new GridBagConstraints();
      memoryPanel.setBackground( Color.white );
      mGBC.fill = GridBagConstraints.HORIZONTAL;
      addComponent( memoryCells, 0, 0, 3, 1, mGBL, mGBC, memoryPanel );
      mGBC.fill = GridBagConstraints.NONE;
      for ( int i = 0; i < 10; i++ )
         addComponent( memoryVisible[ i ], 1, i+1, 1, 1, mGBL, mGBC, memoryPanel );
      mGBC.fill = GridBagConstraints.VERTICAL;
      addComponent( memoryScroller, 2, 1, 1, 10, mGBL, mGBC, memoryPanel );
      mGBC.anchor = GridBagConstraints.EAST;
      for ( int i = 0; i < 10; i++ )
         addComponent( memoryLabels[ i ], 0, i+1, 1, 1, mGBL, mGBC, memoryPanel );

      // ------ Create A/XRegister panel
      aXRegPanel = new Panel();
      aXRegPanel.setLayout( new GridLayout( 2, 2 ) );
      aXRegPanel.setBackground( Color.white );
      aXRegPanel.add( regA );
      aXRegPanel.add( regX );
      aXRegPanel.add( aRegister );
      aXRegPanel.add( xRegister );

      // ------ Create main panel
      mainPanel = new Panel();
      pGBL = new GridBagLayout();
      mainPanel.setLayout( pGBL );
      pGBC = new GridBagConstraints();
      pGBC.fill = GridBagConstraints.HORIZONTAL;
      mainPanel.setBackground( Color.white );
      addComponent( logo, 0, 0, 3, 2, pGBL, pGBC, mainPanel );
      addComponent( aXRegPanel, 0, 2, 3, 2, pGBL, pGBC, mainPanel );
      pGBC.anchor = GridBagConstraints.EAST;
      addComponent( oIndicator, 1, 4, 1, 2, pGBL, pGBC, mainPanel );
      addComponent( cIndicator, 2, 4, 1, 2, pGBL, pGBC, mainPanel );
      pGBC.anchor = GridBagConstraints.CENTER;
      for ( int i = 0; i < 6; i++ )
         addComponent( iRegisters[ i ], 0, 5+(i*2), 1, 1, pGBL, pGBC, mainPanel );
      pGBC.anchor = GridBagConstraints.SOUTH;
      addComponent( regJ, 0, 16, 1, 1, pGBL, pGBC, mainPanel );
      addComponent( jRegister, 0, 17, 1, 1, pGBL, pGBC, mainPanel );
      pGBC.anchor = GridBagConstraints.CENTER;
      addComponent( memoryPanel, 1, 6, 2, 11, pGBL, pGBC, mainPanel );
      pGBC.fill = GridBagConstraints.HORIZONTAL;
      for ( int i = 0; i < 6; i++ )
         addComponent( regIx[ i ], 0, 4+(i*2), 1, 1, pGBL, pGBC, mainPanel );

      setLayout( new BorderLayout() );
      add( "Center", mainPanel );

      // ---- Setup GUI - peripheral windows
      program.setSize( 500, 300 );
//      program.setVisible( true );
//      viewOptions[0].setState( true );
      lpr.setSize( 550, 300 );
//      lpr.setVisible( true );
//      viewOptions[1].setState( true );
      control.setSize( 300, 80 );
      control.setVisible( true );
      viewOptions[2].setState( true );
      clock.setSize( 300, 100 );
//      clock.setVisible( true );
//      viewOptions[3].setState( true );
      pLoader.setSize( 100, 200 );
      pLoader.setResizable( false );
      pLoader.setVisible( true );
   }

   /** addComponent method from Deitel & Deitel p.634
   */
   private void addComponent( Component c, int row, int column, int width, int height,
                                GridBagLayout gbl, GridBagConstraints gbc, Panel p )
   {  gbc.gridx = row;
      gbc.gridy = column;
      gbc.gridwidth = width;
      gbc.gridheight = height;
      gbl.setConstraints( c, gbc );
      p.add( c );
   }
   
   private class ResetActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  System.out.println( "Reset selected..." );
         MIXChoiceDialog message = new MIXChoiceDialog("Reset", "Are you sure you want to reset?");
         if (choiceFlag == true)
         {  clearMemoryContents();
            memoryScroller.setValue( memoryScroller.getValue() ); // Repaints the memory values.
            clock.resetClock();
            lpr.clear();
         }
      }
   }

   private class QuitActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  System.out.println( "Quit selected..." );
         MIXChoiceDialog message = new MIXChoiceDialog( "Quit", "Are you sure you want to quit?" );
         if (choiceFlag == true)
         {  program.dispose();
            lpr.dispose();
            control.dispose();
            clock.dispose();
            pLoader.dispose();
            setVisible( false );
         }
      }
   }
   
   private class HelpActionL implements ActionListener
   {  public void actionPerformed( ActionEvent e )
      {  System.out.println( "Help selected..." );
         MIXDialog message = new MIXDialog( "Information", "On-line help is to be implemented" );
      }
   }
   
   /** Shows/hides the program window
   */
   private class ProgramItemActionL implements ItemListener
   {  public void itemStateChanged( ItemEvent e )
      {  if ( ((CheckboxMenuItem)e.getSource()).getState() )
         {  System.out.println( "Program selected..." );
            program.setVisible( true );
         }
         else
         {  System.out.println( "Program deselected..." );
            program.setVisible( false );
         }
      }
   }
   
   /** Shows/hides the printer window
   */
   private class PrinterItemActionL implements ItemListener
   {  public void itemStateChanged( ItemEvent e )
      {  if ( ((CheckboxMenuItem)e.getSource()).getState() )
         {  System.out.println( "Printer selected..." );
            lpr.setVisible( true );
         }
         else
         {  System.out.println( "Printer deselected..." );
            lpr.setVisible( false );
         }
      }
   }
   
   /** Shows/hides the control window
   */
   private class ControlItemActionL implements ItemListener
   {  public void itemStateChanged( ItemEvent e )
      {  if ( ((CheckboxMenuItem)e.getSource()).getState() )
         {  System.out.println( "Control selected..." );
            control.setVisible( true );
         }
         else
         {  System.out.println( "Control deselected..." );
            control.setVisible( false );
         }
      }
   }
   
   /** Shows/hides the clock window
   */
   private class ClockItemActionL implements ItemListener
   {  public void itemStateChanged( ItemEvent e )
      {  if ( ((CheckboxMenuItem)e.getSource()).getState() )
         {  System.out.println( "Clock selected..." );
            clock.setVisible( true );
         }
         else
         {  System.out.println( "Clock deselected..." );
            clock.setVisible( false );
         }
      }
   }

   /** Adjusts what section of main memory is displayed according
    *  to the position of the memory scroll bar
   */
   private class MemoryAdjustL implements AdjustmentListener
   {  public void adjustmentValueChanged( AdjustmentEvent e )
      {  String zeros;
         int sVal = e.getValue();
         for ( int i = sVal; i < (sVal + 10); i++ )
         {  if (i < 10)
               zeros = "000";
            else if (i < 100)
               zeros = "00";
            else if (i < 1000)
               zeros = "0";
            else zeros = "";
            mMOVE( memory[ i ], memoryVisible[ i-sVal ] );
            memoryLabels[ i - sVal ].setText( zeros + i );
         }
         mainPanel.repaint();
      }
   }

   /** A modal dialogue box with one button to dismiss
   */
   class MIXDialog extends Dialog
   {  Label dTextL;
      Button dismissDB = new Button( "OK" );
      Panel dButtonPanel = new Panel();

      /** Sole constructor
       *  @param dTitle String containing title of dialogue box
       *  @param dText String containing text to be displayed
       *  @rerturn No return value.
      */
      MIXDialog( String dTitle, String dText )
      {  super( MIXMachine.this, dTitle, true );
         setLayout( new BorderLayout() );
         setBackground( Color.white );
         dismissDB.setBackground( Color.lightGray );
         dButtonPanel.setLayout( new FlowLayout() );
         dButtonPanel.setBackground( Color.white );
         dButtonPanel.add( dismissDB );
         add( BorderLayout.SOUTH, dButtonPanel );
         dTextL = new Label( dText, Label.CENTER );
         add( BorderLayout.CENTER, dTextL );
         setSize( 300, 100 );
         setResizable( false );
         dismissDB.addActionListener( new ActionListener()
         {  public void actionPerformed( ActionEvent e )
            {  setVisible( false );
            }
         });
         addWindowListener(new WindowAdapter()
         {  public void windowClosing( WindowEvent e )
            {  dispose();
            }
         });
         setVisible( true );
      }
   }

   /** A modal binary-choice dialogue box.  Clicking 'OK' will set the variable
    *  choiceFlag to true, clicking 'Cancel' will set it to false.  This can then
    *  be used by other sections of the object to see the outcome of a choice.
   */
   class MIXChoiceDialog extends Dialog
   {  Label dTextL;
      Button dBOK     = new Button("OK"),
             dBCancel = new Button("Cancel");
      Panel dButtonPanel = new Panel();

      /** Sole constructor
       *  @param dTitle String containing title of dialogue box
       *  @param dText String containing text to be displayed
       *  @rerturn No return value.
      */
      MIXChoiceDialog( String dTitle, String dText )
      {  super( MIXMachine.this, dTitle, true );
         setLayout( new BorderLayout() );
         setBackground( Color.white );
         dBOK.setBackground( Color.lightGray );
         dBCancel.setBackground( Color.lightGray );
         dButtonPanel.setLayout( new FlowLayout() );
         dButtonPanel.setBackground( Color.white );
         dButtonPanel.add( dBOK );
         dButtonPanel.add( dBCancel );
         add( BorderLayout.SOUTH, dButtonPanel );
         dTextL = new Label( dText, Label.CENTER );
         add( BorderLayout.CENTER, dTextL );
         setSize( 300, 100 );
         setResizable( false );
         choiceFlag = false;
         dBOK.addActionListener( new ActionListener()
         {  public void actionPerformed( ActionEvent e )
            {  choiceFlag = true;
               setVisible( false );
            }
         });
         dBCancel.addActionListener( new ActionListener()
         {  public void actionPerformed( ActionEvent e )
            {  setVisible( false );
            }
         });
         addWindowListener( new WindowAdapter()
         {  public void windowClosing( WindowEvent e )
            {  dispose();
            }
         });
         setVisible( true );
      }
   }

   /** ControlConsole is used to start/stop the machine,
    *  step through program instructions one by one and adjust
    *  the speed.
   */
   class ControlConsole extends Frame
   {  private Button[] buttons = { new Button( "Go" ),
                                   new Button( "Stop" ),
                                   new Button( "Step" ),
                                   new Button( "Faster" ),
                                   new Button( "Slower" ) };

      /** Sole constructor for the ControlConsole.
       *  @return No return value.
      */
      ControlConsole()
      {  setLayout( new FlowLayout() );
         setBackground( Color.white );
         setTitle( "MIX 1009 Control" );

         for ( int i = 0; i < buttons.length; i++ )
         {  buttons[ i ].setBackground( Color.lightGray );
            add( buttons[ i ] );
         }

         buttons[ 0 ].addActionListener( new goButtonActionL() );
         buttons[ 2 ].addActionListener( new stepButtonActionL() );
         setResizable( false );
      }

      private class goButtonActionL implements ActionListener
      {  public void actionPerformed( ActionEvent e )
         {  System.out.println( "Go selected..." );
/*            String error = assembleCode();                // Attempt assembly
            if ( !error.equals( "" ) )                      // If we have an error...
               System.out.println( error );                 // ...report it!
*/            execute( globalPC, false );                   // Then run the code.
         }
      }

      private class stepButtonActionL implements ActionListener
      {  public void actionPerformed( ActionEvent e )
         {  globalPC = execute( globalPC, true );
         }
      }
   }

   /** ProgramLoader is used here to put programs into memory for the
    *  purpose of demonstrating MIX features.  As it stands, the MIX
    *  1009 can only be 'programmed' by creating inner classes inside
    *  this class and placing the instructions in memory directly.
    *  In the final version, I do not intend this class to exist, as
    *  programs will be loaded by assembling them from their MIXAL
    *  source.
   */
   class ProgramLoader extends Frame
   {  private Button[] buttons = { new Button( "Primes" ),
                                   new Button( "Maximum" ) };

      /** Sole constructor
       *  @return No return value
      */
      ProgramLoader()
      {  setLayout( new FlowLayout() );
         setBackground( Color.white );
         setTitle( "MIX 1009 Program Loader" );

         for ( int i=0; i<buttons.length; i++ )
         {  buttons[ i ].setBackground( Color.lightGray );
            add( buttons[ i ] );
         }

         buttons[ 0 ].addActionListener( new primesButtonActionL() );
         buttons[ 1 ].addActionListener( new programMButtonActionL() );
      }

      private class primesButtonActionL implements ActionListener
      {  public void actionPerformed( ActionEvent e )
         {  clearMemoryContents();

            try
            {  memory[ 3000 ].setValue( 4, 35 );
               memory[ 3000 ].setValue( 3, 18 );

               memory[ 3001 ].setValue( 4, 9 );
               memory[ 3001 ].setValue( 3, 5 );
               memory[ 3001 ].setValue( 1, 2050 % 64 );
               memory[ 3001 ].setValue( 0, 2050 / 64 );

               memory[ 3002 ].setValue( 4, 10 );
               memory[ 3002 ].setValue( 3, 5 );
               memory[ 3002 ].setValue( 1, 2051 % 64 );
               memory[ 3002 ].setValue( 0, 2051 / 64 );

               memory[ 3003 ].setValue( 4, 49 );
               memory[ 3003 ].setValue( 1, 1 );

               memory[ 3004 ].setValue( 4, 26 );
               memory[ 3004 ].setValue( 3, 5 );
               memory[ 3004 ].setValue( 2, 1 );
               memory[ 3004 ].setValue( 1, 499 % 64 );
               memory[ 3004 ].setValue( 0, 499 / 64 );

               memory[ 3005 ].setValue( 4, 41 );
               memory[ 3005 ].setValue( 3, 1 );
               memory[ 3005 ].setValue( 1, 3016 % 64 );
               memory[ 3005 ].setValue( 0, 3016 / 64 );

               memory[ 3006 ].setValue( 4, 50 );
               memory[ 3006 ].setValue( 1, 2 );

               memory[ 3007 ].setValue( 4, 51 );
               memory[ 3007 ].setValue( 3, 2 );
               memory[ 3007 ].setValue( 1, 2 );

               memory[ 3008 ].setValue( 4, 48 );
               memory[ 3008 ].setValue( 3, 2 );

               memory[ 3009 ].setValue( 4, 55 );
               memory[ 3009 ].setValue( 3, 2 );
               memory[ 3009 ].setValue( 2, 2 );

               memory[ 3010 ].setValue( 4, 4 );
               memory[ 3010 ].setValue( 3, 5 );
               memory[ 3010 ].setValue( 2, 3 );
               memory[ 3010 ].setValue( 1, 1 );
               memory[ 3010 ].setSign( '-' );

               memory[ 3011 ].setValue( 4, 47 );
               memory[ 3011 ].setValue( 3, 1 );
               memory[ 3011 ].setValue( 1, 3006 % 64 );
               memory[ 3011 ].setValue( 0, 3006 / 64 );

               memory[ 3012 ].setValue( 4, 56 );
               memory[ 3012 ].setValue( 3, 5 );
               memory[ 3012 ].setValue( 2, 3 );
               memory[ 3012 ].setValue( 1, 1 );
               memory[ 3012 ].setSign( '-' );

               memory[ 3013 ].setValue( 4, 51 );
               memory[ 3013 ].setValue( 1, 1 );

               memory[ 3014 ].setValue( 4, 39 );
               memory[ 3014 ].setValue( 3, 6 );
               memory[ 3014 ].setValue( 1, 3008 % 64 );
               memory[ 3014 ].setValue( 0, 3008 / 64 );

               memory[ 3015 ].setValue( 4, 39 );
               memory[ 3015 ].setValue( 1, 3003 % 64 );
               memory[ 3015 ].setValue( 0, 3003 / 64 );

               memory[ 3016 ].setValue( 4, 37 );
               memory[ 3016 ].setValue( 3, 18 );
               memory[ 3016 ].setValue( 1, 1995 % 64 );
               memory[ 3016 ].setValue( 0, 1995 / 64 );

               memory[ 3017 ].setValue( 4, 52 );
               memory[ 3017 ].setValue( 3, 2 );
               memory[ 3017 ].setValue( 1, 2035 % 64 );
               memory[ 3017 ].setValue( 0, 2035 / 64 );

               memory[ 3018 ].setValue( 4, 53 );
               memory[ 3018 ].setValue( 3, 2 );
               memory[ 3018 ].setValue( 1, 50 );
               memory[ 3018 ].setSign( '-' );

               memory[ 3019 ].setValue( 4, 53 );
               memory[ 3019 ].setValue( 1, 501 % 64 );
               memory[ 3019 ].setValue( 0, 501 / 64 );

               memory[ 3020 ].setValue( 4, 8 );
               memory[ 3020 ].setValue( 3, 5 );
               memory[ 3020 ].setValue( 2, 5 );
               memory[ 3020 ].setValue( 1, 1 );
               memory[ 3020 ].setSign( '-' );

               memory[ 3021 ].setValue( 4, 5 );
               memory[ 3021 ].setValue( 3, 1 );

               memory[ 3022 ].setValue( 4, 31 );
               memory[ 3022 ].setValue( 3, 12 );
               memory[ 3022 ].setValue( 2, 4 );

               memory[ 3023 ].setValue( 4, 52 );
               memory[ 3023 ].setValue( 3, 1 );
               memory[ 3023 ].setValue( 1, 1 );

               memory[ 3024 ].setValue( 4, 53 );
               memory[ 3024 ].setValue( 3, 1 );
               memory[ 3024 ].setValue( 1, 50 );

               memory[ 3025 ].setValue( 4, 45 );
               memory[ 3025 ].setValue( 3, 2 );
               memory[ 3025 ].setValue( 1, 3020 % 64 );
               memory[ 3025 ].setValue( 0, 3020 / 64 );

               memory[ 3026 ].setValue( 4, 37 );
               memory[ 3026 ].setValue( 3, 18 );
               memory[ 3026 ].setValue( 2, 4 );

               memory[ 3027 ].setValue( 4, 12 );
               memory[ 3027 ].setValue( 3, 5 );
               memory[ 3027 ].setValue( 2, 4 );
               memory[ 3027 ].setValue( 1, 24 );

               memory[ 3028 ].setValue( 4, 45 );
               memory[ 3028 ].setValue( 1, 3019 % 64 );
               memory[ 3028 ].setValue( 0, 3019 / 64 );

               memory[ 3029 ].setValue( 4, 5 );
               memory[ 3029 ].setValue( 3, 2 );

               memory[ 0000 ].setValue( 4, 2 );

               memory[ 1995 ].setValue( 4, 23 );
               memory[ 1995 ].setValue( 3, 22 );
               memory[ 1995 ].setValue( 2, 19 );
               memory[ 1995 ].setValue( 1, 9 );
               memory[ 1995 ].setValue( 0, 6 );

               memory[ 1996 ].setValue( 4, 5 );
               memory[ 1996 ].setValue( 3, 25 );
               memory[ 1996 ].setValue( 2, 9 );
               memory[ 1996 ].setValue( 1, 6 );
               memory[ 1996 ].setValue( 0, 0 );

               memory[ 1997 ].setValue( 4, 4 );
               memory[ 1997 ].setValue( 3, 15 );
               memory[ 1997 ].setValue( 2, 24 );
               memory[ 1997 ].setValue( 1, 8 );

               memory[ 1998 ].setValue( 4, 17 );
               memory[ 1998 ].setValue( 2, 4 );
               memory[ 1998 ].setValue( 1, 5 );
               memory[ 1998 ].setValue( 0, 19 );

               memory[ 1999 ].setValue( 4, 22 );
               memory[ 1999 ].setValue( 3, 5 );
               memory[ 1999 ].setValue( 2, 14 );
               memory[ 1999 ].setValue( 1, 9 );
               memory[ 1999 ].setValue( 0, 19 );

               memory[ 2024 ].setValue( 4, 2035 % 64 );
               memory[ 2024 ].setValue( 3, 2035 / 64 );

               memory[ 2049 ].setValue( 4, 2010 % 64 );
               memory[ 2049 ].setValue( 3, 2010 / 64 );

               memory[ 2050 ].setValue( 4, 499 % 64 );
               memory[ 2050 ].setValue( 3, 499 / 64 );
               memory[ 2050 ].setSign( '-' );

               memory[ 2051 ].setValue( 4, 3 );

               for( int i=3000; i<3030; i++)
               {  memory[ i ].setPacked( 4, false );
                  memory[ i ].setPacked( 3, false );
                  memory[ i ].setPacked( 2, false );
               }
               for (int i=1995; i<2000; i++)
               {  memory[ i ].setPacked( 4, false );
                  memory[ i ].setPacked( 3, false );
                  memory[ i ].setPacked( 2, false );
                  memory[ i ].setPacked( 1, false );
                  memory[ i ].setPacked( 0, false );
               }

            } catch(Exception x) {System.out.println("Exception encountered when loading program!");
                                    System.out.println( x ); }

         memoryScroller.setValue( memoryScroller.getValue() ); // Repaints the memory values.
         globalPC = 3000;                                      // Where we should start execution.

         MIXDialog message = new MIXDialog("Information", "Program 'primes' loaded successfully");

         }
      }
   }

   private class programMButtonActionL implements ActionListener
   {  long randomLong;

      public void actionPerformed( ActionEvent e )
      {  clearMemoryContents();

         try
         {  // Generate 1000 random numbers between 0 and 1000000000
            for (int i=1000; i<2000; i++ )
            {  randomLong = (long)((Math.random())*1000000000);
               memory[ i ].setValue( 4, (int)(randomLong % 64) );
               randomLong = randomLong / 64;
               memory[ i ].setValue( 3, (int)(randomLong % 64) );
               randomLong = randomLong / 64;
               memory[ i ].setValue( 2, (int)(randomLong % 64) );
               randomLong = randomLong / 64;
               memory[ i ].setValue( 1, (int)(randomLong % 64) );
               memory[ i ].setValue( 0, (int)(randomLong / 64) );
            }

            // Let rI1 know there are 1000 elements
            iRegisters[ 0 ].setValue( 1, 1000 % 64 );
            iRegisters[ 0 ].setValue( 0, 1000 / 64 );

            // Main program
            memory[ 3000 ].setValue( 4, 32 );
            memory[ 3000 ].setValue( 3, 2 );
            memory[ 3000 ].setValue( 1, 3009 % 64 );
            memory[ 3000 ].setValue( 0, 3009 / 64 );

            memory[ 3001 ].setValue( 4, 51 );
            memory[ 3001 ].setValue( 3, 2 );
            memory[ 3001 ].setValue( 2, 1 );

            memory[ 3002 ].setValue( 4, 39 );
            memory[ 3002 ].setValue( 1, 3005 % 64 );
            memory[ 3002 ].setValue( 0, 3005 / 64 );

            memory[ 3003 ].setValue( 4, 56 );
            memory[ 3003 ].setValue( 3, 5 );
            memory[ 3003 ].setValue( 2, 3 );
            memory[ 3003 ].setValue( 1, 1000 % 64 );
            memory[ 3003 ].setValue( 0, 1000 / 64 );

            memory[ 3004 ].setValue( 4, 39 );
            memory[ 3004 ].setValue( 3, 7 );
            memory[ 3004 ].setValue( 1, 3007 % 64 );
            memory[ 3004 ].setValue( 0, 3007 / 64 );

            memory[ 3005 ].setValue( 4, 50 );
            memory[ 3005 ].setValue( 3, 2 );
            memory[ 3005 ].setValue( 2, 3 );

            memory[ 3006 ].setValue( 4, 8 );
            memory[ 3006 ].setValue( 3, 5 );
            memory[ 3006 ].setValue( 2, 3 );
            memory[ 3006 ].setValue( 1, 1000 % 64 );
            memory[ 3006 ].setValue( 0, 1000 / 64 );

            memory[ 3007 ].setValue( 4, 51 );
            memory[ 3007 ].setValue( 3, 1 );
            memory[ 3007 ].setValue( 1, 1 );

            memory[ 3008 ].setValue( 4, 43 );
            memory[ 3008 ].setValue( 3, 2 );
            memory[ 3008 ].setValue( 1, 3003 % 64 );
            memory[ 3008 ].setValue( 0, 3003 / 64 );

            // Code altered - Knuth's infinite loop is changed here to a HLT.
            memory[ 3009 ].setValue( 4, 5 );
            memory[ 3009 ].setValue( 3, 2 );

            for( int i=3000; i<3010; i++)
            {  memory[ i ].setPacked( 4, false );
               memory[ i ].setPacked( 3, false );
               memory[ i ].setPacked( 2, false );
            }


            // Start execution at location 3000
            globalPC = 3000;
         } catch (Exception x) { System.out.println("Exception encountered when loading program!");
                                 System.out.println( x ); }

         memoryScroller.setValue( memoryScroller.getValue() );  // Repaints the memory values.
         MIXDialog message = new MIXDialog("Information", "Program 'Maximum' loaded successfully");

      }
   }

   /** Clears the memory of the MIXMachine - zeroes all values and
    *  resets all indicators.
    *  @return No return value.
   */
   private void clearMemoryContents()
   {  try
      {  for (int i=0; i<4000; i++)
         {  for (int j=0; j<5; j++)
            {   memory[ i ].setValue( j, 0 );
                memory[ i ].setPacked( j, true );
            }
            memory[ i ].setSign( '+' );
         }

         for (int i=0; i<5; i++)
         {  aRegister.setValue( i, 0 );
            xRegister.setValue( i, 0 );
            aRegister.setPacked( i, true );
            xRegister.setPacked( i, true );
         }

         for (int i=0; i<6; i++)
         {  for (int j=0; j<2; j++)
            {  iRegisters[ i ].setValue( j, 0 );
               iRegisters[ i ].setPacked( j, true );
            }
            iRegisters[ i ].setSign( '+' );
         }

         jRegister.setValue( 0, 0 );
         jRegister.setValue( 1, 0 );
         jRegister.setPacked( 0, true );
         jRegister.setPacked( 1, true );
         jRegister.setSign( '+' );

         oIndicator.setState( false );
         cIndicator.setState( ComparisonIndicator.OFF );
      } catch(Exception e) { System.out.println("Problem occurred when clearing memory contents.");
                              System.out.println( e ); }

      for (int i=0; i<10; i++)
         memoryVisible[ i ].repaint();
      repaint();

      globalPC = 0;
   }

   /** Takes the code entered into the Input Console and assembles
    *  the resulting program in Memory.  This takes the form of a
    *  two-pass assembler in order to cleanly deal with symbols,
    *  local declarations, and literal constants.
    *  This method was created using the book "Assemblers, Compilers
    *  and Program Translation" by Peter Calingaert, 1979.
    *  INCOMPLETE AND NOT FUNCTIONAL AT PRESENT.
    *  @return String denoting error encountered during assembly
   */
   String assembleCode()
   {  System.out.println( "Assembling..." );
/*      String rawProg = program.getProgram(),
             pass1text,
             line,
             word,
             error = "";
      int locCounter = 0;                                         // Location counter
      int lineCounter = 0;                                        // Line counter (for debugging)
      int wordCounter = 0;                                        // Word counter (for assembly
                                                                  // error detection) - NEEDED?
      StringTokenizer st1 = new StringTokenizer( rawProg, "\n" ), // Take each line in turn
                      st2;

      while ( st1.hasMoreTokens() )
      {  lineCounter++;                                           // New line...increment line
         line = st1.nextToken();                                  // counter...get the line and...
         st2 = new StringTokenizer( line );
         wordCounter = 0;                                         // ...reset word counter.
         if (line.indexOf(" ") > 0)                               // Something in the LOC field...
         {  word = st2.nextToken();                               // ...so we obtain it.
            if ( word.equals("*") )                               // If it's a comment line,
               break;                                             // ...move onto the next one.
            if ( word.equals

         st2 = new StringTokenizer( st1.nextToken(), " " );       // Take each word in turn
         while ( st2.hasMoreTokens() )
         {  wordCounter++;                                        // New word...increment word
                                                                  // counter
            word = st2.nextToken();
            if (( wordCounter == 1 ) && ( word.equals("*") ))     // If the line starts with a '*'
               break;                                             // just skip it.
            System.out.println( word + " line:" + lineCounter + " word:" + wordCounter );
         }
      } */
      return "";
   }

   /** Method to execute the program in memory.
    *  @param startLoc int representing start location
    *  @param stepped boolean value denoting whether this execution is stepped,
                      and therefore consist of one executuon only.
    *  @return int value denoting the new position of the program counter.
   */
   private int execute( int startLoc, boolean stepped )
   {  System.out.println( "Executing code..." );
      int           pc = startLoc,                          // Set the program counter to the
                                                            // start location.
                  code = 0,                                 // Command identifier code
                 field = 0,                                 // Field identifier = 8L + R
                fStart = 0,                                 // L
                  fEnd = 0,                                 // R
             fOurStart = 0,                                 // 'Our' start - avoids array problems
                 index = 0,                                 // Index code
                  addr = 0,                                 // Address code
                 power = 0,                                 // Used to calculate contents of packed
                                                            // bytes
mycounter = 0,
               counter = 0,                                 // Used in LDA and LDX etc.
           scrollValue = 0;                                 // Used to update the display
      boolean     halt = false;                             // Halt flag        
      long value;                                           // Contents of a packed (or unpacked)
                                                            // byte
      String rtError = "";                                  // Holds the runtime error
      
      do
      {  try
         {  code  = memory[ pc ].getValue( 4 );
            field = memory[ pc ].getValue( 3 );
            fStart = field / 8;
            fEnd   = field % 8;
            if (fStart-1 == -1)
               fOurStart = 0;
            else
               fOurStart = fStart-1;
            index = memory[ pc ].getValue( 2 );
            addr  = ( memory[ pc ].getValue( 0 ) * 64 ) + memory[ pc ].getValue( 1 );
            addr  = ( memory[ pc ].getSign() == '+' ) ? addr : -addr;
            index--;                                        // Adjust to access array.
            if ( index > 5 )
            {  rtError = "Bad index value at pc="+pc;
               break;
            }
            if ( index >= 0 )
            {  addr += (( iRegisters[ index ].getValue(0) * 64 ) + iRegisters[ index ].getValue(1) )
                       * ( (iRegisters[ index ].getSign() == '-') ? -1 : 1 );
            }

            power = 0;
            value = 0;
mycounter++;
            switch (code)
            {  case  0: pc++;                                                             // NOP
                        clock.incrementClock( 1 );
                        break;

               case  1: for ( int i = (fEnd-1); i>=fOurStart; i-- )                       // ADD
                        {  // WORK OUT VALUE
                           value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                           power++;
                        }

                        // SET THE SIGN
                        if (fStart == 0)
                           value = (memory[ addr ].getSign() == '-') ? -value : value;

                        // ADD THE VALUE TO rA
                        mADD( aRegister, value );

                        // 'PACK' rA THE SAME WAY AS THE WORD THE VALUE CAME FROM
                        for ( int i = 0; i<5; i++ )
                           aRegister.setPacked( i, memory[ addr ].isPacked( i ) );

                        // INCREMENT CLOCK & PROGRAM COUNTER
                        pc++;
                        clock.incrementClock( 2 );
                        break;

                case  2: for ( int i = (fEnd-1); i>=fOurStart; i-- )                      // SUB
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // SUBTRACT THE VALUE FROM rA
                         value = -value;
                         mADD( aRegister, value );

                         // 'PACK' rA THE SAME WAY AS THE WORD THE VALUE CAME FROM
                         for ( int i = 0; i<5; i++ )
                            aRegister.setPacked( i, memory[ addr ].isPacked( i ) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case  3: for ( int i = (fEnd-1); i>=fOurStart; i-- )                      // MUL
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // MULTIPLY BY rA AND PLACE IN rA AND rX
                         mMUL( value );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 10 );
                         break;

                case  4: for ( int i = (fEnd-1); i>=fOurStart; i-- )                      // DIV
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // PERFORM THE DIVISION AND PLACE IN rA and rX
                         mDIV( value );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 12 );
                         break;

                case  5: switch (field)
                         {  case 0 : mNUM();                                              // NUM

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 10 );
                                     break;

                            case 1 : mCHAR();                                             // CHAR

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 10 );
                                     break;

                            case 2 : halt = true;                                         // HLT

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     System.out.println( "HLT encountered.  Stopping.  PC now at "+pc );
                                     break;
                         }
                         break;

                case  6: // CHECK SIGN                                        // shift operators
                         if (addr<0)
                         {  rtError = "SLA operand cannot be negative at pc="+pc;
                            break;
                         }

                         switch (field)
                         {  case 0 : mSLA( (long)addr );                                  // SLA
                                     break;

                            case 1 : mSRA( (long)addr );                                  // SRA
                                     break;

                            case 2 : mSLAX( (long)addr, false );                          // SLAX
                                     break;

                            case 3 : mSRAX( (long)addr, false );                          // SRAX
                                     break;

                            case 4 : mSLAX( (long)addr, true );                           // SLC
                                     break;

                            case 5 : mSRAX( (long)addr, true );                           // SRC
                                     break;
                         }

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case  7: if (field == 0)                                                  // MOVE
                            break;

                         int startMove = ((iRegisters[0].getValue(0))*64)+
                                          (iRegisters[0].getValue(1));
                         scrollValue = memoryScroller.getValue();

                         for (int i = 0; i<field; i++)
                         {  mMOVE( memory[ addr+i ], memory[ startMove+i ] );
                            if (((startMove+i)>=(scrollValue)) && ((startMove+i)<=(scrollValue+9)))
                               mMOVE( memory[ startMove+i ],
                               memoryVisible[ (startMove+i) - (scrollValue) ] );
                         }

                         // INCREMENT I1 BY FIELD
                         startMove += field;
                         iRegisters[0].setValue( 0, startMove / 64 );
                         iRegisters[0].setValue( 1, startMove % 64 );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 1+field );
                         break;

                case  8: if (fStart == 0)                                                 // LDA
                            aRegister.setSign( memory[ addr ].getSign() );
                         else
                            aRegister.setSign( '+' );

                         counter = 4;

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  aRegister.setValue( counter, memory[ addr ].getValue( i ) );
                            aRegister.setPacked( counter, memory[ addr ].isPacked( i ) );
                            counter--;
                         }
                         for (int i=counter; i>=0; i--)
                         {  aRegister.setValue( i, 0 );
                            aRegister.setPacked( i, false );
                         }

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case  9:                                                                  // LD1
                         if (((fEnd-fOurStart)+1)>2)
                            for (int i=(fEnd-3); i>=(fOurStart); i--)
                               if (memory[ addr ].getValue( i ) != 0 )
                               {  rtError = "I Register 1 can only be loaded with two bytes at pc="
                                               +pc;
                               }

                         if (!(rtError.equals("")))
                            break;

                         mLDi( 0, memory[ addr ], fEnd-2, fEnd-1, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 10:                                                                  // LD2
                         if (((fEnd-fOurStart)+1)>2)
                            for (int i=(fEnd-3); i>=(fOurStart); i--)
                               if (memory[ addr ].getValue( i ) != 0 )
                               {  rtError = "I Register 2 can only be loaded with two bytes at pc="
                                               +pc;
                               }

                         if (!(rtError.equals("")))
                            break;

                         mLDi( 1, memory[ addr ], fEnd-2, fEnd-1, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 11:                                                                  // LD3
                         if (((fEnd-fOurStart)+1)>2)
                            for (int i=(fEnd-3); i>=(fOurStart); i--)
                               if (memory[ addr ].getValue( i ) != 0 )
                               {  rtError = "I Register 3 can only be loaded with two bytes at pc="
                                               +pc;
                               }

                         if (!(rtError.equals("")))
                            break;

                         mLDi( 2, memory[ addr ], fEnd-2, fEnd-1, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 12:                                                                  // LD4
                         if (((fEnd-fOurStart)+1)>2)
                            for (int i=(fEnd-3); i>=(fOurStart); i--)
                               if (memory[ addr ].getValue( i ) != 0 )
                               {  rtError = "I Register 4 can only be loaded with two bytes at pc="
                                               +pc;
                               }

                         if (!(rtError.equals("")))
                            break;

                         mLDi( 3, memory[ addr ], fEnd-2, fEnd-1, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 13:                                                                  // LD5
                         if (((fEnd-fOurStart)+1)>2)
                            for (int i=(fEnd-3); i>=(fOurStart); i--)
                               if (memory[ addr ].getValue( i ) != 0 )
                               {  rtError = "I Register 5 can only be loaded with two bytes at pc="
                                               +pc;
                               }

                         if (!(rtError.equals("")))
                            break;

                         mLDi( 4, memory[ addr ], fEnd-2, fEnd-1, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 14:                                                                  // LD6
                         if (((fEnd-fOurStart)+1)>2)
                            for (int i=(fEnd-3); i>=(fOurStart); i--)
                               if (memory[ addr ].getValue( i ) != 0 )
                               {  rtError = "I Register 6 can only be loaded with two bytes at pc="
                                               +pc;
                               }

                         if (!(rtError.equals("")))
                            break;

                         mLDi( 5, memory[ addr ], fEnd-2, fEnd-1, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 15: if (fStart == 0)                                                 // LDX
                            xRegister.setSign( memory[ addr ].getSign() );
                         else
                            xRegister.setSign( '+' );

                         counter = 4;

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  xRegister.setValue( counter, memory[ addr ].getValue( i ) );
                            xRegister.setPacked( counter, memory[ addr ].isPacked( i ) );
                            counter--;
                         }
                         for (int i=counter; i>=0; i--)
                         {  xRegister.setValue( i, 0 );
                            xRegister.setPacked( i, false );
                         }

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 16: if (fStart == 0)                                                 // LDAN
                         {  if (memory[ addr ].getSign() == '+')
                               aRegister.setSign( '-' );
                            else
                               aRegister.setSign( '+' );
                         }
                         else
                            aRegister.setSign( '-' );

                         counter = 4;

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  aRegister.setValue( counter, memory[ addr ].getValue( i ) );
                            aRegister.setPacked( counter, memory[ addr ].isPacked( i ) );
                            counter--;
                         }
                         for (int i=counter; i>=0; i--)
                         {  aRegister.setValue( i, 0 );
                            aRegister.setPacked( i, false );
                         }

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 17:                                                                  // LD1N
                         if ( ((fEnd>(fStart+1)) && (fStart > 0)) || ((fStart == 0) && (fEnd>2)) )
                         {  rtError = "I Register 1 can only be loaded with two bytes at pc="+pc;
                            break;
                         }

                         mLDiN( 0, memory[ addr ], fOurStart, fEnd, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 18:                                                                  // LD2N
                         if ( ((fEnd>(fStart+1)) && (fStart > 0)) || ((fStart == 0) && (fEnd>2)) )
                         {  rtError = "I Register 2 can only be loaded with two bytes at pc="+pc;
                            break;
                         }

                         mLDiN( 1, memory[ addr ], fOurStart, fEnd, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 19:                                                                  // LD3N
                         if ( ((fEnd>(fStart+1)) && (fStart > 0)) || ((fStart == 0) && (fEnd>2)) )
                         {  rtError = "I Register 3 can only be loaded with two bytes at pc="+pc;
                            break;
                         }

                         mLDiN( 2, memory[ addr ], fOurStart, fEnd, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 20:                                                                  // LD4N
                         if ( ((fEnd>(fStart+1)) && (fStart > 0)) || ((fStart == 0) && (fEnd>2)) )
                         {  rtError = "I Register 4 can only be loaded with two bytes at pc="+pc;
                            break;
                         }

                         mLDiN( 3, memory[ addr ], fOurStart, fEnd, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 21:                                                                  // LD5N
                         if ( ((fEnd>(fStart+1)) && (fStart > 0)) || ((fStart == 0) && (fEnd>2)) )
                         {  rtError = "I Register 5 can only be loaded with two bytes at pc="+pc;
                            break;
                         }

                         mLDiN( 4, memory[ addr ], fOurStart, fEnd, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 22:                                                                  // LD6N
                         if ( ((fEnd>(fStart+1)) && (fStart > 0)) || ((fStart == 0) && (fEnd>2)) )
                         {  rtError = "I Register 6 can only be loaded with two bytes at pc="+pc;
                            break;
                         }

                         mLDiN( 5, memory[ addr ], fOurStart, fEnd, (fStart==0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 23: if (fStart == 0)                                                 // LDXN
                         {  if (memory[ addr ].getSign() == '+')
                               xRegister.setSign( '-' );
                            else
                               xRegister.setSign( '+' );
                         }
                         else
                            xRegister.setSign( '-' );

                         counter = 4;

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  xRegister.setValue( counter, memory[ addr ].getValue( i ) );
                            xRegister.setPacked( counter, memory[ addr ].isPacked( i ) );
                            counter--;
                         }
                         for (int i=counter; i>=0; i--)
                         {  xRegister.setValue( i, 0 );
                            xRegister.setPacked( i, false );
                         }

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 24: if (fStart == 0)                                                 // STA
                            memory[ addr ].setSign( aRegister.getSign() );

                         counter = 4;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, aRegister.getValue(counter) );
                            memory[ addr ].setPacked( i, aRegister.isPacked(counter) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 25: if (fStart == 0)                                                 // ST1
                            memory[ addr ].setSign( iRegisters[0].getSign() );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           iRegisters[0].getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           iRegisters[0].isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 26: if (fStart == 0)                                                 // ST2
                            memory[ addr ].setSign( iRegisters[1].getSign() );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           iRegisters[1].getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           iRegisters[1].isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 27: if (fStart == 0)                                                 // ST3
                            memory[ addr ].setSign( iRegisters[2].getSign() );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           iRegisters[2].getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           iRegisters[2].isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 28: if (fStart == 0)                                                 // ST4
                            memory[ addr ].setSign( iRegisters[3].getSign() );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           iRegisters[3].getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           iRegisters[3].isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 29: if (fStart == 0)                                                 // ST5
                            memory[ addr ].setSign( iRegisters[4].getSign() );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           iRegisters[4].getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           iRegisters[4].isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 30: if (fStart == 0)                                                 // ST6
                            memory[ addr ].setSign( iRegisters[5].getSign() );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           iRegisters[5].getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           iRegisters[5].isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+10)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 31: if (fStart == 0)                                                 // STX
                            memory[ addr ].setSign( xRegister.getSign() );

                         counter = 4;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, xRegister.getValue(counter) );
                            memory[ addr ].setPacked( i, xRegister.isPacked(counter) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+9)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 32: if (fStart == 0)                                                 // STJ
                            memory[ addr ].setSign( '+' );

                         counter = 1;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, (counter < 0) ? 0 :
                                                           jRegister.getValue( counter ) );
                            memory[ addr ].setPacked( i, (counter < 0) ? false :
                                                           jRegister.isPacked( counter ) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+10)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 33: if (fStart == 0)                                                 // STZ
                            memory[ addr ].setSign( aRegister.getSign() );

                         counter = 4;
                         scrollValue = memoryScroller.getValue();

                         for (int i=(fEnd-1); i>=fOurStart; i--)
                         {  memory[ addr ].setValue( i, 0 );
                            memory[ addr ].setPacked( i, aRegister.isPacked(counter) );
                            counter--;
                         }
                         if (((addr)>=(scrollValue)) && ((addr)<=(scrollValue+10)))
                            mMOVE( memory[ addr ], memoryVisible[ addr - scrollValue ] );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 34: if ((field < 15) || ((field > 17) && (field < 21)))              // JBUS
                            pc++; // If we have the peripheral, it's not going to be busy!
                         else
                         {  jRegister.setValue( 0, (pc+1) / 64 );
                            jRegister.setValue( 1, (pc+1) % 64 );
                            pc = addr;
                         }

                         // INCREMENT CLOCK
                         clock.incrementClock( 1 );
                         break;

                case 35: pc++;                                                            // IOC
                         break;
                         // THIS NEEDS IMPLEMENTING WHEN WE DO THE PERIPHERALS.

                case 36: pc++;                                                            // IN
                         break;
                         // THIS NEEDS IMPLEMENTING WHEN WE DO THE PERIPHERALS.

                case 37: if (field == 18)                                                 // OUT
                         {  if (!(addr==0))                        // ** Doesn't clear page ***
                            {  for (int i=0; i<24; i++)
                                  lpr.print( memory[ addr + i ] );
                                  clock.incrementClock( 1 );
                            }
                         }
                         pc++;
                         break;
                         // THIS NEEDS IMPLEMENTING WHEN WE DO THE PERIPHERALS.

                case 38: if ((field < 15) || ((field > 17) && (field < 21)))                // JRED
                         {  jRegister.setValue( 0, (pc+1) / 64 );
                            jRegister.setValue( 1, (pc+1) % 64 );
                            pc = addr;
                         }
                         else
                            pc++; // If we have the peripheral, it's ready!

                         // INCREMENT CLOCK
                         clock.incrementClock( 1 );
                         break;

                case 39: switch(field)
                         {  case 0 : jRegister.setValue( 0, (pc+1) / 64 );                // JMP
                                     jRegister.setValue( 1, (pc+1) % 64 );
                                     pc = addr;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : pc = addr;                                           // JSJ

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : if (oIndicator.getState())                           // JOV
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                        oIndicator.setState( false );
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : if (oIndicator.getState())                           // JNOV
                                     {  pc++;
                                        oIndicator.setState( false );
                                     }
                                     else
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 0, (pc+1) % 64 );
                                        pc = addr;
                                     }

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 :                                                      // JL
                                     if (cIndicator.getState() == ComparisonIndicator.LESSTHAN)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 :                                                      // JE
                                     if (cIndicator.getState() == ComparisonIndicator.EQUALTO)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 6 :                                                      // JG
                                     if (cIndicator.getState() == ComparisonIndicator.GREATERTHAN)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 7 :                                                      // JGE
                                     if ((cIndicator.getState() == ComparisonIndicator.GREATERTHAN)
                                        || (cIndicator.getState() == ComparisonIndicator.EQUALTO))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 8 :                                                      // JNE
                                     if ((cIndicator.getState() == ComparisonIndicator.LESSTHAN)
                                        || (cIndicator.getState() ==
                                                               ComparisonIndicator.GREATERTHAN))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 9 :                                                      // JLE
                                     if ((cIndicator.getState() == ComparisonIndicator.LESSTHAN)
                                        || (cIndicator.getState() == ComparisonIndicator.EQUALTO))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) / 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 40: switch(field)
                         {  case 0 : for (int i=4; i>=0; i--)                             // JAN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    aRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((aRegister.getSign() == '-') && (value > 0 ))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=4; i>=0; i--)                             // JAZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    aRegister.getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=4; i>=0; i--)                             // JAP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    aRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((aRegister.getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=4; i>=0; i--)                             // JANN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    aRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((aRegister.getSign() != '-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=4; i>=0; i--)                             // JANZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                           aRegister.getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=4; i>=0; i--)                             // JANP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    aRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((aRegister.getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 41: switch(field)
                         {  case 0 : for (int i=1; i>=0; i--)                             // J1N
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[0].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[0].getSign() == '-') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=1; i>=0; i--)                             // J1Z
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                     iRegisters[0].getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=1; i>=0; i--)                             // J1P
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[0].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[0].getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=1; i>=0; i--)                             // J1NN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[0].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[0].getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=1; i>=0; i--)                             // J1NZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[0].getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=1; i>=0; i--)                             // J1NP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[0].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[0].getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 42: switch(field)
                         {  case 0 : for (int i=1; i>=0; i--)                             // J2N
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[1].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[1].getSign() == '-') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=1; i>=0; i--)                             // J2Z
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[1].getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=1; i>=0; i--)                             // J2P
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[1].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[1].getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=1; i>=0; i--)                             // J2NN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[1].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[1].getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=1; i>=0; i--)                             // J2NZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[1].getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=1; i>=0; i--)                             // J2NP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[1].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[1].getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 43: switch(field)
                         {  case 0 : for (int i=1; i>=0; i--)                             // J3N
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[2].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[2].getSign() == '-') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=1; i>=0; i--)                             // J3Z
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[2].getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=1; i>=0; i--)                             // J3P
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[2].getValue( i ));
                                     }

                                     if ((iRegisters[2].getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=1; i>=0; i--)                             // J3NN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[2].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[2].getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=1; i>=0; i--)                             // J3NZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[2].getValue( i ));
                                        power++;
                                     }

                                     if (value != 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=1; i>=0; i--)                             // J3NP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[2].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[2].getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 44: switch(field)
                         {  case 0 : for (int i=1; i>=0; i--)                             // J4N
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[3].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[3].getSign() == '-') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=1; i>=0; i--)                             // J4Z
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[3].getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=1; i>=0; i--)                             // J4P
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[3].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[3].getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=1; i>=0; i--)                             // J4NN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[3].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[3].getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=1; i>=0; i--)                             // J4NZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[3].getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=1; i>=0; i--)                             // J4NP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[3].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[3].getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 45: switch(field)
                         {  case 0 : for (int i=1; i>=0; i--)                             // J5N
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[4].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[4].getSign() == '-') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=1; i>=0; i--)                             // J5Z
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[4].getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=1; i>=0; i--)                             // J5P
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[4].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[4].getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=1; i>=0; i--)                             // J5NN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[4].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[4].getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=1; i>=0; i--)                             // J5NZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[4].getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=1; i>=0; i--)                             // J5NP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[4].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[4].getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 46: switch(field)
                         {  case 0 : for (int i=1; i>=0; i--)                             // J6N
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[5].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[5].getSign() == '-') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=1; i>=0; i--)                             // J6Z
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[5].getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=1; i>=0; i--)                             // J6P
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[5].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[5].getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=1; i>=0; i--)                             // J6NN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[5].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[5].getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=1; i>=0; i--)                             // J6NZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[5].getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=1; i>=0; i--)                             // J6NP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    iRegisters[5].getValue( i ));
                                        power++;
                                     }

                                     if ((iRegisters[5].getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 47: switch(field)
                         {  case 0 : for (int i=4; i>=0; i--)                             // JXN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    xRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((xRegister.getSign() == '-') && (value > 0 ))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc = addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : for (int i=4; i>=0; i--)                             // JXZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    xRegister.getValue( i ));
                                        power++;
                                     }

                                     if (value == 0)
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : for (int i=4; i>=0; i--)                             // JXP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    xRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((xRegister.getSign() == '+') && (value > 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : for (int i=4; i>=0; i--)                             // JXNN
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    xRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((xRegister.getSign() !='-' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 4 : for (int i=4; i>=0; i--)                             // JXNZ
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    xRegister.getValue( i ));
                                        power++;
                                     }

                                     if (value != 0 )
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;

                            case 5 : for (int i=4; i>=0; i--)                             // JXNP
                                     {  // WORK OUT VALUE
                                        value += (long)(Math.pow( 64, power ) *
                                                    xRegister.getValue( i ));
                                        power++;
                                     }

                                     if ((xRegister.getSign() !='+' ) || (value == 0))
                                     {  jRegister.setValue( 0, (pc+1) / 64 );
                                        jRegister.setValue( 1, (pc+1) % 64 );
                                        pc=addr;
                                     }
                                     else
                                        pc++;

                                     // INCREMENT CLOCK
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 48: switch (field)
                         {  case 0 : // ADD THE VALUE TO rA                               // INCA
                                     mADD( aRegister, addr );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DECA

                                     // ADD THE VALUE TO rA
                                     mADD( aRegister, addr );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : aRegister.setSign( (addr>=0) ? '+' : '-' );          // ENTA
                                     if (addr<0)
                                        addr = addr * (-1);
                                     aRegister.setValue( 4, addr % 64 );
                                     aRegister.setPacked( 4, memory[pc].isPacked( 1 ) );
                                     aRegister.setValue( 3, addr / 64 );
                                     aRegister.setPacked( 3, false );
                                     for (int i=2; i>=0; i--)
                                     {  aRegister.setValue( i, 0 );
                                        aRegister.setPacked( i, false );
                                     }

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : aRegister.setSign( (addr>=0) ? '-' : '+' );          // ENNA
                                     if (addr<0)
                                        addr = addr * (-1);
                                     aRegister.setValue( 4, addr % 64 );
                                     aRegister.setPacked( 4, memory[pc].isPacked( 1 ) );
                                     aRegister.setValue( 3, addr / 64 );
                                     aRegister.setPacked( 3, false );
                                     for (int i=2; i>=0; i--)
                                     {  aRegister.setValue( i, 0 );
                                        aRegister.setPacked( i, false );
                                     }

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 49: switch (field)
                         {  case 0 : // ADD THE VALUE TO I1                               // INC1
                                     addr = addr + (((iRegisters[0].getValue(0) * 64) +
                                               iRegisters[0].getValue(1))
                                               * ((iRegisters[0].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[0].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[0].setSign( '+' );

                                     iRegisters[0].setValue( 1, addr % 64 );
                                     iRegisters[0].setValue( 0, (addr/64) % 64 );
                                     iRegisters[0].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[0].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DEC1

                                     // ADD THE VALUE TO I1
                                     addr = addr + (((iRegisters[0].getValue(0) * 64) +
                                               iRegisters[0].getValue(1))
                                               * ((iRegisters[0].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[0].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[0].setSign( '+' );

                                     iRegisters[0].setValue( 1, addr % 64 );
                                     iRegisters[0].setValue( 0, (addr/64) % 64 );
                                     iRegisters[0].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[0].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : iRegisters[0].setSign( (addr>=0) ? '+' : '-' );      // ENT1
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[0].setValue( 1, addr % 64 );
                                     iRegisters[0].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[0].setValue( 0, (addr/64) % 64 );
                                     iRegisters[0].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : iRegisters[0].setSign( (addr>=0) ? '-' : '+' );      // ENN1
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[0].setValue( 1, addr % 64 );
                                     iRegisters[0].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[0].setValue( 0, (addr/64) % 64 );
                                     iRegisters[0].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 50: switch (field)
                         {  case 0 : // ADD THE VALUE TO I2                               // INC2
                                     addr = addr + (((iRegisters[1].getValue(0) * 64) +
                                               iRegisters[1].getValue(1))
                                               * ((iRegisters[1].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[1].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[1].setSign( '+' );

                                     iRegisters[1].setValue( 1, addr % 64 );
                                     iRegisters[1].setValue( 0, (addr/64) % 64 );
                                     iRegisters[1].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[1].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DEC2

                                     // ADD THE VALUE TO I2
                                     addr = addr + (((iRegisters[1].getValue(0) * 64) +
                                               iRegisters[1].getValue(1))
                                               * ((iRegisters[1].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[1].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[1].setSign( '+' );

                                     iRegisters[1].setValue( 1, addr % 64 );
                                     iRegisters[1].setValue( 0, (addr/64) % 64 );
                                     iRegisters[1].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[1].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : iRegisters[1].setSign( (addr>=0) ? '+' : '-' );      // ENT2
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[1].setValue( 1, addr % 64 );
                                     iRegisters[1].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[1].setValue( 0, (addr/64) % 64 );
                                     iRegisters[1].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : iRegisters[1].setSign( (addr>=0) ? '-' : '+' );      // ENN2
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[1].setValue( 1, addr % 64 );
                                     iRegisters[1].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[1].setValue( 0, (addr/64) % 64 );
                                     iRegisters[1].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 51: switch (field)
                         {  case 0 : // ADD THE VALUE TO I3                               // INC3
                                     addr = addr + (((iRegisters[2].getValue(0) * 64) +
                                               iRegisters[2].getValue(1))
                                               * ((iRegisters[2].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[2].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[2].setSign( '+' );

                                     iRegisters[2].setValue( 1, addr % 64 );
                                     iRegisters[2].setValue( 0, (addr/64) % 64 );
                                     iRegisters[2].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[2].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DEC3

                                     // ADD THE VALUE TO I3
                                     addr = addr + (((iRegisters[2].getValue(0) * 64) +
                                               iRegisters[2].getValue(1))
                                               * ((iRegisters[2].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[2].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[2].setSign( '+' );

                                     iRegisters[2].setValue( 1, addr % 64 );
                                     iRegisters[2].setValue( 0, (addr/64) % 64 );
                                     iRegisters[2].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[2].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : iRegisters[2].setSign( (addr>=0) ? '+' : '-' );      // ENT3
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[2].setValue( 1, addr % 64 );
                                     iRegisters[2].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[2].setValue( 0, (addr/64) % 64 );
                                     iRegisters[2].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : iRegisters[2].setSign( (addr>=0) ? '-' : '+' );      // ENN3
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[2].setValue( 1, addr % 64 );
                                     iRegisters[2].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[2].setValue( 0, (addr/64) % 64 );
                                     iRegisters[2].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 52: switch (field)
                         {  case 0 : // ADD THE VALUE TO I4                               // INC4
                                     addr = addr + (((iRegisters[3].getValue(0) * 64) +
                                               iRegisters[3].getValue(1))
                                               * ((iRegisters[3].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[3].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[3].setSign( '+' );

                                     iRegisters[3].setValue( 1, addr % 64 );
                                     iRegisters[3].setValue( 0, (addr/64) % 64 );
                                     iRegisters[3].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[3].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DEC4

                                     // ADD THE VALUE TO I4
                                     addr = addr + (((iRegisters[3].getValue(0) * 64) +
                                               iRegisters[3].getValue(1))
                                               * ((iRegisters[3].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[3].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[3].setSign( '+' );

                                     iRegisters[3].setValue( 1, addr % 64 );
                                     iRegisters[3].setValue( 0, (addr/64) % 64 );
                                     iRegisters[3].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[3].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : iRegisters[3].setSign( (addr>=0) ? '+' : '-' );      // ENT4
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[3].setValue( 1, addr % 64 );
                                     iRegisters[3].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[3].setValue( 0, (addr/64) % 64 );
                                     iRegisters[3].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : iRegisters[3].setSign( (addr>=0) ? '-' : '+' );      // ENN4
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[3].setValue( 1, addr % 64 );
                                     iRegisters[3].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[3].setValue( 0, (addr/64) % 64 );
                                     iRegisters[3].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 53: switch (field)
                         {  case 0 : // ADD THE VALUE TO I5                               // INC5
                                     addr = addr + (((iRegisters[4].getValue(0) * 64) +
                                               iRegisters[4].getValue(1))
                                               * ((iRegisters[4].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[4].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[4].setSign( '+' );

                                     iRegisters[4].setValue( 1, addr % 64 );
                                     iRegisters[4].setValue( 0, (addr/64) % 64 );
                                     iRegisters[4].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[4].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DEC5

                                     // ADD THE VALUE TO I5
                                     addr = addr + (((iRegisters[4].getValue(0) * 64) +
                                               iRegisters[4].getValue(1))
                                               * ((iRegisters[4].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[4].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[4].setSign( '+' );

                                     iRegisters[4].setValue( 1, addr % 64 );
                                     iRegisters[4].setValue( 0, (addr/64) % 64 );
                                     iRegisters[4].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[4].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : iRegisters[4].setSign( (addr>=0) ? '+' : '-' );      // ENT5
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[4].setValue( 1, addr % 64 );
                                     iRegisters[4].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[4].setValue( 0, (addr/64) % 64 );
                                     iRegisters[4].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : iRegisters[4].setSign( (addr>=0) ? '-' : '+' );      // ENN5
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[4].setValue( 1, addr % 64 );
                                     iRegisters[4].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[4].setValue( 0, (addr/64) % 64 );
                                     iRegisters[4].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 54: switch (field)
                         {  case 0 : // ADD THE VALUE TO I6                               // INC6
                                     addr = addr + (((iRegisters[5].getValue(0) * 64) +
                                               iRegisters[5].getValue(1))
                                               * ((iRegisters[5].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[5].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[5].setSign( '+' );

                                     iRegisters[5].setValue( 1, addr % 64 );
                                     iRegisters[5].setValue( 0, (addr/64) % 64 );
                                     iRegisters[5].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[5].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DEC6

                                     // ADD THE VALUE TO I6
                                     addr = addr + (((iRegisters[4].getValue(0) * 64) +
                                               iRegisters[4].getValue(1))
                                               * ((iRegisters[4].getSign() == '+') ? 1 : (-1)));

                                     if ( (addr<(-4096)) || (addr>4096) )
                                        oIndicator.setState( true );

                                     if (addr<0)
                                     {  iRegisters[5].setSign( '-' );
                                        addr = addr * (-1);
                                     }
                                     else
                                        iRegisters[5].setSign( '+' );

                                     iRegisters[5].setValue( 1, addr % 64 );
                                     iRegisters[5].setValue( 0, (addr/64) % 64 );
                                     iRegisters[5].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[5].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : iRegisters[5].setSign( (addr>=0) ? '+' : '-' );      // ENT6
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[5].setValue( 1, addr % 64 );
                                     iRegisters[5].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[5].setValue( 0, (addr/64) % 64 );
                                     iRegisters[5].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : iRegisters[5].setSign( (addr>=0) ? '-' : '+' );      // ENN6
                                     if (addr<0)
                                        addr = addr * (-1);

                                     if (addr>4096)
                                        oIndicator.setState( true );

                                     iRegisters[5].setValue( 1, addr % 64 );
                                     iRegisters[5].setPacked( 1, memory[pc].isPacked( 1 ) );
                                     iRegisters[5].setValue( 0, (addr/64) % 64 );
                                     iRegisters[5].setPacked( 0, memory[pc].isPacked( 0 ) );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 55: switch (field)
                         {  case 0 : // ADD THE VALUE TO rX                               // INCX
                                     mADD( xRegister, addr );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 1 : addr = -addr;                                        // DECX

                                     // ADD THE VALUE TO rX
                                     mADD( xRegister, addr );

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 2 : xRegister.setSign( (addr>=0) ? '+' : '-' );          // ENTX
                                     if (addr<0)
                                        addr = addr * (-1);
                                     xRegister.setValue( 4, addr % 64 );
                                     xRegister.setPacked( 4, memory[pc].isPacked( 1 ) );
                                     xRegister.setValue( 3, addr / 64 );
                                     xRegister.setPacked( 3, false );
                                     for (int i=2; i>=0; i--)
                                     {  xRegister.setValue( i, 0 );
                                        xRegister.setPacked( i, false );
                                     }

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;

                            case 3 : xRegister.setSign( (addr>=0) ? '-' : '+' );          // ENNX
                                     if (addr<0)
                                        addr = addr * (-1);
                                     xRegister.setValue( 4, addr % 64 );
                                     xRegister.setPacked( 4, memory[pc].isPacked( 1 ) );
                                     xRegister.setValue( 3, addr / 64 );
                                     xRegister.setPacked( 3, false );
                                     for (int i=2; i>=0; i--)
                                     {  xRegister.setValue( i, 0 );
                                        xRegister.setPacked( i, false );
                                     }

                                     // INCREMENT CLOCK & PROGRAM COUNTER
                                     pc++;
                                     clock.incrementClock( 1 );
                                     break;
                         }
                         break;

                case 56: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMPA
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO rA
                         mCMP( aRegister, value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 57: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMP1
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO I1
                         mCMPi( iRegisters[0], value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 58: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMP2
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO I1
                         mCMPi( iRegisters[1], value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 59: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMP3
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO I1
                         mCMPi( iRegisters[2], value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 60: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMP4
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO I1
                         mCMPi( iRegisters[3], value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 61: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMP5
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO I1
                         mCMPi( iRegisters[4], value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 62: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMP6
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO I1
                         mCMPi( iRegisters[5], value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;

                case 63: for (int i=(fEnd-1); i>=fOurStart; i--)                          // CMPX
                         {  // WORK OUT VALUE
                            value += (long)(Math.pow( 64, power ) * memory[ addr ].getValue( i ));
                            power++;
                         }

                         // SET THE SIGN
                         if (fStart == 0)
                            value = (memory[ addr ].getSign() == '-') ? -value : value;

                         // COMPARE THE VALUE TO rA
                         mCMP( xRegister, value, fOurStart, fEnd-1, (fStart == 0) );

                         // INCREMENT CLOCK & PROGRAM COUNTER
                         pc++;
                         clock.incrementClock( 2 );
                         break;
            }
         } catch( Exception e )
         {  if (e instanceof NotAMIXCharacterException)
               rtError = "Not a MIX character value at pc="+pc;
            else
            {  System.out.println( e );
               break;
            }
         }

      } while ( (halt == false) && (rtError.equals( "" )) && (stepped == false) );

   if (!(rtError.equals("")))
        System.out.println( rtError );
System.out.println( "Mycounter = "+mycounter );
   return pc;
   }

// NOTE...See Knuth's "baffling" description for reasons why this implementation has been chosen...

   /** Adds a value to an existing word/register in memory.
    *  @param addTo the MIXWord that is going to be added to
    *  @param div the BigInteger value to add
    *  @return No return value
   */
   private void mADD( MIXWord addTo, long div )
   {  long mod;
      long valueOfWord = addTo.toLong();

      valueOfWord += div;
      try
      {  if ( valueOfWord < 0 )
         {  addTo.setSign( '-' );
            valueOfWord = -valueOfWord;
         }
         else
            addTo.setSign( '+' );

         for ( int i = 4; i >=0; i-- )
         {  mod = valueOfWord % 64;
            addTo.setValue( i, (int)mod );
            valueOfWord = valueOfWord / 64;
         }

      }  catch (Exception e) { System.out.println(
                               "An exception has occurred when it shouldn't have in mADD()" ); }

      if ( (!(valueOfWord == 0)) && (addTo == aRegister) )  // If we overflow totally and it's the
                                                            // A-Register
         oIndicator.setState( true );                       // Set the overflow indicator 'on'

   }

   private void mMUL( long mult )
   {  long mod;
      long result = aRegister.toLong();

      result = result * mult;
      try
      {  if ( result < 0 )
         {  aRegister.setSign( '-' );
            xRegister.setSign( '-' );
            result = -result;                               // makes calculations easier
         }
         else
         {  aRegister.setSign( '+' );
            xRegister.setSign( '+' );
         }

         for ( int i = 4; i>=0; i-- )
         {  mod = result % 64;
            xRegister.setValue( i, (int)mod );
            result = result / 64;
         }

         if ( !(result == 0) )
         {  for ( int i = 4; i>=0; i-- )
            {  mod = result % 64;
               aRegister.setValue( i, (int)mod );
               result = result / 64;
            }
         }
         else
         {  for ( int i = 4; i>=0; i-- )
               aRegister.setValue( i, 0 );
         }

         // AMBIGUOUS 'PACKING' INFORMATION SUPPLIED BY KNUTH ON p132 SO WE IGNORE PACK BITS
               
      }  catch (Exception e) { System.out.println(
                               "An exception has occurred when it shouldn't have in mMUL()" ); }
   }

   private void mDIV( long denominator )
   {  long numerator = 0,
                 quotient,
                 remainder,
                 aMod,
                 xMod,
                 biggestNum = 1073741823;
      int aPower = 5,
          xPower = 0;

      if ( denominator == 0 )                               // see p.131
      {  oIndicator.setState( true );
         System.out.println( "Division by zero" );
         return;
      }

      try
      {  for ( int i = 4; i>=0; i-- )                       // get numerator
         {  numerator += (long)(Math.pow(64, aPower) * aRegister.getValue( i ) );
            numerator += (long)(Math.pow(64, xPower) * xRegister.getValue( i ) );
            aPower++;
            xPower++;
         }

         numerator = (aRegister.getSign() == '-') ? -numerator : numerator; // set the numerator's
                                                                            // sign

      quotient  = numerator / denominator;
      remainder = numerator % denominator;

      if ((Math.abs(quotient)) > biggestNum )
      {  oIndicator.setState( true );
         System.out.println( "Returning...quotient too large." );
         return;
      }

      xRegister.setSign( aRegister.getSign() );
      aRegister.setSign( (quotient < 0) ? '-' : '+' );

      for ( int i = 4; i>=0; i-- )
      {  aMod = quotient % 64;
         aRegister.setValue( i, (int)aMod );
         quotient = quotient / 64;
         xMod = remainder % 64;
         xRegister.setValue( i, (int)xMod );
         remainder = remainder / 64;
      }

      } catch (Exception e) { System.out.println(
                              "An exception has occurred when it shouldn't have in mDIV()" ); }

   }

   private void mNUM()
   {  long numVal = 0,
           aMult = 100000,
           xMult = 1;
      int  mod;

      try
      {  for (int i=4; i>=0; i--)
         {  numVal += (((aRegister.getValue( i )) % 10)*aMult);
            numVal += (((xRegister.getValue( i )) % 10)*xMult);
            aMult = aMult*10;
            xMult = xMult*10;
         }

         for (int i=4; i>=0; i--)
         {  mod = (int)(numVal % 64);
            numVal = numVal / 64;
            aRegister.setValue( i, mod );
            aRegister.setPacked( i, true );
         }
         aRegister.setPacked( 0, false );

         if (numVal > 0)
            oIndicator.setState( true );

      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mNUM()" );
                              System.out.println( e ); }
   }

   private void mCHAR()
   {  long numVal = aRegister.toLong();
      int mod;

      numVal = Math.abs(numVal);
      try
      {  for (int i=4; i>=0; i--)
         {  mod = (int)((numVal % 10) + 30);
            xRegister.setValue( i, mod );
            xRegister.setPacked( i, false );
            numVal = numVal / 10;
         }
         for (int i=4; i>=0; i--)
         {  mod = (int)((numVal % 10) + 30);
            aRegister.setValue( i, mod );
            aRegister.setPacked( i, false );
            numVal = numVal / 10;
         }
         if (numVal > 0)
            oIndicator.setState( true );
      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mCHAR()" ); }
   }

   private void mSLA( long shift )
   {  try
      {  for (long i=0; i<shift; i++)
         {  aRegister.setValue( 0, aRegister.getValue( 1 ) );
            aRegister.setValue( 1, aRegister.getValue( 2 ) );
            aRegister.setValue( 2, aRegister.getValue( 3 ) );
            aRegister.setValue( 3, aRegister.getValue( 4 ) );
            aRegister.setValue( 4, 0 );
         }
      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mSLA()" ); }
   }

   private void mSRA( long shift )
   {  try
      {  for (long i=0; i<shift; i++)
         {  aRegister.setValue( 4, aRegister.getValue( 3 ) );
            aRegister.setValue( 3, aRegister.getValue( 2 ) );
            aRegister.setValue( 2, aRegister.getValue( 1 ) );
            aRegister.setValue( 1, aRegister.getValue( 0 ) );
            aRegister.setValue( 0, 0 );
         }
      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mSRA()" ); }
   }

   private void mSLAX( long shift, boolean circular )
   {  int value;
      try
      {  
         for (long i=0; i<shift; i++)
         {  value = aRegister.getValue( 0 );
            aRegister.setValue( 0, aRegister.getValue( 1 ) );
            aRegister.setValue( 1, aRegister.getValue( 2 ) );
            aRegister.setValue( 2, aRegister.getValue( 3 ) );
            aRegister.setValue( 3, aRegister.getValue( 4 ) );
            aRegister.setValue( 4, xRegister.getValue( 0 ) );
            xRegister.setValue( 0, xRegister.getValue( 1 ) );
            xRegister.setValue( 1, xRegister.getValue( 2 ) );
            xRegister.setValue( 2, xRegister.getValue( 3 ) );
            xRegister.setValue( 3, xRegister.getValue( 4 ) );
            if (circular)
               xRegister.setValue( 4, value );
            else
               xRegister.setValue( 4, 0 );
         }
      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mSLAX()" ); }
   }

   private void mSRAX( long shift, boolean circular )
   {  int value;
      try
      {  
         for (long i=0; i<shift; i++)
         {  value = xRegister.getValue( 4 );
            xRegister.setValue( 4, xRegister.getValue( 3 ) );
            xRegister.setValue( 3, xRegister.getValue( 2 ) );
            xRegister.setValue( 2, xRegister.getValue( 1 ) );
            xRegister.setValue( 1, xRegister.getValue( 0 ) );
            xRegister.setValue( 0, aRegister.getValue( 4 ) );
            aRegister.setValue( 4, aRegister.getValue( 3 ) );
            aRegister.setValue( 3, aRegister.getValue( 2 ) );
            aRegister.setValue( 2, aRegister.getValue( 1 ) );
            aRegister.setValue( 1, aRegister.getValue( 0 ) );
            if (circular)
               aRegister.setValue( 0, value );
            else
               aRegister.setValue( 0, 0 );
         }
      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mSRAX()" ); }
   }

   private void mMOVE( MIXWord from, MIXWord to )
   {  try
      {  for (int i=4; i>=0; i--)
         {  to.setValue( i, from.getValue( i ) );
            to.setPacked( i, from.isPacked( i ) );
         }
         to.setSign( from.getSign() );
      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mMOVE()" ); }
   }

   private void mLDi( int regID, MIXWord word, int start, int end, boolean sign )
   {  int counter = 1;

      try
      {  if (sign)
            iRegisters[ regID ].setSign( word.getSign() );
         else
            iRegisters[ regID ].setSign( '+' );

         for (int i=end; i>=start; i--)
         {  iRegisters[ regID ].setValue( counter, word.getValue( i ) );
            iRegisters[ regID ].setPacked( counter, word.isPacked( i ) );
            counter--;
         }

         if (counter==0)
         {  iRegisters[ regID ].setValue( 0, 0 );
            iRegisters[ regID ].setPacked( 0, false );
         }

      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mLDi()" ); }
   }

   private void mLDiN( int regID, MIXWord word, int start, int end, boolean sign )
   {  int counter = 1;

      try
      {  if (sign)
         {  if (word.getSign() == '+')
               iRegisters[ regID ].setSign( '-' );
            else
               iRegisters[ regID ].setSign( '+' );
         }
         else
            iRegisters[ regID ].setSign( '-' );

         for (int i=(end-1); i>=start; i--)
         {  iRegisters[ regID ].setValue( counter, word.getValue( i ) );
            iRegisters[ regID ].setPacked( counter, word.isPacked( i ) );
            counter--;
         }

         if (counter==0)
         {  iRegisters[ regID ].setValue( 0, 0 );
            iRegisters[ regID ].setPacked( 0, false );
         }

      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mLDi()" ); }
   }

   private void mCMP( MIXWord register, long value, int start, int end, boolean sign)
   {  long rValue = 0;
      int  power = 0;

      try
      {  for (int i=end; i>=start; i--)
         {  rValue += ((long)(Math.pow( 64, power ))) * register.getValue( i );
            power++;
         }

         if (sign == true)
             rValue = (register.getSign() == '+') ? rValue : -rValue;

         if (rValue < value)
            cIndicator.setState( ComparisonIndicator.LESSTHAN );
         else
            if (rValue > value)
               cIndicator.setState( ComparisonIndicator.GREATERTHAN );
            else
               cIndicator.setState( ComparisonIndicator.EQUALTO );

      } catch (Exception e) { System.out.println(
                              "An exception occurred when it shouldn't have in mCMP()" ); }
   }

   private void mCMPi( MIXWord register, long value, int start, int end, boolean sign )
   {  long rValue = 0;
      int  power = 0;

      try
      {  for (int i=end; i>=start; i--)
         {  if (i>2)
               rValue += ((long)(Math.pow( 64, power ))) * register.getValue( i-3 );
            power++;
         }

         if (sign == true)
            rValue = (register.getSign() == '+') ? rValue : -rValue;

         if (rValue < value)
            cIndicator.setState( ComparisonIndicator.LESSTHAN );
         else
            if (rValue > value)
               cIndicator.setState( ComparisonIndicator.GREATERTHAN );
            else
               cIndicator.setState( ComparisonIndicator.EQUALTO );

      } catch (Exception e) { System.out.println(
        "An exception occurred when it shouldn't have in mCMPi()" ); }
   }

}
