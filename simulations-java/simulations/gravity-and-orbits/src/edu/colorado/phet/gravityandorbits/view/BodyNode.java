package edu.colorado.phet.gravityandorbits.view;

import java.awt.*;
import java.awt.geom.Point2D;

import edu.colorado.phet.common.phetcommon.math.Function;
import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.model.Property;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.event.CursorHandler;
import edu.colorado.phet.common.piccolophet.nodes.ArrowNode;
import edu.colorado.phet.gravityandorbits.model.Body;
import edu.umd.cs.piccolo.PComponent;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Sam Reid
 */
public class BodyNode extends PNode {
    private Property<ModelViewTransform> modelViewTransform;
    private Body body;
    private final Property<Boolean> toScaleProperty;
    private PNode arrowIndicator;
    private Function.LinearFunction sizer;//mapping to use when 'not to scale'
    private final BodyRenderer bodyRenderer;

    public BodyNode( final Body body, final Property<ModelViewTransform> modelViewTransform, final Property<Boolean> toScaleProperty,
                     final Property<ImmutableVector2D> mousePositionProperty, final PComponent parentComponent, Function.LinearFunction sizer, final double labelAngle ) {
        this.modelViewTransform = modelViewTransform;
        this.body = body;
        this.toScaleProperty = toScaleProperty;
        this.sizer = sizer;

        bodyRenderer = body.createRenderer( getViewDiameter() );
        addChild( bodyRenderer );

        final CursorHandler cursorHandler = new CursorHandler();
        if ( body.isModifyable() ) {
            addInputEventListener( cursorHandler );
            addInputEventListener( new PBasicInputEventHandler() {
                public void mousePressed( PInputEvent event ) {
                    body.setUserControlled( true );
                }

                public void mouseDragged( PInputEvent event ) {
                    final ModelViewTransform.Dimension2DDouble delta = modelViewTransform.getValue().viewToModel( event.getDeltaRelativeTo( getParent() ) );
                    body.translate( new Point2D.Double( delta.getWidth(), delta.getHeight() ) );
                }

                public void mouseReleased( PInputEvent event ) {
                    body.setUserControlled( false );
                }
            } );
        }
        final SimpleObserver updatePosition = new SimpleObserver() {
            public void update() {
                /* we need to determine whether the mouse is over the body both before and after the model change so
                 * that we can toggle the hand pointer over the body.
                 *
                 * otherwise the body can move over the mouse and be dragged without ever seeing the hand pointer
                 */
                boolean isMouseOverBefore = bodyRenderer.getGlobalFullBounds().contains( mousePositionProperty.getValue().toPoint2D() );
                setOffset( modelViewTransform.getValue().modelToView( body.getPosition() ).toPoint2D() );
//                System.out.println( "ModelViewTransform.modelToView( body.getPosition() ) = " + ModelViewTransform.modelToView( body.getPosition() ) );
                boolean isMouseOverAfter = bodyRenderer.getGlobalFullBounds().contains( mousePositionProperty.getValue().toPoint2D() );
                if ( parentComponent != null && body.isModifyable() ) {
                    if ( isMouseOverBefore && !isMouseOverAfter ) {
                        cursorHandler.mouseExited( new PInputEvent( null, null ) {
                            @Override
                            public PComponent getComponent() {
                                return parentComponent;
                            }
                        } );
                    }
                    if ( !isMouseOverBefore && isMouseOverAfter ) {
                        cursorHandler.mouseEntered( new PInputEvent( null, null ) {
                            @Override
                            public PComponent getComponent() {
                                return parentComponent;
                            }
                        } );
                    }
                }
            }
        };
        body.getPositionProperty().addObserver( updatePosition );
        modelViewTransform.addObserver( updatePosition );

        final SimpleObserver updateDiameter = new SimpleObserver() {
            public void update() {
                bodyRenderer.setDiameter( getViewDiameter() );
            }
        };
        body.getDiameterProperty().addObserver( updateDiameter );
        toScaleProperty.addObserver( updateDiameter );
        modelViewTransform.addObserver(updateDiameter);

        //Points to the sphere with a text indicator and line, for when it is too small to see (in modes with realistic units)
        arrowIndicator = new PNode() {{
            Point2D viewCenter = new Point2D.Double( 0, 0 );
            ImmutableVector2D northEastVector = ImmutableVector2D.parseAngleAndMagnitude( 1, labelAngle );
            Point2D tip = northEastVector.getScaledInstance( 10 ).getDestination( viewCenter );
            final Point2D tail = northEastVector.getScaledInstance( 50 ).getDestination( viewCenter );

            addChild( new ArrowNode( tail, tip, 0, 0, 3 ) {{
                setPaint( Color.yellow );
            }} );
            addChild( new PText( body.getName() ) {{
                setOffset( tail.getX() - getFullBounds().getWidth() / 2 - 5, tail.getY() - getFullBounds().getHeight() - 10 );
                setTextPaint( Color.white );
                setFont( new PhetFont( 18, true ) );
            }} );
        }};
        addChild( arrowIndicator );
        toScaleProperty.addObserver( new SimpleObserver() {
            public void update() {
                arrowIndicator.setVisible( toScaleProperty.getValue() );
            }
        } );
    }

    private double getViewDiameter() {
        if ( toScaleProperty.getValue() ) {
            return Math.max( modelViewTransform.getValue().modelToViewDeltaX( body.getDiameter() ), 2 );//anything less than 2 is not visible on the screen with default scaling
        }
        else {
            final double viewDiameter = modelViewTransform.getValue().modelToViewDeltaX( body.getDiameter() );
            final double newDiameter = sizer.evaluate( viewDiameter );
            return newDiameter;
        }
    }

    public Image sphereNodeToImage() {
        return bodyRenderer.toImage();
    }
}
