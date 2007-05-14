/* Copyright 2007, University of Colorado */

package edu.colorado.phet.opticaltweezers.charts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * PhetHistogramDataset is a JFreeChart dataset for creating histograms.
 * It is based on org.jfree.data.statistics.HistogramDataset, 
 * which was unfortunately not written to be extensible or dynamic.
 * <p>
 * This dataset can constain multiple histogram series.
 * Each series has an immutable range, which is divided into a specified 
 * number of bins, each bin having equal width.
 * When a one-dimensional data point (referred to as an observation)
 * is added to a series, it is placed in the bin that corresponds to
 * its value. When the histogram is rendered, each bin is drawn as 
 * a vertical bar, where the height of the bar corresponds to the number
 * of observations in the bin.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class PhetHistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable, PublicCloneable, Serializable {

    //----------------------------------------------------------------------------
    // Data structures
    //----------------------------------------------------------------------------

    /*
     * Data structure that describes a histogram series.
     * numberOfObservations is the sole mutable member, and is included 
     * only to improved performance; the same information could be obtained
     * by summing the number of observations for each bin.
     */
    private static class HistogramSeries {

        private final Comparable key;
        private final double minimum;
        private final double maximum;
        private final double binWidth;
        private int numberOfObservations; // mutable
        private final List bins; // list of HistogramBin
        
        /* Creates an empty series */
        public HistogramSeries( Comparable key, double minimum, double maximum, int numberOfBins ) {
            
            this.key = key;
            this.minimum = minimum;
            this.maximum = maximum;
            this.binWidth = ( maximum - minimum ) / numberOfBins;

            // create a set of empty bins
            this.numberOfObservations = 0;
            double startBoundary = minimum;
            double endBoundary = startBoundary + binWidth;
            List bins = new ArrayList( numberOfBins );
            for ( int i = 0; i < numberOfBins; i++ ) {
                // Set the last bin's upper boundary to the maximum to avoid precision issues.
                if ( i == numberOfBins - 1 ) {
                    endBoundary = maximum;
                }
                bins.add( new HistogramBin( startBoundary, endBoundary ) );
                startBoundary = endBoundary;
                endBoundary = startBoundary + binWidth;
            }
            this.bins = bins;
        }
    }

    /*
     * Data structure that describes a histogram bin.
     * numberOfObservations is the sole mutable member.
     */
    private static class HistogramBin {

        private int numberOfObservations; // mutable
        private final double startBoundary;
        private final double endBoundary;

        /* Creates an empty bin */
        public HistogramBin( double startBoundary, double endBoundary ) {
            this.numberOfObservations = 0;
            this.startBoundary = startBoundary;
            this.endBoundary = endBoundary;
        }
    }

    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private HistogramType _histogramType;
    private List _seriesList; // list of HistogramSeries
    private boolean _ignoreOutOfRangeObservations;
    
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Creates an empty dataset of type HistogramType.FREQUENCY.
     */
    public PhetHistogramDataset() {
        this( HistogramType.FREQUENCY );
    }

    /**
     * Create an empty dataset with a specified HistogramType.
     * 
     * @param histogramType the histogram type (null not permitted)
     */
    public PhetHistogramDataset( HistogramType histogramType ) {
        if ( histogramType == null ) {
            throw new IllegalArgumentException( "histogramType is null" );
        }
        _histogramType = histogramType;
        _seriesList = new ArrayList();
    }

    //----------------------------------------------------------------------------
    // Setters and getters
    //----------------------------------------------------------------------------

    /**
     * Gets the histogram type. 
     * 
     * @return the type (never null)
     */
    public HistogramType getHistogramType() {
        return _histogramType;
    }

    /**
     * Sets the histogram type.
     * Sends a DatasetChangeEvent to all registered listeners.
     * 
     * @param histogramType the histogram type (null not permitted)
     */
    public void setHistogramType( HistogramType histogramType ) {
        if ( histogramType == null ) {
            throw new IllegalArgumentException( "histogramType is null" );
        }
        if ( histogramType != _histogramType ) {
            _histogramType = histogramType;
            notifyListeners( new DatasetChangeEvent( this, this ) );
        }
    }
    
    /**
     * Gets the index of a series using it's key.
     * 
     * @param seriesKey
     * @return series index, -1 if not found
     */
    public int getSeriesIndex( Comparable seriesKey ) {
        int seriesIndex = -1;
        HistogramSeries series;
        for ( int i = 0; i < _seriesList.size(); i++ ) {
            series = (HistogramSeries) _seriesList.get( i );
            if ( series.key.compareTo( seriesKey ) == 0 ) {
                seriesIndex = i;
                break;
            }
        }
        return seriesIndex;
    }

    /**
     * Determines what happens when you attempt to add an observation
     * that is outside of the range to the dataset.
     * If true, simply ignore the observation.
     * If false, throw an exception.
     * 
     * @param seriesIndex
     * @param ignore
     */
    public void setIgnoreOutOfRangeObservations( boolean ignore ) {
        _ignoreOutOfRangeObservations = ignore;
    }
    
    /**
     * Are we ignoring observations that are out of range?
     * 
     * @return true or false
     */
    public boolean getIgnoreOutOfRangeObservations() {
        return _ignoreOutOfRangeObservations;
    }

    //----------------------------------------------------------------------------
    // Series
    //----------------------------------------------------------------------------

    /**
     * Adds an empty series to the dataset.
     * Sends a DatasetChangeEvent to all registered listeners.
     * 
     * @param seriesKey
     * @param numberOfBins
     * @param minimum
     * @param maximum
     * @return the series index
     */
    public int addSeries( Comparable seriesKey, int numberOfBins, double minimum, double maximum ) {
        return addSeries( seriesKey, numberOfBins, minimum, maximum, null /* observations */);
    }

    /**
     * Adds a series to the dataset. 
     * Sends a DatasetChangeEvent to all registered listeners.
     * <p>
     * Any observation falling on a bin boundary will be assigned to the lower bin.
     * Observations that are outside the min/max range are ignored.
     * 
     * @param seriesKey the series key (null not permitted)
     * @param numberOfBins the number of bins (must be at least 1)
     * @param minimum the lower bound of the bin range.
     * @param maximum the upper bound of the bin range.
     * @param observations the raw observations (null OK)
     * @param ignoreOutOfRangeObservations whether to ignore observations that are out of range
     * @return the series index
     */
    public int addSeries( Comparable seriesKey, int numberOfBins, double minimum, double maximum, double[] observations ) {
        if ( seriesKey == null ) {
            throw new IllegalArgumentException( "seriesKey is null" );
        }
        if ( numberOfBins < 1 ) {
            throw new IllegalArgumentException( "numberOfBins must be at least 1" );
        }
        if ( minimum > maximum ) {
            throw new IllegalArgumentException( "minimum > maximum" );
        }

        // create the series
        HistogramSeries series = new HistogramSeries( seriesKey, minimum, maximum, numberOfBins );
        _seriesList.add( series );
        final int seriesIndex = _seriesList.indexOf( series );

        // fill the bins
        if ( observations != null ) {
            for ( int i = 0; i < observations.length; i++ ) {
                addObservation( seriesIndex, observations[i] );
            }
        }

        notifyListeners( new DatasetChangeEvent( this, this ) );

        return seriesIndex;
    }

    /**
     * Removes a series.
     * Sends a DatasetChangeEvent to all registered listeners.
     * <p>
     * After calling this method, the indicies of other series may be changed.
     * 
     * @param seriesIndex
     */
    public void removeSeries( int seriesIndex ) {
        _seriesList.remove( seriesIndex );
        notifyListeners( new DatasetChangeEvent( this, this ) );
    }
    
    /**
     * Removes all series.
     * Sends a DatasetChangeEvent to all registered listeners.
     */
    public void removeAllSeries() {
        _seriesList.clear();
        notifyListeners( new DatasetChangeEvent( this, this ) );
    }

    //----------------------------------------------------------------------------
    // Observations
    //----------------------------------------------------------------------------

    /**
     * Adds an observation to a series, putting it in the proper bin.
     * Sends a DatasetChangeEvent to all registered listeners.
     * 
     * @param seriesIndex
     * @param observation
     */
    public void addObservation( int seriesIndex, double observation ) {
        HistogramSeries series = getSeries( seriesIndex );
        final double minimum = getMinimum( seriesIndex );
        final double maximum = getMaximum( seriesIndex );
        if ( observation >= minimum && observation <= maximum ) {
            double fraction = ( observation - minimum ) / ( maximum - minimum );
            if ( fraction < 0.0 ) {
                fraction = 0.0;
            }
            final int numberOfBins = getNumberOfBins( seriesIndex );
            final int binIndex = (int) ( fraction * numberOfBins );
            HistogramBin bin = getBin( seriesIndex, binIndex );
            bin.numberOfObservations++;
            series.numberOfObservations++;
        }
        else if ( !_ignoreOutOfRangeObservations ) {
            throw new IllegalArgumentException( "series " + seriesIndex + " observation is out of range: " + observation );
        }

        notifyListeners( new DatasetChangeEvent( this, this ) );
    }

    /**
     * Clears all the observation for a series.
     * Sends a DatasetChangeEvent to all registered listeners.
     * All bins in the series will be empty after this method is called.
     * 
     * @param seriesIndex
     */
    public void clearObservations( int seriesIndex ) {
        // clear all bins
        List bins = getBins( seriesIndex );
        Iterator i = bins.iterator();
        HistogramBin bin;
        while ( i.hasNext() ) {
            bin = (HistogramBin) i.next();
            bin.numberOfObservations = 0;
        }
        // clear the series
        HistogramSeries series = getSeries( seriesIndex );
        series.numberOfObservations = 0;

        notifyListeners( new DatasetChangeEvent( this, this ) );
    }

    //----------------------------------------------------------------------------
    // Accessors (private)
    //----------------------------------------------------------------------------

    /*
     * Gets a series by index.
     * 
     * @param seriesIndex
     * @return the series
     */
    private HistogramSeries getSeries( int seriesIndex ) {
        return (HistogramSeries) _seriesList.get( seriesIndex );
    }

    /*
     * Gets the total number of observations for a series.
     * 
     * @param series the series index.
     * @return the total.
     */
    private int getNumberOfObservations( int seriesIndex ) {
        HistogramSeries series = getSeries( seriesIndex );
        return series.numberOfObservations;
    }
    
    /*
     * Gets the number of bins for a series.
     * 
     * @param seriesIndex
     * @return
     */
    private int getNumberOfBins( int seriesIndex ) {
        List bins = getBins( seriesIndex );
        return bins.size();
    }
    
    /*
     * Gets the bins for a series.
     * 
     * @param seriesIndex the series index.
     * @return an array of bins.
     */
    private List getBins( int seriesIndex ) {
        HistogramSeries series = getSeries( seriesIndex );
        return series.bins;
    }

    /*
     * Gets a specific bin from a series.
     * 
     * @param seriesIndex
     * @param binIndex
     * @return a bin
     */
    private HistogramBin getBin( int seriesIndex, int binIndex ) {
        List bins = getBins( seriesIndex );
        return (HistogramBin) bins.get( binIndex );
    }

    /*
     * Gets the bin width for a series.
     * 
     * @param series the series index (zero based).
     * @return the bin width.
     */
    private double getBinWidth( int seriesIndex ) {
        HistogramSeries series = getSeries( seriesIndex );
        return series.binWidth;
    }

    /*
     * Gets the minimum value for a series.
     * 
     * @param seriesIndex
     * @return minimum value
     */
    private double getMinimum( int seriesIndex ) {
        HistogramSeries series = getSeries( seriesIndex );
        return series.minimum;
    }

    /*
     * Gets the maximum value for a series.
     * 
     * @param seriesIndex
     * @return maximum value
     */
    private double getMaximum( int seriesIndex ) {
        HistogramSeries series = getSeries( seriesIndex );
        return series.maximum;
    }

    //----------------------------------------------------------------------------
    // AbstractIntervalXYDataset, etc. implementation
    //----------------------------------------------------------------------------

    /**
     * Returns the key for a series.
     * 
     * @param seriesIndex the series index (zero based).
     * @return the series key.
     */
    public Comparable getSeriesKey( int seriesIndex ) {
        HistogramSeries series = getSeries( seriesIndex );
        return series.key;
    }

    /**
     * Gets the number of series in the dataset.
     * 
     * @return the series count.
     */
    public int getSeriesCount() {
        return _seriesList.size();
    }

    /**
     * Gets the number of data items (bins) for a series.
     * 
     * @param seriesIndex the series index (zero based).
     * @return the item (bin) count.
     */
    public int getItemCount( int seriesIndex ) {
        int count = getNumberOfBins( seriesIndex );
        return count;
    }

    /**
     * Returns the X value for a bin.  This value won't be used for plotting 
     * histograms, since the renderer will ignore it.  But other renderers can 
     * use it (for example, you could use the dataset to create a line
     * chart).
     * 
     * @param seriesIndex the series index (zero based).
     * @param binIndex the bin index (zero based).
     * @return the start value.
     */
    public Number getX( int seriesIndex, int binIndex ) {
        HistogramBin bin = getBin( seriesIndex, binIndex );
        final double x = ( bin.startBoundary + bin.endBoundary ) / 2.;
        return new Double( x );
    }

    /**
     * Returns the y-value for a bin 
     * (calculated to take into account the histogram type).
     * 
     * @param seriesIndex the series index (zero based).
     * @param binIndex the bin index (zero based).
     * @return the y-value.
     */
    public Number getY( int seriesIndex, int binIndex ) {

        HistogramBin bin = getBin( seriesIndex, binIndex );
        final double total = getNumberOfObservations( seriesIndex );
        final double binWidth = getBinWidth( seriesIndex );

        double y = 0;
        if ( _histogramType == HistogramType.FREQUENCY ) {
            y = bin.numberOfObservations;
        }
        else if ( _histogramType == HistogramType.RELATIVE_FREQUENCY ) {
            y = bin.numberOfObservations / total;
        }
        else if ( _histogramType == HistogramType.SCALE_AREA_TO_1 ) {
            y = bin.numberOfObservations / ( binWidth * total );
        }
        else {
            throw new IllegalStateException( "unsupported HistogramType: " + _histogramType );
        }
        return new Double( y );
    }

    /**
     * Returns the start value for a bin.
     * 
     * @param seriesIndex the series index (zero based).
     * @param binIndex the bin index (zero based).
     * @return the start value.
     */
    public Number getStartX( int seriesIndex, int binIndex ) {
        HistogramBin bin = getBin( seriesIndex, binIndex );
        return new Double( bin.startBoundary );
    }

    /**
     * Returns the end value for a bin.
     * 
     * @param seriesIndex the series index (zero based).
     * @param binIndex the bin index (zero based).
     * @return the end value.
     */
    public Number getEndX( int seriesIndex, int binIndex ) {
        HistogramBin bin = getBin( seriesIndex, binIndex );
        return new Double( bin.endBoundary );
    }

    /**
     * Returns the start y-value for a bin (which is the same as the y-value, 
     * this method exists only to support the general form of the 
     * {@link IntervalXYDataset} interface).
     * 
     * @param seriesIndex the series index (zero based).
     * @param binIndex the bin index (zero based).
     * @return the y-value.
     */
    public Number getStartY( int seriesIndex, int binIndex ) {
        return getY( seriesIndex, binIndex );
    }

    /**
     * Returns the end y-value for a bin (which is the same as the y-value, 
     * this method exists only to support the general form of the 
     * {@link IntervalXYDataset} interface).
     * 
     * @param seriesIndex the series index (zero based).
     * @param binIndex the bin index (zero based).
     * @return the Y value.
     */
    public Number getEndY( int seriesIndex, int binIndex ) {
        return getY( seriesIndex, binIndex );
    }

    //----------------------------------------------------------------------------
    // Comparable implementation
    //----------------------------------------------------------------------------

    /**
     * Tests this dataset for equality with an arbitrary object.
     * 
     * @param obj the object to test against (<code>null</code> permitted).
     * @return true or false
     */
    public boolean equals( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !( obj instanceof PhetHistogramDataset ) ) {
            return false;
        }
        PhetHistogramDataset that = (PhetHistogramDataset) obj;
        if ( !ObjectUtilities.equal( this._histogramType, that._histogramType ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal( this._seriesList, that._seriesList ) ) {
            return false;
        }
        return true;
    }

    //----------------------------------------------------------------------------
    // PublicCloneable implementation
    //----------------------------------------------------------------------------

    /**
     * Returns a clone of the dataset.
     * 
     * @return a clone of the dataset.
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
