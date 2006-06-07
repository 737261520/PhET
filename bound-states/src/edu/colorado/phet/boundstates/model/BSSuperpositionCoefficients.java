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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * BSSuperpositionCoefficients is a collection of superposition coefficients.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class BSSuperpositionCoefficients extends BSObservable {
   
    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------
    
    private static final double NORMALIZATION_ERROR = 0.00001;
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private ArrayList _coefficients; // array of Double
   
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    /**
     * Constructor, creates an empty set of coefficients.
     */
    public BSSuperpositionCoefficients() {
        _coefficients = new ArrayList();
    }
    
    /**
     * Copy constructor.
     * 
     * @param sc
     */
    public BSSuperpositionCoefficients( BSSuperpositionCoefficients sc ) {
        this();
        _coefficients.clear();
        Iterator i = sc._coefficients.iterator();
        while( i.hasNext() ) {
            _coefficients.add( i.next() );
        }
    }
    
    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    /**
     * Sets the number of coefficients.
     * <p>
     * If there are no coefficients, the first one is set to 1 and all others to zero.
     * <p>
     * If the number of coefficients is increasing, the new ones are set to zero.
     * <p>
     * If the number of coefficients is decreasing, then we need to examine the 
     * coefficient values. If any non-zero coefficients will be lost, then 
     * the first coefficient is set to 1 and all others to zero.
     * If only zero-valued coefficients will be lost, then they can
     * simply be deleted without changing the values of any of the
     * non-zero coefficients.
     * 
     * @param numberOfCoefficients
     */
    public void setNumberOfCoefficients( int numberOfCoefficients ) {
        if ( numberOfCoefficients < 0 ) {
            throw new IllegalArgumentException( "numberOfCoefficients must be >= 0: " + numberOfCoefficients );
        }
        
        final int currentNumberOfCoefficients = getNumberOfCoefficients();
        
        if ( numberOfCoefficients == currentNumberOfCoefficients ) {
            // no change, do nothing
        }
        else {
            if ( numberOfCoefficients == 0 ) {
                _coefficients.clear();
            }
            else if ( currentNumberOfCoefficients == 0 ) {
                // If we have no coefficients yet, then set the first coefficient to 1 and all the others to 0.
                _coefficients.clear();
                _coefficients.add( new Double( 1 ) );
                for ( int i = 1; i < numberOfCoefficients; i++ ) {
                    _coefficients.add( new Double( 0 ) );
                }
            }
            else if ( numberOfCoefficients > currentNumberOfCoefficients ) {
                // If the number of eigenstates is increasing, add new ones with zero values.
                for ( int i = getNumberOfCoefficients(); i < numberOfCoefficients; i++ ) {
                    _coefficients.add( new Double( 0 ) );
                }
            }
            else  { 
                /* 
                 * The number of coefficients is decreasing.
                 * If we lose any non-zero coefficients, renormalize the remaining coefficients.
                 * If the remaining coefficients are all zero, then set the lowest one to 1.
                 */
                boolean needToNormalize = false;
                for ( int i = currentNumberOfCoefficients - 1; i >= numberOfCoefficients; i-- ) {
                    double coefficient = ((Double)_coefficients.get( i ) ).doubleValue();
                    if ( coefficient != 0 ) {
                        needToNormalize = true;
                    }
                    _coefficients.remove( i );
                }
                
                if ( needToNormalize ) {
                    if ( getSum() == 0 ) {
                        if ( numberOfCoefficients > 0 ) {
                            _coefficients.set( 0, new Double( 1 ) );
                        }
                    }
                    else {
                        normalize();
                    }
                }
            }
            
            notifyObservers();
        }
    }
    
    /**
     * Gets the number of coefficients (possibly zero).
     * 
     * @return
     */
    public int getNumberOfCoefficients() {
        return _coefficients.size();
    }
    
    /**
     * Gets the number of non-zero coefficients.
     * 
     * @return
     */
    public int getNumberOfNonZeroCoefficients() {
        int count = 0;
        Iterator i = _coefficients.iterator();
        while( i.hasNext() ) {
            Double coefficient = (Double)i.next();
            if ( coefficient.doubleValue() != 0 ) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets a coefficient's value.
     * 
     * @param index
     * @return the value, between 0 and 1 inclusive
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getCoefficient( int index ) {
        if ( index > _coefficients.size() - 1 ) {
            throw new IndexOutOfBoundsException( "index is out of bounds: " + index );
        }
        return ((Double)_coefficients.get( index )).doubleValue();
    }
    
    /**
     * Sets a coefficient's value.
     * 
     * @param index
     * @param value a value between 0 and 1 inclusive
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @throws IllegalArgumentException if value isn't between 0 and 1
     */
    public void setCoefficient( int index, double value ) {
        if ( index > _coefficients.size() - 1 ) {
            throw new IndexOutOfBoundsException( "index is out of bounds: " + index );
        }
        if ( value < 0 || value > 1 ) {
            throw new IllegalArgumentException( "value must be between 0 and 1: " + value );
        }
        _coefficients.set( index, new Double( value ) );
        notifyObservers();
    }
    
    /**
     * Sets one coefficient to 1, all others to zero.
     * 
     * @param index
     */
    public void setCoefficient( int index ) {
        if ( index > _coefficients.size() - 1 ) {
            throw new IndexOutOfBoundsException( "index is out of bounds: " + index );
        }
        int numberOfCoefficients = _coefficients.size();
        _coefficients.clear();
        for ( int i = 0; i < numberOfCoefficients; i++ ) {
            if ( i == index ) {
                _coefficients.add( new Double( 1 ) );
            }
            else {
                _coefficients.add( new Double( 0 ) );
            }
        }
        notifyObservers();
    }
    
    /**
     * Normalizes the coefficients, such that
     * c1^2 + c2^2 + ... + cn^2 = 1
     */
    public void normalize() {
        final double sumOfSquares = getSumOfSquares();
        if ( sumOfSquares != 0 ) {
            int numberOfCoefficients = _coefficients.size();
            for ( int i = 0; i < numberOfCoefficients; i++ ) {
                final double coefficient = ((Double) _coefficients.get( i ) ).doubleValue();
                double normalizedValue = Math.sqrt( ( coefficient * coefficient ) / sumOfSquares );
                _coefficients.set( i, new Double( normalizedValue ) );
            }
        }
    }
    
    /**
     * Are the coefficients normalized?
     * Does c1^2 + c2^2 + ... + cn^2 = 1 ?
     * 
     * @return true or false
     */
    public boolean isNormalized() {
        final double sumOfSquares = getSumOfSquares();
        return ( sumOfSquares > 0 && sumOfSquares < 1 + NORMALIZATION_ERROR && sumOfSquares > 1 - NORMALIZATION_ERROR );
    }
    
    /**
     * Gets the sum of all the coefficients.
     * 
     * @return
     */
    public double getSum() {
        double total = 0;
        Iterator i = _coefficients.iterator();
        while ( i.hasNext() ) {
            double coefficient = ((Double)i.next()).doubleValue();
            total += coefficient;
        }
        return total;
    }
    
    /*
     * Gets the sum of squares of all the coefficients
     * (c1^2 + c2^2 + ... + cn^2).
     * 
     * @return
     */
    private double getSumOfSquares() {
        double total = 0;
        Iterator i = _coefficients.iterator();
        while ( i.hasNext() ) {
            double coefficient = ((Double)i.next()).doubleValue();
            total += ( coefficient * coefficient );
        }
        return total;
    }
    
    /**
     * Determines whether this set of coefficients represents a superposition state.
     * We are in a superposition state if we have at least 2 non-zero coefficients.
     * 
     * @return true or false
     */
    public boolean isSuperpositionState() {
        return ( getNumberOfNonZeroCoefficients() > 1 );
    }
}
