/* Copyright 2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.faraday.model;

import edu.colorado.phet.common.math.AbstractVector2D;
import edu.colorado.phet.common.model.ModelElement;
import edu.colorado.phet.common.util.SimpleObserver;


/**
 * AbstractCompass is the base class for all compass models.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public abstract class AbstractCompass extends SpacialObservable implements ModelElement, SimpleObserver {
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private AbstractMagnet _magnetModel;
    private AbstractVector2D _fieldStrength;
    
    //----------------------------------------------------------------------------
    // Constructors & finalizers
    //----------------------------------------------------------------------------
    
    /**
     * Sole constructor.
     * 
     * @param magnetModel the magnet that the compass is observing
     */
    public AbstractCompass( AbstractMagnet magnetModel ) {
        super();
        
        _magnetModel = magnetModel;
        _magnetModel.addObserver( this );
        
        _fieldStrength = _magnetModel.getStrength( getLocation() );
        setDirection( Math.toDegrees( _fieldStrength.getAngle() ) );
    }
    
    /**
     * Finalizes an instance of this type.
     * Call this method prior to releasing all references to an object of this type.
     */
    public void finalize() {
        _magnetModel.removeObserver( this );
        _magnetModel = null;
    }
    
    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------

    /**
     * Gets the strength of the magnetic field at the compass location.
     * 
     * @return the field strength vector
     */
    public AbstractVector2D getFieldStrength() {
        return _magnetModel.getStrength( getLocation() );
    }
    
    //----------------------------------------------------------------------------
    // ModelElement implementation
    //----------------------------------------------------------------------------
    
    /**
     * @see edu.colorado.phet.common.model.ModelElement#stepInTime(double)
     */
    public abstract void stepInTime( double dt );
    
    //----------------------------------------------------------------------------
    // SimpleObserver implementation
    //----------------------------------------------------------------------------
    
    /**
     * @see edu.colorado.phet.common.util.SimpleObserver#update()
     */
    public void update() {
        // Do nothing, handled by stepInTime.
    }
    
    //----------------------------------------------------------------------------
    // Object overrides
    //----------------------------------------------------------------------------
    
    /**
     * Provides a string representation of this object.
     * Do not write code that relies on the format of this string.
     * <p>
     * Since this class is abstract, it is the subclass' responsibility
     * to include this information in its toString method.
     * 
     * @return string representation
     */
    public String toString() {
        return
            "location=" + getLocation() + 
            " direction=" + getDirection() +
            " B=" + _fieldStrength.getMagnitude() +
            " Bx=" + _fieldStrength.getX() +
            " By=" + _fieldStrength.getY() +
            " B0=" + Math.toDegrees(_fieldStrength.getAngle());
    }
}