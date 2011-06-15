// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.sugarandsaltsolutions.water;

import edu.colorado.phet.sugarandsaltsolutions.GlobalSettings;
import edu.colorado.phet.sugarandsaltsolutions.common.SugarAndSaltSolutionsModule;
import edu.colorado.phet.sugarandsaltsolutions.water.model.WaterModel;
import edu.colorado.phet.sugarandsaltsolutions.water.view.WaterCanvas;

/**
 * Module for "water" tab of sugar and salt solutions module
 *
 * @author Sam Reid
 */
public class WaterModule extends SugarAndSaltSolutionsModule {
    private final WaterModel model;

    public WaterModule( GlobalSettings settings ) {
        this( new WaterModel(), settings );
    }

    public WaterModule( final WaterModel model, GlobalSettings settings ) {
        super( "Water", model.clock );
        this.model = model;
        setSimulationPanel( new WaterCanvas( model, settings ) );
    }
}