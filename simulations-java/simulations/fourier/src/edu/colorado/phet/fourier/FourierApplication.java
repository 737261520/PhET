/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.fourier;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.colorado.phet.common.phetcommon.application.PhetApplication;
import edu.colorado.phet.common.phetcommon.application.PhetApplicationConfig;
import edu.colorado.phet.common.phetcommon.util.DialogUtils;
import edu.colorado.phet.common.phetcommon.util.persistence.XMLPersistenceManager;
import edu.colorado.phet.common.phetcommon.view.PhetFrame;
import edu.colorado.phet.common.phetcommon.view.PhetLookAndFeel;
import edu.colorado.phet.fourier.control.OptionsMenu;
import edu.colorado.phet.fourier.module.D2CModule;
import edu.colorado.phet.fourier.module.DiscreteModule;
import edu.colorado.phet.fourier.module.GameModule;
import edu.colorado.phet.fourier.persistence.FourierConfig;
import edu.colorado.phet.fourier.view.HarmonicColors;


/**
 * FourierApplication is the main application for the PhET "Fourier Analysis" simulation.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class FourierApplication extends PhetApplication {

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private DiscreteModule _discreteModule;
    private GameModule _gameModule;
    private D2CModule _d2cModule;
    
    // PersistanceManager handles loading/saving application configurations.
    private XMLPersistenceManager _persistenceManager;

    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Sole constructor.
     */
    public FourierApplication( PhetApplicationConfig config )
    {
        super( config );
        initModules();
        initMenubar();
    }

    //----------------------------------------------------------------------------
    // Modules
    //----------------------------------------------------------------------------

    /*
     * Initializes the modules.
     *
     * @param clock
     */
    private void initModules() {
        
        _discreteModule = new DiscreteModule();
        addModule( _discreteModule );
        
        _gameModule = new GameModule();
        addModule( _gameModule );
        
        _d2cModule = new D2CModule();
        addModule( _d2cModule );
    }

    //----------------------------------------------------------------------------
    // Menubar
    //----------------------------------------------------------------------------

    /*
     * Initializes the menubar.
     */
    private void initMenubar() {

        PhetFrame frame = getPhetFrame();
        
        if ( _persistenceManager == null ) {
            _persistenceManager = new XMLPersistenceManager( frame );
        }

        // File menu
        {
            JMenuItem saveItem = new JMenuItem( FourierResources.getString( "FileMenu.save" ) );
            saveItem.setMnemonic( FourierResources.getChar( "FileMenu.save.mnemonic", 'S' ) );
            saveItem.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    save();
                }
            } );

            JMenuItem loadItem = new JMenuItem( FourierResources.getString( "FileMenu.load" ) );
            loadItem.setMnemonic( FourierResources.getChar( "FileMenu.load.mnemonic", 'L' ) );
            loadItem.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    load();
                }
            } );

            frame.addFileMenuItem( saveItem );
            frame.addFileMenuItem( loadItem );
            frame.addFileMenuSeparator();
        }

        // Options menu
        OptionsMenu optionsMenu = new OptionsMenu( this );
        getPhetFrame().addMenu( optionsMenu );
    }

    //----------------------------------------------------------------------------
    // Persistence
    //----------------------------------------------------------------------------
    
    private void save() {

        FourierConfig appConfig = new FourierConfig();

        // Global config
        {
            FourierConfig.GlobalConfig globalConfig = appConfig.getGlobalConfig();
            appConfig.setGlobalConfig( globalConfig );

            // Version & build info
            globalConfig.setVersionNumber( getVersion() );

            // Harmonic colors
            int[] r = new int[HarmonicColors.getInstance().getNumberOfColors()];
            int[] g = new int[r.length];
            int[] b = new int[r.length];
            for ( int i = 0; i < r.length; i++ ) {
                Color color = HarmonicColors.getInstance().getColor( i );
                r[i] = color.getRed();
                g[i] = color.getGreen();
                b[i] = color.getBlue();
            }
            globalConfig.setHarmonicColorsRed( r );
            globalConfig.setHarmonicColorsGreen( g );
            globalConfig.setHarmonicColorsBlue( b );
        }
        
        // Modules
        appConfig.setDiscreteConfig( _discreteModule.save() );
        appConfig.setGameConfig( _gameModule.save() );
        appConfig.setD2CConfig( _d2cModule.save() );

        _persistenceManager.save( appConfig );
    }
    
    private void load() {
        
        Object object = _persistenceManager.load();
        
        if ( object != null ) {
            
            if ( object instanceof FourierConfig ) {
                
                FourierConfig appConfig = (FourierConfig) object;
                
                // Globals
                {
                    // Harmonic colors
                    int[] r = appConfig.getGlobalConfig().getHarmonicColorsRed();
                    int[] g = appConfig.getGlobalConfig().getHarmonicColorsGreen();
                    int[] b = appConfig.getGlobalConfig().getHarmonicColorsBlue();
                    for ( int i = 0; i < r.length; i++ ) {
                        HarmonicColors.getInstance().setColor( i, new Color( r[i], g[i], b[i] ) );
                    }
                }
                
                // Modules
                _discreteModule.load( appConfig.getDiscreteConfig() );
                _gameModule.load( appConfig.getGameConfig() );
                _d2cModule.load( appConfig.getD2CConfig() );
            }
            else {
                String message = FourierResources.getString( "message.notAConfigFile" );
                String title = FourierResources.getString( "title.error" );
                DialogUtils.showErrorDialog( getPhetFrame(), message, title );
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // main
    //----------------------------------------------------------------------------

    /**
     * Main entry point for the PhET Fourier application.
     *
     * @param args command line arguments
     */
    public static void main( final String[] args ) throws IOException {

        /*
         * Wrap the body of main in invokeLater, so that all initialization occurs
         * in the event dispatch thread. Sun now recommends doing all Swing init in
         * the event dispatch thread. And the Piccolo-based tabs in TabbedModulePanePiccolo
         * seem to cause startup deadlock problems if they aren't initialized in the
         * event dispatch thread. Since we don't have an easy way to separate Swing and
         * non-Swing init, we're stuck doing everything in invokeLater.
         */
        SwingUtilities.invokeLater( new Runnable() {

            public void run() {
                
                // Initialize look-and-feel
                PhetLookAndFeel laf = new PhetLookAndFeel();
                laf.initLookAndFeel();
                
                PhetApplicationConfig config = new PhetApplicationConfig( args, FourierConstants.FRAME_SETUP, FourierResources.getResourceLoader() );

                // Create the application.
                FourierApplication app = new FourierApplication( config );

                // Start the application.
                app.startApplication();
            }
        } );
    }
}
