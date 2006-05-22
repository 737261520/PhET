/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

import edu.colorado.phet.simlauncher.JnlpResource;
import edu.colorado.phet.simlauncher.JarResource;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * ImageResourceTest
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class JarResourceTest {
    public static void main( String[] args ) throws IOException {
        URL url = null;
        try {
            url = new URL( "http://www.colorado.edu/physics/phet/simulations/cck/cck.jnlp" );
        }
        catch( MalformedURLException e ) {
            e.printStackTrace();
        }
        File localRoot = new File( "/phet/temp" );
        JnlpResource jr  = new JnlpResource( url, localRoot );

        JarResource[] jarResources = jr.getJarResources();
        for( int i = 0; i < jarResources.length; i++ ) {
            JarResource jarResource = jarResources[i];
            // See if we can reference the resource
            jarResource.download();
            System.out.println( "jarResource.getLocalFile().getAbsolutePath() = " + jarResource.getLocalFile().getAbsolutePath() );
            System.out.println( "jarResource.getLocalFile().exists() = " + jarResource.getLocalFile().exists() );
        }
    }
}
