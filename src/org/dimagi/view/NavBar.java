package org.dimagi.view;

import org.dimagi.utils.ViewUtils;

public class NavBar extends Component implements ISizeChangeListener, IActionListener {
	DProgressBar progressBar = new DProgressBar(100);
	
	DButton backButton = new DButton("Back");
	DButton nextButton = new DButton("Next");
	
	public NavBar() {
		setupComponents();
		this.addSizeChangeListener(this);
		nextButton.addActionListener(this);
	}
	
	private void setupComponents() {
		backButton.setBackgroundColor(ViewUtils.LIGHT_GREY);
		nextButton.setBackgroundColor(ViewUtils.LIGHT_GREY);
		this.add(progressBar);
		this.add(backButton);
		this.add(nextButton);
	}
	
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
		nextButton.setText("Sweet!");
	}
	
}
