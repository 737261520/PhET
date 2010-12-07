/* Copyright 2010, University of Colorado */

package edu.colorado.phet.greenhouse.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.colorado.phet.common.phetcommon.math.ImmutableVector2D;
import edu.colorado.phet.common.phetcommon.math.Vector2D;
import edu.colorado.phet.common.phetcommon.view.util.RectangleUtils;

/**
 * Class that represents a molecule in the model.  This, by its nature, is
 * essentially a composition of other objects, generally atoms and atomic
 * bonds.
 *
 * @author John Blanco
 */
public abstract class Molecule {

    //------------------------------------------------------------------------
    // Class Data
    //------------------------------------------------------------------------

    private static final double PHOTON_EMISSION_SPEED = 2; // Picometers per second.

    private static final double PHOTON_ABSORPTION_DISTANCE = 100;

    private static final Random RAND = new Random();

    private static final double VIBRATION_FREQUENCY = 3;  // Cycles per second of sim time.
    private static final double ROTATION_RATE = 3;  // Revolutions per second of sim time.
    private static final double ABSORPTION_HYSTERESIS_TIME = 200; // Milliseconds of sim time.

    private static final double MIN_PHOTON_HOLD_TIME = 600; // Milliseconds of sim time.
    private static final double MAX_PHOTON_HOLD_TIME = 1200; // Milliseconds of sim time.

    //------------------------------------------------------------------------
    // Instance Data
    //------------------------------------------------------------------------

    // Atoms and bonds that comprise this molecule.
    protected final ArrayList<Atom> atoms = new ArrayList<Atom>();
    protected final ArrayList<AtomicBond> atomicBonds = new ArrayList<AtomicBond>();

    // Offsets for each atom from the molecule's center of gravity.
    protected final HashMap<Atom, Vector2D> atomCogOffsets = new HashMap<Atom, Vector2D>();

    // Listeners to events that come from this molecule.
    protected final ArrayList<Listener> listeners = new ArrayList<Listener>();

    // This is basically the location of the molecule, but it is specified as
    // the center of gravity since a molecule is a composite object.
    private final Point2D centerOfGravity = new Point2D.Double();

    // Velocity for this molecule.
    private final Vector2D velocity = new Vector2D( 0, 0 );

    // Map that matches photon wavelengths to photon absorption strategies.
    // The strategies contained in this structure define whether the
    // molecule can absorb a given photon and, if it does absorb it, how it
    // will react.
    private final HashMap<Double, PhotonAbsorptionStrategy> mapWavelengthToAbsorptionStrategy = new HashMap<Double, PhotonAbsorptionStrategy>();

    // Currently active photon absorption strategy, active because a photon
    // was absorbed that activated it.
    private PhotonAbsorptionStrategy activeStrategy = new NullPhotonAbsorptionStrategy( this );

    // Variable that tracks whether or not a photon has been absorbed and has
    // not yet been re-emitted.
    private boolean photonAbsorbed = false;

    //PHOTON RE_EMISSION
    // Variables involved in the holding and re-emitting of photons.
    private double photonHoldCountdownTime = 0;
    // Photon to be emitted when the photon hold timer expires.
    protected Photon photonToEmit = null;

    //BREAK APART
    // Variables involved in the holding and re-emitting of photons.
    private double breakApartCountdownTime = 0;

    private double absorbtionHysteresisCountdownTime = 0;

    // The "pass through photon list" keeps track of photons that were not
    // absorbed due to random probability (essentially a simulation of quantum
    // properties).  This is needed since the absorption of a given photon
    // will likely be tested at many time steps as the photon moves past the
    // molecule, and we don't want to keep deciding about the same photon.
    private static final int PASS_THROUGH_PHOTON_LIST_SIZE = 10;
    private final ArrayList<Photon> passThroughPhotonList = new ArrayList<Photon>( PASS_THROUGH_PHOTON_LIST_SIZE );

    // The current point within this molecule's vibration sequence.
    private double currentVibrationRadians = 0;

    // List of photon wavelengths that this molecule can absorb.  This is a
    // list of frequencies, which is a grand oversimplification of the real
    // behavior, but works for the current purposes of this sim.
    private final ArrayList<Double> photonAbsorptionWavelengths = new ArrayList<Double>(2);

    // Tracks if molecule is higher energy than its ground state.
    private boolean highElectronicEnergyState = false;

