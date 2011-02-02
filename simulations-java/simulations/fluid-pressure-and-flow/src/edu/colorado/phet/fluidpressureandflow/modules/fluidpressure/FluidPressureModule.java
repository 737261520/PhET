// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.fluidpressureandflow.modules.fluidpressure;

import edu.colorado.phet.fluidpressureandflow.FPAFStrings;
import edu.colorado.phet.fluidpressureandflow.FluidPressureAndFlowModule;

/**
 * @author Sam Reid
 */
public class FluidPressureModule extends FluidPressureAndFlowModule<FluidPressureModel> {
    public FluidPressureModule() {
        super( FPAFStrings.PRESSURE, new FluidPressureModel() );
        setSimulationPanel( new FluidPressureCanvas( this ) );
    }
}
