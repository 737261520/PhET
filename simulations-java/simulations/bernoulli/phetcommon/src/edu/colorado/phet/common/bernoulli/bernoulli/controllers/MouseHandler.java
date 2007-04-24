package edu.colorado.phet.common.bernoulli.bernoulli.controllers;

import java.awt.event.MouseEvent;

/**
 * User: Sam Reid
 * Date: Sep 16, 2003
 * Time: 10:08:42 PM
 *
 */
public interface MouseHandler {

    boolean canHandleMousePress(MouseEvent event);

    void mousePressed(MouseEvent event);

    void mouseDragged(MouseEvent event);

    void mouseReleased(MouseEvent event);

    void mouseEntered(MouseEvent event);

    void mouseExited(MouseEvent event);
}
