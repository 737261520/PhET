// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.sugarandsaltsolutions.micro;

import java.util.ArrayList;
import java.util.Random;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

import edu.colorado.phet.common.phetcommon.math.ImmutableRectangle2D;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction0;
import edu.colorado.phet.common.phetcommon.util.function.VoidFunction1;
import edu.colorado.phet.sugarandsaltsolutions.common.model.SugarAndSaltSolutionModel;

/**
 * Model for "micro" tab for sugar and salt solutions.
 *
 * @author Sam Reid
 */
public class MicroscopicModel extends SugarAndSaltSolutionModel {

    //List of all model objects objects
    private ArrayList<WaterMolecule> waterList = new ArrayList<WaterMolecule>();

    //Listeners who are called back when the physics updates
    private ArrayList<VoidFunction0> frameListeners = new ArrayList<VoidFunction0>();

    //Box2d world which updates the physics
    private World world;

    //Listeners that are notified when a water molecule enters the model.  Removal listeners are added to the water molecule
    private ArrayList<VoidFunction1<WaterMolecule>> waterAddedListeners = new ArrayList<VoidFunction1<WaterMolecule>>();

    private Random random = new Random();
    public final Barrier floor;
    public final Barrier rightWall;
    public final Barrier leftWall;

    public MicroscopicModel() {
        //Set the bounds of the physics engine.  The docs say things should be mostly between 0.1 and 10 units
        AABB worldAABB = new AABB();
        worldAABB.lowerBound = new Vec2( -200, -200 );
        worldAABB.upperBound = new Vec2( 200, 200 );

        //Create the world
        world = new World( worldAABB, new Vec2( 0, 1 ), true );

        //Add water particles
        addWaterParticles( System.currentTimeMillis() );

        //Create beaker floor
        floor = createBarrier( 0, 99, 100, 2 );

        //Create sides
        rightWall = createBarrier( 99, 0, 2, 100 );
        leftWall = createBarrier( -99, 0, 2, 100 );

        //Move to a stable state on startup
        //Commented out because it takes too long
//        long startTime = System.currentTimeMillis();
//        for ( int i = 0; i < 10; i++ ) {
//            world.step( (float) ( clock.getDt() * 10 ), 1 );
//        }
//        System.out.println( "stable start time: " + ( System.currentTimeMillis() - startTime ) );
    }

    private void addWaterParticles( long seed ) {
        Random random = new Random( seed );
        for ( int i = 0; i < 200; i++ ) {
            WaterMolecule water = createWater( random.nextFloat() * 200 - 100, random.nextFloat() * 200 - 100, 0, 0, (float) ( random.nextFloat() * Math.PI * 2 ) );
            waterList.add( water );
            for ( VoidFunction1<WaterMolecule> waterAddedListener : waterAddedListeners ) {
                waterAddedListener.apply( water );
            }
        }
    }

    //Creates a rectangular barrier
    private Barrier createBarrier( final float x, final float y, final float width, final float height ) {
        PolygonDef shape = new PolygonDef() {{
            restitution = 0.2f;
            setAsBox( width, height );
        }};
        BodyDef bd = new BodyDef() {{
            position = new Vec2( x, y );
        }};
        Body body = world.createBody( bd );
        body.createShape( shape );
        body.setMassFromShapes();

        return new Barrier( body, new ImmutableRectangle2D( x, y, width, height ) );
    }

    public void addWaterAddedListener( VoidFunction1<WaterMolecule> waterAddedListener ) {
        waterAddedListeners.add( waterAddedListener );
    }

    //Wrapper class which contains Body and shape
    public static class WaterMolecule {
        public Body body;
        public CircleDef oxygen;
        public CircleDef h1;
        public CircleDef h2;
        private ArrayList<VoidFunction0> removalListeners = new ArrayList<VoidFunction0>();

        WaterMolecule( Body body, CircleDef oxygen, CircleDef h1, CircleDef h2 ) {
            this.body = body;
            this.oxygen = oxygen;
            this.h1 = h1;
            this.h2 = h2;
        }

        //Add a listener that will be notified when this water leaves the model
        public void addRemovalListener( VoidFunction0 removalListener ) {
            removalListeners.add( removalListener );
        }

        //Notify listeners that this water molecule has left the model
        public void notifyRemoved() {
            for ( VoidFunction0 removalListener : removalListeners ) {
                removalListener.apply();
            }
        }
    }

    //Adds a circular body at the specified location with the given velocity
    private WaterMolecule createWater( float x, float y, float vx, float vy, float angle ) {

        //First create the body def at the right location
        BodyDef bodyDef = new BodyDef();
        bodyDef.position = new Vec2( x, y );
        bodyDef.angle = angle;

        //Now create the body
        Body body = world.createBody( bodyDef );

        //Make it a bouncy circle
        CircleDef oxygen = new CircleDef() {{
            restitution = 0.4f;
            density = 1;
            radius = 4;
        }};
        body.createShape( oxygen );

        CircleDef h1 = new CircleDef() {{
            restitution = 0.4f;
            localPosition = new Vec2( 3, 3 );
            radius = 2;
            density = 1;
        }};
        body.createShape( h1 );

        CircleDef h2 = new CircleDef() {{
            restitution = 0.4f;
            localPosition = new Vec2( -3, 3 );
            radius = 2;
            density = 1;
        }};
        body.createShape( h2 );
        body.setMassFromShapes();

        //Set the velocity
        body.setLinearVelocity( new Vec2( vx, vy ) );

        //Add the body to the list
        return new WaterMolecule( body, oxygen, h1, h2 );
    }

    @Override protected void updateModel( double dt ) {
        //Ignore super update for now
//        super.updateModel( dt );

        for ( WaterMolecule waterMolecule : waterList ) {
            //Apply a random force so the system doesn't settle down
            float rand1 = ( random.nextFloat() - 0.5f ) * 2;
            waterMolecule.body.applyForce( new Vec2( rand1 * 1000, random.nextFloat() ), waterMolecule.body.getPosition() );

            //Setting random velocity looks funny
//            double randomAngle = random.nextDouble() * Math.PI * 2;
//            ImmutableVector2D v = ImmutableVector2D.parseAngleAndMagnitude( rand1 * 1, randomAngle );
//            Vec2 linearVelocity = waterMolecule.body.getLinearVelocity();
//            waterMolecule.body.setLinearVelocity( new Vec2( linearVelocity.x + (float) v.getX(), linearVelocity.y + (float) v.getY() ) );
        }
        world.step( (float) dt * 5, 10 );

        //Notify listeners that the model changed
        for ( VoidFunction0 frameListener : frameListeners ) {
            frameListener.apply();
        }
    }

    //Get all bodies in the model
    public ArrayList<WaterMolecule> getWaterList() {
        return waterList;
    }

    //Register for a callback when the model steps
    public void addFrameListener( VoidFunction0 listener ) {
        frameListeners.add( listener );
    }

    //Resets the model, clearing water molecules and starting over
    @Override public void reset() {
        clearWater();
        addWaterParticles( System.currentTimeMillis() );
    }

    //Removes all water from the model
    private void clearWater() {
        for ( WaterMolecule waterMolecule : waterList ) {
            world.destroyBody( waterMolecule.body );
            waterMolecule.notifyRemoved();
        }
        waterList.clear();
    }

    //Model object representing a barrier, such as the beaker floor or wall which particles shouldn't pass through
    public static class Barrier {
        public final Body body;
        public final ImmutableRectangle2D shape;

        public Barrier( Body body, ImmutableRectangle2D shape ) {
            this.body = body;
            this.shape = shape;
        }
    }
}