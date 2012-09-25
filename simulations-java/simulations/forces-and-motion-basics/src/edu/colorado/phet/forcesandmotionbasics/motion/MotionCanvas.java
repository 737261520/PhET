package edu.colorado.phet.forcesandmotionbasics.motion;

import fj.data.List;
import fj.function.Doubles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;

import edu.colorado.phet.common.phetcommon.model.Resettable;
import edu.colorado.phet.common.phetcommon.model.clock.ClockAdapter;
import edu.colorado.phet.common.phetcommon.model.clock.ClockEvent;
import edu.colorado.phet.common.phetcommon.model.clock.IClock;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.model.property.doubleproperty.DoubleProperty;
import edu.colorado.phet.common.phetcommon.util.Option;
import edu.colorado.phet.common.phetcommon.util.Option.Some;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.view.controls.PropertyCheckBox;
import edu.colorado.phet.common.piccolophet.activities.PActivityDelegateAdapter;
import edu.colorado.phet.common.piccolophet.nodes.ControlPanelNode;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.colorado.phet.common.piccolophet.nodes.PhetPText;
import edu.colorado.phet.common.piccolophet.nodes.ResetAllButtonNode;
import edu.colorado.phet.common.piccolophet.nodes.SpeedometerNode;
import edu.colorado.phet.common.piccolophet.nodes.background.SkyNode;
import edu.colorado.phet.common.piccolophet.nodes.layout.HBox;
import edu.colorado.phet.common.piccolophet.nodes.layout.VBox;
import edu.colorado.phet.common.piccolophet.nodes.mediabuttons.PlayPauseButton;
import edu.colorado.phet.common.piccolophet.nodes.mediabuttons.RewindButton;
import edu.colorado.phet.common.piccolophet.nodes.mediabuttons.StepButton;
import edu.colorado.phet.forcesandmotionbasics.ForcesAndMotionBasicsResources.Images;
import edu.colorado.phet.forcesandmotionbasics.common.AbstractForcesAndMotionBasicsCanvas;
import edu.colorado.phet.forcesandmotionbasics.common.ForceArrowNode;
import edu.colorado.phet.forcesandmotionbasics.common.TextLocation;
import edu.colorado.phet.forcesandmotionbasics.tugofwar.Context;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.pswing.PSwing;

import static edu.colorado.phet.common.phetcommon.math.vector.Vector2D.v;
import static edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform.createIdentity;

/**
 * @author Sam Reid
 */
public class MotionCanvas extends AbstractForcesAndMotionBasicsCanvas implements StackableNodeContext {

    public static final Color BROWN = new Color( 197, 154, 91 );
    private final Property<Boolean> showSpeedometer = new Property<Boolean>( false );
    private final Property<Boolean> showValues = new Property<Boolean>( false );
    private final PImage skateboard;
    private final List<StackableNode> stackableNodes;
    private final Property<List<StackableNode>> stack = new Property<List<StackableNode>>( List.<StackableNode>nil() );

    //TODO: Move to model?
    private final DoubleProperty appliedForce = new DoubleProperty( 0.0 );
    private final DoubleProperty velocity = new DoubleProperty( 0.0 );
    private final DoubleProperty position = new DoubleProperty( 0.0 );
    private final PNode forcesNode;
    private final Property<Option<Double>> speed = new Property<Option<Double>>( new Some<Double>( 0.0 ) );

    public static final int FRIDGE_OFFSET_WITHIN_SKATEBOARD = 33;
    public static final int CRATE_OFFSET_WITHIN_SKATEBOARD = 48;

