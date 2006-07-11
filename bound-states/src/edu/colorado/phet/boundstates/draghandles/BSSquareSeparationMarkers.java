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

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Observable;
import java.util.Observer;

import org.jfree.chart.axis.ValueAxis;

import edu.colorado.phet.boundstates.BSConstants;
import edu.colorado.phet.boundstates.color.BSColorScheme;
import edu.colorado.phet.boundstates.model.BSSquarePotential;
import edu.colorado.phet.boundstates.view.BSCombinedChartNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * BSSquareSeparationMarkers
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class BSSquareSeparationMarkers extends PComposite implements Observer {

    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------
    
    private static final Color DEFAULT_COLOR = Color.BLACK;
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private BSSquarePotential _potential;
    private BSCombinedChartNode _chartNode;
    private PPath _leftNode, _rightNode;
    private GeneralPath _leftPath, _rightPath;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    public BSSquareSeparationMarkers( BSSquarePotential potential, BSCombinedChartNode chartNode) {
        super();
 
        _chartNode = chartNode;
        
        _leftPath = new GeneralPath();
        _leftNode = new PPath();
        _leftNode.setStroke( BSConstants.DRAG_HANDLE_MARKERS_STROKE );
        _leftNode.setStrokePaint( DEFAULT_COLOR );
        addChild( _leftNode );
        
        _rightPath = new GeneralPath();
        _rightNode = new PPath();
        _rightNode.setStroke( BSConstants.DRAG_HANDLE_MARKERS_STROKE );
        _rightNode.setStrokePaint( DEFAULT_COLOR );
        addChild( _rightNode );
        
        setPotential( potential );
    }
    
    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    public void setPotential( BSSquarePotential potential ) {
        if ( _potential != null ) {
            _potential.deleteObserver( this );
        }
        _potential = potential;
        _potential.addObserver( this );
        updateView();
    }
    
    public void setColorScheme( BSColorScheme colorScheme ) {
        _leftNode.setStrokePaint( colorScheme.getDragHandleMarkersColor() );
        _rightNode.setStrokePaint( colorScheme.getDragHandleMarkersColor() );
    }
    
    //----------------------------------------------------------------------------
    // Overrides
    //----------------------------------------------------------------------------
    
    public void setVisible( boolean visible ) {
        super.setVisible( visible );
        updateView();
    }
    
    //----------------------------------------------------------------------------
    // Updaters
    //----------------------------------------------------------------------------
    
    public void updateView() {
        assert( _potential.getCenter() == 0 );

        _leftPath.reset();
        _rightPath.reset();

        final int n = _potential.getNumberOfWells();
        if ( n > 1 ) {

            final double separation = _potential.getSeparation();
            double positionLeft = 0;
            double positionRight = 0;
            if ( n % 2 == 0 ) {
                // even number of wells
                positionLeft = -( separation / 2 );
                positionRight = +( separation / 2 );
            }
            else {
                // odd number of wells
                final double width = _potential.getWidth();
                positionLeft = ( width / 2 );
                positionRight = ( width / 2 ) + separation;
            }

            ValueAxis yAxis = _chartNode.getEnergyPlot().getRangeAxis();
            final double minEnergy = yAxis.getLowerBound();
            final double maxEnergy = yAxis.getUpperBound();
          
            final double offset = _potential.getOffset();
            final double height = _potential.getHeight();

            // Left marker path
            {
                Point2D modelPoint1 = new Point2D.Double( positionLeft, maxEnergy );
                Point2D nodePoint1 = _chartNode.energyToNode( modelPoint1 );
                Point2D globalPoint1 = _chartNode.localToGlobal( nodePoint1 );

                Point2D modelPoint2 = new Point2D.Double( positionLeft, offset + height );
                Point2D nodePoint2 = _chartNode.energyToNode( modelPoint2 );
                Point2D globalPoint2 = _chartNode.localToGlobal( nodePoint2 );

                _leftPath.moveTo( (float) globalPoint1.getX(), (float) globalPoint1.getY() );
                _leftPath.lineTo( (float) globalPoint2.getX(), (float) globalPoint2.getY() );
            }

            // Right marker path
            {
                Point2D modelPoint1 = new Point2D.Double( positionRight, maxEnergy );
                Point2D nodePoint1 = _chartNode.energyToNode( modelPoint1 );
                Point2D globalPoint1 = _chartNode.localToGlobal( nodePoint1 );

                Point2D modelPoint2 = new Point2D.Double( positionRight, offset + height );
                Point2D nodePoint2 = _chartNode.energyToNode( modelPoint2 );
                Point2D globalPoint2 = _chartNode.localToGlobal( nodePoint2 );

                _rightPath.moveTo( (float) globalPoint1.getX(), (float) globalPoint1.getY() );
                _rightPath.lineTo( (float) globalPoint2.getX(), (float) globalPoint2.getY() );
            }
        }

        _leftNode.setPathTo( _leftPath );
        _rightNode.setPathTo( _rightPath );
    }

    //----------------------------------------------------------------------------
    // Observer implementation
    //----------------------------------------------------------------------------

    /**
     * Updates the view when the model changes.
     * @param o
     * @param arg
     */
    public void update( Observable o, Object arg ) {
        assert( o == _potential );
        updateView();
    }
}
