/** Sam Reid*/
package edu.colorado.phet.cck3.circuit.components;

import edu.colorado.phet.cck3.circuit.Junction;
import edu.colorado.phet.cck3.circuit.KirkhoffListener;
import edu.colorado.phet.common.math.AbstractVector2D;

import java.awt.geom.Point2D;

/**
 * User: Sam Reid
 * Date: May 28, 2004
 * Time: 1:11:39 PM
 * Copyright (c) May 28, 2004 by Sam Reid
 */
public class Battery extends CircuitComponent {
    public static final double MIN_RESISTANCE = 0.0001;

    public Battery( Point2D start, AbstractVector2D dir, double length, double height, KirkhoffListener kl ) {
        this( start, dir, length, height, kl, MIN_RESISTANCE );
    }

    public Battery( Point2D start, AbstractVector2D dir, double length, double height, KirkhoffListener kl, double internalResistance ) {
        super( kl, start, dir, length, height );
        setVoltageDrop( 9.0 );
        setResistance( internalResistance );
    }

    public Battery( KirkhoffListener kl, Junction startJunction, Junction endjJunction, double length, double height, double internalResistance ) {
        super( kl, startJunction, endjJunction, length, height );
        setVoltageDrop( 9.0 );
        setResistance( internalResistance );
    }

    public void setVoltageDrop( double voltageDrop ) {
        super.setVoltageDrop( voltageDrop );
        super.fireKirkhoffChange();
    }

    public double getEffectiveVoltageDrop() {
        return getVoltageDrop() - getCurrent() * getResistance();
    }

    public void setResistance( double resistance ) {
        if( resistance < MIN_RESISTANCE ) {
            throw new IllegalArgumentException( "Resistance was les than the min, value=" + resistance + ", min=" + MIN_RESISTANCE );
        }
        super.setResistance( resistance );
    }
}
