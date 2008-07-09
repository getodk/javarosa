package org.javarosa.utils;

/**
 * Simple datatype used to represent a rectangle. 
 *
 */
public class Rectangle {

	// Make my own rectangle class because MIDP doesn't have one
	
	private int x;
	private int y;
	private int width;
	private int height;
	
	/**
	 * Constructs a rectangle with the given parameters
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return The x position of the rectangle
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * @return The y position of the rectangle
	 */
	public int getY() {
		return y;
	}
	
	/**	
	 * @return The rectangle's width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * @return The rectangle's height
	 */
	public int getHeight() {
		return height;
	}
	
}
