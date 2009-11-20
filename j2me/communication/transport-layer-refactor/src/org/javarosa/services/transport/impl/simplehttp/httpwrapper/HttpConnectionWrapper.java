/* License
 * 
 * Copyright 1994-2005 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *  * Redistribution of source code must retain the above copyright notice,
 *	this list of conditions and the following disclaimer.
 * 
 *  * Redistribution in binary form must reproduce the above copyright notice,
 *	this list of conditions and the following disclaimer in the
 *	documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *  
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility. 
 */

package org.javarosa.services.transport.impl.simplehttp.httpwrapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

/**
 * A wrapper for the HttpConnection interface that logs method
 * calls and state transitions. Wrap a connection immediately 
 * after obtaining it from Connector.open. Information is logged
 * according to the log level set the "httpwrapper" logger.
 */

public class HttpConnectionWrapper implements HttpConnection {
    
    // Defines the logger used for logging everything.
    
    public static final Logger logger = Logger.getLogger( "httpwrapper" );
    
    static {
        logger.setLevel( Level.FINE ); // Change this accordingly
    }
    
    public static final String SETUP_STATE = "setup";
    public static final String CONNECTED_STATE = "connected";
    public static final String CLOSED_STATE = "closed";
    
    private static final String MSG_ALREADY_CLOSED = 
            "Connection has already been closed";
    private static final String MSG_INPUT_STREAM =
            "Input stream has already been opened";
    private static final String MSG_OUTPUT_STREAM =
            "Output stream has already been opened";
    private static final String MSG_NOT_SETUP_STATE =
            "Not in setup state";
    
    private int                 _id;
    private String              _idString;
    private HttpConnection      _original;
    private String              _state;
    private InputStreamWrapper  _streamIn;
    private OutputStreamWrapper _streamOut;
    
    private static int          _lastID;
    
    /**
     * Constructs a wrapper for an HttpConnection.
     */
    
    public HttpConnectionWrapper( HttpConnection original ) {
        _original = original;
        _state = SETUP_STATE;
        _id = ++_lastID;
        _idString = generateID( _id );
        
        info( "Wrapping HttpConnection " + original.getURL() );
        info( "Entering setup state" );
    }
    
    /**
     * Generates a new string ID for the wrapper.
     */
    
    private static String generateID( int id ) {
        StringBuffer b = new StringBuffer();
        b.append( "[HTTP " );
        b.append( id );
        b.append( "] " );
        
        return b.toString();
    }

    //--------------------------------------------------------------------------------------------
    // Methods that can only be invoked in Setup state. Note that changing values doesn't
    // have any effect once an output stream is open, so to catch these we throw an
    // exception right away.
    
    public void setRequestMethod(String str) throws IOException {
        onlyInSetup( "setRequestMethod" );
        if( _streamOut == null ){
            info( "setRequestMethod( " + str + " )" );
            try {
                _original.setRequestMethod( str );
            }
            catch( IOException e ){
                rethrow( "setRequestMethod", e );
            }
        } else {
            throw makeException( "setRequestMethod", MSG_OUTPUT_STREAM );
        }
    }
    
    public void setRequestProperty(String str, String str1) throws IOException {
        onlyInSetup( "setRequestProperty" );
        if( _streamOut == null ){
            info( "setRequestProperty( " + str + ", " + str1 + " )" );
            try {
                _original.setRequestProperty( str, str1 );
            } 
            catch( IOException e ){
                rethrow( "setRequestProperty", e );
            }
        } else {
            throw makeException( "setRequestProperty", MSG_OUTPUT_STREAM );
        }
    }

    //--------------------------------------------------------------------------------------------
    // Methods that cause transition to Connected state from Setup state
    // and throw an error otherwise if closed

