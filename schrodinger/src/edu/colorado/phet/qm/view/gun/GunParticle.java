/* Copyright 2004, Sam Reid */
package edu.colorado.phet.qm.view.gun;

import edu.colorado.phet.common.math.Vector2D;
import edu.colorado.phet.qm.SchrodingerModule;
import edu.colorado.phet.qm.model.DiscreteModel;
import edu.colorado.phet.qm.model.WaveSetup;
import edu.colorado.phet.qm.model.Wavefunction;
import edu.colorado.phet.qm.model.math.Complex;
import edu.colorado.phet.qm.model.waves.GaussianWave2D;
import edu.colorado.phet.qm.phetcommon.ImageComboBox;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * User: Sam Reid
 * Date: Jul 8, 2005
 * Time: 9:02:12 PM
 * Copyright (c) Jul 8, 2005 by Sam Reid
 */
public abstract class GunParticle extends ImageComboBox.Item {
    private AbstractGunGraphic gunGraphic;
    private ArrayList momentumChangeListeners = new ArrayList();
    private double intensityScale = 1.0;
    protected static final double minWavelength = 8;
    protected static final double maxWavelength = 30;

    public GunParticle( final AbstractGunGraphic gunGraphic, String label, String imageLocation ) {
        super( label, imageLocation );
        this.gunGraphic = gunGraphic;
    }

    public abstract void setup( AbstractGunGraphic abstractGunGraphic );

    public abstract void deactivate( AbstractGunGraphic abstractGunGraphic );

    public AbstractGunGraphic getGunGraphic() {
        return gunGraphic;
    }

    public abstract double getStartPy();

    public WaveSetup getInitialWavefunction( Wavefunction currentWave ) {
        double x = getDiscreteModel().getGridWidth() * 0.5;
        double y = getStartY();
        double px = 0;
        double py = getStartPy();

//        Point phaseLockPoint = new Point( (int)x, (int)( y + 5 ) );
        Point phaseLockPoint = new Point( (int)x, (int)( y - 5 ) );

        double dxLattice = getStartDxLattice();
        GaussianWave2D wave2DSetup = new GaussianWave2D( new Point( (int)x, (int)y ),
                                                         new Vector2D.Double( px, py ), dxLattice );

        double desiredPhase = currentWave.valueAt( phaseLockPoint.x, phaseLockPoint.y ).getComplexPhase();

        Wavefunction copy = currentWave.createEmptyWavefunction();
        wave2DSetup.initialize( copy );

        double uneditedPhase = copy.valueAt( phaseLockPoint.x, phaseLockPoint.y ).getComplexPhase();
        double deltaPhase = desiredPhase - uneditedPhase;

        wave2DSetup.setPhase( deltaPhase );
        wave2DSetup.setScale( getIntensityScale() );

        return wave2DSetup;
    }

    private double getIntensityScale() {
        return intensityScale;
    }

    protected double getStartY() {
        return getDiscreteModel().getGridHeight() * 0.85;
    }

    private WaveSetup getInitialWavefunctionVerifyCorrect( Wavefunction currentWave ) {
        double x = getDiscreteModel().getGridWidth() * 0.5;
        double y = getStartY();
        double px = 0;
        double py = getStartPy();
        System.out.println( "py = " + py );

        Point phaseLockPoint = new Point( (int)x, (int)( y - 5 ) );

        double dxLattice = getStartDxLattice();
        System.out.println( "dxLattice = " + dxLattice );
        GaussianWave2D wave2DSetup = new GaussianWave2D( new Point( (int)x, (int)y ),
                                                         new Vector2D.Double( px, py ), dxLattice );

        Complex centerValue = currentWave.valueAt( phaseLockPoint.x, phaseLockPoint.y );
        double desiredPhase = centerValue.getComplexPhase();

        System.out.println( "original Center= " + centerValue + ", desired phase=" + desiredPhase );

        Wavefunction copy = currentWave.createEmptyWavefunction();
        wave2DSetup.initialize( copy );

        Complex centerValueCopy = copy.valueAt( phaseLockPoint.x, phaseLockPoint.y );
        System.out.println( "unedited: =" + centerValueCopy + ", unedited phase=" + centerValueCopy.getComplexPhase() );

        double uneditedPhase = centerValueCopy.getComplexPhase();
        double deltaPhase = desiredPhase - uneditedPhase;

        System.out.println( "deltaPhase = " + deltaPhase );
        wave2DSetup.setPhase( deltaPhase );

        Wavefunction test = currentWave.createEmptyWavefunction();
        wave2DSetup.initialize( test );
        Complex testValue = test.valueAt( phaseLockPoint.x, phaseLockPoint.y );
        System.out.println( "created testValue = " + testValue + ", created phase=" + testValue.getComplexPhase() );

        return wave2DSetup;
    }

