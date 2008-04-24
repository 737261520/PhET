package edu.colorado.phet.fitness.util;

import java.awt.*;

import edu.colorado.phet.common.phetcommon.math.MathUtil;

/**
 * Created by: Sam
 * Apr 24, 2008 at 2:51:21 PM
 */
public class FitnessUtil {
    public static Color brighter( Color color, int delta ) {
        return new Color( brighter( color.getRed(), delta ),
                          brighter( color.getGreen(), delta ),
                          brighter( color.getBlue(), delta ) );
    }

    private static int brighter( int value, int delta ) {
        return (int) MathUtil.clamp( 0, value + delta, 255 );
    }
}
