package org.javarosa.util;

public class DebugMessage {
	
	private DebugMessage(){
		
	}
	
	public static void show(){
		System.out.println("DEBUG - DEBUG - DEBUG");
	}
	
	public static void show(String msg){
		System.out.println(msg);
	}
	
	public static void showError(String msg){
		System.err.println(msg);
	}
	
	public static void showError(Exception e){
		e.printStackTrace();
	}
}
