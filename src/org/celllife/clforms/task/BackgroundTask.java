package org.celllife.clforms.task;

import java.io.IOException;
import java.util.TimerTask;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public abstract class BackgroundTask extends TimerTask {

  private Thread th;
  private boolean stopped;

  // Could be set in derived class

  // If the task is successfully completed
  protected Displayable nextScreen;
  // If the task is aborted
  protected Displayable prevScreen;
  // Display to draw-on
  protected Display display;
  // The gauge screen title
  protected String title;

  // Do we need to display an alert before
  // moving to the nextScreen?
  protected boolean needAlert = false;
  protected Alert alertScreen;

  public BackgroundTask (Display d) {
    display = d;
    th = new Thread(this);
  }

  public void go () {
    stopped = false;
    th.start();
  }

  public void stop () {
    stopped = true;
    th.setPriority(Thread.MIN_PRIORITY);
  }

  public void run() {

    ProgressGauge pg = null;
    try {
      pg = new ProgressGauge(this, title, display, prevScreen);
      runTask ();

    } catch (IOException ioe) {
      needAlert = true;
      alertScreen = new Alert("IO error");
      alertScreen.setString( "Please check your network or server setup" );
      alertScreen.setTimeout(Alert.FOREVER);
      nextScreen = prevScreen;
      System.out.println("Background task IO Error");
      ioe.printStackTrace();

    } catch (Exception e) {
      needAlert = true;
      alertScreen = new Alert("Task error");
      alertScreen.setString( "Please contact customer support. \n\n"
                             + "The error message is "
                             + e.getMessage() + "\n\n"
                             + "Error description is "
                             + e.toString() );
      alertScreen.setTimeout(Alert.FOREVER);
      nextScreen = prevScreen;
      System.out.println("Background task Error");
      e.printStackTrace();

    } finally {
      // Since pg could callback and reset "stopped" when its
      // Cancel key is pressed, we'd better check.
      if (!stopped) {
        if ( needAlert ) {
          pg.setNextScreen(alertScreen, nextScreen);
        } else {
          pg.setNextScreen(nextScreen);
        }
        pg.stop();  // notify progress gauge to quit
      }
    }
  }

  // template method.
  public abstract void runTask () throws Exception;
}
