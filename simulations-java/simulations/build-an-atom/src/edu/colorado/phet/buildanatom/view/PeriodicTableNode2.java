// Copyright 2002-2011, University of Colorado
package edu.colorado.phet.buildanatom.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;

import edu.colorado.phet.buildanatom.model.AtomIdentifier;
import edu.colorado.phet.buildanatom.model.IDynamicAtom;
import edu.colorado.phet.common.phetcommon.util.SimpleObserver;
import edu.colorado.phet.common.phetcommon.view.util.PhetFont;
import edu.colorado.phet.common.piccolophet.nodes.PhetPPath;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.pswing.PSwing;

/**
 *
 * TODO: This class is a prototype that should be integrated with the other
 * periodic table node(s).
 *
 * This class defines a node that represents a periodic table of the elements.
 * It is not interactive by default, but provides overrides that can be used
 * to add interactivity.
 *
 * This makes some assumptions about which portions of the table to display,
 * and may not work for all situations.
 *
 * @author Sam Reid
 * @author John Blanco
 */
public class PeriodicTableNode2 extends PNode {

    // ------------------------------------------------------------------------
    // Class Data
    // ------------------------------------------------------------------------

    public static int CELL_DIMENSION = 20; // Cells are square.

    // ------------------------------------------------------------------------
    // Instance Data
    // ------------------------------------------------------------------------

