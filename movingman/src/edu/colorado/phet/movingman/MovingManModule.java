/*PhET, 2004.*/
package edu.colorado.phet.movingman;

import edu.colorado.phet.common.application.ApplicationModel;
import edu.colorado.phet.common.application.Module;
import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.common.math.ImmutableVector2D;
import edu.colorado.phet.common.model.BaseModel;
import edu.colorado.phet.common.model.Command;
import edu.colorado.phet.common.model.ModelElement;
import edu.colorado.phet.common.model.clock.AbstractClock;
import edu.colorado.phet.common.model.clock.SwingTimerClock;
import edu.colorado.phet.common.view.ApparatusPanel;
import edu.colorado.phet.common.view.BasicGraphicsSetup;
import edu.colorado.phet.common.view.BasicPhetPanel;
import edu.colorado.phet.common.view.PhetFrame;
import edu.colorado.phet.common.view.graphics.BufferedGraphicForComponent;
import edu.colorado.phet.common.view.util.FrameSetup;
import edu.colorado.phet.movingman.common.RangeToRange;
import edu.colorado.phet.movingman.common.WiggleMe;
import edu.colorado.phet.movingman.common.plaf.PhetLookAndFeel;
import edu.colorado.phet.movingman.misc.JEPFrame;
import edu.colorado.phet.movingman.plots.MMPlot;
import edu.colorado.phet.movingman.plots.PlotAndText;
import smooth.util.SmoothUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * User: Sam Reid
 * Date: Jun 30, 2003
 * Time: 12:19:49 AM
 * Copyright (c) Jun 30, 2003 by Sam Reid
 */
public class MovingManModule extends Module {
    private int numResetPoints = 1;//number of points to use in the reset routine.
    private boolean paused = true;
    private static boolean addJEP = true;

    private ManGraphic manGraphic;
    private RangeToRange manPositionTransform;

    private MMTimer recordTimer;
    private MMTimer playbackTimer;
    private MovingManLayout layout;

    private Mode mode;//the current mode.

    private RecordMode recordMode;
    private PlaybackMode playbackMode;
    private MotionMode motionMode;
    private MovingManControlPanel movingManControlPanel;
    private TimeGraphic timerGraphic;
    private BufferedGraphicForComponent backgroundGraphic;
    private WalkWayGraphic walkwayGraphic;

    private Color purple = new Color( 200, 175, 250 );
    private PhetFrame frame;
    private ModelElement mainModelElement;
    private Observer crashObserver;
    private Color backgroundColor;
    private MovingManModel model;
    private PlotSet plotSet;
    private WiggleMe wiggleMe;
    private ArrayList stateListeners = new ArrayList();
    boolean initMediaPanel = false;

