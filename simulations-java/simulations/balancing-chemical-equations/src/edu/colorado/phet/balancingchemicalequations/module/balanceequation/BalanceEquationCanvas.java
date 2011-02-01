// Copyright 2002-2011, University of Colorado

package edu.colorado.phet.balancingchemicalequations.module.balanceequation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;

import edu.colorado.phet.balancingchemicalequations.control.BalanceChoiceNode;
import edu.colorado.phet.balancingchemicalequations.control.BalanceChoiceNode.BalanceChoice;
import edu.colorado.phet.balancingchemicalequations.control.EquationChoiceNode;
import edu.colorado.phet.balancingchemicalequations.view.*;
import edu.colorado.phet.common.phetcommon.model.Property;
import edu.colorado.phet.common.phetcommon.model.Resettable;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.piccolophet.nodes.ResetAllButtonNode;
import edu.colorado.phet.common.piccolophet.util.PNodeLayoutUtils;

/**
 * Canvas for the "Balance Equation" module.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class BalanceEquationCanvas extends BCECanvas {

    private final Property<BalanceChoice> balanceChoiceProperty; // determines the visual representation of "balanced"

    public BalanceEquationCanvas( Frame parentFrame, Resettable resettable, final BalanceEquationModel model, boolean dev ) {

        balanceChoiceProperty = new Property<BalanceChoice>( BalanceChoice.BAR_CHARTS );

        // control for choosing an equation
        EquationChoiceNode equationChoiceNode = new EquationChoiceNode( model.getEquations(), model.getCurrentEquationProperty() );
        addChild( equationChoiceNode );

        // equation, in formula format
        final EquationNode equationNode = new EquationNode( model.getCurrentEquationProperty(), model.getCoefficientsRange(), true );
        addChild( equationNode );

        // boxes that show molecules corresponding to the equation coefficients
        BoxesNode boxesNode = new BoxesNode(  model.getCurrentEquationProperty(), model.getCoefficientsRange(), new Dimension( 300, 180 ) /* boxSize */ );
        addChild( boxesNode );

        // control for choosing the visual representation of "balanced"
        BalanceChoiceNode balanceChoiceNode = new BalanceChoiceNode( balanceChoiceProperty );
        addChild( balanceChoiceNode );

        // bar charts representation of "balanced"
        final BarChartsNode barChartsNode = new BarChartsNode( model.getCurrentEquationProperty() );
        addChild( barChartsNode );

        // balance scales representation of "balanced"
        final BalanceScalesNode balanceScalesNode = new BalanceScalesNode( model.getCurrentEquationProperty() );
        addChild( balanceScalesNode );

        // smiley face, for showing when equation is balanced
        BalanceEquationFaceNode faceNode = new BalanceEquationFaceNode( model.getCurrentEquationProperty() );
        addChild( faceNode );

        // Reset All button
        ResetAllButtonNode resetAllButtonNode = new ResetAllButtonNode( resettable, parentFrame, 12, Color.BLACK, Color.WHITE );
        resetAllButtonNode.setConfirmationEnabled( false );
        addChild( resetAllButtonNode );

        // layout
        double x = 0;
        double y = 0;
        final double ySpacing = 15;
        equationChoiceNode.setOffset( x, y );
        y = equationChoiceNode.getFullBoundsReference().getMaxY() + ySpacing;
        equationNode.setOffset( x, y );
        y = equationNode.getFullBoundsReference().getMaxY() + ySpacing;
        boxesNode.setOffset( x, y );
        y = boxesNode.getFullBoundsReference().getMaxY() + ySpacing;
        balanceChoiceNode.setOffset( x, y );
        x = equationChoiceNode.getFullBoundsReference().getMinX() - PNodeLayoutUtils.getOriginXOffset( barChartsNode );
        y = balanceChoiceNode.getFullBoundsReference().getMaxY() + 100;
        barChartsNode.setOffset( x, y );
        x = equationChoiceNode.getFullBoundsReference().getMinX() - PNodeLayoutUtils.getOriginXOffset( balanceScalesNode );
        y = balanceChoiceNode.getFullBoundsReference().getMaxY() + 100;
        balanceScalesNode.setOffset( x, y );
        x = boxesNode.getFullBoundsReference().getCenterX() - ( resetAllButtonNode.getFullBoundsReference().getWidth() / 2 );
        y = barChartsNode.getFullBoundsReference().getMaxY() + ySpacing;
        resetAllButtonNode.setOffset( x, y );
        x = boxesNode.getFullBoundsReference().getCenterX() - ( faceNode.getFullBoundsReference().getWidth() / 2 );
        y = boxesNode.getFullBoundsReference().getCenterY() - ( faceNode.getFullBoundsReference().getHeight() / 2 );
        faceNode.setOffset( x, y );

        // observers
        balanceChoiceProperty.addObserver( new SimpleObserver() {
            public void update() {
                barChartsNode.setVisible( balanceChoiceProperty.getValue().equals( BalanceChoice.BAR_CHARTS ) );
                balanceScalesNode.setVisible( balanceChoiceProperty.getValue().equals( BalanceChoice.BALANCE_SCALES ) );
            }
        } );
    }

    public void reset() {
        balanceChoiceProperty.reset();
    }
}