    protected void clearWavefunction() {
        getDiscreteModel().clearWavefunction();
    }

    public void fireParticle() {
        WaveSetup initialWavefunction = getInitialWavefunction( getDiscreteModel().getWavefunction() );
        getSchrodingerModule().fireParticle( initialWavefunction );
    }

    SchrodingerModule getSchrodingerModule() {
        return gunGraphic.getSchrodingerModule();
    }

    protected DiscreteModel getDiscreteModel() {
        return getSchrodingerModule().getDiscreteModel();
    }

    protected double getStartDxLattice() {
        return 0.06 * getDiscreteModel().getGridWidth();
    }

    private ArrayList changeHandlers = new ArrayList();

    public Point getGunLocation() {
        return new Point( -10, 35 );
    }

//    protected void detachListener( AbstractGun.MomentumChangeListener listener ) {
//        ArrayList toRemove = new ArrayList();
//        for( int i = 0; i < changeHandlers.size(); i++ ) {
//            ChangeHandler changeHandler = (ChangeHandler)changeHandlers.get( i );
//            if( changeHandler.momentumChangeListener == listener ) {
//                toRemove.add( changeHandler );
//            }
//        }
//
//        for( int i = 0; i < toRemove.size(); i++ ) {
//            ChangeHandler changeHandler = (ChangeHandler)toRemove.get( i );
//            changeHandlers.remove( changeHandler );
//            detachListener( changeHandler );
//        }
//    }

    class ChangeHandler implements ChangeListener {
        private AbstractGunGraphic.MomentumChangeListener momentumChangeListener;

        public ChangeHandler( AbstractGunGraphic.MomentumChangeListener momentumChangeListener ) {
            this.momentumChangeListener = momentumChangeListener;
        }

        public void stateChanged( ChangeEvent e ) {
            notifyMomentumChanged();
        }
    }

    public void addMomentumChangeListerner( AbstractGunGraphic.MomentumChangeListener momentumChangeListener ) {
        momentumChangeListeners.add( momentumChangeListener );
        ChangeHandler changeHandler = new ChangeHandler( momentumChangeListener );
        changeHandlers.add( changeHandler );
        hookupListener( changeHandler );
    }

    public void removeMomentumChangeListener( AbstractGunGraphic.MomentumChangeListener listener ) {
        momentumChangeListeners.remove( listener );

        ArrayList toRemove = new ArrayList();
        for( int i = 0; i < changeHandlers.size(); i++ ) {
            ChangeHandler changeHandler = (ChangeHandler)changeHandlers.get( i );
            if( changeHandler.momentumChangeListener == listener ) {
                toRemove.add( changeHandler );
            }
        }

        for( int i = 0; i < toRemove.size(); i++ ) {
            ChangeHandler changeHandler = (ChangeHandler)toRemove.get( i );
            changeHandlers.remove( changeHandler );
            detachListener( changeHandler );
        }

//        detachListener( listener );
    }

    protected abstract void detachListener( ChangeHandler changeHandler );

    protected abstract void hookupListener( ChangeHandler changeHandler );

    protected void notifyMomentumChanged() {
        double momentum = getStartPy();
        for( int i = 0; i < momentumChangeListeners.size(); i++ ) {
            AbstractGunGraphic.MomentumChangeListener momentumChangeListener = (AbstractGunGraphic.MomentumChangeListener)momentumChangeListeners.get( i );
            momentumChangeListener.momentumChanged( momentum );
        }
    }


    public void autofire() {
        fireParticle();
    }

    public void setIntensityScale( double intensityScale ) {
        this.intensityScale = intensityScale;
    }
}