    // Boolean values that track whether the molecule is vibrating or
    // rotating.
    private boolean vibrating = false;
    private boolean rotating = false;
    private boolean rotationDirectionClockwise = true; // Controls the direction of rotation.

    //------------------------------------------------------------------------
    // Constructor(s)
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------------

    protected void setPhotonAbsorptionStrategy( double wavelength, PhotonAbsorptionStrategy stratgy ){
        mapWavelengthToAbsorptionStrategy.put( wavelength, activeStrategy );
    }

    protected boolean isPhotonAbsorbed() {
        // If there is an active non-null photon absorption strategy, it
        // indicates that a photon has been absorbed.
        return !(activeStrategy instanceof NullPhotonAbsorptionStrategy);
    }

    protected void setPhotonAbsorbed( boolean photonAbsorbed ) {
        this.photonAbsorbed = photonAbsorbed;
    }

    /**
     * Get the ID used for this molecule.
     */
    public abstract MoleculeID getMoleculeID();

    /**
     * Static factory method for producing molecules of a given type.
     */
    public static Molecule createMolecule( MoleculeID moleculeID){
        Molecule newMolecule = null;
        switch ( moleculeID ){
        case CH4:
            newMolecule = new CH4();
            break;
        case CO2:
            newMolecule = new CO2();
            break;
        case H2O:
            newMolecule = new H2O();
            break;
        case N2:
            newMolecule = new N2();
            break;
        case N2O:
            newMolecule = new N2O();
            break;
        case O:
            newMolecule = new O2();
            break;
        case O2:
            newMolecule = new O();
            break;
        default:
            System.err.println("Molecule: " + " - Error: Unrecognized molecule type.");
            assert false;
        }

        return newMolecule;
    }

    /**
     * Advance the molecule one step in time.
     */
    public void stepInTime(double dt){

        activeStrategy.stepInTime( dt );

        if (absorbtionHysteresisCountdownTime > 0){
            absorbtionHysteresisCountdownTime -= dt;
        }

        if ( vibrating ){
            advanceVibration( dt * VIBRATION_FREQUENCY / 1000 * 2 * Math.PI );
        }

        if ( rotating ){
            int directionMultiplier = rotationDirectionClockwise ? -1 : 1;
            rotate( dt * ROTATION_RATE / 1000 * 2 * Math.PI * directionMultiplier );
        }

        // Do any linear movement that is required.
        setCenterOfGravityPos( velocity.getDestination( centerOfGravity ) );
        setCenterOfGravityPos( centerOfGravity.getX() + velocity.getX() * dt, centerOfGravity.getY() + velocity.getY() * dt);
    }

    /**
     * Reset the molecule.  Any photons that have been absorbed are forgotten,
     * and any vibration is reset.
     * @return
     */
    public void reset(){
        photonAbsorbed = false;
        photonToEmit = null;
        activeStrategy.reset();
        activeStrategy = new NullPhotonAbsorptionStrategy( this );
        photonHoldCountdownTime = 0;
        breakApartCountdownTime = 0;
        absorbtionHysteresisCountdownTime = 0;
    }

    public boolean isVibration() {
        return vibrating;
    }

    public void setVibrating( boolean vibration ) {
        this.vibrating = vibration;
    }

    public boolean isRotating() {
        return rotating;
    }

    public void setRotating( boolean rotating ) {
        this.rotating = rotating;
    }

    protected boolean isRotationDirectionClockwise() {
        return rotationDirectionClockwise;
    }

    protected void setRotationDirectionClockwise( boolean rotationDirectionClockwise ) {
        this.rotationDirectionClockwise = rotationDirectionClockwise;
    }

    public double getAbsorbtionHysteresisCountdownTime() {
        return absorbtionHysteresisCountdownTime;
    }

    /**
     * Initialize the offsets from the center of gravity for each atom within
     * this molecule.  This should be in the "relaxed" (i.e. non-vibrating)
     * state.
     */
    protected abstract void initializeAtomOffsets();

