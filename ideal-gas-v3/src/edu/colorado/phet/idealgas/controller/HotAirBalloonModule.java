/**
 * Created by IntelliJ IDEA.
 * User: Another Guy
 * Date: Feb 19, 2003
 * Time: 3:35:40 PM
 * To change this template use Options | File Templates.
 */
package edu.colorado.phet.idealgas.controller;

import edu.colorado.phet.collision.SphereHotAirBalloonExpert;
import edu.colorado.phet.common.math.Vector2D;
import edu.colorado.phet.common.model.clock.AbstractClock;
import edu.colorado.phet.common.view.PhetControlPanel;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.idealgas.model.*;
import edu.colorado.phet.idealgas.view.HotAirBalloonGraphic;

import java.awt.geom.Point2D;

public class HotAirBalloonModule extends IdealGasModule {

    private static final double initialVelocity = 35;

    private HotAirBalloon balloon;

    public HotAirBalloonModule( AbstractClock clock ) {
        super( clock, SimStrings.get( "ModuleTitle.HotAirBalloon" ) );

        // Add collision experts to the model
        getIdealGasModel().addCollisionExpert( new SphereHotAirBalloonExpert( getIdealGasModel(), clock.getDt() ) );

        // Set the size of the box
        Box2D box = getIdealGasModel().getBox();

        // Add the hot air balloon to the model
        double habRadius = 50;
        double habX = box.getMinX() + box.getWidth() / 2;
        double habY = box.getMaxY() - habRadius;
        balloon = new HotAirBalloon( new Point2D.Double( habX, habY ),
                                     new Vector2D.Double( 0, 0 ),
                                     new Vector2D.Double( 0, 0 ),
                                     200, habRadius,
                                     60,
                                     getIdealGasModel() );
        box.setMinimumWidth( balloon.getRadius() * 3 );

        // Give the balloon a high layer number so it always is over whatever
        // particles are pumped into the box
        getIdealGasModel().addModelElement( balloon );
        Constraint constraintSpec = new BoxMustContainParticle( box, balloon, getIdealGasModel() );
        balloon.addConstraint( constraintSpec );
        HotAirBalloonGraphic graphic = new HotAirBalloonGraphic( getApparatusPanel(), balloon );
        addGraphic( graphic, 20 );

        // Put some particles in the box outside the balloonn
        double xOrigin = 200;
        double yOrigin = 250;
        double xDiag = 434;
        double yDiag = 397;

//        for( int i = 0; i < 0; i++ ) {
////        for( int i = 0; i < 1; i++ ) {
////        for( int i = 0; i < 50; i++ ) {
////        for( int i = 0; i < 100; i++ ) {
//            double x = Math.random() * ( xDiag - xOrigin - 20 ) + xOrigin + 50;
//            double y = Math.random() * ( yDiag - yOrigin - 20 ) + yOrigin + 10;
//            double theta = Math.random() * Math.PI * 2;
//            double vx = Math.cos( theta ) * initialVelocity;
//            double vy = Math.sin( theta ) * initialVelocity;
//            double m = 10;
//            GasMolecule p1 = new HeavySpecies( new Point2D.Double( x, y ),
//                                               new Vector2D.Double( vx, vy ),
//                                               new Vector2D.Double( 0, 0 ) );
//            getIdealGasModel().addModelElement( p1 );
//            constraintSpec = new BoxMustContainParticle( box, p1, getIdealGasModel() );
//            p1.addConstraint( constraintSpec );
//        }

        // Put some particles inside the balloon
//        GasMolecule p1 = null;
//        int num = 0;
//        for( int i = 1; i <= num; i++ ) {
//            for( int j = 0; j < num; j++ ) {
//
//                double vx = initialVelocity;
//                double vy = 0;
//                double m = 10;
//                p1 = new HeavySpecies( new Point2D.Double( 350 + i * 10, 350 + j * 10 ),
//                                       new Vector2D.Double( vx, vy ),
//                                       new Vector2D.Double( 0, 0 ) );
//                balloon.addContainedBody( p1 );
//                getIdealGasModel().addModelElement( p1 );
//
//                constraintSpec = new BoxMustContainParticle( box, p1, getIdealGasModel() );
//                p1.addConstraint( constraintSpec );
//            }
//        }

        // Add the specific controls we need for the hot air balloon
        IdealGasControlPanel controlPanel = new IdealGasControlPanel( this );
        controlPanel.addComponent( new HotAirBalloonControlPanel( balloon ) );
        this.setControlPanel( new PhetControlPanel( this ) );
        getControlPanel().add( controlPanel );
    }
}
