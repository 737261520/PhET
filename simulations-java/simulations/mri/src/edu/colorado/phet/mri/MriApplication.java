/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.mri;

import edu.colorado.phet.common.phetcommon.application.Module;
import edu.colorado.phet.common.phetcommon.application.NonPiccoloPhetApplication;
import edu.colorado.phet.common.phetcommon.util.PhetUtilities;
import edu.colorado.phet.common.phetcommon.view.PhetLookAndFeel;
import edu.colorado.phet.common.phetcommon.view.util.FrameSetup;
import edu.colorado.phet.common.phetcommon.view.util.SimStrings;
import edu.colorado.phet.common.piccolophet.PhetApplication;
import edu.colorado.phet.mri.controller.HeadModule;
import edu.colorado.phet.mri.controller.NmrModule;
import edu.colorado.phet.mri.controller.OptionMenu;

import javax.swing.*;
import java.awt.*;

/**
 * MriApplication
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class MriApplication extends PhetApplication {

    //--------------------------------------------------------------------------------------------------
    // Class fields and methods
    //--------------------------------------------------------------------------------------------------
    private static FrameSetup frameSetup = new FrameSetup.CenteredWithSize( 1024, 768 );

    //--------------------------------------------------------------------------------------------------
    // Instance fields and methods
    //--------------------------------------------------------------------------------------------------

    private Module[] singleModule = new Module[]{
            new NmrModule(),
    };

    private Module[] fullAppModules = new Module[]{
            new NmrModule(),
            new HeadModule()
//            ,new ScanModule(),
//            new ScanModuleB(),
    };

    private Module[] modules = fullAppModules;

    public MriApplication( String[] args ) {
        super( args, SimStrings.getInstance().getString( "mri.name" ),
               SimStrings.getInstance().getString( "mri.description" ),
               MriConfig.VERSION,
               frameSetup );
//        PhetTabbedPane.setLogoVisible( false );
        setModules( modules );
    }

    protected void parseArgs( String[] args ) {
        super.parseArgs( args );

        for( int i = 0; args != null && i < args.length; i++ ) {
            String arg = args[i];
            if( arg.startsWith( "-d" ) ) {
                PhetUtilities.getPhetFrame().addMenu( new OptionMenu() );
            }
            if( arg.equals( "-singlemodule" ) ) {
                modules = singleModule;
            }
        }
    }


    public static void main( final String[] args ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                SimStrings.getInstance().init( args, MriConfig.STRINGS_BUNDLE_NAME );

                // Set the look and feel. Make the fonts a bit stronger
                PhetLookAndFeel.setLookAndFeel();
                PhetLookAndFeel lookAndFeel = new PhetLookAndFeel();
                Font orgFont = UIManager.getFont( "Label.font" );
                Font newFont = new Font( orgFont.getName(), Font.BOLD, orgFont.getSize() );
                lookAndFeel.setFont( newFont );
                lookAndFeel.setTitledBorderFont( newFont );
                Color background = UIManager.getColor( "Panel.background" );
                lookAndFeel.setBackgroundColor( background );
                lookAndFeel.apply();

                NonPiccoloPhetApplication app = new MriApplication( args );
                app.startApplication();
            }
        } );
    }
}
