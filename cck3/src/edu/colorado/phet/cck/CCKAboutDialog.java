package edu.colorado.phet.cck;

import edu.colorado.phet.common.application.PhetApplication;
import edu.colorado.phet.common.view.PhetLookAndFeel;
import edu.colorado.phet.common.view.VerticalLayoutPanel;
import edu.colorado.phet.common.view.util.ImageLoader;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.common.view.util.SwingUtils;
import org.srr.localjnlp.ServiceSource;

import javax.jnlp.BasicService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: Sam Reid
 * Date: Oct 9, 2006
 * Time: 2:46:38 AM
 * Copyright (c) Oct 9, 2006 by Sam Reid
 */

public class CCKAboutDialog extends JDialog {
    private VerticalLayoutPanel contentPanel;

    public CCKAboutDialog( PhetApplication phetApplication ) throws HeadlessException {
        super( phetApplication.getPhetFrame(), SimStrings.get( "Common.HelpMenu.AboutTitle" ) + " " + phetApplication.getTitle() );
        contentPanel = new VerticalLayoutPanel();
        JLabel iconLabel = null;
        try {
            BufferedImage image = ImageLoader.loadBufferedImage( PhetLookAndFeel.PHET_LOGO_120x50 );
            iconLabel = new JLabel( new ImageIcon( image ) );
            iconLabel.setBorder( BorderFactory.createLineBorder( Color.black ) );
            iconLabel.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
            iconLabel.addMouseListener( new MouseAdapter() {
                public void mousePressed( MouseEvent e ) {
                    showPhetPage();
                }
            } );
        }
        catch( IOException e ) {
            e.printStackTrace();
        }

        JTextArea jTextArea = new JTextArea() {
            protected void paintComponent( Graphics g ) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                super.paintComponent( g );
            }
        };
        jTextArea.setEditable( false );
        jTextArea.setBorder( BorderFactory.createEtchedBorder() );
        String javaVersion = SimStrings.get( "Common.HelpMenu.JavaVersion" ) + ": " + System.getProperty( "java.version" );
        final String msg = phetApplication.getTitle() + " (" + phetApplication.getVersion() + ")\n" + phetApplication.getDescription() + "\n\n" + javaVersion + "\n";
        jTextArea.setText( msg );

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new FlowLayout() );

        JButton copyrightAndLicenseInfo = new JButton( "<html>License</html>" );
        copyrightAndLicenseInfo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showLicenseInfo();
            }
        } );
        buttonPanel.add( copyrightAndLicenseInfo );

        JButton phetHomepage = new JButton( "PhET homepage" );
        phetHomepage.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                showPhetPage();
            }
        } );
        buttonPanel.add( phetHomepage );

        JButton close = new JButton( "Close" );
        close.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                close();
            }
        } );
        buttonPanel.add( close );

        addSpacing();
        contentPanel.setFillNone();
        contentPanel.add( iconLabel );
        contentPanel.setFillHorizontal();
        addSpacing();
        contentPanel.add( jTextArea );
        addSpacing();
        contentPanel.add( buttonPanel );
        addSpacing();
        setContentPane( contentPanel );
        pack();
        SwingUtils.centerDialogInParent( this );
    }

    private void showLicenseInfo() {
        String s = readText( "phet-license.txt" );
        s += System.getProperty( "line.separator" );
        s += System.getProperty( "line.separator" );
        s += readText( "cck-projects.txt" );
        JTextArea jTextArea = new JTextArea( s );
        jTextArea.setColumns( 45 );
        jTextArea.setLineWrap( true );
        jTextArea.setWrapStyleWord( true );
        jTextArea.setEditable( false );
        //this line fails due to this known sun bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4545951
//        JOptionPane.showMessageDialog( getOwner(), jTextArea, "License Information", JOptionPane.INFORMATION_MESSAGE );

        //so we use their recommended workaround.
        JOptionPane optionPane = new JOptionPane( jTextArea, JOptionPane.INFORMATION_MESSAGE );
        JDialog dialog = optionPane.createDialog( null, "License Information" );

        //This forces correct resizing
        jTextArea.invalidate();
        dialog.pack();

        dialog.show();
    }

    private static String readText( String textFilename ) {
        String text = new String();
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource( textFilename );
            InputStream inputStream = url.openStream();
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
            String line = bufferedReader.readLine();
            while( line != null ) {
                text += line;
                line = bufferedReader.readLine();
                if( line != null ) {
                    text += System.getProperty( "line.separator" );
                }
            }
        }
        catch( FileNotFoundException e ) {
            e.printStackTrace();
        }
        catch( IOException e ) {
            e.printStackTrace();
        }
        return text;
    }

    private void close() {
        dispose();
    }

    private void addSpacing() {
        contentPanel.add( Box.createVerticalStrut( 15 ) );
    }

    private void showPhetPage() {
        ServiceSource ss = new ServiceSource();
        BasicService bs = ss.getBasicService();
        try {
            URL url = new URL( "http://phet.colorado.edu" );
            bs.showDocument( url );
        }
        catch( MalformedURLException e ) {
            e.printStackTrace();
        }
    }

}
