// Copyright 2002-2012, University of Colorado
package edu.colorado.phet.energyformsandchanges.common.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import edu.colorado.phet.common.phetcommon.math.vector.Vector2D;
import edu.colorado.phet.common.phetcommon.model.property.BooleanProperty;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.common.phetcommon.view.graphics.transforms.ModelViewTransform;
import edu.colorado.phet.common.phetcommon.view.util.ColorUtils;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.colorado.phet.energyformsandchanges.common.EFACConstants;
import edu.colorado.phet.energyformsandchanges.common.model.Beaker;
import edu.colorado.phet.energyformsandchanges.common.model.EnergyChunk;
import edu.colorado.phet.energyformsandchanges.intro.model.EnergyChunkContainerSliceNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.nodes.PClip;

/**
 * Object that represents a beaker in the view.  This representation is split
 * between a front node and a back node, which must be separately added to the
 * canvas.  This is done to allow a layering effect.  Hence, this cannot be
 * added directly to the canvas, and the client must add each layer separately.
 *
 * @author John Blanco
 */
public class BeakerView {

    private static final Stroke OUTLINE_STROKE = new BasicStroke( 3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
    private static final Color OUTLINE_COLOR = Color.LIGHT_GRAY;
    private static final double PERSPECTIVE_PROPORTION = -EFACConstants.Z_TO_Y_OFFSET_MULTIPLIER;
    private static final Font LABEL_FONT = new PhetFont( 32, false );
    private static final boolean SHOW_MODEL_RECT = false;
    private static final Color BEAKER_COLOR = new Color( 250, 250, 250, 100 );

    protected final ModelViewTransform mvt;
    protected final PClip energyChunkClipNode;

    protected final PNode frontNode = new PNode();
    protected final PNode backNode = new PNode();

    public BeakerView( final Beaker beaker, BooleanProperty energyChunksVisible, final ModelViewTransform mvt ) {

        this.mvt = mvt;

        // Extract the scale transform from the MVT so that we can separate the
        // shape from the position.
        AffineTransform scaleTransform = AffineTransform.getScaleInstance( mvt.getTransform().getScaleX(), mvt.getTransform().getScaleY() );

        // Get a version of the rectangle that defines the beaker size and
        // location in the view.
        final Rectangle2D beakerViewRect = scaleTransform.createTransformedShape( beaker.getRawOutlineRect() ).getBounds2D();

        // Create the shapes for the top and bottom of the beaker.  These are
        // ellipses in order to create a 3D-ish look.
        double ellipseHeight = beakerViewRect.getWidth() * PERSPECTIVE_PROPORTION;
        final Ellipse2D.Double topEllipse = new Ellipse2D.Double( beakerViewRect.getMinX(), beakerViewRect.getMinY() - ellipseHeight / 2, beakerViewRect.getWidth(), ellipseHeight );
        final Ellipse2D.Double bottomEllipse = new Ellipse2D.Double( beakerViewRect.getMinX(), beakerViewRect.getMaxY() - ellipseHeight / 2, beakerViewRect.getWidth(), ellipseHeight );

        // Add the bottom ellipse.
        backNode.addChild( new PhetPPath( bottomEllipse, BEAKER_COLOR, OUTLINE_STROKE, OUTLINE_COLOR ) );

        // Create and add the shape for the body of the beaker.
        final Area beakerBody = new Area( beakerViewRect );
        beakerBody.add( new Area( bottomEllipse ) );
        beakerBody.subtract( new Area( topEllipse ) );
        frontNode.addChild( new PhetPPath( beakerBody, BEAKER_COLOR, OUTLINE_STROKE, OUTLINE_COLOR ) );

        // Add the water.  It will adjust its size based on the fluid level.
        final PerspectiveWaterNode water = new PerspectiveWaterNode( beakerViewRect, beaker.fluidLevel );
        frontNode.addChild( water );

        // Add the top ellipse.  It is behind the water for proper Z-order behavior.
        backNode.addChild( new PhetPPath( topEllipse, BEAKER_COLOR, OUTLINE_STROKE, OUTLINE_COLOR ) );

        // Add a rectangle to the back that is invisible but allows the user to
        // grab the beaker.
        backNode.addChild( new PhetPPath( beakerViewRect, new Color( 0, 0, 0, 0 ) ) );

        // Make the front node non-pickable so that things in the beaker can be removed.
        frontNode.setPickable( false );
        frontNode.setChildrenPickable( false );

        // Add the label.  Position it just below the front, top water line.
        // TODO: i18n
        final PText label = new PText( "Water" );
        label.setFont( LABEL_FONT );
        label.setOffset( beakerViewRect.getCenterX() - label.getFullBoundsReference().width / 2,
                         beakerViewRect.getMaxY() - beakerViewRect.getHeight() * beaker.fluidLevel.get() + topEllipse.getHeight() / 2 );
        label.setPickable( false );
        label.setChildrenPickable( false );
        frontNode.addChild( label );

        // Create the layers where the contained energy chunks will be placed.
        // A clipping node is used to enable occlusion when interacting with
        // other model elements.
        final PNode energyChunkRootNode = new PNode();
        backNode.addChild( energyChunkRootNode );
        energyChunkClipNode = new PClip();
        energyChunkClipNode.setPathTo( beakerViewRect );
        energyChunkRootNode.addChild( energyChunkClipNode );
        energyChunkClipNode.setStroke( null );
        for ( int i = beaker.getSlices().size() - 1; i >= 0; i-- ) {
            int colorBase = (int) ( 255 * (double) i / beaker.getSlices().size() );
            energyChunkClipNode.addChild( new EnergyChunkContainerSliceNode( beaker.getSlices().get( i ), mvt, new Color( colorBase, 255 - colorBase, colorBase ) ) );
        }

        // Watch for coming and going of energy chunks that are approaching
        // this model element and add/remove them as needed.
        beaker.approachingEnergyChunks.addElementAddedObserver( new VoidFunction1<EnergyChunk>() {
            public void apply( final EnergyChunk addedEnergyChunk ) {
                final PNode energyChunkNode = new EnergyChunkNode( addedEnergyChunk, mvt );
                energyChunkRootNode.addChild( energyChunkNode );
                beaker.approachingEnergyChunks.addElementRemovedObserver( new VoidFunction1<EnergyChunk>() {
                    public void apply( EnergyChunk removedEnergyChunk ) {
                        if ( removedEnergyChunk == addedEnergyChunk ) {
                            energyChunkRootNode.removeChild( energyChunkNode );
                            beaker.approachingEnergyChunks.removeElementRemovedObserver( this );
                        }
                    }
                } );
            }
        } );

        // If enabled, show the outline of the rectangle that represents the
        // beaker's position in the model.
        if ( SHOW_MODEL_RECT ) {
            frontNode.addChild( new PhetPPath( beakerViewRect, new BasicStroke( 1 ), Color.RED ) );
        }

        // Update the offset if and when the model position changes.
        beaker.position.addObserver( new VoidFunction1<Vector2D>() {
            public void apply( Vector2D position ) {
                frontNode.setOffset( mvt.modelToView( position ).toPoint2D() );
                backNode.setOffset( mvt.modelToView( position ).toPoint2D() );
                // Compensate the energy chunk layer so that the energy chunk
                // nodes can handle their own positioning.
                energyChunkRootNode.setOffset( mvt.modelToView( position ).getRotatedInstance( Math.PI ).toPoint2D() );
            }
        } );

        // Adjust the transparency of the water and label based on energy
        // chunk visibility.
        energyChunksVisible.addObserver( new VoidFunction1<Boolean>() {
            public void apply( Boolean energyChunksVisible ) {
                label.setTransparency( energyChunksVisible ? 0.5f : 1f );
                water.setTransparency( energyChunksVisible ? EFACConstants.NOMINAL_WATER_OPACITY / 2 : EFACConstants.NOMINAL_WATER_OPACITY );
            }
        } );

    }

    // Class that represents water contained within the beaker.
    private static class PerspectiveWaterNode extends PNode {
        private static final Color WATER_OUTLINE_COLOR = ColorUtils.darkerColor( EFACConstants.WATER_COLOR_IN_BEAKER, 0.2 );
        private static final Stroke WATER_OUTLINE_STROKE = new BasicStroke( 2 );

        private PerspectiveWaterNode( final Rectangle2D beakerOutlineRect, Property<Double> waterLevel ) {

            final PhetPPath waterBodyNode = new PhetPPath( EFACConstants.WATER_COLOR_IN_BEAKER, WATER_OUTLINE_STROKE, WATER_OUTLINE_COLOR );
            addChild( waterBodyNode );
            final PhetPPath waterTopNode = new PhetPPath( EFACConstants.WATER_COLOR_IN_BEAKER, WATER_OUTLINE_STROKE, WATER_OUTLINE_COLOR );
            addChild( waterTopNode );

            waterLevel.addObserver( new VoidFunction1<Double>() {
                public void apply( Double fluidLevel ) {
                    assert fluidLevel >= 0 && fluidLevel <= 1; // Bounds checking.

                    Rectangle2D waterRect = new Rectangle2D.Double( beakerOutlineRect.getX(),
                                                                    beakerOutlineRect.getY() + beakerOutlineRect.getHeight() * ( 1 - fluidLevel ),
                                                                    beakerOutlineRect.getWidth(),
                                                                    beakerOutlineRect.getHeight() * fluidLevel );

                    double ellipseHeight = PERSPECTIVE_PROPORTION * beakerOutlineRect.getWidth();
                    Shape topEllipse = new Ellipse2D.Double( waterRect.getMinX(),
                                                             waterRect.getMinY() - ellipseHeight / 2,
                                                             waterRect.getWidth(),
                                                             ellipseHeight );

                    Shape bottomEllipse = new Ellipse2D.Double( waterRect.getMinX(),
                                                                waterRect.getMaxY() - ellipseHeight / 2,
                                                                waterRect.getWidth(),
                                                                ellipseHeight );

                    // Update the shape of the body and bottom of the water.
                    Area waterBodyArea = new Area( waterRect );
                    waterBodyArea.add( new Area( bottomEllipse ) );
                    waterBodyArea.subtract( new Area( topEllipse ) );
                    waterBodyNode.setPathTo( waterBodyArea );

                    // Update the shape of the water based on the proportionate
                    // water level.
                    waterTopNode.setPathTo( topEllipse );
                }
            } );
        }
    }

    public PNode getFrontNode() {
        return frontNode;
    }

    public PNode getBackNode() {
        return backNode;
    }
}
