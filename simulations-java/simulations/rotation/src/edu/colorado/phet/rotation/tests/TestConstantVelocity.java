package edu.colorado.phet.rotation.tests;

import edu.colorado.phet.common.motion.model.MotionModel;
import edu.colorado.phet.common.motion.model.PositionDriven;
import edu.colorado.phet.common.motion.model.TimeData;

/**
 * User: Sam Reid
 * Date: Dec 29, 2006
 * Time: 11:47:31 PM
 */

public class TestConstantVelocity {
    public static void main( String[] args ) {
        PositionDriven updateRule = new PositionDriven();
        MotionModel model = new MotionModel();
        model.setUpdateStrategy( updateRule );
        System.out.println( "init state=" + model.getLastState() );
        for( int i = 0; i <= 100; i++ ) {
            model.stepInTime( 1.0 );
            System.out.println( "i = " + i + ", state=" + model.getLastState() );
        }

        TimeData[] timeData = model.getAccelerationTimeSeries( 5 );
        for( int i = 0; i < timeData.length; i++ ) {
            TimeData data = timeData[i];
            System.out.println( "i = " + i + ", data=" + data );
        }
    }
}