    public InputStream openInputStream() throws IOException {
        if( _streamIn != null ){
            throw makeException( "openInputStream", MSG_INPUT_STREAM );
        }
        transitionToConnected( "openInputStream" );
        info( "Opening input stream" );
        try {
            _streamIn = new InputStreamWrapper( _original.openInputStream() );
        }
        catch( IOException e ){
            rethrow( "openInputStream", e );
        }
        return _streamIn;
    }
    
    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream( openInputStream() );
    }

    public long getLength() {
        transitionToConnectedNoThrow( "getLength" );
        long len = _original.getLength();
        info( "getLength() = " + len );
        return len;
    }

    public String getType() {
        transitionToConnectedNoThrow( "getType" );
        String type = _original.getType();
        info( "getType() = " + type );
        return type;
    }

    public String getEncoding() {
        transitionToConnectedNoThrow( "getEncoding" );
        String encoding = _original.getEncoding();
        info( "getEncoding() = " + encoding );
        return encoding;
    }

    public String getHeaderField(int param) throws IOException {
        transitionToConnected( "getHeaderField" );
        String field = null;
        
        try {
            field = _original.getHeaderField( param );
        }
        catch( IOException e ){
            rethrow( "getHeaderField", e );
        }
        info( "getHeaderField( " + param + " ) = " + field );
        return field;
    }
    
    public String getHeaderField(String str) throws IOException {
        transitionToConnected( "getHeaderField" );
        String field = null;
        try {
            field = _original.getHeaderField( str );
        }
        catch( IOException e ){
            rethrow( "getHeaderField", e );
        }
        info( "getHeaderField( " + str + " ) = " + field );
        return field;
    }

    public int getResponseCode() throws IOException {
        transitionToConnected( "getResponseCode" );
        int rc = 0;
        try {
            rc = _original.getResponseCode();
        }
        catch( IOException e ){
            rethrow( "getResponseCode", e );
        }
        info( "getResponseCode() = " + rc );
        return rc;
    }

    public String getResponseMessage() throws IOException {
        transitionToConnected( "getResponseMessage" );
        String msg = null;
        try {
            msg = _original.getResponseMessage();
        }
        catch( IOException e ){
            rethrow( "getResponseMessage", e );
        }
        info( "getResponseMessage() = " + msg );
        return msg;
    }

    public int getHeaderFieldInt(String str, int param) throws IOException {
        transitionToConnected( "getHeaderFieldInt" );
        int field = 0;
        try {
            field = _original.getHeaderFieldInt( str, param );
        }
        catch( IOException e ){
            rethrow( "getHeaderFieldInt", e );
        }
        info( "getHeaderFieldInt( " + param + " ) = " + field );
        return field;
    }

    public long getHeaderFieldDate(String str, long param) throws IOException {
        transitionToConnected( "getHeaderFieldDate" );
        long date = 0;
        try {
            date = _original.getHeaderFieldDate( str, param );
        }
        catch( IOException e ){
            rethrow( "getHeaderFieldDate", e );
        }
        info( "getHeaderFieldDate( " + param + " ) = " + date );
        return date;
    }

    public long getExpiration() throws IOException {
        transitionToConnected( "getExpiration" );
        long exp = 0;
        try {
            exp = _original.getExpiration();
        }
        catch( IOException e ){
            rethrow( "getExpiration", e );
        }
        info( "getExpiration() = " + exp );
        return exp;
    }

    public long getDate() throws IOException {
        transitionToConnected( "getDate" );
        long date = 0;
        try {
            date = _original.getDate();
        }
        catch( IOException e ){
            rethrow( "getDate", e );
        }
        info( "getDate() = " + date );
        return date;
    }

    public long getLastModified() throws IOException {
        transitionToConnected( "getLastModified" );
        long mod = 0;
        try {
            mod = _original.getLastModified();
        }
        catch( IOException e ){
            rethrow( "getLastModified", e );
        }
        info( "getLastModified() = " + mod );
        return mod;
    }

    public String getHeaderFieldKey(int param) throws IOException {
        transitionToConnected( "getHeaderFieldKey" );
        String key = null;
        try {
            key = _original.getHeaderFieldKey( param );
        }
        catch( IOException e ){
            rethrow( "getHeaderFieldKey", e );
        }
        info( "getHeaderFieldKey( " + param + " ) = " + key );
        return key;
    }
    
    //-------------------------------------------------------------------
    // Methods that technically don't cause a transition to Connected 
    // until the stream is closed

    public OutputStream openOutputStream() throws IOException {
        if( _streamOut != null ){
            throw makeException( "openOutputStream", MSG_OUTPUT_STREAM );
        }
        try {
            _streamOut = new OutputStreamWrapper( _original.openOutputStream() );
        }
        catch( IOException e ){
            rethrow( "openOutputStream", e );
        }
        return _streamOut;
    }
    
    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream( openOutputStream() );
    }
    
    //------------------------------------------------------------------------
    // Methods that can be invoked as long as the state is not Closed    

    public void close() throws IOException {
        if( _state == CLOSED_STATE ){
            throw makeException( "close", MSG_ALREADY_CLOSED );
        }
        
        info( "Entering closed state" );
        
        if( _streamIn != null && !_streamIn.isClosed() ){
            warning( "The input stream is still open, be sure to close it" );
        }
        
        if( _streamOut != null && !_streamOut.isClosed() ){
            warning( "The output stream is still open, be sure to close it" );
        }
        
        _state = CLOSED_STATE;
        
        try {
            _original.close();
        }
        catch( IOException e ){
            rethrow( "close", e );
        }
    }

    public String getRequestMethod() {
        notIfClosedNoThrow( "getRequestMethod" );
        String method = _original.getRequestMethod();
        info( "getRequestMethod() = " + method );
        return method;
    }

    public String getRequestProperty(String str) {
        notIfClosedNoThrow( "getRequestProperty" );
        String value = _original.getRequestProperty( str );
        info( "getRequestProperty( " + str + " ) = " + value );
        return value;
    }

    public String getURL() {
        notIfClosedNoThrow( "getURL" );
        String url = _original.getURL();
        info( "getURL() = " + url );
        return url;
    }

    public String getProtocol() {
        notIfClosedNoThrow( "getProtocol" );
        String protocol = _original.getProtocol();
        info( "getProtocol() = " + protocol );
        return protocol;
    }

    public String getHost() {
        notIfClosedNoThrow( "getHost" );
        String host = _original.getHost();
        info( "getHost() = " + host );
        return host;
    }

    public String getFile() {
        notIfClosedNoThrow( "getFile" );
        String file = _original.getFile();
        info( "getFile() = " + file );
        return file;
    }

    public String getRef() {
        notIfClosedNoThrow( "getRef" );
        String ref = _original.getRef();
        info( "getRef() = " + ref );
        return ref;
    }

    public int getPort() {
        notIfClosedNoThrow( "getPort" );
        int port = _original.getPort();
        info( "getPort() = " + port );
        return port;
    }

    public String getQuery() {
        notIfClosedNoThrow( "getQuery" );
        String query = _original.getQuery();
        info( "getQuery() = " + query );
        return query;
    }

    //----------------------------------------------------------------------
    // State management methods

    /**
     * Creates an IOException based on a method name and the given message.
     * 
     * @param method the method that is the root of the exception.
     * @param msg    the exception message.
     *
     * @return IOException the new exception.
     */
    
    private IOException makeException( String method, String msg ) {
        StringBuffer b = new StringBuffer();
        b.append( _idString );
        b.append( method );
        b.append( ": " );
        b.append( msg );
        
        String m = b.toString();
        
        logger.warning( m );
        return new IOException( m );
    }

    /** 
     * Rethrows an exception after logging it.
     *
     * @param method the method invoked when the exception occurred.
     * @param e      the exception.
     *
     * @throws IOException the original exception.
     */
    
    private void rethrow( String method, IOException e ) throws IOException {
        StringBuffer b = new StringBuffer();
        b.append( _idString );
        b.append( method );
        b.append( " throws exception " );
        b.append( e.toString() );
        
        logger.warning( b.toString() );
        throw e;
    }
    
    /**
     * Ensures that a method is invoked in the setup state only.
     *
     * @param method the method invoked.
     *
     * @throws IOException if not in setup state.
     */
    
    private void onlyInSetup( String method ) throws IOException {
        fine( "Invoking " + method );
        if( _state != SETUP_STATE ){
            throw makeException( method, MSG_NOT_SETUP_STATE );
        }
    }
    
    /**
     * Ensures that a method is not invoked in closed state.
     *
     * @param method the method invoked.
     * 
     * @throws IOException if in closed state.
     */
    
    private void notIfClosed( String method ) throws IOException {
        fine( "Invoking " + method );
        if( _state == CLOSED_STATE ){
            throw makeException( method, MSG_ALREADY_CLOSED );
        }
    }
    
    /**
     * Ensures that a method is not invoked in closed state, but
     * swallows the resulting exception if any.
     *
     * @param method the method invoked.
     */
    
    private void notIfClosedNoThrow( String method ) {
        try {
            notIfClosed( method );
        }
        catch( IOException e ){
            // do nothing with it
        }
    }
    
    /** 
     * Transitions from setup to connected state.
     *
     * @param method the method invoked.
     *
     * @throws IOException if in closed state.
     */
    
    private void transitionToConnected( String method ) throws IOException {
        fine( "Invoking " + method );
        if( _state == SETUP_STATE ){
            info( "Entering connected state" );
            _state = CONNECTED_STATE;
        }
        
        if( _state != CONNECTED_STATE ){
            throw makeException( method, MSG_ALREADY_CLOSED );
        }
    }
    
    /** 
     * Transitions from setup to connected state, but swallows
     * the resulting exception if any.
     *
     * @param method the method invoked.
     */
    
    private void transitionToConnectedNoThrow( String method ) {
        try {
            transitionToConnected( method );
        }
        catch( IOException e ){
            // do nothing
        }
    }

    /**
     * Logs the message at the INFO level.
     *
     * @param msg the message to log.
     */
    
    private void info( String msg ) {
        logger.info( _idString + msg );
    }
    
    /**
     * Logs the message at the FINE level.
     *
     * @param msg the message to log.
     */
    
    private void fine( String msg ) {
        logger.fine( _idString + msg );
    }
    
    /**
     * Logs the message at the WARNING level.
     *
     * @param msg the message to log.
     */
    
    private void warning( String msg ) {
        logger.warning( _idString + msg );
    }
    
    //--------------------------------------------------------------------------------------------
    // A simple wrapper for tracking when an input stream is closed.
    
    private class InputStreamWrapper extends InputStream {
        private boolean     _isClosed;
        private InputStream _original;
        
        public InputStreamWrapper( InputStream original ) {
            _original = original;
        }
        
        public void close() throws IOException {
            info( "Closing input stream" );
            
            _isClosed = true;
            _original.close();
        }
        
        public boolean isClosed() {
            return _isClosed;
        }
        
        public int read() throws IOException {
            return _original.read();
        }
    }
    
    //--------------------------------------------------------------------------------------------
    // A simple wrapper for tracking when an output stream is closed or flushed.
    
    private class OutputStreamWrapper extends OutputStream {   
        private boolean      _isClosed;
        private OutputStream _original;
        
        /** Creates a new instance of OutputStreamWrapper */
        public OutputStreamWrapper( OutputStream original ) {
            _original = original;
        }
        
        public void close() throws IOException {
            info( "Closing output stream" );
            
            if( _state == SETUP_STATE ){
                transitionToConnected( "OutputStream.close" );
            }
           
            _isClosed = true;
            _original.close();
        }
        
        public void flush() throws IOException {
            info( "Flushing output stream" );
            
            if( _state == SETUP_STATE ){
                transitionToConnected( "OutputStream.flush" );
            }
            
            _original.flush();
        }
        
        public boolean isClosed() {
            return _isClosed;
        }
        
        public void write(int param) throws IOException {
            _original.write( param );
        }
    }
}
