/**
 * Created by IntelliJ IDEA.
 * User: Another Guy
 * Date: Feb 19, 2003
 * Time: 2:48:01 PM
 * To change this template use Options | File Templates.
 */
package edu.colorado.phet.idealgas.controller;

import edu.colorado.phet.common.math.Vector2D;
import edu.colorado.phet.idealgas.model.GasMolecule;
import edu.colorado.phet.idealgas.model.HollowSphere;
import edu.colorado.phet.idealgas.model.IdealGasModel;
import edu.colorado.phet.idealgas.model.SphericalBody;
import edu.colorado.phet.mechanics.Body;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class HotAirBalloon extends HollowSphere {

    private Rectangle2D.Double opening;
    private double theta;
    private IdealGasModel model;
    private double oxOffset;
    private double oyOffset;
    private double heatSource;

    public HotAirBalloon( Point2D center,
                          Vector2D velocity,
                          Vector2D acceleration,
                          double mass,
                          double radius,
                          double openingAngle,
                          IdealGasModel model ) {
        super( center, velocity, acceleration, mass, radius );
        theta = openingAngle;
        this.model = model;
        setOpening();
    }

    public double getOpeningAngle() {
        return theta;
    }

    public void setOpeningAngle( double theta ) {
        this.theta = theta;

        // Set the current location of the opening. It moves with the balloon
        double angle = theta * Math.PI / 180;
        oxOffset = getRadius() * Math.sin( angle / 2 );
        oyOffset = getRadius() * Math.cos( angle / 2 );
        setOpening();
    }

    public void stepInTime( double dt ) {

        super.stepInTime( dt );

        // Set the current location of the opening. It moves with the balloon
        double angle = theta * Math.PI / 180;
        oxOffset = getRadius() * Math.sin( angle / 2 );
        oyOffset = getRadius() * Math.cos( angle / 2 );
        setOpening();

        // Add heat to the bodies contained in the balloon.
        if( heatSource != 0 ) {
            List bodies = model.getBodies();
            for( int i = 0; i < bodies.size(); i++ ) {
                Body body = (Body)bodies.get( i );
                if( body instanceof GasMolecule && this.contains( (GasMolecule)body ) ) {
                    body.setVelocity( body.getVelocity().scale( 1 + heatSource / 1000 ) );
                }
            }
        }
    }

    private boolean contains( GasMolecule molecule ) {
        return molecule.getPosition().distanceSq( this.getPosition() )
               <= ( this.getRadius() + molecule.getRadius() ) * ( this.getRadius() + molecule.getRadius() );
    }

    public Rectangle2D getOpening() {
        return opening;
    }

    private void setOpening() {
        double o2x = getPosition().getX() + oxOffset;
        double o1x = getPosition().getX() - oxOffset;
        double o1y = getPosition().getY() + oyOffset;
        opening = new Rectangle2D.Double( o1x, o1y, o2x - o1x, 20 );
    }

    public boolean isInOpening( SphericalBody particle ) {
        double px = particle.getPosition().getX();
        double py = particle.getPosition().getY();
        boolean b = px - particle.getRadius() >= opening.getMinX()
                    && px + particle.getRadius() <= opening.getMaxX()
                    && py + particle.getRadius() >= opening.getMinY();
        return b;
    }

    public void setHeatSource( double value ) {
        heatSource = value;
    }

    public double getHeatSource() {
        return heatSource;
    }
}
