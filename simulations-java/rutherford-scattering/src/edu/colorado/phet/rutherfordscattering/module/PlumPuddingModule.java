/* Copyright 2007, University of Colorado */

package edu.colorado.phet.rutherfordscattering.module;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.colorado.phet.common.model.clock.IClock;
import edu.colorado.phet.common.view.ClockControlPanel;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.piccolo.PhetPCanvas;
import edu.colorado.phet.piccolo.help.MotionHelpBalloon;
import edu.colorado.phet.rutherfordscattering.RSConstants;
import edu.colorado.phet.rutherfordscattering.control.PlumPuddingControlPanel;
import edu.colorado.phet.rutherfordscattering.help.RSWiggleMe;
import edu.colorado.phet.rutherfordscattering.model.*;
import edu.colorado.phet.rutherfordscattering.view.*;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * PlumPuddingModule is the "Plum Pudding" module for this simulation.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class PlumPuddingModule extends AbstractModule {
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    // Model
    private RSModel _model;
    private Gun _gun;
    
    // View
    private PhetPCanvas _canvas;
    private PNode _rootNode;
    private PNode _boxBeamGunParent;
    private BoxOfHydrogenNode _boxOfHydrogenNode;
    private BeamNode _beamNode;
    private GunNode _gunNode;
    private AnimationBoxNode _animationBoxNode;
    private ZoomIndicatorNode _zoomIndicatorNode;
    private TracesNode _alphaParticleTracesNode;
    private RSModelViewManager _modelViewManager;
    
    // Control panels
    private ClockControlPanel _clockControlPanel;
    private PlumPuddingControlPanel _controlPanel;
    
    // Help
    private RSWiggleMe _wiggleMe;
    private boolean _wiggleMeInitialized = false;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    public PlumPuddingModule() {
        super( SimStrings.get( "PlumPuddingModule.title" ), new RSClock(), RSConstants.CLOCK_PAUSED );

        //----------------------------------------------------------------------------
        // Model
        //----------------------------------------------------------------------------

        IClock clock = getClock();

        // Gun
        Point2D position = new Point2D.Double( 0, 0 );
        double orientation = Math.toRadians( -90 ); // pointing straight up
        double nozzleWidth = RSConstants.ANIMATION_BOX_SIZE.width;
        _gun = new Gun( position, orientation, nozzleWidth, 
                RSConstants.INITIAL_SPEED_RANGE,
                RSConstants.BEAM_OF_ALPHA_PARTICLES_COLOR, 
                RSConstants.ANIMATION_BOX_SIZE );

        // Space
        double spaceWidth = _gun.getNozzleWidth();
        double spaceHeight = RSConstants.ANIMATION_BOX_SIZE.height;
        Rectangle2D bounds = new Rectangle2D.Double( -spaceWidth / 2, -spaceHeight, spaceWidth, spaceHeight );
        Space space = new Space( bounds );

        // Atom
        Point2D spaceCenter = space.getCenter();
        double radius = 0.95 * ( RSConstants.ANIMATION_BOX_SIZE.width / 2 );
        PlumPuddingAtom atom = new PlumPuddingAtom( spaceCenter, radius );
        
        // Model
        _model = new RSModel( clock, _gun, space, atom );

        //----------------------------------------------------------------------------
        // View
        //----------------------------------------------------------------------------

        // Piccolo canvas
        {
            _canvas = new PhetPCanvas( RSConstants.CANVAS_RENDERING_SIZE );
            _canvas.setBackground( RSConstants.CANVAS_BACKGROUND );
            setSimulationPanel( _canvas );
        }

        // Root of our scene graph
        _rootNode = new PNode();
        _canvas.addWorldChild( _rootNode );
        
        //  Box of Hydrogen / Beam / Gun
        {
            // Parent node, used for layout
            _boxBeamGunParent = new PNode();

            _boxOfHydrogenNode = new BoxOfHydrogenNode( RSConstants.BOX_OF_HYDROGEN_SIZE, RSConstants.TINY_BOX_SIZE );
            _beamNode = new BeamNode( RSConstants.BEAM_SIZE, _model.getGun() );
            _gunNode = new GunNode( _model.getGun() );

            // Layering order
            _boxBeamGunParent.addChild( _beamNode );
            _boxBeamGunParent.addChild( _boxOfHydrogenNode );
            _boxBeamGunParent.addChild( _gunNode );
            
            // Positioning
            final double gunCenterOffset = 28;
            final double boxWidth = _boxOfHydrogenNode.getFullBounds().getWidth();
            final double gunWidth = _gunNode.getFullBounds().getWidth();
            if ( boxWidth > gunWidth ) {
                _boxOfHydrogenNode.setOffset( 0, 0 );
                _beamNode.setOffset( ( boxWidth - _beamNode.getFullBounds().getWidth() ) / 2, _boxOfHydrogenNode.getFullBounds().getMaxY() );
                _gunNode.setOffset( ( ( boxWidth - gunWidth ) / 2 ) + gunCenterOffset, _beamNode.getFullBounds().getMaxY() );
            }
            else {
                _boxOfHydrogenNode.setOffset( ( ( gunWidth - boxWidth ) / 2 ) - gunCenterOffset, 0 );
                _beamNode.setOffset( _boxOfHydrogenNode.getFullBounds().getX() + ( boxWidth - _beamNode.getFullBounds().getWidth() ) / 2, _boxOfHydrogenNode.getFullBounds().getMaxY() );
                _gunNode.setOffset( 0, _beamNode.getFullBounds().getMaxY() );
            }
        }

        // Animation box
        _animationBoxNode = new AnimationBoxNode( RSConstants.ANIMATION_BOX_SIZE );

        // Zoom indicator
        _zoomIndicatorNode = new ZoomIndicatorNode();
        
        // Atom
        PlumPuddingNode atomNode = new PlumPuddingNode( atom );
        _animationBoxNode.getAtomLayer().addChild(  atomNode  );
        
        // Alpha Particles tracer
        {
            _alphaParticleTracesNode = new TracesNode( _model );
            _alphaParticleTracesNode.setBounds( 0, 0, _animationBoxNode.getWidth(), _animationBoxNode.getHeight() );
            _animationBoxNode.getTraceLayer().addChild( _alphaParticleTracesNode );
            _alphaParticleTracesNode.setEnabled( true );
        }

        // Layering order on the canvas (back-to-front)
        {
            _rootNode.addChild( _boxBeamGunParent );
            _rootNode.addChild( _animationBoxNode );
            _rootNode.addChild( _zoomIndicatorNode );
        }
        
        //----------------------------------------------------------------------------
        // Model-View management
        //----------------------------------------------------------------------------
        
        _modelViewManager = new RSModelViewManager( _model, _animationBoxNode );
        
        //----------------------------------------------------------------------------
        // Control
        //----------------------------------------------------------------------------

        // Clock controls
        _clockControlPanel = new ClockControlPanel( (RSClock) getClock() );
        setClockControlPanel( _clockControlPanel );
        
        // Control panel
        _controlPanel = new PlumPuddingControlPanel( this );
        setControlPanel( _controlPanel );

        //----------------------------------------------------------------------------
        // Help
        //----------------------------------------------------------------------------

        if ( hasHelp() ) {
            //XXX add help items to the help pane
//            HelpPane helpPane = getDefaultHelpPane();
        }
        
        // See initWiggleMe for Wiggle Me initialization.

        //----------------------------------------------------------------------------
        // Initialize the module state
        //----------------------------------------------------------------------------

        reset();
        updateCanvasLayout();
    }

    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------

    public Gun getGun() {
        return _gun;
    }
    
    public void removeAllAlphaParticles() {
        _model.removeAllAlphaParticles();
    }
    
    //----------------------------------------------------------------------------
    // AbstractModule implementation
    //----------------------------------------------------------------------------

    /*
     * Resets the module to its default state.
     */
    public void reset() {
        _gun.setEnabled( RSConstants.GUN_ENABLED );
        _gun.setIntensity( RSConstants.GUN_INTENSITY );
        _gun.setSpeed( RSConstants.INITIAL_SPEED_RANGE.getDefault() );
    }
    
    /*
     * Updates the layout of stuff on the canvas.
     */
    protected void updateCanvasLayout() {

        Dimension worldSize = getWorldSize();
//        System.out.println( "HAModule.updateCanvasLayout worldSize=" + worldSize );//XXX
        if ( worldSize.getWidth() == 0 || worldSize.getHeight() == 0 ) {
            // canvas hasn't been sized, blow off layout
            return;
        }
        
        // margins and spacing
        final double xMargin = 40;
        final double yMargin = 20;
        final double xSpacing = 20;
        final double ySpacing = 10;

        // reusable (x,y) coordinates, for setting offsets
        double x, y;

        // Box of Hydrogen / Beam / Gun
        {
            x = xMargin;
            y = 250;
            _boxBeamGunParent.setOffset( x, y );
        }
        
        // Animation box
        {
            // to the right of the box/beam/gun, below the "not to scale" label
            x = _boxBeamGunParent.getFullBounds().getMaxX() + xSpacing;
            y = yMargin;
            _animationBoxNode.setOffset( x, y );
        }

        // Zoom Indicator
        {
            Rectangle2D tinyBoxBounds = _zoomIndicatorNode.globalToLocal( _boxOfHydrogenNode.getTinyBoxGlobalFullBounds() );
            Rectangle2D bigBoxBounds = _zoomIndicatorNode.globalToLocal( _animationBoxNode.getGlobalFullBounds() );
            _zoomIndicatorNode.update( tinyBoxBounds, bigBoxBounds );
        }
        
        initWiggleMe();
    }
    
    //----------------------------------------------------------------------------
    // Wiggle Me
    //----------------------------------------------------------------------------
    
    /*
     * Initializes a wiggle me that points to the gun on/off button.
     */
    private void initWiggleMe() {
        if ( !_wiggleMeInitialized ) {
            
            // Create wiggle me, add to root node.
            String wiggleMeString = SimStrings.get( "wiggleMe.gun" );  
            _wiggleMe = new RSWiggleMe( _canvas, wiggleMeString );
            _wiggleMe.setArrowTailPosition( MotionHelpBalloon.TOP_LEFT );
            _wiggleMe.setArrowLength( 60 );
            _rootNode.addChild( _wiggleMe );
            
            // Animate from the upper- right to the gun button position
            PNode gunButtonNode = _gunNode.getButtonNode();
            Rectangle2D bounds = _rootNode.globalToLocal( gunButtonNode.getGlobalFullBounds() );
            final double x = bounds.getX() + ( bounds.getWidth() / 2 );
            final double y = bounds.getMaxY();
            _wiggleMe.setOffset( 400, -100 );
            _wiggleMe.animateTo( x, y );
            
            // Clicking on the canvas makes the wiggle me go away.
            _canvas.addInputEventListener( new PBasicInputEventHandler() {
                public void mousePressed( PInputEvent event ) {
                    _wiggleMe.setEnabled( false );
                    _rootNode.removeChild( _wiggleMe );
                    _canvas.removeInputEventListener( this );
                    _wiggleMe = null;
                }
            } );
            
            _wiggleMeInitialized = true;
        }
    }
}
