package edu.colorado.phet.common.phetcommon.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Base class for all properties file interfaces.
 * Setting a property value stores it in the file immediately.
 * <p>
 * This class is implemented using composition instead of inheritance
 * because it's not appropriate to expose the entire File interface.
 * The interface should be limited to getting and setting properties.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class AbstractPropertiesFile {
    
    private final File propertiesFile;
    private final Properties properties;
    private String header;

    public AbstractPropertiesFile( String filename ) {
        this( new File( filename ) );
    }
    
    public AbstractPropertiesFile( File file ) {
        this.propertiesFile = file;
        this.properties = loadProperties( file );
        this.header = null;
    }

    /*
     * Loads the properties from the file, if it exists.
     */
    private static Properties loadProperties( File file ) {
        Properties properties = new Properties();
        if ( file.exists() ) {
            try {
                properties.load( new BufferedInputStream( new FileInputStream( file ) ) );
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        return properties;
    }
    
    /**
     * Sets the header in the properties file.
     * @param header
     */
    public void setHeader( String header ) {
        this.header = header;
        store();
    }
    
    /**
     * Does this properties file exist?
     * @return
     */
    public boolean exists() {
        return propertiesFile.exists();
    }
    
    /**
     * Gets the names of all properties in the file.
     * @return
     */
    public Enumeration getPropertyNames() {
        return properties.propertyNames();
    }
    
    /*
     * Store the properties to the file.
     */
    private void store() {
        try {
            properties.store( new FileOutputStream( propertiesFile ), header );
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
    }
    
    /*
     * Setting a property stores it immediately.
     * <p>
     * This method is protected because subclasses should not expose key values,
     * they should have set/get methods for each property.
     * 
     * @param key 
     * @param value
     */
    protected void setProperty( String key, String value ) {
        properties.setProperty( key, value );
        store();
    }
    
    protected void setProperty( String key, int value ) {
        properties.setProperty( key, String.valueOf( value ) );
    }
    
    /*
     * Gets a property value as a String.
     * <p>
     * This method is protected because subclasses should not expose key values,
     * they should have set/get methods for each property.
     * @param key
     * @return String value
     */
    protected String getProperty( String key ) {
        return properties.getProperty( key );
    }
    
    /*
     * Gets a property as an integer value.
     * If the property value can't be converted to an integer, -1 is returned.
     * <p>
     * This method is protected because subclasses should not expose key values,
     * they should have set/get methods for each property.
     * 
     * @param key
     * @return int value
     */
    protected int getPropertyInt( String key ) {
        int i = -1;
        String s = getProperty( key );
        if ( s == null ) {
            System.err.println( "PropertiesFile.getPropertyInt: " + key + " is missing from file " + propertiesFile.getAbsolutePath() );
        }
        else {
            try {
                i = Integer.parseInt( s );
            }
            catch ( NumberFormatException e ) {
                System.err.println( "PropertiesFile.getPropertyInt: " + key + " is not an integer in file " + propertiesFile.getAbsolutePath() );
            }
        }
        return i;
    }
    
    public String toString() {
        return properties.toString();
    }
}