    public Color backgroundColor = null;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * @param backgroundColor
     */
    public PeriodicTableNode2( final IDynamicAtom atom, Color backgroundColor ) {
        this.backgroundColor = backgroundColor;
        //See http://www.ptable.com/
        final PNode table = new PNode();
        for ( int i = 1; i <= 56; i++ ) {
            addElement( atom, table, i );
        }
        // Add in a single entry to represent the lanthanide series.
        addElement( atom, table, 57 );
        for ( int i = 71; i <= 88; i++ ) {
            addElement( atom, table, i );
        }
        // Add in a single entry to represent the actinide series.
        addElement( atom, table, 89 );
        for ( int i = 103; i <= 112; i++ ) {
            addElement( atom, table, i );
        }

        addChild( table );
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    private void addElement( final IDynamicAtom atom, final PNode table, int atomicNumber ) {
        ButtonElementCell elementCell = new ButtonElementCell( atom, atomicNumber, backgroundColor );
        final Point gridPoint = getGridPoint( atomicNumber );
        double x = ( gridPoint.getY() - 1 ) * CELL_DIMENSION;     //expansion cells render as "..." on top of each other
        double y = ( gridPoint.getX() - 1 ) * CELL_DIMENSION;
        elementCell.setOffset( x, y );
        table.addChild( elementCell );
        elementCellCreated( elementCell );
    }

    /**
     * Listener callback, override when needing notification of the creation
     * of element cells.  This is useful when creating an interactive chart,
     * since it is a good opportunity to hook up event listeners to the cell.
     *
     * @param elementCell
     */
    protected void elementCellCreated( ButtonElementCell elementCell ) {
    }

    /**
     * Reports (row,column) on the grid, with a 1-index
     *
     * @param i
     * @return
     */
    private Point getGridPoint( int i ) {
        //http://www.ptable.com/ was useful here
        if ( i == 1 ) {
            return new Point( 1, 1 );
        }
        if ( i == 2 ) {
            return new Point( 1, 18 );
        }
        else if ( i == 3 ) {
            return new Point( 2, 1 );
        }
        else if ( i == 4 ) {
            return new Point( 2, 2 );
        }
        else if ( i >= 5 && i <= 10 ) {
            return new Point( 2, i + 8 );
        }
        else if ( i == 11 ) {
            return new Point( 3, 1 );
        }
        else if ( i == 12 ) {
            return new Point( 3, 2 );
        }
        else if ( i >= 13 && i <= 18 ) {
            return new Point( 3, i );
        }
        else if ( i >= 19 && i <= 36 ) {
            return new Point( 4, i - 18 );
        }
        else if ( i >= 37 && i <= 54 ) {
            return new Point( 5, i - 36 );
        }
        else if ( i >= 19 && i <= 36 ) {
            return new Point( 4, i - 36 );
        }
        else if ( i == 55 ) {
            return new Point( 6, 1 );
        }
        else if ( i == 56 ) {
            return new Point( 6, 2 );
        }
        else if ( i >= 57 && i <= 71 ) {
            return new Point( 6, 3 );
        }
        else if ( i >= 72 && i <= 86 ) {
            return new Point( 6, i - 68 );
        }
        else if ( i == 87 ) {
            return new Point( 7, 1 );
        }
        else if ( i == 88 ) {
            return new Point( 7, 2 );
        }
        else if ( i >= 89 && i <= 103 ) {
            return new Point( 7, 3 );
        }
        else if ( i >= 104 && i <= 118 ) {
            return new Point( 7, i - 100 );
        }
        return new Point( 1, 1 );
    }

    // -----------------------------------------------------------------------
    // Inner Classes and Interfaces
    //------------------------------------------------------------------------

    /**
     * PNode that represents a cell on the periodic table.  This version is a
     * basic square cell.
     */
    public class BasicElementCell extends PNode {
        private final int atomicNumber;
        private final PText text;
        private final PhetPPath cellBoundary;
        private final boolean disabledLooking = false;

        public BasicElementCell( final IDynamicAtom atom, final int atomicNumber, final Color backgroundColor ) {
            this.atomicNumber = atomicNumber;
            cellBoundary = new PhetPPath( new Rectangle2D.Double( 0, 0, CELL_DIMENSION, CELL_DIMENSION ),
                    backgroundColor, new BasicStroke( 1 ), Color.black );
            addChild( cellBoundary );

            String abbreviation = AtomIdentifier.getSymbol( atomicNumber );
            text = new PText( abbreviation );
            text.setOffset( cellBoundary.getFullBounds().getCenterX() - text.getFullBounds().getWidth() / 2,
                    cellBoundary.getFullBounds().getCenterY() - text.getFullBounds().getHeight() / 2 );
            addChild( text );
            atom.addObserver( new SimpleObserver() {
                public void update() {
                    boolean match = atom.getNumProtons() == atomicNumber;
                    text.setFont( new PhetFont( PhetFont.getDefaultFontSize(), match ) );
                    if ( match ) {
                        cellBoundary.setStroke( new BasicStroke( 2 ) );
                        cellBoundary.setStrokePaint( Color.RED );
                        cellBoundary.setPaint( Color.white );
                        BasicElementCell.this.moveToFront();
                    }
                    else {
                        if ( !disabledLooking ){
                            cellBoundary.setStroke( new BasicStroke( 1 ) );
                            cellBoundary.setStrokePaint( Color.BLACK );
                            cellBoundary.setPaint( backgroundColor );
                        }
                        else{
                            text.setTextPaint( Color.LIGHT_GRAY );
                            cellBoundary.setStrokePaint( Color.LIGHT_GRAY );
                        }
                    }
                }
            } );
        }

        public int getAtomicNumber() {
            return atomicNumber;
        }
    }

    /**
     * PNode that represents a cell on the periodic table.  This version is a
     * cell that looks like a button, intended to convey to the user that it
     * is interactive.
     */
    public class ButtonElementCell extends PNode {
        private final int atomicNumber;
        private final JButton button;
        private final PText text;

        public ButtonElementCell( final IDynamicAtom atom, final int atomicNumber, final Color backgroundColor ) {
            this.atomicNumber = atomicNumber;
            button = new JButton(){{
                setPreferredSize( new Dimension( CELL_DIMENSION, CELL_DIMENSION) );
            }};
            PNode buttonNode = new PSwing( button );
            addChild( buttonNode );

            text = new PText( AtomIdentifier.getSymbol( atomicNumber ) );
            text.centerBoundsOnPoint( CELL_DIMENSION / 2, CELL_DIMENSION / 2 );
            text.setPickable( false ); // Don't pick up mouse events intended for the button.
            addChild( text );

            atom.addObserver( new SimpleObserver() {
                public void update() {
                    boolean match = atom.getNumProtons() == atomicNumber;
                    text.setFont( new PhetFont( PhetFont.getDefaultFontSize(), match ) );
                    if ( match ) {
                        button.setBackground( Color.RED );
                    }
                    else {
                        button.setBackground( Color.LIGHT_GRAY );
                    }
                }
            } );
        }

        public int getAtomicNumber() {
            return atomicNumber;
        }

        public void setButtonEnabled(boolean enabled){
            button.setEnabled( enabled );
        }

        public void addActionListener( ActionListener actionListener ){
            button.addActionListener( actionListener );
        }

        public void setTextColor( Color color ){
            text.setTextPaint( color );
        }
    }
}