    public void addListener(Listener listener){
        // Don't bother adding if already there.
        if (!listeners.contains( listener )){
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener){
        listeners.remove(listener);
    }

    public Point2D getCenterOfGravityPos(){
        return new Point2D.Double(centerOfGravity.getX(), centerOfGravity.getY());
    }

    protected Point2D getCenterOfGravityPosRef(){
        return centerOfGravity;
    }

    /**
     * Add a wavelength to the list of those absorbed by this molecule.
     *
     * @param wavelength
     */
    protected void addPhotonAbsorptionWavelength(double wavelength){
        photonAbsorptionWavelengths.add( wavelength );
    }

    /**
     * Set the location of this molecule by specifying the center of gravity.
     * This will be unique to each molecule's configuration, and it will cause
     * the individual molecules to be located such that the center of gravity
     * is in the specified location.  The relative orientation of the atoms
     * that comprise the molecules will not be changed.
     *
     * @param x the x location to set
     * @param y the y location to set
     */
    public void setCenterOfGravityPos( double x, double y ){
        this.centerOfGravity.setLocation( x, y );
        updateAtomPositions();
    }

    public void setCenterOfGravityPos(Point2D centerOfGravityPos){
        setCenterOfGravityPos( centerOfGravityPos.getX(), centerOfGravityPos.getY() );
    }

    /**
     * Set the angle, in terms of radians from 0 to 2*PI, where this molecule
     * is in its vibration sequence.
     */
    protected void setVibration( double vibrationRadians ) {
        currentVibrationRadians = vibrationRadians;
        return; // Implements no vibration by default, override in descendant classes as needed.
    }

    /**
     * Advance the vibration by the prescribed radians.
     *
     * @param deltaRadians
     */
    public void advanceVibration( double deltaRadians ){
        currentVibrationRadians += deltaRadians;
        setVibration( currentVibrationRadians );
    }

    /**
     * Rotate the molecule about the center of gravity by the specified number
     * of radians.
     *
     * @param deltaRadians
     */
    public void rotate( double deltaRadians ){
        for ( Vector2D atomOffsetVector : atomCogOffsets.values() ){
            atomOffsetVector.rotate( deltaRadians );
        }
    }

    /**
     * Enable/disable a molecule's high electronic energy state, which in the
     * real world is a state where one or more electrons has moved to a higher
     * orbit.  In this simulation, it is generally depicted by having the
     * molecule appear to glow.
     *
     * @param highElectronicEnergyState
     */
    protected void setHighElectronicEnergyState( boolean highElectronicEnergyState ){
        this.highElectronicEnergyState  = highElectronicEnergyState;
    }

    public boolean isHighElectronicEnergyState(){
        return highElectronicEnergyState;
    }

    /**
     * Cause the molecule to dissociate, i.e. to break apart.
     */
    protected void breakApart() {
        System.err.println( getClass().getName() + " - Error: breakApart invoked on a molecule for which the action is not implemented." );
        assert false;
    }

    protected void markPhotonForPassThrough(Photon photon){
        if (passThroughPhotonList.size() >= PASS_THROUGH_PHOTON_LIST_SIZE){
            // Make room for this photon be deleting the oldest one.
            passThroughPhotonList.remove( 0 );
        }
        passThroughPhotonList.add( photon );
    }

    protected boolean isPhotonMarkedForPassThrough(Photon photon){
        return (passThroughPhotonList.contains( photon ));
    }

    public ArrayList<Atom> getAtoms() {
        return new ArrayList<Atom>(atoms);
    }

    public ArrayList<AtomicBond> getAtomicBonds() {
        return new ArrayList<AtomicBond>(atomicBonds);
    }

    /**
     * Decide whether or not to absorb the offered photon.  If the photon is
     * absorbed, the matching absorption strategy is set so that it can
     * control the molecule's post-absorption behavior.
     *
     * @param photon - The photon offered for absorption.
     * @return
     */
    public boolean queryAbsorbPhoton( Photon photon ){

        boolean absorbPhoton = false;

        if ( !isPhotonAbsorbed() &&
             absorbtionHysteresisCountdownTime <= 0 &&
             photon.getLocation().distance( getCenterOfGravityPos() ) < PHOTON_ABSORPTION_DISTANCE &&
             !isPhotonMarkedForPassThrough( photon ) ) {

            // The circumstances for absorption are correct, but do we have an
            // absorption strategy for this photon's wavelength?
            PhotonAbsorptionStrategy candidateAbsorptionStrategy = mapWavelengthToAbsorptionStrategy.get( photon.getWavelength() );
            if ( candidateAbsorptionStrategy != null ){
                // Yes, there is a strategy available for this wavelength.
                // Ask it if it wants the photon.
                if ( candidateAbsorptionStrategy.quearyAbsorbPhoton( photon ) ){
                    // It does want it, so consider the photon absorbed.
                    absorbPhoton = true;
                    activeStrategy = candidateAbsorptionStrategy;
                }
            }
        }

        if ( !absorbPhoton ){
            // Add this unabsorbed photon to the list of photons to ignore.
            passThroughPhotonList.add( photon );
        }

        return absorbPhoton;
    }

    protected void addAtom( Atom atom ){
        atoms.add( atom );
    }

    protected void addAtomicBond( AtomicBond atomicBond ){
        atomicBonds.add( atomicBond );
    }

    /**
     * This method is used by subclasses to let the base class know that it
     * should start the remission timer.
     *
     * @param photonToEmit
     */
    protected void startPhotonEmissionTimer( Photon photonToEmit ){
        photonHoldCountdownTime = MIN_PHOTON_HOLD_TIME + RAND.nextDouble() * (MAX_PHOTON_HOLD_TIME - MIN_PHOTON_HOLD_TIME);
        this.photonToEmit = photonToEmit;
    }

    protected void startBreakApartTimer( Photon photonToEmit ){
        breakApartCountdownTime = MIN_PHOTON_HOLD_TIME;
    }

    /**
     * Cause the atom to emit a photon of the specified wavelength.
     *
     * TODO: Not sure if this version is needed, verify that it is or delete it.
     *
     * @param wavelength
     */
    protected void emitPhoton( double wavelength ){
        emitPhoton( new Photon( wavelength, null ) );
    }

    /**
     * Emit the specified photon in a random direction.
     *
     * @param photonToEmit
     */
    protected void emitPhoton( Photon photonToEmit ){
        double emissionAngle = RAND.nextDouble() * Math.PI * 2;
        photonToEmit.setVelocity( (float)(PHOTON_EMISSION_SPEED * Math.cos( emissionAngle )),
                (float)(PHOTON_EMISSION_SPEED * Math.sin( emissionAngle )));
        photonToEmit.setLocation( getCenterOfGravityPosRef() );
        notifyPhotonEmitted( photonToEmit );

    }

    // TODO: Not sure if this version is needed, verify that it is or delete it.
    protected void emitPhoton(){
        emitPhoton( photonToEmit );
    }

    private void notifyPhotonEmitted(Photon photon){
        for (Listener listener : listeners){
            listener.photonEmitted( photon );
        }
    }

    /**
     * Update the positions of all atoms that comprise this molecule based on
     * the current center of gravity and the offset for each atom.
     */
    protected void updateAtomPositions(){
        for (Atom atom : atoms){
            Vector2D offset = atomCogOffsets.get( atom );
            if (offset != null){
                atom.setPosition( centerOfGravity.getX() + offset.getX(), centerOfGravity.getY() + offset.getY() );
            }
            else{
                // This shouldn't happen, and needs to be debugged if it does.
                assert false;
                System.err.println(getClass().getName() + " - Error: No offset found for atom.");
            }
        }
    }

    public void setVelocity( double vx, double vy ) {
        setVelocity( new ImmutableVector2D( vx, vy ) );
    }

    public void setVelocity( ImmutableVector2D newVelocity) {
        this.velocity.setValue( newVelocity );
    }

    public ImmutableVector2D getVelocity() {
        return velocity;
    }

    /**
     * Get an enclosing rectangle for this molecule.  This was created to
     * support searching for open locations for new molecules.
     *
     * @return
     */
    public Rectangle2D getBoundingRect(){
        Rectangle2D [] atomRects = new Rectangle2D[atoms.size()];
        for (int i = 0; i < atoms.size(); i++){
            atomRects[i] = atoms.get( i ).getBoundingRect();
        }

        return RectangleUtils.union( atomRects );
    }

    public ArrayList<Molecule> getBreakApartConstituents() {
        return new ArrayList<Molecule>(  );
    }

    //------------------------------------------------------------------------
    // Inner Classes and Interfaces
    //------------------------------------------------------------------------

    public interface Listener {
        void photonEmitted( Photon photon );
        void brokeApart( Molecule molecule );
        void electronicEnergyStateChanged( Molecule molecule );
    }

    public static class Adapter implements Listener {
        public void photonEmitted(Photon photon) {}
        public void brokeApart(Molecule molecule) {}
        public void electronicEnergyStateChanged( Molecule molecule ) {}
    }

    /**
     * This is the base class for the strategies that define how a molecule
     * reacts to a given photon.  It is responsible for the following:
     * - Whether a given photon should be absorbed.
     * - How the molecule reacts to the absorption of a photon, i.e. whether
     *   it vibrates, rotates, breaks apart, etc.
     * - Maintenance of any counters or timers associated with the reaction to
     *   the absorption.
     */
    public abstract static class PhotonAbsorptionStrategy {

        private static final double ABSORPTION_HYSTERESIS_TIME = 200; // Milliseconds of sim time.

        private static final double MIN_PHOTON_HOLD_TIME = 600; // Milliseconds of sim time.
        private static final double MAX_PHOTON_HOLD_TIME = 1200; // Milliseconds of sim time.

        private static final Random RAND = new Random();

        private final Molecule molecule;
        private Photon lastPhoton;

        // Variables involved in the holding and re-emitting of photons.
        private Photon absorbedPhoton;
        private boolean isPhotonAbsorbed = false;
        private double photonHoldCountdownTime = 0;
        private double absorbtionHysteresisCountdownTime = 0;

        /**
         * Constructor.
         */
        public PhotonAbsorptionStrategy( Molecule molecule ) {
            this.molecule = molecule;
        }

        protected Molecule getMolecule(){
            return molecule;
        }

        /**
         * Step the strategy forward in time by the given time.
         *
         * @param dt
         */
        public abstract void stepInTime( double dt );

        /**
         * Reset the strategy.  In most cases, this will need to be overridden in the descendant classes, but those
         * overrides should also call this one.
         */
        public void reset(){
            absorbedPhoton = null;
            isPhotonAbsorbed = false;
            photonHoldCountdownTime = 0;
            absorbtionHysteresisCountdownTime = 0;
        }

        /**
         * Decide whether the provided photon should be absorbed.  By design,
         * a given photon should only be requested once, not multiple times.
         * @param photon
         * @return
         */
        public boolean quearyAbsorbPhoton( Photon photon ) {

            // Debug/test code.
            if ( lastPhoton != null ) {
                System.err.println( getClass().getName() + " - Error: Multiple requests to absorb the same photon." );
                assert ( lastPhoton != photon );
            }

            boolean absorbPhoton = false;
            if ( !isPhotonAbsorbed && photonHoldCountdownTime <= 0 ) {
                // All circumstances are correct for photon absorption, so now
                // we decide probabalistically whether or not to actually do
                // it.  This essentially simulates the quantum nature of the
                // absorption.

                if ( RAND.nextDouble() < SingleMoleculePhotonAbsorptionProbability.getInstance().getAbsorptionsProbability() ) {
                    absorbPhoton = true;
                    isPhotonAbsorbed = true;
                    photonHoldCountdownTime = MIN_PHOTON_HOLD_TIME + RAND.nextDouble() * ( MAX_PHOTON_HOLD_TIME - MIN_PHOTON_HOLD_TIME );
                }
                else {
                    // Do NOT absorb it.
                    absorbPhoton = false;
                }
            }
            else {
                absorbPhoton = false;
            }

            return absorbPhoton;
        }

        protected boolean isPhotonAbsorbed() {
            return isPhotonAbsorbed;
        }
    }

    public static class VibrationStrategy extends PhotonAbsorptionStrategy {

        public VibrationStrategy( Molecule molecule ) {
            super( molecule );
        }

        @Override
        public void stepInTime( double dt ) {
            getMolecule().advanceVibration( dt * VIBRATION_FREQUENCY / 1000 * 2 * Math.PI );
        }
    }

    public static class RotationStrategy extends PhotonAbsorptionStrategy {

        public RotationStrategy( Molecule molecule ) {
            super( molecule );
        }

        @Override
        public void stepInTime( double dt ) {
            // TODO Auto-generated method stub
        }
    }

    public static class NullPhotonAbsorptionStrategy extends PhotonAbsorptionStrategy {
        /**
         * Constructor.
         */
        public NullPhotonAbsorptionStrategy( Molecule molecule ) {
            super( molecule );
        }

        @Override
        public void stepInTime( double dt ) {
            // Does nothing.
        }
    }
}
