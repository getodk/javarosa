package com.actionscript.m07;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;

/**
 * Thread for opening a Connection reading all from the input
 * @author Massimo Carli
 */
public class ConnectionThread extends Thread{
    
    /*
     * Connection listener
     */
    private ConnectionThreadListener listener;
    
    /*
     * Connection URi
     */
    private String uri;
    
    /**
     * Creates a new instance of ConnectionThread
     */
    public ConnectionThread(String uri) {
        this.uri=uri;
    }
    
    public void run(){
        // InputStream initialization
        InputStream is = null;
        // Connection initialization
        Connection connection = null;
        try {
            // Here we have to open the Connection
            connection = Connector.open(uri);
            // Here we obtain InputStream
            is = Connector.openInputStream(uri);
            // Buffer for output
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            // If the connection is a Content Connection we get information
            if(connection instanceof ContentConnection){
                // Cast it
                ContentConnection contentConnection = (ContentConnection)connection;
                // Get Type information
                String type = contentConnection.getType();
                // Get Encoding
                String encoding = contentConnection.getEncoding();
                // Get Length
                long length = contentConnection.getLength();
                // Compose message and write to buffer
                byte[] message = ("\ntype:"+type+" \nenconding:"+encoding+"\nlength:"+length).getBytes();
                buffer.write(message);
                // Now we test if a HttpConnection
                if(contentConnection instanceof HttpConnection){
                    // Cast it
                    HttpConnection httpConnection = (HttpConnection)contentConnection;
                    // We read some useful information
                    int responseCode        = httpConnection.getResponseCode();
                    String responseMessage  = httpConnection.getResponseMessage();
                    String query            = httpConnection.getQuery();
                    // Compose message
                    message = ("\nresponseCode:"+responseCode+"\nresponseMessage:"+responseMessage+"\nquery:"+query).getBytes();
                    buffer.write(message);
                }
            }
            // Read data from the InputStream
            int ch;
            while ((ch = is.read()) != -1) {
                buffer.write((byte)ch);
            }
            // We have finished so we return data to the listener
            if(listener!=null){
                String result = new String(buffer.toByteArray());
                listener.notifyData(result);
            }
        } catch (Exception e) {
            if(listener!=null){
                listener.notifyError(e.getMessage());
            }
        }finally{
            // Close all
            if(is!=null){
                try {
                    is.close();
                } catch (IOException ex) {
                    if(listener!=null){
                        listener.notifyError(ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Set ConnectionListener
     *@param listener Listener of the connection
     */
    public void setConnectionListener(ConnectionThreadListener listener){
        this.listener=listener;
    }
    
}
