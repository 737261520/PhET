/* Copyright 2005, University of Colorado */

/*
 * CVS Info -
 * Filename : $Source$
 * Branch : $Name$
 * Modified by : $Author$
 * Revision : $Revision$
 * Date modified : $Date$
 */

package edu.colorado.phet.fourier.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import edu.colorado.phet.common.math.MathUtil;
import edu.colorado.phet.common.util.SimpleObserver;
import edu.colorado.phet.common.view.phetcomponents.PhetJComponent;
import edu.colorado.phet.common.view.phetgraphics.GraphicLayerSet;
import edu.colorado.phet.common.view.phetgraphics.PhetGraphic;
import edu.colorado.phet.common.view.phetgraphics.PhetShapeGraphic;
import edu.colorado.phet.common.view.util.SimStrings;
import edu.colorado.phet.fourier.FourierConfig;
import edu.colorado.phet.fourier.event.HarmonicColorChangeEvent;
import edu.colorado.phet.fourier.event.HarmonicColorChangeListener;
import edu.colorado.phet.fourier.event.HarmonicFocusEvent;
import edu.colorado.phet.fourier.event.HarmonicFocusListener;
import edu.colorado.phet.fourier.model.Harmonic;


/**
 * AmplitudeSlider is a slider for controlling harmonic amplitude.
 * The user can change the amplitude by (1) clicking anywhere above,
 * below or on the colored section of the slider, (2) click and drag
 * the knob, (3) typing a value into the text field, or (4) selecting
 * the text field and using the up/down arrow keys.
 * <br>
 * The clickable area (aka, the click zone) is indicated by a hand
 * cursor.  The knob is indicated by an "up down" cursor.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 * @version $Revision$
 */
