// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.geneexpressionbasics.manualgeneexpression.model;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.model.property.ChangeObserver;
import edu.colorado.phet.geneexpressionbasics.common.model.BiomoleculeShapeUtils;
import edu.colorado.phet.geneexpressionbasics.common.model.MobileBiomolecule;
import edu.colorado.phet.geneexpressionbasics.common.model.behaviorstates.TranslatingMRnaState;

/**
 * Class that represents the a ribosome in the model.
 *
 * @author John Blanco
 */
public class Ribosome extends MobileBiomolecule {

    private static final double WIDTH = 290;                  // In nanometers.
    private static final double OVERALL_HEIGHT = 300;         // In nanometers.
    private static final double TOP_SUBUNIT_HEIGHT_PROPORTION = 0.6;
    private static final double TOP_SUBUNIT_HEIGHT = OVERALL_HEIGHT * TOP_SUBUNIT_HEIGHT_PROPORTION;
    private static final double BOTTOM_SUBUNIT_HEIGHT = OVERALL_HEIGHT * ( 1 - TOP_SUBUNIT_HEIGHT_PROPORTION );
    private static final double MRNA_CAPTURE_THRESHOLD = 1000;
    private static final ImmutableVector2D OFFSET_TO_TRANSLATION_CHANNEL_ENTRANCE = new ImmutableVector2D( WIDTH / 2, -OVERALL_HEIGHT / 2 + BOTTOM_SUBUNIT_HEIGHT );

    public Ribosome( GeneExpressionModel model ) {
        this( model, new Point2D.Double( 0, 0 ) );
    }

    public Ribosome( final GeneExpressionModel model, Point2D position ) {
        super( model, createShape(), new Color( 205, 155, 29 ) );
        setPosition( position );
        userControlled.addObserver( new ChangeObserver<Boolean>() {
            public void update( Boolean isUserControlled, Boolean wasUserControlled ) {
                if ( wasUserControlled && !isUserControlled ) {
                    // The user just dropped this ribosome.  If there is an
                    // mRNA nearby, attach to it.
                    for ( MessengerRna messengerRna : model.getMessengerRnaList() ) {
                        if ( messengerRna.getPosition().distance( getPosition() ) < MRNA_CAPTURE_THRESHOLD ) {

                            // Move to the appropriate location in order to
                            // look attached to the mRNA.
                            setPosition( new ImmutableVector2D( messengerRna.getTranslationAttachmentPoint() ).getSubtractedInstance( OFFSET_TO_TRANSLATION_CHANNEL_ENTRANCE ).toPoint2D() );

                            // Attach to this mRNA.
                            messengerRna.connectToRibosome( Ribosome.this );

                            // Move into the translating state.
                            behaviorState = new TranslatingMRnaState( messengerRna, Ribosome.this );
                        }
                    }
                }
            }
        } );
    }

    private static Shape createShape() {
        // Draw the top portion, which in this sim is the larger subunit.  The
        // shape is essentially a lumpy ellipse, and is based on some drawings
        // seen on the web.
        List<Point2D> topSubunitPointList = new ArrayList<Point2D>() {{
            // Define the shape with a series of points.  Starts at top left.
            add( new Point2D.Double( -WIDTH * 0.3, TOP_SUBUNIT_HEIGHT * 0.9 ) );
            add( new Point2D.Double( WIDTH * 0.3, TOP_SUBUNIT_HEIGHT ) );
            add( new Point2D.Double( WIDTH * 0.5, 0 ) );
            add( new Point2D.Double( WIDTH * 0.3, -TOP_SUBUNIT_HEIGHT * 0.4 ) );
            add( new Point2D.Double( 0, -TOP_SUBUNIT_HEIGHT * 0.5 ) ); // Center bottom.
            add( new Point2D.Double( -WIDTH * 0.3, -TOP_SUBUNIT_HEIGHT * 0.4 ) );
            add( new Point2D.Double( -WIDTH * 0.5, 0 ) );
        }};
        Shape topSubunitShape = AffineTransform.getTranslateInstance( 0, OVERALL_HEIGHT / 4 ).createTransformedShape( BiomoleculeShapeUtils.createRoundedShapeFromPoints( topSubunitPointList ) );
        // Draw the bottom portion, which in this sim is the smaller subunit.
        List<Point2D> bottomSubunitPointList = new ArrayList<Point2D>() {{
            // Define the shape with a series of points.
            add( new Point2D.Double( -WIDTH * 0.45, BOTTOM_SUBUNIT_HEIGHT * 0.5 ) );
            add( new Point2D.Double( 0, BOTTOM_SUBUNIT_HEIGHT * 0.45 ) );
            add( new Point2D.Double( WIDTH * 0.45, BOTTOM_SUBUNIT_HEIGHT * 0.5 ) );
            add( new Point2D.Double( WIDTH * 0.45, -BOTTOM_SUBUNIT_HEIGHT * 0.5 ) );
            add( new Point2D.Double( 0, -BOTTOM_SUBUNIT_HEIGHT * 0.45 ) );
            add( new Point2D.Double( -WIDTH * 0.45, -BOTTOM_SUBUNIT_HEIGHT * 0.5 ) );
        }};
        Shape bottomSubunitShape = AffineTransform.getTranslateInstance( 0, -OVERALL_HEIGHT / 4 ).createTransformedShape( BiomoleculeShapeUtils.createRoundedShapeFromPoints( bottomSubunitPointList ) );
        // Combine the two subunits into one shape.
        Area combinedShape = new Area( topSubunitShape );
        combinedShape.add( new Area( bottomSubunitShape ) );
        return combinedShape;
    }

    public ImmutableVector2D getEntranceOfRnaChannelPos() {
        return new ImmutableVector2D( getPosition().getX() + WIDTH / 2, getRnaChannelYPos() );
    }

    public ImmutableVector2D getExitOfRnaChannelPos() {
        return new ImmutableVector2D( getPosition().getX() - WIDTH / 2, getRnaChannelYPos() );
    }

    private double getRnaChannelYPos() {
        return getPosition().getY() - ( OVERALL_HEIGHT / 2 ) + BOTTOM_SUBUNIT_HEIGHT;
    }

    public double getTranslationChannelLength() {
        return WIDTH;
    }
}
