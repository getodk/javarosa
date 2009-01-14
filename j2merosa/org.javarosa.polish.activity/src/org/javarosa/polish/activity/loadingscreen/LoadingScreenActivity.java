/**
 * 
 */
package org.javarosa.polish.activity.loadingscreen;

import javax.microedition.lcdui.Gauge;

import org.javarosa.core.Context;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.ICommand;
import org.javarosa.core.api.IShell;

/**
 * @author Brian DeRenzi
 *
 */
public class LoadingScreenActivity implements IActivity, Runnable {
	
	private IShell parent = null;
	private LoadingView lview = null;
	
	Context context;
	
	// Need to have the loading screen in a separate thread.
	private Thread thread;
	
	private boolean stopped = false;
	
	public LoadingScreenActivity(IShell p) {
		System.out.println("loading screen activity constructor");
		
		// Setup the thread
		this.thread = new Thread(this);
		this.thread.setPriority(Thread.MIN_PRIORITY);
		
		this.parent = p;
		this.lview = new LoadingView();
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#start(org.javarosa.core.Context)
	 */
	public void start(Context context) {
		System.out.println("loading screen start");
		
		
		// Start the thread part
		this.stopped = false;
		
		// only start the tread if not alive
		if (this.thread.isAlive() == false) {
			this.thread.start();
		}
	}

	public void stop() {
		stopped = true;
	}

	public void run() {

		// In rare cases, stop() might be called after the thread
		// start() called but run() is not yet called ...
		if (!stopped) {
			if(this.lview == null)
				this.lview = new LoadingView();
			this.parent.setDisplay(this, this.lview);
		}
		
		// do until notified to stop/quit
		while (stopped == false) {
			this.lview.update();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// At this point we're completely done.  We don't return anywhere though since we're a background process.
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#contextChanged(org.javarosa.core.Context)
	 */
	public void contextChanged(Context globalContext) {
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#destroy()
	 */
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#getActivityContext()
	 */
	public Context getActivityContext() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#halt()
	 */
	public void halt() {
		stop();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#resume(org.javarosa.core.Context)
	 */
	public void resume(Context globalContext) {
		start(globalContext);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#setShell(org.javarosa.core.api.IShell)
	 */
	public void setShell(IShell shell) {
		this.parent = shell;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IActivity#annotateCommand(org.javarosa.core.api.ICommand)
	 */
	public void annotateCommand(ICommand command) {
		throw new RuntimeException("The Activity Class " + this.getClass().getName() + " Does Not Yet Implement the annotateCommand Interface Method. Please Implement It.");
	}
}
