/* Copyright 2004, Sam Reid */
package edu.colorado.phet.qm.tests.thirdparty;

import java.applet.Applet;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.*;

/**
 * User: Sam Reid
 * Date: Feb 9, 2006
 * Time: 9:13:28 PM
 * Copyright (c) Feb 9, 2006 by Sam Reid
 */
public class QuantumBox extends Applet implements ComponentListener {
    QuantumBoxFrame ogf;

    void destroyFrame() {
        if( ogf != null ) {
            ogf.dispose();
        }
        ogf = null;
        repaint();
    }

    boolean started = false;

    public void init() {
        addComponentListener( this );
    }

    void showFrame() {
        if( ogf == null ) {
            started = true;
            ogf = new QuantumBoxFrame( this );
            ogf.init();
            repaint();
        }
    }

    public void paint( Graphics g ) {
        String s = "Applet is open in a separate window.";
        if( !started ) {
            s = "Applet is starting.";
        }
        else if( ogf == null ) {
            s = "Applet is finished.";
        }
        else {
            ogf.show();
        }
        g.drawString( s, 10, 30 );
    }

    public void componentHidden( ComponentEvent e ) {
    }

    public void componentMoved( ComponentEvent e ) {
    }

    public void componentShown( ComponentEvent e ) {
        showFrame();
    }

    public void componentResized( ComponentEvent e ) {
    }

    public void destroy() {
        if( ogf != null ) {
            ogf.dispose();
        }
        ogf = null;
        repaint();
    }
}
