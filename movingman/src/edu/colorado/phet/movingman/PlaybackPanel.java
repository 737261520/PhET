/** Sam Reid*/
package edu.colorado.phet.movingman;

import edu.colorado.phet.common.view.util.ImageLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * User: Sam Reid
 * Date: Nov 6, 2004
 * Time: 3:21:19 PM
 * Copyright (c) Nov 6, 2004 by Sam Reid
 */
public class PlaybackPanel extends JPanel {
    private JButton play;
    private JButton pause;
    private JButton rewind;
    private JButton slowMotion;
    private MovingManModule module;

    public PlaybackPanel( final MovingManModule module ) throws IOException {
        this.module = module;
        ImageIcon pauseIcon = new ImageIcon( new ImageLoader().loadImage( "images/icons/java/media/Pause24.gif" ) );

        pause = new JButton( "Pause", pauseIcon );
        pause.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                //pausing from playback leaves it alone
                module.setPaused( true );
            }
        } );
        ImageIcon playIcon = new ImageIcon( new ImageLoader().loadImage( "images/icons/java/media/Play24.gif" ) );
        play = new JButton( "Playback", playIcon );
        play.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                module.startPlaybackMode( 1.0 );
            }
        } );

        ImageIcon rewindIcon = new ImageIcon( new ImageLoader().loadImage( "images/icons/java/media/Rewind24.gif" ) );
        rewind = new JButton( "Rewind", rewindIcon );
        rewind.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                module.rewind();
                module.setPaused( true );
            }
        } );

        ImageIcon slowIcon = new ImageIcon( new ImageLoader().loadImage( "images/icons/java/media/StepForward24.gif" ) );
        slowMotion = new JButton( "Slow Playback", slowIcon );
        slowMotion.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                module.startPlaybackMode( .4 );
            }
        } );

        add( play );
        add( slowMotion );
        add( pause );
        add( rewind );
//        JLabel separator = new JLabel();
//        separator.setPreferredSize( new Dimension( 20, 10 ) );
//        add( separator );
//        add( phetIconLabel );

        MovingManModule.Listener listener = new MovingManModule.Listener() {
            public void recordingStarted() {
                setButtons( false, false, false, false );
            }

            public void recordingPaused() {
                setButtons( true, true, false, false );
            }

            public void recordingFinished() {
                setButtons( true, true, false, false );
            }

            public void playbackFinished() {
                setButtons( false, false, false, true );
            }

            public void playbackStarted() {
                setButtons( false, false, true, true );
            }

            public void playbackPaused() {
                setButtons( true, true, false, true );
            }

            public void modeChanged() {
            }

            public void reset() {
                setButtons( false, false, false, false );
            }

            public void rewind() {
                setButtons( true, true, false, false );
            }
        };
        module.addListener( listener );
    }

    private void setButtons( boolean playBtn, boolean slowBtn, boolean pauseBtn, boolean rewindBtn ) {
        play.setEnabled( playBtn );
        slowMotion.setEnabled( slowBtn );
        pause.setEnabled( pauseBtn );
        rewind.setEnabled( rewindBtn );
    }

}
