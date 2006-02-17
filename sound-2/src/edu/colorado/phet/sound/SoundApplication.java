/**
 * Class: SoundApplication
 * Package: edu.colorado.phet.sound
 * Author: Another Guy
 * Date: Aug 3, 2004
 */
package edu.colorado.phet.sound;

import edu.colorado.phet.common.application.ApplicationModel;
import edu.colorado.phet.common.application.Module;
import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.common.model.clock.SwingTimerClock;
import edu.colorado.phet.common.view.util.FrameSetup;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.sound.model.SoundClock;

import javax.swing.*;
import java.util.Locale;
import java.util.ArrayList;


public class SoundApplication extends PhetApplication {


    public SoundApplication( String[] args ) {

        super( args, SimStrings.get( "SoundApplication.title" ),
               SimStrings.get( "SoundApplication.description" ),
               SoundConfig.VERSION,
               new SoundClock( SoundConfig.s_timeStep, SoundConfig.s_waitTime ),
               true,
               new FrameSetup.CenteredWithSize( 900, 750 ) );

        // Set the look and feel to Metal, so the stopwatch dialog and the multi-line
        // tabs look right on the Mac
        try {
            UIManager.setLookAndFeel( "javax.swing.plaf.metal.MetalLookAndFeel");
        }
        catch( ClassNotFoundException e ) {
            e.printStackTrace();
        }
        catch( InstantiationException e ) {
            e.printStackTrace();
        }
        catch( IllegalAccessException e ) {
            e.printStackTrace();
        }
        catch( UnsupportedLookAndFeelException e ) {
            e.printStackTrace();
        }

        // Must not be resizable, because the performance tanks when you make the
        // window bigger. SHOULD BE FIXED!!!
        getPhetFrame().setResizable( false );

        // Set up the modules
        Module singleSourceModule = new SingleSourceListenModule( this );
        Module measureModule = new SingleSourceMeasureModule( this );
        Module twoSourceIntereferenceModule = new TwoSpeakerInterferenceModule( this );
        Module wallInterferenceModule = new WallInterferenceModule( this );
        Module evacuatedBoxModule = new SingleSourceWithBoxModule( this );
        this.setModules( new Module[]{singleSourceModule, measureModule,
                                      twoSourceIntereferenceModule, wallInterferenceModule,
                                      evacuatedBoxModule} );
        this.setInitialModule( singleSourceModule );

    }

    public static void main( String[] args ) {
        SimStrings.init( args, SoundConfig.localizedStringsPath );

        PhetApplication app = new SoundApplication( args );
        app.getPhetFrame().setResizable( false );

        app.startApplication();
    }
}
