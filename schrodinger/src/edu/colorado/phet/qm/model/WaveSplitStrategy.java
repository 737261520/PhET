/* Copyright 2004, Sam Reid */
package edu.colorado.phet.qm.model;

import edu.colorado.phet.qm.model.potentials.HorizontalDoubleSlit;

import java.awt.*;

/**
 * User: Sam Reid
 * Date: Jul 10, 2005
 * Time: 9:04:38 AM
 * Copyright (c) Jul 10, 2005 by Sam Reid
 * //todo assumes all same size waves
 */

public class WaveSplitStrategy {
    private SplitModel splitModel;

    public WaveSplitStrategy( SplitModel splitModel ) {
        this.splitModel = splitModel;
    }

    public void copyDetectorAreasToWaves() {
        Rectangle[] slits = splitModel.getDoubleSlitPotential().getSlitAreas();
        copy( slits[0], splitModel.getLeftWavefunction() );
        copy( slits[1], splitModel.getRightWavefunction() );
    }

    protected Propagator getLeftPropagator() {
        return splitModel.getLeftPropagator();
    }

    protected void copy( Rectangle slit, Wavefunction dest ) {
        for( int i = slit.x; i < slit.x + slit.width; i++ ) {
            for( int j = slit.y; j < slit.y + slit.height; j++ ) {
                if( splitModel.getWavefunction().containsLocation( i, j ) ) {
                    Complex value = splitModel.getWavefunction().valueAt( i, j );
                    dest.setValue( i, j, value );
                }
            }
        }
    }

    public void copyNorthRegionToSplits() {
        int yMax = getDoubleSlitPotential().getY();
        for( int i = 0; i < getLeftWavefunction().getWidth(); i++ ) {
            for( int j = 0; j < yMax; j++ ) {
                Complex v = getWavefunction().valueAt( i, j );
                v.scale( 0.5 );
                getLeftWavefunction().setValue( i, j, v );
                getRightWavefunction().setValue( i, j, v );
            }
        }
    }

    private Wavefunction getRightWavefunction() {
        return splitModel.getRightWavefunction();
    }

    public void copySplitsToNorthRegion() {
        int yMax = getDoubleSlitPotential().getY();
        for( int i = 0; i < getLeftWavefunction().getWidth(); i++ ) {
            for( int j = 0; j < yMax; j++ ) {
                double sum = SplitModel.sumMagnitudes( getLeftWavefunction().valueAt( i, j ), getRightWavefunction().valueAt( i, j ) );
                getWavefunction().setValue( i, j, new Complex( sum, 0 ) );
            }
        }
    }

    public void clearLRWavesSouthPart() {
        int topYClear = (int)getDoubleSlitPotential().getSlitAreas()[0].getMaxY();
        Rectangle rectangle = new Rectangle( 0, topYClear, getLeftWavefunction().getWidth(), getLeftWavefunction().getHeight() );
        clear( getLeftWavefunction(), rectangle );
        clear( getRightWavefunction(), rectangle );
    }

    private Wavefunction getLeftWavefunction() {
        return splitModel.getLeftWavefunction();
    }

    public void clearEntrantWaveNorthArea() {
        int topYClear = getTopYClear();
        Wavefunction toClear = getWavefunction();
        clearNorthArea( toClear, topYClear );
    }

    protected int getTopYClear() {
        int topYClear = (int)getDoubleSlitPotential().getSlitAreas()[0].getMinY();
        return topYClear;
    }

    private Wavefunction getWavefunction() {
        return splitModel.getWavefunction();
    }

    private HorizontalDoubleSlit getDoubleSlitPotential() {
        return splitModel.getDoubleSlitPotential();
    }

    public void clear( Wavefunction wavefunction, Rectangle area ) {
        for( int i = area.x; i < area.x + area.width; i++ ) {
            for( int j = area.y; j < area.y + area.height; j++ ) {
                if( wavefunction.containsLocation( i, j ) ) {
                    wavefunction.setValue( i, j, new Complex() );
                }
            }
        }
    }

    protected void clearNorthArea( Wavefunction toClear, int topYClear ) {
        Rectangle rect = new Rectangle( toClear.getWidth(), topYClear );
        clear( toClear, rect );
    }

    public SplitModel getSplitModel() {
        return splitModel;
    }
}
