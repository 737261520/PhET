/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.fourier.enum;


/**
 * WaveType is a typesafe enumueration of "wave type" values.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class WaveType extends FourierEnum {

    /* This class is not intended for instantiation. */
    private WaveType( String name ) {
        super( name );
    }
    
    // Wave Type values
    public static final WaveType UNDEFINED = new WaveType( "undefined" );
    public static final WaveType SINES = new WaveType( "sines" );
    public static final WaveType COSINES = new WaveType( "cosines" );
}