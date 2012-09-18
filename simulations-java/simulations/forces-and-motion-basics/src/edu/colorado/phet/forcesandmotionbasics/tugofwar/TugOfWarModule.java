package edu.colorado.phet.forcesandmotionbasics.tugofwar;

import edu.colorado.phet.common.phetcommon.model.clock.ConstantDtClock;
import edu.colorado.phet.common.piccolophet.SimSharingPiccoloModule;
import edu.colorado.phet.forcesandmotionbasics.ForcesAndMotionBasicsSimSharing.UserComponents;

/**
 * @author Sam Reid
 */
public class TugOfWarModule extends SimSharingPiccoloModule implements Context {
    public TugOfWarModule() {
        super( UserComponents.tugOfWarTab, "Tug of War", new ConstantDtClock() );
        setSimulationPanel( new TugOfWarCanvas( this, getClock() ) );
        setClockControlPanel( null );
    }

    @Override public void reset() {
        super.reset();
        setSimulationPanel( new TugOfWarCanvas( this, getClock() ) );
    }
}