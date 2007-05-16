package edu.colorado.phet.rotation.timeseries;

import edu.colorado.phet.rotation.RotationResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * User: Sam Reid
 * Date: Jan 9, 2007
 * Time: 12:47:07 PM
 */

public class TimeSeriesControlPanel extends JPanel {
    private TwoModeButton recordButton;
    private TwoModeButton playbackButton;
    private TimeSeriesButton slowMotionButton;
    private TimeSeriesButton rewindButton;
    private TimeSeriesButton stepButton;
    private TimeSeriesButton clearButton;
    private TimeSeriesModel timeSeriesModel;

    public TimeSeriesControlPanel( final TimeSeriesModel timeSeriesModel ) {
        this.timeSeriesModel = timeSeriesModel;
        setLayout( new GridBagLayout() );
        GridBagConstraints gridBagConstraints = new GridBagConstraints( GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 );
        ButtonMode recordMode = new ButtonMode( "Record", loadIcon( "Play24.gif" ), new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.setRecording();
            }
        } );
        ButtonMode pauseRecordMode = new ButtonMode( "Pause Recording", loadIcon( "Pause24.gif" ), new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.setPaused();
            }
        } );
        recordButton = new TwoModeButton( recordMode, pauseRecordMode );

        ButtonMode playMode = new ButtonMode( "Playback", loadIcon( "Play24.gif" ), new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.setPlayback();
            }
        } );
        ButtonMode pausePlayback = new ButtonMode( "Pause Playback", loadIcon( "Pause24.gif" ), new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.setPaused();
            }
        } );
        playbackButton = new TwoModeButton( playMode, pausePlayback );
        slowMotionButton = new TimeSeriesButton( "Slow Playback", "StepForward24.gif" );
        slowMotionButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.setSlowMotion();
            }
        } );
        rewindButton = new TimeSeriesButton( "Rewind", "Rewind24.gif" );
        rewindButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.rewind();
            }
        } );
        stepButton = new TimeSeriesButton( "Step", "StepForward24.gif" );
        stepButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.step();
            }
        } );
        clearButton = new TimeSeriesButton( "Clear", "Stop24.gif" );
        clearButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                timeSeriesModel.clear();
            }
        } );

        add( recordButton, gridBagConstraints );
        add( playbackButton, gridBagConstraints );
        add( slowMotionButton, gridBagConstraints );
        add( stepButton, gridBagConstraints );
        add( rewindButton, gridBagConstraints );
        add( clearButton, gridBagConstraints );
        timeSeriesModel.addListener( new TimeSeriesModel.Listener() {
            public void stateChanged() {
                updateButtons();
            }

            public void clear() {
            }
        } );
        updateButtons();
        setBorder( BorderFactory.createLineBorder( Color.lightGray ) );
    }

    private ImageIcon loadIcon( String s ) {
        try {
            return new ImageIcon( RotationResources.loadBufferedImage( "icons/java/media/" + s ) );
        }
        catch( IOException e ) {
            e.printStackTrace();
            throw new RuntimeException( s );
        }
    }

    private void updateButtons() {
        if( timeSeriesModel.isRecording() && !timeSeriesModel.isPaused() ) {
            recordButton.setPauseMode();
        }
        else {
            recordButton.setGoMode();
        }
        if( timeSeriesModel.isPlayback() && !timeSeriesModel.isPaused() ) {
            playbackButton.setPauseMode();
        }
        else {
            playbackButton.setGoMode();
        }
        slowMotionButton.setEnabled( !timeSeriesModel.isSlowMotion() || timeSeriesModel.isPaused() );
        stepButton.setEnabled( timeSeriesModel.getPlaybackIndex() < timeSeriesModel.numPlaybackStates() );
        rewindButton.setEnabled( timeSeriesModel.getPlaybackIndex() > 0 && timeSeriesModel.numPlaybackStates() > 0 );
        clearButton.setEnabled( timeSeriesModel.numPlaybackStates() > 0 );
    }

    private static class ButtonMode {
        private String label;
        private Icon icon;
        private ActionListener actionListener;

        public ButtonMode( String label, Icon icon, ActionListener actionListener ) {
            this.label = label;
            this.icon = icon;
            this.actionListener = actionListener;
        }

        public void actionPerformed( ActionEvent e ) {
            actionListener.actionPerformed( e );
        }

        public String getLabel() {
            return label;
        }

        public Icon getIcon() {
            return icon;
        }
    }

    static class TwoModeButton extends JButton {
        ButtonMode goMode;
        ButtonMode pauseMode;

        ButtonMode currentMode;

        public TwoModeButton( ButtonMode goMode, ButtonMode pauseMode ) {
            this.goMode = goMode;
            this.pauseMode = pauseMode;
            setMode( goMode );
            addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    currentMode.actionPerformed( e );
                }
            } );
        }

        private void setMode( ButtonMode mode ) {
            currentMode = mode;
            setText( mode.getLabel() );
            setIcon( mode.getIcon() );
        }

        public void setPauseMode() {
            setMode( pauseMode );
        }

        public void setGoMode() {
            setMode( goMode );
        }
    }

    static class TimeSeriesButton extends JButton {
        private ImageIcon icon;
        private String label;

        public TimeSeriesButton( String label, String imageIcon ) {
            super( label );
            try {
                icon = new ImageIcon( RotationResources.loadBufferedImage( "icons/java/media/" + imageIcon ) );
                this.label = label;
                setIcon( icon );
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }

        public void restoreIcon() {
            setIcon( icon );
        }

        public void restoreLabel() {
            setText( label );
        }

        public void restore() {
            restoreIcon();
            restoreLabel();
        }
    }
}