    public MovingManModule( AbstractClock clock ) throws IOException {
        super( "The Moving Man" );
        model = new MovingManModel( this, clock );
        ApparatusPanel mypanel = new ApparatusPanel() {
            public void repaint( long tm, int x, int y, int width, int height ) {
                super.repaint( tm, x, y, width, height );
            }

            public void repaint( Rectangle r ) {
                super.repaint( r );
            }

            public void repaint() {
                super.repaint();
            }

            public void repaint( long tm ) {
                super.repaint( tm );
            }

            public void repaint( int x, int y, int width, int height ) {
                super.repaint( x, y, width, height );
            }

        };
        super.setApparatusPanel( mypanel );
        mypanel.addGraphicsSetup( new BasicGraphicsSetup() );
        mypanel.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
        super.setModel( new BaseModel() );

        backgroundColor = new Color( 250, 190, 240 );
        backgroundGraphic = new BufferedGraphicForComponent( 0, 0, 800, 400, backgroundColor, getApparatusPanel() );

        manPositionTransform = new RangeToRange( -getMaxManPosition(), getMaxManPosition(), 50, 600 );
        manGraphic = new ManGraphic( this, model.getMan(), 0, manPositionTransform );
        getModel().addModelElement( new ModelElement() {
            public void stepInTime( double dt ) {
                manGraphic.update();
            }
        } );
        getApparatusPanel().addGraphic( manGraphic, 1 );
        recordTimer = new MMTimer( "Record", MovingManModel.TIMER_SCALE );
        playbackTimer = new MMTimer( "Playback", MovingManModel.TIMER_SCALE );
        timerGraphic = new TimeGraphic( this, recordTimer, playbackTimer, 80, 40 );
        getApparatusPanel().addGraphic( timerGraphic, 1 );

        walkwayGraphic = new WalkWayGraphic( this, 11 );
        backgroundGraphic.addGraphic( walkwayGraphic, 0 );
        layout = new MovingManLayout( 800, 400, 3 );
        layout.relayout();
        plotSet = new PlotSet( this );
        movingManControlPanel = new MovingManControlPanel( this );
        super.setControlPanel( movingManControlPanel );//TODO control panel.
        mainModelElement = new ModelElement() {
            public void stepInTime( double dt ) {
                if( !paused ) {
                    mode.stepInTime( dt );
                }
            }
        };
        getModel().addModelElement( mainModelElement );
        crashObserver = new Observer() {
            public void update( Observable o, Object arg ) {
                if( isMotionMode() ) {
                    double manx = ( model.getMan().getX() );
                    double manv = getVelocity();
                    if( manx >= getMaxManPosition() && manv > 0 ) {
                        motionMode.collidedWithWall();
                    }
                    else if( manx <= -getMaxManPosition() && manv < 0 ) {
                        motionMode.collidedWithWall();
                    }
                }
            }
        };
        model.getMan().addObserver( crashObserver );
//        cursorGraphic = new CursorGraphic( this, playbackTimer, Color.black, layout.getPlotY( 0 ), layout.getTotalPlotHeight() );
//        getApparatusPanel().addGraphic( cursorGraphic, 6 );

        getApparatusPanel().addComponentListener( new ComponentAdapter() {
            public void componentShown( ComponentEvent e ) {
                initMediaPanel();
                relayoutApparatusPanel();
            }

            public void componentResized( ComponentEvent e ) {
                getModel().execute( new Command() {
                    public void doIt() {
                        initMediaPanel();
                        relayoutApparatusPanel();
                    }
                } );
            }
        } );
        recordMode = new RecordMode( this );
        playbackMode = new PlaybackMode( this );
        motionMode = new MotionMode( this );
//        setMode( motionMode );
        setMode( recordMode );

        getApparatusPanel().addGraphic( backgroundGraphic, 0 );
        clock.addClockTickListener( getModel() );

        Point2D start = manGraphic.getRectangle().getLocation();
        start = new Point2D.Double( start.getX() + 50, start.getY() + 50 );
        wiggleMe = new WiggleMe( getApparatusPanel(), start,
                                 new ImmutableVector2D.Double( 0, 1 ), 15, .02, "Drag the Man" );
        setWiggleMeVisible( true );
        getApparatusPanel().addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent e ) {
                getApparatusPanel().requestFocus();
            }
        } );
    }

    public void repaintBackground( Rectangle rect ) {
        backgroundGraphic.paintBufferedImage( rect );
        getApparatusPanel().repaint( rect );
    }

    public void setWiggleMeVisible( boolean b ) {
        if( !b ) {
            wiggleMe.setVisible( false );
            getApparatusPanel().removeGraphic( wiggleMe );
            getModel().removeModelElement( wiggleMe );
        }
        else {
            wiggleMe.setVisible( true );
            getApparatusPanel().addGraphic( wiggleMe, 100 );
            getModel().addModelElement( wiggleMe );
        }
    }

    public int getNumResetPoints() {
        return numResetPoints;
    }

    public boolean isPaused() {
        return paused;
    }

    public BufferedGraphicForComponent getBackground() {
        return backgroundGraphic;
    }

    public MovingManModel getMovingManModel() {
        return model;
    }

    public MovingManLayout getLayout() {
        return layout;
    }

    public void addStateListener( StateListener stateListener ) {
        stateListeners.add( stateListener );
    }

    public void setPaused( boolean paused ) {
        if( paused != this.paused ) {
            this.paused = paused;
            for( int i = 0; i < stateListeners.size(); i++ ) {
                StateListener stateListener = (StateListener)stateListeners.get( i );
                stateListener.stateChanged( this );
            }
            if( !paused ) {
                movingManControlPanel.motionStarted();
                movingManControlPanel.goPressed();
            }
        }
    }

    public Color getPurple() {
        return purple;
    }

    public void setNumSmoothingPoints( int n ) {
        model.setNumSmoothingPoints( n );
        plotSet.setNumSmoothingPoints( n );
    }

    public void setRightDirPositive( boolean rightPos ) {
        RangeToRange newTransform;
        double appPanelWidth = getApparatusPanel().getWidth();
        int inset = 50;
        if( rightPos ) {//as usual
            newTransform = new RangeToRange( -getMaxManPosition(), getMaxManPosition(), inset, appPanelWidth - inset );
            walkwayGraphic.setTreeX( -10 );
            walkwayGraphic.setHouseX( 10 );
        }
        else {
            newTransform = new RangeToRange( getMaxManPosition(), -getMaxManPosition(), inset, appPanelWidth - inset );
            walkwayGraphic.setTreeX( 10 );
            walkwayGraphic.setHouseX( -10 );
        }
        manGraphic.setTransform( newTransform );
        setManTransform( newTransform );
        setMode( recordMode );
        reset();
        setPaused( true );
    }

    public void repaintBackground() {
        backgroundGraphic.paintBufferedImage();
        getApparatusPanel().repaint();
    }

    public void setPositionPlotMagnitude( double positionMagnitude ) {
        plotSet.setPositionPlotMagnitude( positionMagnitude );
    }

    public void setVelocityPlotMagnitude( double maxVelocity ) {
        plotSet.setVelocityPlotMagnitude( maxVelocity );
    }

    public void setAccelerationPlotMagnitude( double maxAccel ) {
        plotSet.setAccelerationPlotMagnitude( maxAccel );
    }

    public RangeToRange getManPositionTransform() {
        return manPositionTransform;
    }

    private void initMediaPanel() {
        if( initMediaPanel ) {
            return;
        }
        final JFrame parent = (JFrame)SwingUtilities.getWindowAncestor( getApparatusPanel() );
        JPanel jp = (JPanel)parent.getContentPane();
        BasicPhetPanel bpp = (BasicPhetPanel)jp;
        bpp.setAppControlPanel( movingManControlPanel.getMediaPanel() );
        initMediaPanel = true;
    }

    public ManGraphic getManGraphic() {
        return manGraphic;
    }

    public MotionMode getMotionMode() {
        return motionMode;
    }

    public PlotAndText getAccelerationPlot() {
        return plotSet.getAccelerationPlot();
    }

    public PlotAndText getPositionPlot() {
        return plotSet.getPositionPlot();
    }

    public PlotAndText getVelocityPlot() {
        return plotSet.getVelocityPlot();
    }

    public SmoothDataSeries getPosition() {
        return model.getPosition();
    }

    public void setMode( Mode mode ) {
        boolean same = mode == this.mode;
        if( !same ) {
            this.mode = mode;
            this.mode.initialize();
            System.out.println( "Changed mode to: " + mode.getName() );
            repaintBackground();
        }
    }

    private void relayoutApparatusPanel() {
        layout.relayout( this );
        Component c = getApparatusPanel();
        if( c.getHeight() > 0 && c.getWidth() > 0 ) {
            backgroundGraphic.setSize( c.getWidth(), c.getHeight() );
            backgroundGraphic.paintBufferedImage();
            getApparatusPanel().repaint();
        }
    }

    public void activate( PhetApplication app ) {
    }

    public void deactivate( PhetApplication app ) {
    }

    private void setFrame( PhetFrame frame ) {
        this.frame = frame;
    }

    public static void fixComponent( Container jc ) {
        jc.invalidate();
        jc.validate();
        jc.repaint();
    }

    public Man getMan() {
        return model.getMan();
    }

    public MMTimer getRecordingTimer() {
        return recordTimer;
    }

    public void reset( double modelCoordinate ) {
        setReplayTimeIndex( 0 );
        model.reset();
        recordTimer.reset();
        for( int i = 0; i < numResetPoints; i++ ) {
            double dt = 1;
            model.getMan().setX( modelCoordinate );
            recordTimer.stepInTime( dt );
            model.step( dt );
        }
//        cursorGraphic.setVisible( false );
        setCursorsVisible( false );
        playbackTimer.setTime( 0 );
        getPositionString().update( null, null );
        getVelocityString().update( null, null );
        getAccelString().update( null, null );
        backgroundGraphic.paintBufferedImage();
        getApparatusPanel().repaint();
    }

    public void setCursorsVisible( boolean visible ) {
        plotSet.setCursorsVisible( visible );
    }

    private void setReplayTimeIndex( int timeIndex ) {
        model.setReplayTimeIndex( timeIndex );
    }

    public void setReplayTime( double requestedTime ) {
        /**Find the position for the time.*/
        int timeIndex = (int)( requestedTime / MovingManModel.TIMER_SCALE );
        setReplayTimeIndex( timeIndex );
        cursorMovedToTime( requestedTime );
    }

    public void rewind() {
        playbackTimer.setTime( 0 );
        getMan().reset();
    }

    public void setRecordMode() {
        setMode( recordMode );
    }

    public void startPlaybackMode( double playbackSpeed ) {
        playbackMode.setPlaybackSpeed( playbackSpeed );
        setMode( playbackMode );
        setPaused( false );
    }

    public boolean isRecording() {
        return mode == recordMode && !isPaused();
    }

    public MMPlot getPositionGraphic() {
        return plotSet.getPositionPlot().getPlot();
    }

    public MMPlot getAccelerationGraphic() {
        return plotSet.getAccelerationPlot().getPlot();
    }

    public MMPlot getVelocityGraphic() {
        return plotSet.getVelocityPlot().getPlot();
    }

    public ValueGraphic getPositionString() {
        return plotSet.getPositionPlot().getText();
    }

    public ValueGraphic getVelocityString() {
        return plotSet.getVelocityPlot().getText();
    }

    public ValueGraphic getAccelString() {
        return plotSet.getAccelerationPlot().getText();
    }

    public void cursorMovedToTime( double requestedTime ) {
        if( requestedTime < 0 || requestedTime > recordTimer.getTime() ) {
            return;
        }
        else {
            playbackTimer.setTime( requestedTime );
            int timeIndex = (int)( requestedTime / MovingManModel.TIMER_SCALE );
            if( timeIndex < model.getPosition().numSmoothedPoints() && timeIndex >= 0 ) {
                double x = model.getPosition().smoothedPointAt( timeIndex );
                getMan().setX( x );
            }
            plotSet.cursorMovedToTime( requestedTime );
        }
    }

    public int getVisiblePlotCount() {
        return plotSet.getVisiblePlotCount();
    }

    public void setPositionGraphVisible( boolean selected ) {
        setPlotVisible( plotSet.getPositionPlot(), selected );
    }

    public void setPlotVisible( PlotAndText plot, boolean selected ) {
        plot.setVisible( selected );
        int plotCount = getVisiblePlotCount();
        layout.setNumPlots( plotCount );
        relayoutApparatusPanel();
    }

    public void setVelocityGraphVisible( boolean selected ) {
        setPlotVisible( plotSet.getVelocityPlot(), selected );
    }

    public void setAccelerationGraphVisible( boolean selected ) {
        setPlotVisible( plotSet.getAccelerationPlot(), selected );
    }

    public boolean isMotionMode() {
        return mode == motionMode;
    }

    public void setManTransform( RangeToRange transform ) {
        this.manPositionTransform = transform;
    }

    public void reset() {
        setPaused( true );
        reset( 0 );
        //TODO should reset sliders here.
    }

    public double getVelocity() {
        return model.getVelocity();
    }

    public SmoothDataSeries getVelocityData() {
        return model.getVelocitySeries();
    }

    public SmoothDataSeries getAcceleration() {
        return model.getAcceleration();
    }

    public double getTimeScale() {
        return MovingManModel.TIMER_SCALE;
    }

    public void setInitialPosition( double init ) {
        reset( init );
    }

    public void goPressed() {
        movingManControlPanel.goPressed();
    }

    public double getFinalManPosition() {
        return model.getFinalManPosition();
    }

    public JFrame getFrame() {
        return frame;
    }

    public MovingManControlPanel getMovingManControlPanel() {
        return movingManControlPanel;
    }

    public double getMaxTime() {
        return model.getMaxTime();
    }

    public double getMaxManPosition() {
        return model.getMaxManPosition();
    }

    public MMTimer getPlaybackTimer() {
        return playbackTimer;
    }

    public boolean getRecordMode() {
        return mode == recordMode;
    }

    public boolean isTakingData() {
        return !isPaused() && mode.isTakingData();
    }

    public static void main( String[] args ) throws UnsupportedLookAndFeelException, IOException {
        SmoothUtilities.setFractionalMetrics( false );
        UIManager.setLookAndFeel( new PhetLookAndFeel() );
        AbstractClock clock = new SwingTimerClock( 1, 30, true );
//        AbstractClock clock = new SwingTimerClock( 1, 30, false );
        MovingManModule m = new MovingManModule( clock );
        FrameSetup setup = new FrameSetup.MaxExtent();

        ApplicationModel desc = new ApplicationModel( "The Moving Man", "The Moving Man Application.",
                                                      ".02-beta-x 10-18-2004", setup, m, clock );
        PhetApplication tpa = new PhetApplication( desc );

        final PhetFrame frame = tpa.getApplicationView().getPhetFrame();
        m.setFrame( frame );
        if( m.getControlPanel() != null ) {
            tpa.getApplicationView().getBasicPhetPanel().add( m.getControlPanel(), BorderLayout.WEST );
//            tpa.getApplicationView().getBasicPhetPanel().setLayout( null);
//            tpa.getApplicationView().getBasicPhetPanel().add( m.getControlPanel() );
//            tpa.getApplicationView().getBasicPhetPanel().add(m.getApparatusPanel());
        }
        if( addJEP ) {
            addJEP( m );
        }
        tpa.startApplication();
        frame.setVisible( true );
        fixComponent( frame.getContentPane() );
        frame.setExtendedState( JFrame.MAXIMIZED_BOTH );
        frame.invalidate();
        frame.validate();
        frame.repaint();
        m.repaintBackground();
        m.recordMode.initialize();
//        m.cursorGraphic.setVisible( false );
        m.getApparatusPanel().repaint();
//        m.cursorGraphic.setVisible( false );

        final Runnable dofix = new Runnable() {
            public void run() {
                try {
                    Thread.sleep( 300 );
                    fixComponent( frame.getContentPane() );
                    fixComponent( frame );
                }
                catch( InterruptedException e1 ) {
                    e1.printStackTrace();
                }
            }
        };
        frame.addWindowFocusListener( new WindowFocusListener() {
            public void windowGainedFocus( WindowEvent e ) {
                new Thread( dofix ).start();
            }

            public void windowLostFocus( WindowEvent e ) {
            }
        } );
        frame.addWindowListener( new WindowListener() {
            public void windowActivated( WindowEvent e ) {
                new Thread( dofix ).start();
            }

            public void windowClosed( WindowEvent e ) {
            }

            public void windowClosing( WindowEvent e ) {
            }

            public void windowDeactivated( WindowEvent e ) {
            }

            public void windowDeiconified( WindowEvent e ) {
            }

            public void windowIconified( WindowEvent e ) {
            }

            public void windowOpened( WindowEvent e ) {
                new Thread( dofix ).start();
            }
        } );
        new Thread( dofix ).start();
//        RepaintDebugGraphic rdp = new RepaintDebugGraphic( m, m.getApparatusPanel(), clock );
//        m.backgroundGraphic.addGraphic( rdp, -100 );
    }

    private static void addJEP( MovingManModule module ) {
        final JFrame frame = module.getFrame();
        JMenu misc = new JMenu( "Misc" );
        JMenuItem jep = new JMenuItem( "Expression Evaluator" );
        misc.add( jep );
        final JEPFrame jef = new JEPFrame( frame, module );
        jep.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                jef.setVisible( true );
            }
        } );
        frame.getJMenuBar().add( misc );
    }

    public void step( double dt ) {
        getPosition().addPoint( getMan().getX() );
        getPosition().updateSmoothedSeries();
        getPosition().updateDerivative( dt * MovingManModel.TIMER_SCALE );
        getVelocityData().updateSmoothedSeries();
        getVelocityData().updateDerivative( dt * MovingManModel.TIMER_SCALE );
        getAcceleration().updateSmoothedSeries();
        plotSet.updateSliders();
    }

    interface StateListener {
        void stateChanged( MovingManModule module );
    }
}


