/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.photoelectric.model;

import edu.colorado.phet.common.model.BaseModel;
import edu.colorado.phet.common.model.ModelElement;
import edu.colorado.phet.common.model.clock.AbstractClock;
import edu.colorado.phet.common.math.Vector2D;
import edu.colorado.phet.dischargelamps.model.*;
import edu.colorado.phet.dischargelamps.DischargeLampsConfig;
import edu.colorado.phet.lasers.model.photon.Photon;
import edu.colorado.phet.lasers.model.photon.CollimatedBeam;
import edu.colorado.phet.lasers.model.photon.PhotonEmittedListener;
import edu.colorado.phet.lasers.model.photon.PhotonEmittedEvent;
import edu.colorado.phet.lasers.model.ResonatingCavity;
import edu.colorado.phet.photoelectric.model.util.BeamIntensityMeter;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * PhotoelectricModel
 * <p/>
 * Builds on the DischargeLampModel.
 * <p/>
 * Uses a PhotoelectricTarget, which is an extension of Electrode
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class PhotoelectricModel extends DischargeLampModel {

    //----------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------

    public static double MIN_VOLTAGE = -0.03;
    public static double MAX_VOLTAGE = 0.03;
    public static double MIN_WAVELENGTH = 100;
    public static double MAX_WAVELENGTH = 600;

    //----------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------

    private List photons = new ArrayList();
    private List electrons = new ArrayList();

    // Target specification
    private PhotoelectricTarget target;
    private double defaultTargetPotential = 0;

    private ArrayList electronSources = new ArrayList();
    private ArrayList electronSinks = new ArrayList();

    // Right-hand plate
    private ElectronSink rightHandPlate;

    // Beam specification
    private CollimatedBeam beam;
    private double defaultBeamWavelength = 400;
    private double beamMaxPhotonsPerSecond = 500;
//    private double beamMaxPhotonsPerSecond = 200;
    private double beamWidth = 80;
    private double beamHeight = 100;
    private double beamSourceToTargetDist = 300;
    private double beamAngle = Math.toRadians( 130 );
    private double beamFanout = Math.toRadians( 5 );
    private Ammeter ammeter;
    private BeamIntensityMeter beamIntensityMeter;
    private ResonatingCavity tube;

    //----------------------------------------------------------------
    // Contructors and initialization
    //----------------------------------------------------------------

    /**
     *
     */
    public PhotoelectricModel( AbstractClock clock ) {

        // todo: this isn't correct. The rotated beam doesn't look right. Try an angle of 170 deg. to see.
        // Create a photon beam and add a listener that will add the photons it produces to the model
        double alpha = beamAngle - Math.PI;
        Point2D beamLocation = new Point2D.Double( DischargeLampsConfig.CATHODE_LOCATION.getX() + Math.cos( alpha ) * beamSourceToTargetDist
                                                   + beamHeight / 2 * Math.sin( alpha ),
                                                   ( DischargeLampsConfig.CATHODE_LOCATION.getY() - beamHeight / 2 ) + Math.sin( alpha ) * beamSourceToTargetDist
                                                   + beamHeight / 2 * Math.cos( alpha ) );
        beam = new CollimatedBeam( defaultBeamWavelength,
                                   beamLocation,
                                   beamHeight,
                                   beamWidth,
                                   new Vector2D.Double( Math.cos( beamAngle ), Math.sin( beamAngle ) ),
                                   beamMaxPhotonsPerSecond,
                                   beamFanout );
        addModelElement( beam );
        beam.setPhotonsPerSecond( beamMaxPhotonsPerSecond );
        beam.setEnabled( true );
        beam.addPhotonEmittedListener( new PhotonTracker() );

        // Create the right-hand plate
        rightHandPlate = new ElectronSink( this,
                                           DischargeLampsConfig.ANODE_LINE.getP1(),
                                           DischargeLampsConfig.ANODE_LINE.getP2() );
        this.addModelElement( rightHandPlate );

        // Create the target plate.
        target = new PhotoelectricTarget( this, DischargeLampsConfig.CATHODE_LINE.getP1(),
                                          DischargeLampsConfig.CATHODE_LINE.getP2() );
        addModelElement( target );
        target.setPotential( defaultTargetPotential );
        target.addListener( new ElectronTracker() );

        // Create the tube
        double x = DischargeLampsConfig.CATHODE_LOCATION.getX() - DischargeLampsConfig.ELECTRODE_INSETS.left;
        double y = DischargeLampsConfig.CATHODE_LOCATION.getY() - DischargeLampsConfig.CATHODE_LENGTH / 2
                   - DischargeLampsConfig.ELECTRODE_INSETS.top;
        double length = DischargeLampsConfig.ANODE_LOCATION.getX() - DischargeLampsConfig.CATHODE_LOCATION.getX()
                        + DischargeLampsConfig.ELECTRODE_INSETS.left + DischargeLampsConfig.ELECTRODE_INSETS.right;
        double height = DischargeLampsConfig.CATHODE_LENGTH
                        + DischargeLampsConfig.ELECTRODE_INSETS.top + DischargeLampsConfig.ELECTRODE_INSETS.bottom;
        Point2D tubeLocation = new Point2D.Double( x, y );
        tube = new ResonatingCavity( tubeLocation, length, height );
        addModelElement( tube );


        //----------------------------------------------------------------
        // Intrumentation
        //----------------------------------------------------------------

        // Add an ammeter to the right-hand-plate
        ammeter = new Ammeter( clock );
        getRightHandPlate().addListener( new ElectronSink.ElectronAbsorptionListener() {
            public void electronAbsorbed( ElectronSink.ElectronAbsorptionEvent event ) {
                ammeter.recordElectron();
            }
        } );

        // Add an intensity meter for the beam
        beamIntensityMeter = new BeamIntensityMeter( clock );
        getBeam().addPhotonEmittedListener( new PhotonEmittedListener() {
            public void photonEmittedEventOccurred( PhotonEmittedEvent event ) {
                beamIntensityMeter.recordPhoton();
            }
        } );
    }

    /**
     * Tracks special classes of model elements
     *
     * @param modelElement
     */
    public void addModelElement( ModelElement modelElement ) {

        // If the model element is an ElectronSource, add all the known ElectronSinks
        // to it as listeners
        if( modelElement instanceof ElectronSource ) {
            ElectronSource electronSource = (ElectronSource)modelElement;
            electronSources.add( electronSource );
            for( Iterator iterator = electronSinks.iterator(); iterator.hasNext(); ) {
                ElectronSink electronSink = (ElectronSink)iterator.next();
                electronSource.addListener( electronSink );
            }
        }

        // If the model element is an ElectronSink, add it as a listener to all known
        // ElectronSources
        if( modelElement instanceof ElectronSink ) {
            ElectronSink electronSink = (ElectronSink)modelElement;
            electronSinks.add( electronSink );
            for( Iterator iterator = electronSources.iterator(); iterator.hasNext(); ) {
                ElectronSource electronSource = (ElectronSource)iterator.next();
                electronSource.addListener( electronSink );
            }
        }
        super.addModelElement( modelElement );
    }

    /**
     * Handles production of photons from the cathode
     *
     * @param dt
     */
    public void stepInTime( double dt ) {
        super.stepInTime( dt );

        // Check for photons hitting the cathode
        for( int i = 0; i < photons.size(); i++ ) {
            Photon photon = (Photon)photons.get( i );
            if( target.isHitByPhoton( photon ) ) {
                target.handlePhotonCollision( photon );
                photon.removeFromSystem();
            }
        }

        // Check for electrons that get out of the tube (Only matters if the
        // electrons leave the target at an angle)
        for( int i = 0; i < electrons.size(); i++ ) {
            Electron electron = (Electron)electrons.get(i);
            if( !tube.getBounds().contains( electron.getPosition() )) {
                electron.leaveSystem();
            }
        }
    }

    //----------------------------------------------------------------
    // Getters and setters 
    //----------------------------------------------------------------

    public ResonatingCavity getTube() {
        return tube;
    }

    public PhotoelectricTarget getTarget() {
        return target;
    }

    public CollimatedBeam getBeam() {
        return beam;
    }

    public ElectronSink getRightHandPlate() {
        return rightHandPlate;
    }

    public Ammeter getAmmeter() {
        return ammeter;
    }

    public BeamIntensityMeter getBeamIntensityMeter() {
        return beamIntensityMeter;
    }

    public double getAnodePotential() {
        return rightHandPlate.getPotential() - target.getPotential() ;
    }

    //----------------------------------------------------------------
    // Listeners for tracking the creation and destruction of
    // certain model elements
    //----------------------------------------------------------------

    /**
     * Tracks the creation and destruction of photons
     */
    private class PhotonTracker implements PhotonEmittedListener, Photon.LeftSystemEventListener {
        public void photonEmittedEventOccurred( PhotonEmittedEvent event ) {
            Photon photon = event.getPhoton();
            addModelElement( photon );
            photons.add( photon );
            photon.addLeftSystemListener( this );
        }

        public void leftSystemEventOccurred( Photon.LeftSystemEvent event ) {
            photons.remove( event.getPhoton() );
        }
    }

    /**
     * Track the creation and destruction of electrions
     */
    private class ElectronTracker implements ElectronSource.ElectronProductionListener, Electron.ChangeListener {

        public void electronProduced( ElectronSource.ElectronProductionEvent event ) {
            Electron electron = event.getElectron();
            addModelElement( electron );
            electrons.add( electron );
            electron.addChangeListener( this );
        }

        public void leftSystem( Electron.ChangeEvent changeEvent ) {
            electrons.remove( changeEvent.getElectrion() );
        }

        public void energyChanged( Electron.ChangeEvent changeEvent ) {
            // noop
        }
    }
}
