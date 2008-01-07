package edu.colorado.phet.movingman;

import edu.colorado.phet.common.phetcommon.view.util.PhetDefaultFont;

import java.awt.*;

/**
 * User: Sam Reid
 * Date: Nov 8, 2004
 * Time: 3:20:49 PM
 */
public class MMFontManager {
    private static MMFontSet fontSet;

    public static MMFontSet getFontSet() {
        return fontSet;
    }

    public static class MMFontSet {
        protected Font axisFont;
        protected Font titleFont;
        protected Font readoutFont;
        protected Font wiggleMeFont;
        protected Font controlButtonFont;
        protected Font chartButtonFont;
        protected Font textBoxFont;
        protected Font timeLabelFont;
        protected Font timeFont;
        protected Font walkwayFont;
        protected Font verticalTitleFont;

        public Font getAxisFont() {
            return axisFont;
        }

        public Font getTitleFont() {
            return titleFont;
        }

        public Font getReadoutFont() {
            return readoutFont;
        }

        public Font getWiggleMeFont() {
            return wiggleMeFont;
        }

        public Font getControlButtonFont() {
            return controlButtonFont;
        }

        public Font getTextBoxFont() {
            return textBoxFont;
        }

        public Font getChartButtonFont() {
            return chartButtonFont;
        }

        public Font getTimeLabelFont() {
            return timeLabelFont;
        }

        public Font getTimeFont() {
            return timeFont;
        }

        public Font getWalkwayFont() {
            return walkwayFont;
        }

        public Font getVerticalTitleFont() {
            return verticalTitleFont;
        }
    }

    static class Large extends MMFontSet {
        public Large() {
            axisFont = new PhetDefaultFont( Font.BOLD, 14 );
            titleFont = new PhetDefaultFont( Font.BOLD, 12 );
            readoutFont = new PhetDefaultFont( Font.BOLD, 22 );
            wiggleMeFont = new PhetDefaultFont( Font.BOLD, 16 );
            controlButtonFont = new PhetDefaultFont( Font.PLAIN, 14 );
            textBoxFont = new PhetDefaultFont( Font.BOLD, 11 );
            chartButtonFont = new PhetDefaultFont( Font.BOLD, 14 );
            timeLabelFont = new PhetDefaultFont( Font.BOLD, 14 );
            timeFont = new PhetDefaultFont( Font.PLAIN, 36 );
            walkwayFont = new Font( "dialog", Font.PLAIN, 20 );
            verticalTitleFont = new PhetDefaultFont( Font.BOLD, 16 );
        }
    }

    static class Medium extends MMFontSet {
        public Medium() {
            axisFont = new PhetDefaultFont( Font.BOLD, 14 );
            titleFont = new PhetDefaultFont( Font.BOLD, 10 );
            readoutFont = new PhetDefaultFont( Font.BOLD, 16 );
            wiggleMeFont = new PhetDefaultFont( Font.BOLD, 16 );
            controlButtonFont = new PhetDefaultFont( Font.PLAIN, 10 );
            textBoxFont = new PhetDefaultFont( Font.BOLD, 10 );
            chartButtonFont = new PhetDefaultFont( Font.BOLD, 10 );
            timeLabelFont = new PhetDefaultFont( Font.BOLD, 12 );
            timeFont = new PhetDefaultFont( Font.PLAIN, 36 );
            walkwayFont = new Font( "dialog", Font.PLAIN, 20 );
            verticalTitleFont = new PhetDefaultFont( Font.BOLD, 12 );
        }
    }

    static class Small extends Medium {

    }

    static {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if( d.width > 1024 ) {
            fontSet = new Large();
//            System.out.println( "MM: Chose font for width> 1280" );
        }
        else if( d.width <= 800 ) {
            fontSet = new Small();
//            System.out.println( "MM: Chose font for <=800" );
        }
        else {
            fontSet = new Medium();
//            System.out.println( "MM: Chose font for width between between 800 and 1280" );
        }
    }
}
