package edu.colorado.phet.fractionsintro.buildafraction.view;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;

import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.colorado.phet.common.piccolophet.nodes.layout.HBox;
import edu.colorado.phet.fractionsintro.buildafraction.model.BuildAFractionModel;
import edu.colorado.phet.fractionsintro.intro.model.Fraction;
import edu.umd.cs.piccolo.PNode;

import static edu.colorado.phet.fractionsintro.buildafraction_functional.view.BuildAFractionCanvas.controlPanelStroke;

/**
 * Node that shows a target scoring cell, where a correct fraction can be collected.
 *
 * @author Sam Reid
 */
public class ScoreBoxNode extends PNode {
    public final Fraction fraction;
    private final HBox box;

    public ScoreBoxNode( final int numerator, final int denominator, final PNode representationBox, final BuildAFractionModel model ) {
        this.box = new HBox( new PhetPPath( new RoundRectangle2D.Double( 0, 0, 140, 150, 30, 30 ), controlPanelStroke, Color.darkGray ) {{

            setStrokePaint( Color.darkGray );
            setStroke( controlPanelStroke );
            //Light up if the user matched
//            model.addObserver( new ChangeObserver<BuildAFractionState>() {
//                public void update( final BuildAFractionState newValue, final BuildAFractionState oldValue ) {
//                    if ( newValue.containsMatch( numerator, 6 ) != oldValue.containsMatch( numerator, 6 ) ) {
//
//                    }
//                    if ( newValue.containsMatch( numerator, 6 ) ) {
//
//                        //Pulsate for a few seconds then stay highlighted.
//                        double matchTime = newValue.getMatchTimes( numerator, 6 ).maximum( Ord.<Double>comparableOrd() );
//                        final double timeSinceMatch = newValue.time - matchTime;
//                        final float strokeWidth = timeSinceMatch < 2 ? 2 + 8 * (float) Math.abs( Math.sin( model.state.get().time * 4 ) ) :
//                                                  2 + 4;
//
//                        //Block against unnecessary repainting for target cell highlighting
//                        float originalWidth = ( (BasicStroke) getStroke() ).getLineWidth();
//                        if ( originalWidth != strokeWidth ) {
//                            setStroke( new BasicStroke( strokeWidth, CAP_ROUND, BasicStroke.JOIN_ROUND, 1f ) );
//                        }
//                    }
//                }
//            } );
        }}, representationBox );
        this.fraction = new Fraction( numerator, denominator );

        addChild( box );
    }
}