/* Copyright 2006, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.boundstates.draghandles;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;

import edu.colorado.phet.boundstates.BSConstants;
import edu.colorado.phet.boundstates.model.BSAsymmetricPotential;
import edu.colorado.phet.boundstates.model.BSCoulomb1DPotential;
import edu.colorado.phet.boundstates.module.BSPotentialSpec;
import edu.colorado.phet.boundstates.view.BSCombinedChartNode;
import edu.colorado.phet.common.view.util.SimStrings;

/**
 * BSAsymmetricOffsetHandle is the drag handle used to control the 
 * offset attribute of a potential composed of a single asymmetric well.
 * <p>
 * The handle is at the bottom left corner of the well.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class BSAsymmetricOffsetHandle extends BSPotentialHandle {
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    /**
     * Constructor.
     * 
     * @param potential
     * @param potentialSpec used to get the range of the attribute controlled
     * @param chartNode
     */
    public BSAsymmetricOffsetHandle( BSAsymmetricPotential potential, 
            BSPotentialSpec potentialSpec, BSCombinedChartNode chartNode ) {
        super( potential, potentialSpec, chartNode, BSAbstractHandle.VERTICAL );
        
        int significantDecimalPlaces = potentialSpec.getOffsetRange().getSignificantDecimalPlaces();
        String numberFormat = createNumberFormat( significantDecimalPlaces );
        setValueNumberFormat( numberFormat );
        setValuePattern( SimStrings.get( "drag.offset" ) );
        
        updateDragBounds();
    }
    
    //----------------------------------------------------------------------------
    // AbstractDragHandle implementation
    //----------------------------------------------------------------------------
    
    /**
     * Updates the drag bounds.
     */
    public void updateDragBounds() {
        
        BSAsymmetricPotential potential = (BSAsymmetricPotential)getPotential();
        BSPotentialSpec spec = getPotentialSpec();
        BSCombinedChartNode chartNode = getChartNode();
        
        if ( potential.getCenter() != 0 || potential.getNumberOfWells() != 1 ) {
            throw new UnsupportedOperationException( "this implementation only supports 1 well centered at 0" );
        }
        
        //  position -> x coordinates
        final double minPosition = BSConstants.POSITION_VIEW_RANGE.getLowerBound();
        final double maxPosition = BSConstants.POSITION_VIEW_RANGE.getUpperBound();
        final double minX = chartNode.positionToNode( minPosition );
        final double maxX = chartNode.positionToNode( maxPosition );
        
        // energy -> y coordinates (+y is down!)
        final double minEnergy = spec.getOffsetRange().getMin();
        final double maxEnergy =  spec.getOffsetRange().getMax();
        final double minY = chartNode.energyToNode( maxEnergy );
        final double maxY = chartNode.energyToNode( minEnergy );
        
        // bounds, local coordinates
        final double w = maxX - minX;
        final double h = maxY - minY;
        Rectangle2D dragBounds = new Rectangle2D.Double( minX, minY, w, h );

        // Convert to global coordinates
        dragBounds = chartNode.localToGlobal( dragBounds );

        setDragBounds( dragBounds );
        updateView();
    }
    
    /**
     * Updates the model to match the drag handle.
     */
    protected void updateModel() {

        BSAsymmetricPotential potential = (BSAsymmetricPotential)getPotential();
        BSCombinedChartNode chartNode = getChartNode();
        
        potential.deleteObserver( this );
        {
            Point2D globalNodePoint = getGlobalPosition();
            Point2D localNodePoint = chartNode.globalToLocal( globalNodePoint );
            Point2D modelPoint = chartNode.nodeToEnergy( localNodePoint );
            final double offset = modelPoint.getY();
            potential.setOffset( offset );
            setValueDisplay( offset );
        }
        potential.addObserver( this );
    }

    /**
     * Updates the drag handle to match the model.
     */
    protected void updateView() {

        BSAsymmetricPotential potential = (BSAsymmetricPotential)getPotential();
        BSCombinedChartNode chartNode = getChartNode();
        
        removePropertyChangeListener( this );
        {
            final double position = -( potential.getWidth() / 2 ); // handle in center of well
            final double offset = potential.getOffset(); // handle at bottom of well
            Point2D modelPoint = new Point2D.Double( position, offset );
            Point2D localNodePoint = chartNode.energyToNode( modelPoint );
            Point2D globalNodePoint = chartNode.localToGlobal( localNodePoint );
            setGlobalPosition( globalNodePoint );
            setValueDisplay( offset );
        }
        addPropertyChangeListener( this );
    }
}