    public MotionCanvas( final Context context, final IClock clock ) {

        setBackground( BROWN );
        //use view coordinates since nothing compex happening in model coordinates.

        //for a canvas height of 710, the ground is at 452 down from the top
        final int width = 10000;

        //Reverse bottom and top because using view coordinates
        final int grassY = 425;
        addChild( new SkyNode( createIdentity(), new Rectangle2D.Double( -width / 2, -width / 2 + grassY, width, width / 2 ), grassY, SkyNode.DEFAULT_TOP_COLOR, SkyNode.DEFAULT_BOTTOM_COLOR ) );

        PNode terrain = new PNode() {{
            final BufferedImage tile = Images.BRICK_TILE;
            position.addObserver( new VoidFunction1<Double>() {
                public void apply( final Double position ) {
                    removeAllChildren();
                    for ( int i = -10; i < 10; i++ ) {
                        final int finalI = i;
                        addChild( new PImage( tile ) {{
                            scale( 0.4 );
                            setOffset( finalI * tile.getWidth() * getScale() + STAGE_SIZE.getWidth() / 2 - position * 100, grassY );
                        }} );
                    }
                }
            } );
        }};
        addChild( terrain );

        final JCheckBox speedCheckBox = new PropertyCheckBox( null, "Speed", showSpeedometer ) {{ setFont( CONTROL_FONT ); }};
        final JCheckBox showValuesCheckBox = new PropertyCheckBox( null, "Values", showValues ) {{setFont( CONTROL_FONT );}};
        final ControlPanelNode controlPanelNode = new ControlPanelNode(
                new VBox( 2, VBox.LEFT_ALIGNED,

                          //Nudge "show" to the right so it will align with checkboxes
                          new HBox( 5, new PhetPPath( new Rectangle2D.Double( 0, 0, 0, 0 ) ), new PhetPText( "Show", CONTROL_FONT ) ),
                          new PSwing( showValuesCheckBox ), new PSwing( speedCheckBox ) ), new Color( 227, 233, 128 ), new BasicStroke( 2 ), Color.black );
        controlPanelNode.setOffset( STAGE_SIZE.width - controlPanelNode.getFullWidth() - INSET, INSET );
        addChild( controlPanelNode );

        addChild( new ResetAllButtonNode( new Resettable() {
            public void reset() {
                context.reset();
            }
        }, this, CONTROL_FONT, Color.black, Color.orange ) {{
            setOffset( controlPanelNode.getFullBounds().getCenterX() - getFullBounds().getWidth() / 2, controlPanelNode.getMaxY() + INSET );
            setConfirmationEnabled( false );
        }} );

        skateboard = new PImage( Images.SKATEBOARD );
        skateboard.setOffset( STAGE_SIZE.getWidth() / 2 - skateboard.getFullBounds().getWidth() / 2, grassY - skateboard.getFullBounds().getHeight() );
        addChild( skateboard );

        PhetPPath toolbox = new PhetPPath( new RoundRectangle2D.Double( 0, 0, 250, 160, 20, 20 ), new Color( 231, 232, 233 ), new BasicStroke( 1 ), Color.black );
        toolbox.setOffset( INSET, STAGE_SIZE.height - INSET - toolbox.getFullBounds().getHeight() );
        addChild( toolbox );

        SliderControl sliderControl = new SliderControl( appliedForce, stack );
        sliderControl.setOffset( STAGE_SIZE.getWidth() / 2 - sliderControl.getFullBounds().getWidth() / 2, grassY + 50 );
        addChild( sliderControl );

        final PusherNode pusherNode = new PusherNode( skateboard, grassY, appliedForce, stack );
        addChild( pusherNode );

        HBox timeControls = new HBox( -3, new RewindButton( 60 ), new PlayPauseButton( 70 ), new StepButton( 60 ) );
        timeControls.setOffset( STAGE_SIZE.width / 2 - timeControls.getFullWidth() / 2, STAGE_SIZE.height - timeControls.getFullHeight() );
        addChild( timeControls );

        StackableNode fridge = new StackableNode( this, Images.FRIDGE, 200, FRIDGE_OFFSET_WITHIN_SKATEBOARD );
        StackableNode crate1 = new StackableNode( this, Images.CRATE, 50, CRATE_OFFSET_WITHIN_SKATEBOARD );
        StackableNode crate2 = new StackableNode( this, Images.CRATE, 50, CRATE_OFFSET_WITHIN_SKATEBOARD );

        stackableNodes = fj.data.List.list( fridge, crate1, crate2 );

        double INTER_OBJECT_SPACING = 10;
        fridge.setInitialOffset( toolbox.getFullBounds().getX() + 10, toolbox.getFullBounds().getCenterY() - fridge.getFullBounds().getHeight() / 2 );
        crate1.setInitialOffset( fridge.getFullBounds().getMaxX() + INTER_OBJECT_SPACING, fridge.getFullBounds().getMaxY() - crate1.getFullBounds().getHeight() );
        crate2.setInitialOffset( crate1.getFullBounds().getMaxX() + INTER_OBJECT_SPACING, fridge.getFullBounds().getMaxY() - crate2.getFullBounds().getHeight() );

        addChild( fridge );
        addChild( crate1 );
        addChild( crate2 );

        clock.addClockListener( new ClockAdapter() {
            @Override public void simulationTimeChanged( final ClockEvent clockEvent ) {
                double sumOfForces = appliedForce.get();
                final double mass = getMassOfObjectsOnSkateboard();
                double acceleration = mass != 0 ? sumOfForces / mass : 0.0;
                final double dt = clockEvent.getSimulationTimeChange();
                velocity.set( velocity.get() + acceleration * dt );
                position.set( position.get() + velocity.get() * dt );
                speed.set( new Some<Double>( Math.abs( velocity.get() ) ) );

                if ( appliedForce.get() == 0.0 ) {
                    final double delta = -velocity.get() * dt * 100;
                    pusherNode.setOffset( pusherNode.getOffset().getX() + delta, pusherNode.getOffset().getY() );
                }
            }
        } );

        appliedForce.addObserver( new VoidFunction1<Double>() {
            public void apply( final Double appliedForce ) {
                if ( appliedForce != 0.0 ) {
                    pusherNode.setOffset( 0, 0 );
                }
            }
        } );

        forcesNode = new PNode() {{
            final SimpleObserver updateForces = new SimpleObserver() {
                public void update() {
                    removeAllChildren();
                    if ( showValuesCheckBox.isSelected() ) {
                        addChild( new ForceArrowNode( false, v( skateboard.getFullBounds().getCenterX(), skateboard.getFullBounds().getCenterY() - 75 ),
                                                      appliedForce.get() / 2, "Applied Force", new Color( 233, 110, 36 ), TextLocation.SIDE, false ) );
                    }
                }
            };
            showValues.addObserver( updateForces );
            appliedForce.addObserver( updateForces );
        }};
        addChild( forcesNode );

        SpeedometerNode speedometerNode = new SpeedometerNode( "Speed", 125, speed, 50 ) {{
            showSpeedometer.addObserver( new VoidFunction1<Boolean>() {
                public void apply( final Boolean show ) {
                    setVisible( show );
                }
            } );

            //scale up so fonts and stroke thicknesses look good
            scale( 1.25 );
            setOffset( STAGE_SIZE.width / 2 - getFullBounds().getWidth() / 2, 10 );
        }};
        addChild( speedometerNode );
    }

