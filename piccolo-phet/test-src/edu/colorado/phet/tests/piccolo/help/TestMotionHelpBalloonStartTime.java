package edu.colorado.phet.tests.piccolo.help;

import edu.colorado.phet.common.application.Module;
import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.common.model.clock.SwingClock;
import edu.colorado.phet.piccolo.help.MotionHelpBalloon;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PPaintContext;

public class TestMotionHelpBalloonStartTime extends PhetApplication {

    public TestMotionHelpBalloonStartTime( String[] args ) throws InterruptedException {
        super( args, "tmhbst", "", "" );
        addModule( new TestMotionHelpBalloonStartTimeModule() );
    }

    private static class TestMotionHelpBalloonStartTimeModule extends Module {
        public TestMotionHelpBalloonStartTimeModule() throws InterruptedException {
            super( "test", new SwingClock( 30, 1 ) );

            PCanvas pCanvas = new PCanvas();
            pCanvas.setAnimatingRenderQuality( PPaintContext.HIGH_QUALITY_RENDERING );
            pCanvas.setDefaultRenderQuality( PPaintContext.HIGH_QUALITY_RENDERING );
            pCanvas.setInteractingRenderQuality( PPaintContext.HIGH_QUALITY_RENDERING );
            MotionHelpBalloon helpBalloon = new MotionHelpBalloon( pCanvas, "<html>Help!<br>Wiggle Me!</html>" );

//            helpBalloon.animateTo( 500, 500 );
            pCanvas.getLayer().addChild( helpBalloon );
            helpBalloon.setOffset( 0, 0 );

            setSimulationPanel( pCanvas );
            Thread.sleep( 5000 );//simulate long startup time
        }
    }

    public static void main( String[] args ) throws InterruptedException {
        new TestMotionHelpBalloonStartTime( args ).startApplication();
    }

}
