package org.javarosa.communication.reliablehttp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.HttpHeaderAppendingVisitor;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class ReliableHttpTransportMethod extends HttpTransportMethod {

	private static final String name = "RELIABLE_HTTP";

	private ITransportManager manager;
	
	private IActivity destinationRetrievalActivity;
	private ReliableWorkerThread primaryWorker;

    //RL: Temporary values until we start using timeouts
    private static final int MAX_NUM_RETRIES = 7; 
    private static final int MD5 = 999; 

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#transmit(org.openmrs.transport.TransportMessage)
	 */
	public void transmit(TransportMessage message, ITransportManager manager) {
		cacheURL(message);		
		this.manager = manager;
		primaryWorker = new ReliableWorkerThread();
		primaryWorker.setMessage(message);	
		new Thread(primaryWorker).start();
	}

	/**
	 * 
	 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
	 */
	private class ReliableWorkerThread implements Runnable {
		private HttpConnection con = null;
		private InputStream in = null;
		private OutputStream out = null;
		
		private TransportMessage message;
		
		public void setMessage(TransportMessage message) {
			this.message  = message;
		}
		
		
		public void cleanStreams(){
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (IOException e) {
					// ignore
				}
			}
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			
            try{
    			IDataPayload payload = message.getPayloadData();
    			HttpHeaderAppendingVisitor visitor = new HttpHeaderAppendingVisitor();
    			IDataPayload httpload = (IDataPayload)payload.accept(visitor);
    
    			HttpTransportDestination destination = (HttpTransportDestination)message.getDestination();
			
    			reliableHttpPost(destination.getURL(),MD5,
		            visitor.getOverallContentType(),httpload);		

				// update status
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				//#if debug.output==verbose
				System.out.println("Status: " + message.getStatus());
				//#endif
				message.setChanged();
				message.notifyObservers(message.getReplyloadData());

			} catch (ClassCastException e) {
				Alert alert = new Alert("ERROR! cce", e.getMessage(), null, AlertType.ERROR);
				throw new IllegalArgumentException(message.getDestination()
						+ " is not a valid HTTP URL");
			} catch (IOException e) {
				Alert alert = new Alert("ERROR! ioe", e.getMessage(), null, AlertType.ERROR);
				//#if debug.output==verbose || debug.output==exception
				System.out.println("Error! ioe: " + e.getMessage());
                e.printStackTrace();
				//#endif
			} catch (OtherIOException e){
                Alert alert = new Alert("ERROR! oioe", e.getMessage(), null, AlertType.ERROR);
                //#if debug.output==verbose || debug.output==exception
                System.out.println("Error! oioe: " + e.getMessage() );
                e.printStackTrace();
                //#endif			  
			} catch(java.lang.SecurityException se) {
				Alert alert = new Alert("ERROR! se", se.getMessage(), null, AlertType.ERROR);
	             /***
                 * This exception was added to deal with the user denying access to airtime
                 */
			 // update status
                message.setStatus(TransportMessage.STATUS_FAILED);
        		//#if debug.output==verbose || debug.output==exception
                System.out.println("Status: " + message.getStatus());
                //#endif
                message.setChanged();
                message.notifyObservers(null); 
		    }finally {
				cleanUp(in);
				cleanUp(out);
				cleanUp(con);
			}
		}

		private void process(byte data) {
			System.out.print(data);
			byte[] temp = new byte[1];
			temp[0] = data;

			message.setReplyloadData(temp);
		}

		private void process(byte[] data) {
			//#if debug.output==verbose
			System.out.println(new String(data));
			//#endif
			message.setReplyloadData(data);
		}
		
		/*
		 * Sends a header of the form
		 * POST /upload HTTP/1.1 / Host: example.com / If-Match: "vEpr6barcD" / Content-Length: 100 / Content-Range: 0-99/100
		 */
		private void reliableHttpPost(String url, int MD5, String contentType, IDataPayload pl)
		    throws IOException, OtherIOException {
		    boolean sendFailed;
            InputStream in = pl.getPayloadStream();

		    //Eventually we will use timeouts instead of maximum number of retries
		    int numTries = 0;
            while (numTries<MAX_NUM_RETRIES){
                numTries++;
                sendFailed = false;
                //Posting to a non-existent machine should generate an exception in the following code

                int lastByte = reliableRequestLastByte(url, MD5);
                if (lastByte != -1 ){
                    //server supports reliable http and has returned a valid lastByte
                    in.skip(lastByte);
                }
                
                //#if debug.output==verbose || debug.output==exception
                try{
                //#endif
                    con = (HttpConnection) Connector.open(url);
                    con.setRequestMethod(HttpConnection.POST);
                    setDefaultRequestProperties(con);
                    con.setRequestProperty("Content-Type",contentType);
                    con.setRequestProperty("If-Match",String.valueOf(MD5));
                    out = con.openOutputStream(); // Problem exists here on 3110c CommCare Application: open hangs
                //#if debug.output==verbose || debug.output==exception
                } catch (IOException e){
                    throw new IOException("reliableHttpPost: error in initialization");
                }
                //#endif
                
                //We only want to deal with errors in streaming files
                try{
                    //RL: This should be optimized to read in packets of 1450 bytes at a time
                    int c = inputStreamReadCastExceptions(in);
                    while(c != -1) {
                        out.write(c);
                        c = inputStreamReadCastExceptions(in);
                    }
                    out.flush();	   
                    
                    //Check whether we have received the final ACK of this file
                    int responseCode = con.getResponseCode();
                    String ETag = con.getHeaderField("ETag");
                    if (responseCode != HttpConnection.HTTP_OK){
                        //RL: todo - verify that this is the *correct* MD5
                        if (ETag != null){ 
                            //If the ETag is valid, this indicates the server knows the reliablehttp protocol
                            //So we should try to resend
                            sendFailed = true;
                        }
                        else{
                            //The server has generated some unknown response code
                            throw new OtherIOException("IOEXCEPTION: Server response code: " + responseCode);       //CHANGE THIS BACK                 
                        }
                    }
                } catch (IOException e){ 
                    sendFailed = true;
                }
                
                if (sendFailed) {
                    cleanUp(out);
                    cleanUp(con);
                    //Ask the server to transmit the last byte it has received
                    cleanUp(in);
                    in = pl.getPayloadStream();
                    try{
                        // many network errors are transient, so it's good to give a little buffer between re-tries
                        Thread.sleep(1000);
                    } catch (InterruptedException e){}
                    continue;
                }
                //At this point, we have successfully transmitted the whole file and gotten a final ACK
                if( numTries == MAX_NUM_RETRIES ){
                    //The connection is truly foo-bar'd. Give up. 
                    throw new IOException("Max num retransmits exceeded");
                }                   
                
                in.close();
                in = con.openInputStream();              
                
                // Get the length and process the data
                int len = (int) con.getLength();
                if (len > 0) {
                    int actual = 0;
                    int bytesread = 0;
                    byte[] data = new byte[len];
                    while ((bytesread != len) && (actual != -1)) {
                        actual = in.read(data, bytesread, len - bytesread);
                        bytesread += actual;
                    }
                    process(data);
                } else {
                    int ch;
                    while ((ch = in.read()) != -1) {
                        process((byte) ch);
                    }
                }
                break;
            }
   		}
		
		/*
		* HEAD /upload/eCn14NjNAy HTTP/1.1
		* Host: example.com
		* Content-Length: 0
		* Content-Range: bytes *\/100
		* @returns "-1" if not byte received
		*/
		private int reliableRequestLastByte (String url, int MD5) throws IOException{
		    //send out request
		    int numTries = 0;
		    int r = -1;
		    while(numTries < MAX_NUM_RETRIES){
		        numTries++;
                try{
    	            HttpConnection con = (HttpConnection) Connector.open(url);
    	            con.setRequestMethod(HttpConnection.HEAD);
    	            setDefaultRequestProperties(con);
    	            //RL: todo - We probably need to request the correct Content-Type
    	            //con.setRequestProperty("Content-Type",visitor.getOverallContentType());           
                    con.setRequestProperty("If-Match", String.valueOf(MD5));
                    
    	            OutputStream out = con.openOutputStream(); 
    	            out.flush();
    	            out.close();
    	            r = readLastByteReceived(con);
		        } catch (IOException e){ 
		            //keep on trying until maximum retransmission timeout
		            cleanUp(out);
		            cleanUp(con);
                    // many network errors are transient, so it's good to give a little buffer between re-tries
                    try{
                        Thread.sleep(5000);
                    } catch (InterruptedException e2){}
		            continue;
		        }
	            break;
		    }
		    
		    if (numTries >= MAX_NUM_RETRIES ){
    		    //The connection is truly messed up. Give up.
    		    throw new IOException("Max num retransmits exceeded");
		    }
            cleanUp(con);
            return r; 
		}
		
		/* Server response expected is of the form:
		* HTTP/1.1 308 Resume Incomplete / ETag: "md5sum_of_posted_file" / Content-Length: 0 / Range: range_of_bytes_the_server_has_received
        * @returns "-1" if cannot determine last byte received by server
		*/
		private int readLastByteReceived(HttpConnection con) throws IOException
		{
	          String ETag = con.getHeaderField("ETag");
	          // RL: This should check that ETag == MD5 of file. For now, just checks for non-null.
	          if (ETag == null) return -1; 
              String bytesReceivedByServer = con.getHeaderField("Range");
              if (bytesReceivedByServer==null) return -1;
              int i = bytesReceivedByServer.indexOf('-');
              int j = bytesReceivedByServer.indexOf('/');
              if ( i==-1 || i>j || j==-1 || j>(bytesReceivedByServer.length()-1) ) return -1;
              try {
                  return Integer.parseInt(bytesReceivedByServer.substring(i+1,j));
              } catch (NumberFormatException e){
                  return -1;
              }
		}
		
		private void setDefaultRequestProperties(HttpConnection con) throws IOException {
            con.setRequestProperty("User-Agent","Profile/MIDP-2.0 Configuration/CLDC-1.1");
            con.setRequestProperty("Content-Language", "en-US");
            con.setRequestProperty("MIME-version", "1.0");
		}

		/*
		 * The only reason this function exists is so that IOExceptions from the
		 * inputstream do not cause reliablehttp to attempt to retransmit
		 */
		private int inputStreamReadCastExceptions (InputStream in) throws OtherIOException{
		    try{
		        return in.read();
		    } catch (IOException e) {
		        throw new OtherIOException(": inputstream");
		    }
		}
		
	    /*
	     * I really hate creating custom exceptions... 
	     * But this is the only way to differentiate inputstream errors and httpconnection/outputstream errors
	     */
	    private class OtherIOException extends java.lang.Exception
	    {
	        OtherIOException(String msg){
	            super(msg);
	        }
	    }
	}

	private void cleanUp(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public void cleanUp(OutputStream out) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * @param con
	 *            the connection
	 */
	private void cleanUp(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (IOException e) {
				// ignore
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#getId()
	 */
	public int getId() {
		return TransportMethod.HTTP_GCF;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.TransportMethod#getDefaultDestination()
	 */
	public ITransportDestination getDefaultDestination() {
		String url = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(ReliableHttpTransportProperties.POST_URL_PROPERTY);
		if(url == null) {
			return null;
		} else {
			return new HttpTransportDestination(url);
		}
	}
	public void setDestinationRetrievalActivity(IActivity activity) {
		destinationRetrievalActivity = activity;
	}
	
	public IActivity getDestinationRetrievalActivity() {
		return destinationRetrievalActivity;
	}

	
	public void closeConnections() {
		if(primaryWorker!=null){
			primaryWorker.cleanStreams();
		}
		
		
	}
}
