/**
 * Class: BaseLaserModule
 * Package: edu.colorado.phet.lasers.view
 * Author: Another Guy
 * Date: Mar 21, 2003
 * Latest Change:
 *      $Author$
 *      $Date$
 *      $Name$
 *      $Revision$
 */
package edu.colorado.phet.lasers.controller.module;

import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.common.math.Vector2D;
import edu.colorado.phet.lasers.controller.ApparatusConfiguration;
import edu.colorado.phet.lasers.controller.LaserConfig;
import edu.colorado.phet.lasers.model.LaserModel;
import edu.colorado.phet.lasers.model.atom.Atom;
import edu.colorado.phet.lasers.model.photon.CollimatedBeam;
import edu.colorado.phet.lasers.model.photon.Photon;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 */
public class MultipleAtomBaseModule extends BaseLaserModule {

    static protected final Point2D s_origin = LaserConfig.ORIGIN;
    static protected final double s_boxHeight = 150;
    static protected final double s_boxWidth = 500;
    static protected final double s_laserOffsetX = 100;

    private CollimatedBeam stimulatingBeam;
    private CollimatedBeam pumpingBeam;
    private double s_maxSpeed = .1;
    private ArrayList atoms;

    /**
     *
     */
    public MultipleAtomBaseModule( String title ) {
        super( title );
        stimulatingBeam = new CollimatedBeam( getLaserModel(),
                                              Photon.RED,
                                              s_origin,
                                              s_boxHeight - Photon.s_radius,
                                              s_boxWidth + s_laserOffsetX * 2,
                                              new Vector2D.Double( 1, 0 ) );
        stimulatingBeam.addListener( this );
        stimulatingBeam.setActive( true );
        getLaserModel().setStimulatingBeam( stimulatingBeam );

        pumpingBeam = new CollimatedBeam( getLaserModel(),
                                          Photon.BLUE,
                                          new Point2D.Double( s_origin.getX() + s_laserOffsetX, s_origin.getY() - s_laserOffsetX ),
                                          s_boxHeight + s_laserOffsetX * 2,
                                          s_boxWidth,
                                          new Vector2D.Double( 0, 1 ) );
        pumpingBeam.addListener( this );
        pumpingBeam.setActive( true );
        getLaserModel().setPumpingBeam( pumpingBeam );
    }

    /**
     *
     */
    public void activate( PhetApplication app ) {
        super.activate( app );

        Atom atom = null;
        atoms = new ArrayList();
        for( int i = 0; i < 20; i++ ) {
            atom = new Atom();
            boolean placed = false;

            // Place atoms so they don't overlap
            do {
                placed = true;
                atom.setPosition( ( getLaserOrigin().getX() + ( Math.random() ) * ( s_boxWidth - atom.getRadius() * 2 ) + atom.getRadius() ),
                                  ( getLaserOrigin().getY() + ( Math.random() ) * ( s_boxHeight - atom.getRadius() * 2 ) ) + atom.getRadius() );
                //                atom.setVelocity( ( Math.random() - 0.5 ) * s_maxSpeed,
                //                                  ( Math.random() - 0.5 ) * s_maxSpeed );
                for( int j = 0; j < atoms.size(); j++ ) {
                    Atom atom2 = (Atom)atoms.get( j );
                    double d = atom.getPosition().distance( atom2.getPosition() );
                    if( d <= atom.getRadius() + atom2.getRadius() ) {
                        placed = false;
                        break;
                    }
                }
            } while( !placed );
            atoms.add( atom );
            addAtom( atom );
        }

        ApparatusConfiguration config = new ApparatusConfiguration();
        config.setStimulatedPhotonRate( 2.0f );
        config.setMiddleEnergySpontaneousEmissionTime( 0.5775f );
        config.setPumpingPhotonRate( 100f );
        config.setHighEnergySpontaneousEmissionTime( 0.1220f );
        config.setReflectivity( 0.7f );
        config.configureSystem( (LaserModel)getModel() );
    }

    /**
     *
     */
    public void deactivate( PhetApplication app ) {
        super.deactivate( app );
        for( int i = 0; i < atoms.size(); i++ ) {
            Atom atom = (Atom)atoms.get( i );
            getLaserModel().removeModelElement( atom );
            atom.removeFromSystem();
        }
        atoms.clear();
    }
}
