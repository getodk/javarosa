package org.javarosa.communication;

public class TransportLayerFactory {
	
	private static TransportLayerFactory factory;
	
	private TransportLayerFactory(){
	
	}
	
	public static TransportLayerFactory getInstance(){
		if(factory == null)
			factory = new TransportLayerFactory();
		return factory;
	}
	
	public TransportLayer getTransportLayer(){
		return new DefaultTransportLayer();
	}
	
	//Is this method really necessary?
	public TransportLayer getTransportLayer(Class cls){
		TransportLayer layer = null;
		
		try{
			layer = (TransportLayer)cls.newInstance();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return layer;
	}

}