    private double getMassOfObjectsOnSkateboard() {
        return stackableNodes.filter( StackableNode._isOnSkateboard ).map( StackableNode._mass ).foldLeft( Doubles.add, 0.0 );
    }

    public void stackableNodeDropped( final StackableNode stackableNode ) {
        PBounds bounds = skateboard.getGlobalFullBounds();
        bounds.add( skateboard.getGlobalFullBounds().getCenterX(), skateboard.getGlobalFullBounds().getMinY() - stackableNode.getGlobalFullBounds().getHeight() );
        if ( bounds.intersects( stackableNode.getGlobalFullBounds() ) ) {
            PBounds skateboardBounds = skateboard.getGlobalFullBounds();
            Rectangle2D localBounds = stackableNode.getParent().globalToLocal( skateboardBounds );
            stackableNode.animateToPositionScaleRotation( localBounds.getCenterX() - stackableNode.getFullBounds().getWidth() / 2, localBounds.getY() - stackableNode.getFullBounds().getHeight() + 8, 1, 0, 200 ).setDelegate( new PActivityDelegateAdapter() {
                @Override public void activityFinished( final PActivity activity ) {
                    stackableNode.setOnSkateboard( true );
                    stack.set( List.single( stackableNode ) );
                }
            } );
        }
        else {
            stackableNode.setOnSkateboard( false );
            stack.set( List.<StackableNode>nil() );
            stackableNode.animateHome();
        }
    }
}