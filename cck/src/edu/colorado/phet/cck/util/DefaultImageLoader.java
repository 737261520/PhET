package edu.colorado.phet.cck.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Loads ARGB BufferedImages, without alpha patching.
 */
public class DefaultImageLoader implements ImageLoader {
    ClassLoader loader;
    Component observer;

    public DefaultImageLoader( ClassLoader loader, Component observer ) {
        this.loader = loader;
        this.observer = observer;
    }

    public URL getResource( String name ) {
        return loader.getResource( name );
    }

    public BufferedImage loadBufferedImage( String name ) {
        //URL fileLoc=findResource("images/"+name,c);
        URL fileLoc = loader.getResource( name );
        try {
            Image img = observer.getToolkit().createImage( fileLoc );
            BufferedImage argb = ImageConverter.toBufferedImageARGB( img, observer );
            return argb;
        }
        catch( Exception e ) {
            throw new RuntimeException( e.toString() + " -> looked for resource : " + name + ", fileLoc=" + fileLoc );
        }
    }
}
