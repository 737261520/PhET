/**
 * Class: Config
 * Package: edu.colorado.phet.nuclearphysics
 * Author: Another Guy
 * Date: Mar 6, 2004
 */
package edu.colorado.phet.nuclearphysics;

import edu.colorado.phet.nuclearphysics.model.PotentialProfile;

public class Config {

    public static PotentialProfile u235PotentialProfile = new PotentialProfile( 500, 250, 75 );
    public static double AlphaLocationUncertaintySigmaFactor = 0.4;
//    public static double AlphaLocationUncertaintySigmaFactor = 0.333;

    public static final double neutronSpeed = 400;
    public static final double fissionDisplacementVelocity = 2;
}
