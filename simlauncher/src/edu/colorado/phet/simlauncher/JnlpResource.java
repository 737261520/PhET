/* Copyright 2003-2004, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */
package edu.colorado.phet.simlauncher;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * ImageResource
 *
 * @author Ron LeMaster
 * @version $Revision$
 */
public class JnlpResource extends SimResource {

    public JnlpResource( URL url, File localRoot ) {
        super( url, localRoot );
    }

    public void download() {
        super.download();

        // Modify codebase of the file
        JnlpFile jnlpFile = null;
        try {
            jnlpFile = new JnlpFile( getLocalFile() );
        }
        catch( InvalidJnlpException e ) {
            e.printStackTrace();
        }
        URL orgCodebase = null;
        try {
            orgCodebase = new URL( jnlpFile.getCodebase() );
        }
        catch( MalformedURLException e ) {
            e.printStackTrace();
        }
        String localPath = getLocalFileName();
        if( System.getProperty( "os.name" ).toLowerCase().contains( "windows" ) && localPath.contains( ":" ) ) {
            localPath = localPath.substring( localPath.indexOf( ':' ) + 1 );
        }
        String newCodebase = "file://" + localPath;
        jnlpFile.setCodebase( newCodebase );

        try {
            BufferedWriter out = new BufferedWriter( new FileWriter( getLocalFileName() ) );
            out.write( jnlpFile.toString() );
            out.close();
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public String getLocalFileName() {
        return getLocalFile().getAbsolutePath();
    }

    public JarResource[] getJarResources() {
        JnlpFile jnlpFile = null;
        try {
            jnlpFile = new JnlpFile( getLocalFile() );
        }
        catch( InvalidJnlpException e ) {
            e.printStackTrace();
        }
        String[] urlStrings = jnlpFile.getJarUrls();
        JarResource[] jarResources = new JarResource[urlStrings.length];
        for( int i = 0; i < urlStrings.length; i++ ) {
            String urlString = urlStrings[i];
            URL url = null;
            try {
                url = new URL( urlString );
            }
            catch( MalformedURLException e ) {
                e.printStackTrace();
            }
            JarResource jarResource = new JarResource( url, getLocalFile() );
            jarResources[i] = jarResource;
        }
        return jarResources;
    }

    /**
     * For debug
     */
    public String getLocalContents() {
        JnlpFile2 jnlpFile = new JnlpFile2( getLocalFile().getAbsolutePath() );
        String s = jnlpFile.getContents();
        return s;
    }
}
