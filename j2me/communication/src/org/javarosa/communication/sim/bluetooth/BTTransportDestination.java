package org.javarosa.communication.sim.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class BTTransportDestination implements ITransportDestination {
	private String URL;
	
	public BTTransportDestination(){
		
	}
	
    public BTTransportDestination(String URL){
    	this.URL = URL;
	}
    
    public String getURL() {
		return URL;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStr
	        	JavaRosaServiceProvider.instance().getPropertyManager().setProperty(POST_URL_LIST_PROPERTY, new Vector());
	        }
	        
	        // PostURL Property
	        Vector postUrls = new Vector();eam, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		URL = in.readUTF();
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(URL);
		//out.flush();
	}
}
