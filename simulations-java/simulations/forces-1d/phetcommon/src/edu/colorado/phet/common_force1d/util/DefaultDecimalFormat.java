/*  */
package edu.colorado.phet.common_force1d.util;

import java.text.DecimalFormat;

/**
 * This formatter never returns zero-values with a minus
 * sign prefix.  For example,
 * new DecimalFormat("0.0").format(-0)  returns "-0.0";
 * whereas
 * new DefaultDecimalFormat("0.0").format(-0) returns "0.0";
 */
public class DefaultDecimalFormat {
    private DecimalFormat decimalFormat;

    public DefaultDecimalFormat( String str ) {
        this( new DecimalFormat( str ) );
    }

    public DefaultDecimalFormat( DecimalFormat decimalFormat ) {
        this.decimalFormat = decimalFormat;
    }

    public String format( double val ) {
        String str = decimalFormat.format( val );
        if( Double.parseDouble( str ) == 0 && str.indexOf( "-" ) >= 0 ) {
            return format( 0 );
        }
        return str;
    }
}
