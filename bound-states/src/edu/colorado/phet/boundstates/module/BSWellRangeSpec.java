/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.boundstates.module;

import edu.colorado.phet.boundstates.control.ZoomControl.ZoomSpec;
import edu.colorado.phet.boundstates.util.AxisSpec;
import edu.colorado.phet.boundstates.util.DoubleRange;

/**
 * BSWellRangeSpec is the specification of ranges for a well type.
 * It contains the union of all well attributes; irrelevant attributes are set to null.
 * Use the inner subclasses to create a specification for a specific well type.
 * Instances are immutable.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class BSWellRangeSpec {
    
    //----------------------------------------------------------------------------
    // Subclasses for each well type
    //----------------------------------------------------------------------------
    
    public static class Asymmetric extends BSWellRangeSpec {
        public Asymmetric( ZoomSpec energyZoomSpec, DoubleRange offsetRange, DoubleRange widthRange, DoubleRange heightRange ) {
            super( energyZoomSpec, offsetRange, null, widthRange, heightRange, null, null );
        }
    }
    
    public static class Coulomb1D extends BSWellRangeSpec {
        public Coulomb1D( ZoomSpec energyZoomSpec, DoubleRange offsetRange, DoubleRange spacingRange ) {
            super( energyZoomSpec, offsetRange, spacingRange, null, null, null, null );
        }
    }
    
    public static class Coulomb3D extends BSWellRangeSpec {
        public Coulomb3D( ZoomSpec energyZoomSpec, DoubleRange offsetRange ) {
            super( energyZoomSpec, offsetRange, null, null, null, null, null );
        }
    }
    
    public static class HarmonicOscillator extends BSWellRangeSpec {
        public HarmonicOscillator( ZoomSpec energyZoomSpec, DoubleRange offsetRange, DoubleRange angularFrequencyRange ) {
            super( energyZoomSpec, offsetRange, null, null, null, null, angularFrequencyRange );
        }
    }
    
    public static class Square extends BSWellRangeSpec {
        public Square( ZoomSpec energyZoomSpec, DoubleRange offsetRange, DoubleRange widthRange, DoubleRange heightRange, DoubleRange separationRange ) {
            super( energyZoomSpec, offsetRange, null, widthRange, heightRange, separationRange, null );
        }
    }

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private ZoomSpec _energyZoomSpec;
    private DoubleRange _offsetRange;
    private DoubleRange _spacingRange;
    private DoubleRange _widthRange;
    private DoubleRange _heightRange;
    private DoubleRange _separationRange;
    private DoubleRange _angularFrequencyRange;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    /* Do not instantiate, use inner classes */
    private BSWellRangeSpec( 
            ZoomSpec energyZoomSpec,
            DoubleRange offsetRange, DoubleRange spacingRange, 
            DoubleRange widthRange, DoubleRange heightRange,
            DoubleRange separationRange, DoubleRange angularFrequencyRange ) 
    {
        _energyZoomSpec = energyZoomSpec;
        _offsetRange = offsetRange;
        _spacingRange = spacingRange;
        _widthRange = widthRange;
        _heightRange = heightRange;
        _separationRange = separationRange;
        _angularFrequencyRange = angularFrequencyRange;
    }

    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    public ZoomSpec getEnergyZoomSpec() {
        return _energyZoomSpec;
    }
    
    public DoubleRange getHeightRange() {
        return _heightRange;
    }

    public DoubleRange getOffsetRange() {
        return _offsetRange;
    }
    
    public DoubleRange getWidthRange() {
        return _widthRange;
    }
    
    public DoubleRange getSpacingRange() {
        return _spacingRange;
    }
    
    public DoubleRange getAngularFrequencyRange() {
        return _angularFrequencyRange;
    }
    
    public DoubleRange getSeparationRange() {
        return _separationRange;
    }
}
