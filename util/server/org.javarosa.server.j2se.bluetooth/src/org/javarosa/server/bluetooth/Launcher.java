package org.javarosa.server.bluetooth;

public class Launcher {

	public static void main(String args[]){
		System.out.println("Server starting!!!");
		//RfcommServer server = new RfcommServer();
		//server.run();
		
		new Thread(new RfcommServer()).start();
	}
}
