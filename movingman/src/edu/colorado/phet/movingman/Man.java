/*PhET, 2004.*/
package edu.colorado.phet.movingman;

import edu.colorado.phet.movingman.common.AutomatedObservable;

/**
 * User: Sam Reid
 * Date: Jun 30, 2003
 * Time: 12:25:22 AM
 * Copyright (c) Jun 30, 2003 by Sam Reid
 */
public class Man extends AutomatedObservable {
    private double x;
    private double x0;
    private boolean grabbed = false;
    private double velocity;
    private double acceleration;
    private double min;
    private double max;

    public Man( double x, double min, double max ) {
        this.x0 = x;
        this.x = x;
        this.min = min;
        this.max = max;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity( double velocity ) {
        this.velocity = velocity;
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public void setGrabbed( boolean grabbed ) {
        this.grabbed = grabbed;
    }

    public double getX() {
        return x;
    }

    public void setX( double x ) {
        if( x < min ) {
            x = min;
        }
        else if( x > max ) {
            x = max;
        }
        this.x = x;
        updateObservers();
    }

    public void reset() {
        this.x = x0;
        setVelocity( 0 );
        updateObservers();
    }

    public void stepInTime( double dt ) {
        double newVelocity = velocity + acceleration * dt;
        double newX = x + velocity * dt;

        newX = Math.min( newX, max );
        newX = Math.max( newX, min );
//        if( x == module.getMaxManPosition() ) {
//            module.getMan().setVelocity( 0 );
//        }
//        if( x == -module.getMaxManPosition() ) {
//            module.getMan().setVelocity( 0 );
//        }
        setVelocity( newVelocity );
        setX( newX );
    }

    public void setAcceleration( double acceleration ) {
        this.acceleration = acceleration;
    }
}
