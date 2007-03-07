/** Sam Reid*/
package edu.colorado.phet.semiconductor_semi.macro.energy;

import edu.colorado.phet.semiconductor_semi.macro.energy.bands.BandParticle;
import edu.colorado.phet.semiconductor_semi.macro.energy.bands.EnergyCell;

/**
 * User: Sam Reid
 * Date: Apr 26, 2004
 * Time: 10:32:27 AM
 * Copyright (c) Apr 26, 2004 by Sam Reid
 */
public class Unexcite implements ParticleAction {
    EnergyCell cell;

    public Unexcite( EnergyCell cell ) {
        this.cell = cell;
    }

    public void apply( BandParticle particle ) {
        if( particle.getEnergyCell() == cell
            && particle.isLocatedAtCell()
        ) {
            particle.setExcited( false );
        }
    }
}
