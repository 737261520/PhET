/**
 * Class: CollimatedBeam
 * Package: edu.colorado.phet.lasers.physics.photon
 * Author: Another Guy
 * Date: Mar 21, 2003
 */
package edu.colorado.phet.lasers.physics.photon;

import edu.colorado.phet.physics.Vector2D;
import edu.colorado.phet.physics.body.Particle;
import edu.colorado.phet.controller.command.AddParticleCmd;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

/**
 * A CollimatedBeam is a collection of photons that all have identical
 * velocities. The beam has a height, and the photons are randomly distributed
 * across that height.
 */
public class CollimatedBeam extends Particle {

    private int wavelength;
    private Point2D.Float origin;
    private float height;
    private float width;
    private Vector2D velocity;
    private ArrayList photons = new ArrayList();
    // The rate at which the beam produces photons
    private float timeSinceLastPhotonProduced = 0;
    // Used to deterimine when photons should be produced
    private float photonsPerSecond = 30;
    // Is the collimated beam currently generating photons?
    private boolean isActive;


    /**
     *
     * @param wavelength
     * @param origin
     * @param height
     * @param width
     */
    public CollimatedBeam( int wavelength, Point2D.Float origin, float height, float width, Vector2D direction ) {
        this.wavelength = wavelength;
        this.origin = origin;
        this.height = height;
        this.width = width;
        this.velocity = new Vector2D( direction ).normalize().multiply( Photon.s_speed );
    }

    public Point2D.Float getOrigin() {
        return origin;
    }

    public void setOrigin( Point2D.Float origin ) {
        this.origin = origin;
    }

    /**
     *
     * @return
     */
    public float getHeight() {
        return height;
    }

    /**
     *
     * @param height
     */
    public void setHeight( float height ) {
        this.height = height;
    }

    /**
     *
     * @return
     */
    public float getWidth() {
        return width;
    }

    /**
     *
     * @param width
     */
    public void setWidth( float width ) {
        this.width = width;
    }

    /**
     *
     * @return
     */
    public float getPhotonsPerSecond() {
        return photonsPerSecond;
    }

    /**
     *
     * @param photonsPerSecond
     */
    public void setPhotonsPerSecond( float photonsPerSecond ) {

        // The following if statement prevents the system from sending out a big
        // wave of photons if it has been set at a rate of 0 for awhile.
        if( this.photonsPerSecond == 0 ) {
            timeSinceLastPhotonProduced = 0;
        }
        this.photonsPerSecond = photonsPerSecond;
        nextTimeToProducePhoton = getNextTimeToProducePhoton();
    }

    /**
     *
     * @return
     */
    public int getWavelength() {
        return wavelength;
    }

    /**
     *
     */
    public void addPhoton() {
        Photon newPhoton = Photon.create( this );
        newPhoton.setPosition( genPositionX(), genPositionY() + newPhoton.getRadius() );
        newPhoton.setVelocity( new Vector2D( velocity ) );
        newPhoton.setWavelength( this.wavelength );
        new AddParticleCmd( newPhoton ).doIt();
        photons.add( newPhoton );
    }

    /**
     *
     * @param photon
     */
    public void removePhoton( Photon photon ) {
        photons.remove( photon );
    }

    /**
     *
     */
    private float nextTimeToProducePhoton = 0;

    public void stepInTime( float dt ) {

        super.stepInTime( dt );

        // Produce photons
        if( isActive() ) {
            timeSinceLastPhotonProduced += dt;
            int numPhotons = (int)( photonsPerSecond * timeSinceLastPhotonProduced );
//            for( int i = 0; i < numPhotons; i++ ) {
            if( nextTimeToProducePhoton < timeSinceLastPhotonProduced ) {
                timeSinceLastPhotonProduced = 0;
                this.addPhoton();
                nextTimeToProducePhoton = getNextTimeToProducePhoton();
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     *
     * @param active
     */
    public void setActive( boolean active ) {
        isActive = active;
        timeSinceLastPhotonProduced = 0;
    }

    /**
     *
     * @return
     */
    private float genPositionY() {
        float yDelta = velocity.getX() != 0 ? (float)Math.random() * height : 0;
        return this.getPosition().getY() + yDelta;
    }

    /**
     *
     * @return
     */
    private float genPositionX() {
        float xDelta = velocity.getY() != 0 ? (float)Math.random() * width : 0;
        return this.getPosition().getX() + xDelta;
    }

    private Random gaussianGenerator = new Random();
    private float getNextTimeToProducePhoton() {
        double temp = ( gaussianGenerator.nextGaussian() + 1.0 );
        return (float)temp / photonsPerSecond;
    }

}
