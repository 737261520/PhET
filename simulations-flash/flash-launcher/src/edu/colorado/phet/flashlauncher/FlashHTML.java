package edu.colorado.phet.flashlauncher;

// Functions to generate and write Flash HTML files

import edu.colorado.phet.flashlauncher.util.AnnotationParser;
import edu.colorado.phet.flashlauncher.util.XMLUtils;

import java.io.*;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;

//import edu.colorado.phet.buildtools.util.FileUtils;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Feb 1, 2009
 * Time: 4:22:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class FlashHTML {
    //    private static final String HTML_TEMPLATE = "flash-template.html";
    private static final String NULL = "null";

    public static String distribution_tag_dummy = "@@DISTRIBUTION_TAG@@";
    public static String installation_timestamp_dummy = "@@INSTALLATION_TIMESTAMP@@";
    public static String installer_creation_timestamp_dummy = "@@INSTALLER_CREATION_TIMESTAMP@@";

    public static String generateHTML( String simName, String language, String country, String deployment,
                                       String distributionTag, String installationTimestamp, String installerCreationTimestamp,
                                       String versionMajor, String versionMinor, String versionDev, String versionRevision,
                                       String versionTimestamp, String simDev, String bgcolor, String encodedSimXML,
                                       String encodedCommonXML, String minimumFlashMajorVersion, String HTML_TEMPLATE,
                                       String agreementVersion, String encodedAgreementHTML, String creditsString,
                                       String titleString
                                    ) throws IOException {
        String s = "";
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( HTML_TEMPLATE );
        BufferedReader bufferedReader = null;
        //todo: pass in this content instead of having this switch
        if ( inputStream == null ) {
            bufferedReader = new BufferedReader( new FileReader( HTML_TEMPLATE ) );
        }
        else {
            bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
        }
        String line = bufferedReader.readLine();
        while ( line != null ) {
            s += line;
            line = bufferedReader.readLine();
            if ( line != null ) {
                s += System.getProperty( "line.separator" );
            }
        }

        // get the integer value from the hex of bgcolor
        String bgcolorint = String.valueOf( Integer.parseInt( bgcolor.substring( 1 ), 16 ) );

        // TODO: a more elegant way?
        String flashVars = "languageCode=@@language@@&countryCode=@@country@@&internationalization=@@encodedSimXML@@" +
                           "&commonStrings=@@encodedCommonXML@@&versionMajor=@@versionMajor@@&versionMinor=@@versionMinor@@&" +
                           "versionDev=@@versionDev@@&versionRevision=@@versionRevision@@&simName=@@simName@@&simDeployment=@@deployment@@&" +
                           "simDev=@@simDev@@&simDistributionTag=@@distributionTag@@&installationTimestamp=@@installationTimestamp@@&" +
                           "installerCreationTimestamp=@@installerCreationTimestamp@@&versionTimestamp=@@versionTimestamp@@&" +
                           "bgColor=@@bgcolorint@@&agreementVersion=@@agreementVersion@@&agreementText=@@agreementText@@&" +
                           "creditsText=@@creditsText@@";

        s = s.replaceAll( "@@flashVars@@", flashVars );

        s = s.replaceAll( "@@simName@@", simName );
        s = s.replaceAll( "@@language@@", language );
        s = s.replaceAll( "@@country@@", country );
        s = s.replaceAll( "@@deployment@@", deployment );
        s = s.replaceAll( "@@simDev@@", simDev );
        s = s.replaceAll( "@@distributionTag@@", distributionTag );
        s = s.replaceAll( "@@installationTimestamp@@", installationTimestamp );
        s = s.replaceAll( "@@installerCreationTimestamp@@", installerCreationTimestamp );
        s = s.replaceAll( "@@versionMajor@@", versionMajor );
        s = s.replaceAll( "@@versionMinor@@", versionMinor );
        s = s.replaceAll( "@@versionDev@@", versionDev );
        s = s.replaceAll( "@@versionRevision@@", versionRevision );
        s = s.replaceAll( "@@versionTimestamp@@", versionTimestamp );
        s = s.replaceAll( "@@bgcolor@@", bgcolor );
        s = s.replaceAll( "@@encodedSimXML@@", encodedSimXML );
        s = s.replaceAll( "@@encodedCommonXML@@", encodedCommonXML );
        s = s.replaceAll( "@@minimumFlashMajorVersion@@", minimumFlashMajorVersion );
        s = s.replaceAll( "@@bgcolorint@@", bgcolorint );
        s = s.replaceAll( "@@locale@@", localeString( language, country ) );
        s = s.replaceAll( "@@agreementVersion@@", agreementVersion );
        s = s.replaceAll( "@@agreementText@@", encodedAgreementHTML );
        s = s.replaceAll( "@@creditsText@@", parseCredits( creditsString ) );
        s = s.replaceAll( "@@simTitle@@", XMLUtils.HTMLEntityEncode( titleString ) );

        return s;
    }

    public static String localeString( String language, String country ) {
        String ret = language;
        if ( !country.equals( NULL ) ) {
            ret += "_" + country;
        }
        return ret;
    }

    private static String rawFile( String filename ) throws IOException {
        return rawFile( new File( filename ) );
    }

    public static String rawFile( File inFile ) throws IOException {
        // BAD BAD BAD! FileUtils depends on TranslationDiscrepancy which depends on most of buildtools, which depends on phetcommon
        // using this would require all of buildtools, phetcommon, and many external libraries to be included in each JAR
        // TODO: maybe in the future we can remove this, however I need a fix right now.
        //return FileUtils.loadFileAsString( inFile );


        // TODO: duplicating FileUtils.loadFileAsString, needs to be fixed
        InputStream inStream = new FileInputStream( inFile );

        ByteArrayOutputStream outStream;

        try {
            outStream = new ByteArrayOutputStream();

            int c;
            while ( ( c = inStream.read() ) >= 0 ) {
                outStream.write( c );
            }
            outStream.flush();
        }
        finally {
            inStream.close();
        }

        return new String( outStream.toByteArray(), "utf-8" );
    }

    public static String encodeXML( String rawXML ) throws UnsupportedEncodingException {
        return URLEncoder.encode( rawXML, "UTF-8" );
    }

    public static String encodeXMLFile( File file ) throws IOException, FileNotFoundException {
        return encodeXML( rawFile( file ) );
    }

    private static String parseCredits( String creditsString ) throws UnsupportedEncodingException {
        if ( creditsString.trim().length() == 0 ) {
            //all simulations should specify credits eventually
            return "No credits found.";
        }

        // The keys in this file are "software-development" "design-team" "lead-design" and "interviews"; these are used as suffixes
        // replace these so we can translate them in Flash
        creditsString = creditsString.replaceAll( "software-development", "{0}" );
        creditsString = creditsString.replaceAll( "design-team", "{1}" );
        creditsString = creditsString.replaceAll( "lead-design", "{2}" );
        creditsString = creditsString.replaceAll( "interviews", "{3}" );

        AnnotationParser.Annotation t = AnnotationParser.parse( creditsString );
        HashMap map = t.getMap();
        ArrayList keys = t.getKeyOrdering();
        String credits = "";
        for ( Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            String value = (String) map.get( key );
            credits += key + ": " + value + "<br>";
        }
        return encodeXML(credits);
    }

    public static String extractTitleFromXML( File xmlFile ) {
        try {
            String xmlString = rawFile( xmlFile );

            Document document = XMLUtils.toDocument( xmlString );

            //System.out.println( XMLUtils.toString( document ) );

            NodeList strings = document.getElementsByTagName( "string" );

            for( int i = 0; i < strings.getLength(); i++ ) {
                Element element = (Element) strings.item( i );

                String key = element.getAttribute( "key" );

                //System.out.println( "key: " + key );

                if( !key.equals( "simTitle" ) ) {
                    continue;
                }

                String value = element.getAttribute( "value" );

                //System.out.println( "value: " + value );

                return value;
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (TransformerException e) {
            e.printStackTrace();
            return null;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    public static void main(String[] args) {
        String str = extractTitleFromXML( new File( "/home/jon/phet/svn/trunk/simulations-flash/simulations/arithmetic/data/arithmetic/localization/arithmetic-strings_en.xml" ) );

        System.out.println( "Title is: " + str );
        
    }
    */

}