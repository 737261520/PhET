/*Copyright, Sam Reid, 2003.*/
package edu.colorado.phet.semiconductor.common;

import edu.colorado.phet.common.math.PhetVector;
import edu.colorado.phet.common.model.ModelElement;
import edu.colorado.phet.common.model.simpleobservable.SimpleObservable;

/**
 * User: Sam Reid
 * Date: Dec 31, 2003
 * Time: 2:48:10 PM
 * Copyright (c) Dec 31, 2003 by Sam Reid
 */
public class Particle extends SimpleObservable implements ModelElement {
    PhetVector position;
    PhetVector velocity;
    PhetVector acceleration;

    public Particle( double x, double y ) {
        this.position = new PhetVector( x, y );
        this.velocity = new PhetVector();
        this.acceleration = new PhetVector();
    }

    public Particle( PhetVector position ) {
        this( position.getX(), position.getY() );
    }

    public PhetVector getPosition() {
        return position;
    }

    public void stepInTime( double dt ) {
        //acceleration doesn't change here.
        PhetVector dv = acceleration.getScaledInstance( dt );
        this.velocity = velocity.getAddedInstance( dv );
        PhetVector dx = velocity.getScaledInstance( dt );
        this.position = position.getAddedInstance( dx );
        updateObservers();
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public void setPosition( double x, double y ) {
        this.position = new PhetVector( x, y );
    }

    public void translate( double dx, double dy ) {
        setPosition( getX() + dx, getY() + dy );
    }

}
