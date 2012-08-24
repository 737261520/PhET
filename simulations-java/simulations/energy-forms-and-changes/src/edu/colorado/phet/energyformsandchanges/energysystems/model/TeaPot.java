// Copyright 2002-2012, University of Colorado
package edu.colorado.phet.energyformsandchanges.energysystems.model;

import java.util.ArrayList;
import java.util.List;

import edu.colorado.phet.common.phetcommon.math.vector.Vector2D;
import edu.colorado.phet.common.phetcommon.model.property.Property;
import edu.colorado.phet.common.phetcommon.simsharing.messages.IUserComponent;
import edu.colorado.phet.energyformsandchanges.EnergyFormsAndChangesResources;
import edu.colorado.phet.energyformsandchanges.EnergyFormsAndChangesSimSharing;

import static edu.colorado.phet.energyformsandchanges.EnergyFormsAndChangesResources.Images.TEAPOT_MEDIUM;

/**
 * Class that represents the steam-generating tea pot in the model.
 *
 * @author John Blanco
 */
public class TeaPot extends EnergySource {

    //-------------------------------------------------------------------------
    // Class Data
    //-------------------------------------------------------------------------

    private static final double MAX_ENERGY_PRODUCTION_RATE = 200; // In joules/second
    private static final double MAX_ENERGY_CHANGE_RATE = 10; // In joules/second
    private static final double COOLING_CONSTANT = 0.1; // Controls rate at which tea pot cools down, empirically determined.

    public static final ModelElementImage TEAPOT_IMAGE = new ModelElementImage( TEAPOT_MEDIUM, new Vector2D( 0.015, 0.015 ) );

    private static final List<ModelElementImage> IMAGE_LIST = new ArrayList<ModelElementImage>() {{
        add( TEAPOT_IMAGE );
    }};

    //-------------------------------------------------------------------------
    // Instance Data
    //-------------------------------------------------------------------------

    public final Property<Double> heatCoolAmount = new Property<Double>( 0.0 );

    private double energyProductionRate = 0;

    //-------------------------------------------------------------------------
    // Constructor(s)
    //-------------------------------------------------------------------------

    protected TeaPot() {
        super( EnergyFormsAndChangesResources.Images.TEAPOT_ICON, IMAGE_LIST );
    }

    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------

    @Override public Energy stepInTime( double dt ) {
        if ( active ) {
            double energyProductionIncreaseRate = heatCoolAmount.get() * MAX_ENERGY_CHANGE_RATE;
            energyProductionRate = Math.min( energyProductionRate + energyProductionIncreaseRate * dt, MAX_ENERGY_PRODUCTION_RATE );
            double energyProductionDecreaseRate = energyProductionRate * COOLING_CONSTANT;
            energyProductionRate = energyProductionRate + energyProductionIncreaseRate * dt - energyProductionDecreaseRate * dt;
        }
        System.out.println( "energyProductionRate = " + energyProductionRate );
        return new Energy( Energy.Type.MECHANICAL, energyProductionRate * dt, Math.PI / 2 );
    }

    @Override public void deactivate() {
        super.deactivate();
        heatCoolAmount.reset();
        energyProductionRate = 0;
    }

    @Override public IUserComponent getUserComponent() {
        return EnergyFormsAndChangesSimSharing.UserComponents.selectTeapotButton;
    }
}
