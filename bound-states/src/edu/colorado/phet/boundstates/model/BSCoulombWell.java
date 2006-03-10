/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.boundstates.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import edu.colorado.phet.boundstates.BSConstants;
import edu.colorado.phet.boundstates.enum.WellType;


/**
 * BSCoulombWell is the model of a potential composed of one or more Coulomb wells.
 * <p>
 * Our model supports these parameters:
 * <ul>
 * <li>number of wells
 * <li>spacing
 * <li>offset
 * </ul>
 * Offset and spacing are identical for each well.
 * Spacing is irrelevant if the number of wells is 1.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class BSCoulombWell extends BSAbstractPotential {
    
    public BSCoulombWell( int numberOfWells ) {
        this( numberOfWells, 
                BSConstants.DEFAULT_COULOMB_SPACING,
                BSConstants.DEFAULT_COULOMB_OFFSET, 
                BSConstants.DEFAULT_WELL_CENTER );
    }
    
    public BSCoulombWell( int numberOfWells, double spacing, double offset, double center ) {
        super( numberOfWells, spacing, offset, center );
    }

    public WellType getWellType() {
        return WellType.COULOMB;
    }

    //HACK 20 dummy eigenstates
    public BSEigenstate[] getEigenstates() {
        final int numberOfEigenstates = 20;
        BSEigenstate[] eigenstates = new BSEigenstate[ numberOfEigenstates ];
        final double maxEnergy = getOffset();
        final double minEnergy = getOffset() - 5;
        final double deltaEnergy = ( maxEnergy - minEnergy ) / numberOfEigenstates;
        for ( int i = 0; i < eigenstates.length; i++ ) {
            eigenstates[i] = new BSEigenstate( minEnergy + ( i * deltaEnergy ) );
        }
        return eigenstates;
    }

    public Point2D[] getPoints( double minX, double maxX, double dx ) {
        ArrayList points = new ArrayList();
        //HACK straight line at offset
        for ( double x = minX; x <= maxX; x += dx ) {
            points.add( new Point2D.Double( x, getOffset() ) );
        }
        // Convert to an array...
        return (Point2D[]) points.toArray( new Point2D.Double[points.size()] );
    }
}
