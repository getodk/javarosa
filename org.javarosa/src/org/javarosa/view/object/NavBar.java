package org.javarosa.view.object;

import org.javarosa.utils.ViewUtils;

/**
 * The Navigation Bar component is responsible for controlling flow over a protocol,
 * and displaying the current position in the protocol.
 * 
 * @author ctsims
 * @date Aug-08-2007
 *
 */
public class NavBar extends Component implements ISizeChangeListener, IActionListener {
	DProgressBar progressBar = new DProgressBar(100);
	
	DButton backButton = new DButton("Back");
	DButton nextButton = new DButton("Next");
	
	/**
	 * Creates a new navication bar
	 */
	public NavBar() {
		setupComponents();
		this.addSizeChangeListener(this);
		nextButton.addActionListener(this);
	}
	
	/**
	 * Sets up the various components
	 */
	private void setupComponents() {
		backButton.setBackgroundColor(ViewUtils.LIGHT_GREY);
		nextButton.setBackgroundColor(ViewUtils.LIGHT_GREY);
		this.add(progressBar);
		this.add(backButton);
		this.add(nextButton);
	}
	
	/**
	 * Lays out the components in the navigation bar. 
	 */
	public void sizeChanged() {
		int yBuffer = this.getHeight()/5;
		progressBar.setWidth(this.getWidth()/2);
		progressBar.setHeight(this.getHeight() - yBuffer);
		progressBar.setY(yBuffer/2);
		progressBar.setX(this.getWidth()/4);
		
		backButton.setWidth(this.getWidth()/5);
		backButton.setHeight(this.getHeight() - yBuffer);
		backButton.setY(yBuffer/2);
		backButton.setX(this.getWidth()/7 - backButton.getWidth()/2);
		
		nextButton.setWidth(this.getWidth()/5);
		nextButton.setHeight(this.getHeight() - yBuffer);
		nextButton.setY(yBuffer/2);
		nextButton.setX(6*this.getWidth()/7 - backButton.getWidth()/2);
	}
	
	public void OnAction() {
		
	}
}