public class AmplitudeSlider extends GraphicLayerSet
    implements SimpleObserver, HarmonicColorChangeListener {

    //----------------------------------------------------------------------------
    // Class data
    //----------------------------------------------------------------------------
    
    // Layers
    private static final double CLICK_ZONE_LAYER = 1;
    private static final double TRACK_LAYER = 2;
    private static final double KNOB_LAYER = 3;
    private static final double VALUE_LAYER = 4;
    private static final double LABEL_LAYER = 5;

    // Label parameters
    private static final Color LABEL_COLOR = Color.BLACK;
    private static final Font LABEL_FONT = new Font( "Lucida Sans", Font.PLAIN, 12 );
    private static final Font LABEL_SUBSCRIPT_FONT = new Font( "Lucida Sans", Font.PLAIN, 12 );
    private static final int LABEL_Y_OFFSET = 45; // above the maximum height of the slider track
    
    // Value parameters
    private static final double MAX_AMPLITUDE = FourierConfig.MAX_HARMONIC_AMPLITUDE;
    private static final double VALUE_STEP = 0.01;
    private static final String VALUE_FORMAT = "0.00";
    private static final int VALUE_COLUMNS = 3;
    private static final Font VALUE_FONT = new Font( "Lucida Sans", Font.PLAIN, 12 );
    private static final int VALUE_Y_OFFSET = 27; // above the maximum height of the slider track

    // Knob parameters
    private static final Color KNOB_FILL_COLOR = Color.BLACK;
    private static final Color KNOB_STROKE_COLOR = Color.BLACK;
    private static final Stroke KNOB_STROKE = new BasicStroke( 1f );

    // Track parameters
    private static final Dimension DEFAULT_TRACK_SIZE = new Dimension( 40, 100 );
    private static final Color DEFAULT_TRACK_COLOR = Color.WHITE;
    private static final Stroke TRACK_STROKE = new BasicStroke( 1f );
    private static final Color TRACK_BORDER_COLOR = Color.BLACK;
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private Harmonic _harmonic;
    private Dimension _maxSize;
    private SubscriptedSymbol _labelGraphic;
    private PhetGraphic _valueGraphic;
    private JTextField _valueTextField;
    private NumberFormat _valueFormatter;
    private PhetShapeGraphic _trackGraphic;
    private Rectangle _trackRectangle;
    private Color _trackColor;
    private PhetShapeGraphic _knobGraphic;
    private Rectangle _knobRectangle;
    private PhetShapeGraphic _clickZoneGraphic;
    private Rectangle _clickZoneRectangle;
    private EventListenerList _listenerList;
    private Point _somePoint;
    
    //----------------------------------------------------------------------------
    // Constructors & finalizers
    //----------------------------------------------------------------------------
    
    /**
     * Sole constructor.
     * 
     * @param component the parent Component
     * @param harmonic the model that this slider controls
     */
    public AmplitudeSlider( Component component, Harmonic harmonic ) {
        super( component );

        // Model
        assert ( harmonic != null );
        _harmonic = harmonic;
        _harmonic.addObserver( this );

        // Misc initialization
        {
            _maxSize = new Dimension( DEFAULT_TRACK_SIZE );
            _listenerList = new EventListenerList();
            _somePoint = new Point();
        }

        // Label (An)
        {          
            String symbol = SimStrings.get( "symbol.amplitude" );
            String subscript = String.valueOf( _harmonic.getOrder() + 1 );
            _labelGraphic = new SubscriptedSymbol( component, symbol, subscript, LABEL_FONT, LABEL_COLOR );
            _labelGraphic.setLocation( 0, 0 );
        }
        
        // Value
        {
            _valueFormatter = new DecimalFormat( VALUE_FORMAT );
            _valueTextField = new JTextField( _valueFormatter.format( 0.0 ) );
            _valueTextField.setFont( VALUE_FONT );
            _valueTextField.setColumns( VALUE_COLUMNS );
            _valueGraphic = PhetJComponent.newInstance( component, _valueTextField );
            _valueGraphic.setName( "AmplitudeSlider.value" );
            _valueGraphic.centerRegistrationPoint();
            _valueGraphic.setLocation( 0, 0 );
        }

        // Click Zone
        {
           _clickZoneGraphic = new PhetShapeGraphic( component );
           _clickZoneRectangle = new Rectangle( 1, 1, _maxSize.width, _maxSize.height );
           _clickZoneGraphic.setShape( _clickZoneRectangle );
           _clickZoneGraphic.setName( "AmplitudeSlider.clickZone" );
           _clickZoneGraphic.setPaint( new Color( 0, 0, 0, 0 ) ); // transparent
           _clickZoneGraphic.centerRegistrationPoint();
           _clickZoneGraphic.setLocation( 0, 0 );
        }
        
        // Slider Track
        {
            _trackRectangle = new Rectangle();
            _trackColor = DEFAULT_TRACK_COLOR;
            _trackGraphic = new PhetShapeGraphic( component );
            _trackGraphic.setName( "AmplitudeSlider.track" );
            _trackGraphic.setShape( _trackRectangle );
            _trackGraphic.setPaint( _trackColor );
            _trackGraphic.setBorderColor( TRACK_BORDER_COLOR );
            _trackGraphic.setStroke( TRACK_STROKE );
            _trackGraphic.setLocation( 0, 0 );
        }

        // Slider Knob
        {
            _knobRectangle = new Rectangle( 0, 0, _maxSize.width, 2 );
            _knobGraphic = new PhetShapeGraphic( component );
            _knobGraphic.setName( "AmplitudeSlider.knob" );
            _knobGraphic.setShape( _knobRectangle );
            _knobGraphic.setPaint( KNOB_FILL_COLOR );
            _knobGraphic.setStroke( KNOB_STROKE );
            _knobGraphic.setBorderColor( KNOB_STROKE_COLOR );
            _knobGraphic.centerRegistrationPoint();
            _knobGraphic.setLocation( 0, 0 );
        }
        
        // Interactivity
        {
            _labelGraphic.setIgnoreMouse( true );
            
            TextFieldEventListener textFieldListener = new TextFieldEventListener();
            _valueTextField.addActionListener( textFieldListener );
            _valueTextField.addFocusListener( textFieldListener );
            _valueTextField.addKeyListener( textFieldListener );
            
            _clickZoneGraphic.setCursorHand();
            _clickZoneGraphic.addMouseInputListener( new ClickZoneEventListener() );
            
            _knobGraphic.setCursorHand();
            _knobGraphic.addMouseInputListener( new KnobEventListener() );
            
            _trackGraphic.setCursorHand();
            _trackGraphic.addMouseInputListener( new TrackEventListener() );
        }

        addGraphic( _labelGraphic, LABEL_LAYER );
        addGraphic( _valueGraphic, VALUE_LAYER );
        addGraphic( _clickZoneGraphic, CLICK_ZONE_LAYER );
        addGraphic( _trackGraphic, TRACK_LAYER );
        addGraphic( _knobGraphic, KNOB_LAYER );

        // Enable antialiasing for all children.
        setRenderingHints( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );

        // Interested in changes to harmonic colors.
        HarmonicColors.getInstance().addHarmonicColorChangeListener( this );
        
        update();
    }
    
    /**
     * Call this method prior to releasing all references to an object of this type.
     */
    public void cleanup() {
        _harmonic.removeObserver( this );
        _harmonic = null;
        HarmonicColors.getInstance().removeHarmonicColorChangeListener( this );
    }

    //----------------------------------------------------------------------------
    // Accessors
    //----------------------------------------------------------------------------
    
    /**
     * Sets the harmonic that this slider controls.
     * 
     * @param harmonic
     */
    public void setHarmonic( Harmonic harmonic ) {
        assert ( harmonic != null );
        if ( _harmonic != null ) {
            _harmonic.removeObserver( this );
        }
        _harmonic = harmonic;
        _harmonic.addObserver( this );
        update();
    }
    
    /**
     * Gets the harmonic that this slider controls.
     * 
     * @return the model
     */
    public Harmonic getHarmonic() {
        return _harmonic;
    }
    
    /**
     * Sets the maximum size of the slider track.
     * 
     * @param maxSize the maximum size
     */
    public void setMaxSize( Dimension maxSize ) {
        setMaxSize( maxSize );
    }
    
    /**
     * Sets the maximum size of the slider track.
     * 
     * @param width the width
     * @param height the height
     */
    public void setMaxSize( int width, int height ) {
        _maxSize.setSize( width, height );

        _clickZoneRectangle.setRect( 0, 0, _maxSize.width, _maxSize.height );
        _clickZoneGraphic.setShapeDirty();
        _clickZoneGraphic.centerRegistrationPoint();

        _knobRectangle.setRect( 1, 1, _maxSize.width, 2 ); //XXX why is 1,1 necessary?
        _knobGraphic.setShapeDirty();
        _knobGraphic.centerRegistrationPoint();

        update();
    }
    
    /**
     * Gets the maximum size of the slider track.
     * 
     * @return the maximum size
     */
    public Dimension getMaxSize() {
        return new Dimension( _maxSize );
    }
    
    //----------------------------------------------------------------------------
    // User input processing
    //----------------------------------------------------------------------------
    
    /**
     * Processes the contents of the value field.
     * 
     * @return true if the value is valid, false otherwise
     */
    private boolean processUserInput() {
        boolean success = true;
        String stringValue = _valueTextField.getText();
        double amplitude = 0.0;
        try {
            Double doubleValue = new Double( stringValue );
            amplitude = doubleValue.doubleValue();
        }
        catch ( NumberFormatException nfe ) {
            success = false;
            showUserInputErrorDialog();
            update();
        }
        if ( Math.abs( amplitude ) > MAX_AMPLITUDE ) {
            success = false;
            showUserInputErrorDialog();
            update();
        }
        else if ( amplitude != _harmonic.getAmplitude() ) {
            _harmonic.setAmplitude( amplitude );
            fireChangeEvent();
        }
        return success;
    }
    
    /*
     * Displays a modal error dialog for invalid user inputs.
     */
    private void showUserInputErrorDialog() {
        String message = SimStrings.get( "AmplitudeErrorDialog.message" );
        JOptionPane op = new JOptionPane( message, JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION );
        op.createDialog( getComponent(), null ).show();
    }
    
    //----------------------------------------------------------------------------
    // Event handling
    //----------------------------------------------------------------------------
    
    /**
     * Adds a HarmonicFocusListener.
     * 
     * @param listener
     */
    public void addHarmonicFocusListener( HarmonicFocusListener listener ) {
        _listenerList.add( HarmonicFocusListener.class, listener );
    }
  
    /**
     * Removes a HarmonicFocusListener.
     * 
     * @param listener
     */
    public void removeHarmonicFocusListener( HarmonicFocusListener listener ) {
        _listenerList.remove( HarmonicFocusListener.class, listener );
    }
    
    /*
     * Fires an event indicating that the Harmonic associated with this slider
     * has gained or lost focus.
     * 
     * @param hasFocus true or false
     */
    private void fireHarmonicFocusEvent( boolean hasFocus ) {
        if ( _harmonic.getAmplitude() != 0 ) {
            HarmonicFocusEvent event = new HarmonicFocusEvent( this, _harmonic, hasFocus );
            Object[] listeners = _listenerList.getListenerList();
            for ( int i = 0; i < listeners.length; i += 2 ) {
                if ( listeners[i] == HarmonicFocusListener.class ) {
                    HarmonicFocusListener listener = (HarmonicFocusListener) listeners[i + 1];
                    if ( hasFocus ) {
                        listener.focusGained( event );
                    }
                    else {
                        listener.focusLost( event );
                    }
                }
            }
        }
    }

    /**
     * Adds a ChangeListener.
     * 
     * @param listener
     */
    public void addChangeListener( ChangeListener listener ) {
        _listenerList.add( ChangeListener.class, listener );
    }
  
    /**
     * Removes a ChangeListener.
     * 
     * @param listener
     */
    public void removeChangeListenerListener( ChangeListener listener ) {
        _listenerList.remove( ChangeListener.class, listener );
    }
    
    /*
     * Fires an event indicating that the slider has been moved by the user.
     */
    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent( this );
        Object[] listeners = _listenerList.getListenerList();
        for ( int i = 0; i < listeners.length; i += 2 ) {
            if ( listeners[i] == ChangeListener.class ) {
                ChangeListener listener = (ChangeListener) listeners[i + 1];
                listener.stateChanged( event );
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // HarmonicColorChangeListener implementation
    //----------------------------------------------------------------------------
    
    /*
     * Update when the color associated with this slider's harmonic changes.
     */
    public void harmonicColorChanged( HarmonicColorChangeEvent e ) {
        if ( e.getOrder() == _harmonic.getOrder()  ) {
            update();
        }
    }
    
    //----------------------------------------------------------------------------
    // SimpleObserver implementation
    //----------------------------------------------------------------------------
    
    /**
     * Synchronizes the view with the model.
     */
    public void update() {
        
        double amplitude = _harmonic.getAmplitude();
        Color harmonicColor = HarmonicColors.getInstance().getColor( _harmonic );
        
        // Label location
        _labelGraphic.setLocation( 0, -( ( _maxSize.height / 2 ) + LABEL_Y_OFFSET ) );
        
        // Value
        _valueTextField.setText( _valueFormatter.format( amplitude ) );
        _valueGraphic.setLocation( 0, -( ( _maxSize.height / 2 ) + VALUE_Y_OFFSET ) );
        
        // Track size
        int trackWidth = _maxSize.width;
        int trackHeight = (int) Math.abs( ( _maxSize.height / 2 ) * ( amplitude / MAX_AMPLITUDE ) );
        int trackX = -( trackWidth / 2 );
        int trackY = ( amplitude > 0 ) ? -trackHeight : 0;
        _trackRectangle.setBounds( trackX, trackY, trackWidth, trackHeight );
        _trackGraphic.setShapeDirty();
        _trackGraphic.setPaint( harmonicColor );
        
        // Knob location
        int knobX = _knobGraphic.getX();
        int knobY = (int) -( ( _maxSize.height / 2 ) * ( amplitude / MAX_AMPLITUDE ) );
        _knobGraphic.centerRegistrationPoint();
        _knobGraphic.setLocation( knobX, knobY );
        
        repaint();
    }
    
    //----------------------------------------------------------------------------
    // Inner classes
    //----------------------------------------------------------------------------
    
    /*
     * ClickZoneEventListener handles events related to the "click zone".
     */
    private class ClickZoneEventListener extends MouseInputAdapter {
        
        /* Sole constructor */
        public ClickZoneEventListener() {
            super();
        }
        
        /* Sets the harmonic's amplitude as the mouse is dragged. */
        public void mouseDragged( MouseEvent event ) {
            setAmplitude( event.getPoint() );
        }
        
        /* Sets the harmonic's amplitude when based on where the mouse is pressed. */
        public void mousePressed( MouseEvent event ) {
            setAmplitude( event.getPoint() );
            fireHarmonicFocusEvent( true );
        }
    }
    
    /*
     * KnobEventListener handles events related to the knob.
     */
    private class KnobEventListener extends MouseInputAdapter {
        
        public KnobEventListener() {
            super();
        }
        
        /* Sets the harmonic's amplitude as the mouse is dragged. */
        public void mouseDragged( MouseEvent event ) {
            setAmplitude( event.getPoint() );
        }
        
        /* Harmonic gains focus when the mouse enters the track. */
        public void mouseEntered( MouseEvent event ) {
            fireHarmonicFocusEvent( true );
        }
        
        /* Harmonic loses focus when the mouse exits the track. */
        public void mouseExited( MouseEvent event ) {
            fireHarmonicFocusEvent( false );
        }
    }
    
    /**
     * TrackEventListener handles events related to the slider track.
     *
     * @author Chris Malley (cmalley@pixelzoom.com)
     * @version $Revision$
     */
    private class TrackEventListener extends MouseInputAdapter {
        
        public TrackEventListener() {
            super();
        }
        
        /* Sets the harmonic's amplitude as the mouse is dragged. */
        public void mouseDragged( MouseEvent event ) {
            setAmplitude( event.getPoint() );
        }
        
        /* Sets the harmonic's ampltude when based on where the mouse is pressed. */
        public void mousePressed( MouseEvent event ) {
            setAmplitude( event.getPoint() );
        }
        
        /* Harmonic gains focus when the mouse enters the track. */
        public void mouseEntered( MouseEvent event ) {
            fireHarmonicFocusEvent( true );
        }
        
        /* Harmonic loses focus when the mouse exits the track. */
        public void mouseExited( MouseEvent event ) {
            fireHarmonicFocusEvent( false );
        }
    }
    
    /* 
     * Sets the harmonic's amplitude based on the mouse location.
     */
    private void setAmplitude( Point mousePoint ) {
        double localY = 0;
        try {
            AffineTransform transform = getNetTransform();
            transform.inverseTransform( mousePoint, _somePoint /* output */ );
            localY = _somePoint.getY();
        }
        catch ( NoninvertibleTransformException e ) {
            e.printStackTrace();
        }
        localY = -localY; // +Y is up
        double amplitude = MAX_AMPLITUDE * ( localY / ( _maxSize.height / 2.0 ) );
        amplitude = MathUtil.clamp( -MAX_AMPLITUDE, amplitude, +MAX_AMPLITUDE );
        _harmonic.setAmplitude( amplitude );
        
        fireChangeEvent();
    }
    
    /*
     * TextFieldEventListener handles events related to the text field.
     */
    private class TextFieldEventListener extends KeyAdapter implements ActionListener, FocusListener {
        
        /* Sole constructor. */
        public TextFieldEventListener() {}
        
        /* Processes the input value when the user presses the Enter key. */
        public void actionPerformed( ActionEvent event ) {
            if ( event.getSource() == _valueTextField ) {
                processUserInput();
            }        
        }
        
        /* Selects the contents of the text field when focus is gained. */
        public void focusGained( FocusEvent event ) {
            _valueTextField.selectAll();
        }
        
        /* Processes the input value when focus is lost. */
        public void focusLost( FocusEvent event ) {
            if ( ! event.isTemporary() ) {
                boolean success = processUserInput();
                if ( !success ) {
                    _valueTextField.requestFocus();
                }
            }
        }
        
        /* Changes the amplitude value using the up/down arrow keys. */
        public void keyPressed( KeyEvent event ) {
            if ( event.getKeyCode() == KeyEvent.VK_UP ) {
                double amplitude = _harmonic.getAmplitude() + VALUE_STEP;
                if ( amplitude <= MAX_AMPLITUDE ) {
                    _harmonic.setAmplitude( amplitude );
                    fireChangeEvent();
                }
            }
            else if ( event.getKeyCode() == KeyEvent.VK_DOWN ) {
                double amplitude = _harmonic.getAmplitude() - VALUE_STEP;
                if ( amplitude >= -MAX_AMPLITUDE ) {
                    _harmonic.setAmplitude( amplitude );
                    fireChangeEvent(); 
                }
            }
        }
    }
}
