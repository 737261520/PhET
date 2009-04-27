package edu.colorado.phet.unfuddletool;

public class UpdateThread extends Thread {
    public void run() {
        try {
            while ( true ) {
                // every minute
                Thread.sleep( 1000 * 60 );

                System.out.println( "Requesting latest activity from the server" );
                Activity.requestRecentActivity( 3 );
            }
        }
        catch( InterruptedException e ) {
            e.printStackTrace();
        }
    }
}
