package org.javarosa.communication.test;

import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import org.javarosa.communication.http.HttpTransportDestination;
import org.javarosa.communication.http.HttpTransportMethod;
import org.javarosa.communication.http.HttpTransportModule;
import org.javarosa.communication.reliablehttp.ReliableHttpTransportMethod;
import org.javarosa.core.Context;
import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.services.TransportManager;
import org.javarosa.core.services.transport.DataPointerPayload;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.storage.RmsStorage;
import org.javarosa.core.services.transport.ByteArrayPayload;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.j2me.storage.rms.RMSStorageModule;
import org.javarosa.media.image.model.FileDataPointer;

import j2meunit.framework.Test;
import j2meunit.framework.TestCase;
import j2meunit.framework.TestMethod;
import j2meunit.framework.TestSuite;



/*
 * This test case currently generates an error on javax.microedition.rms.RecordStore.getSlowingFactor 
 * This is due to some ant build/preverification funniness with not launching the emulator (which I'm not sure how to fix)  
 * If this is copied into the end of JavaRosaDemoShell's init(), it works fine.
 * 
 * @author <a href="mailto:rowenaluk@gmail.com">Rowena Luk</a>
 */
public class ReliableHttpTransportMethodTest extends TestCase {
    public static final String TEST_SERVER_URL = "http://192.168.0.102:8080/org.javarosa.server.util";
    public static final String TEST_DATA = "testdata";
    
    public ReliableHttpTransportMethodTest(String name, TestMethod rTestMethod) {
        super(name, rTestMethod);
    }
    
    public ReliableHttpTransportMethodTest(String name) {
        super(name);
    }
    
    public ReliableHttpTransportMethodTest() {
        super();
    }   
    
    public Test suite() {
        TestSuite aSuite = new TestSuite();
        
        aSuite.addTest(new ReliableHttpTransportMethodTest("ReliableHttpTransportMethod Test", new TestMethod() {
            public void run (TestCase tc) {
                ((ReliableHttpTransportMethodTest)tc).testTransmitString();
            }
        }));
            
        return aSuite;
    }

    public void testTransmitString(){
        WorkflowStack stack = new WorkflowStack();
        Context context = new Context();
        new RMSStorageModule().registerModule(context); 
        
        /* Ideally this unit test would launch its own server thread
         * to receive transmissions. For now, we assume a server is running at TEST_SERVER_URL
         * which may or may not support the reliableHttpProtocol
         * (This code should work either way)
         
        ServerSocketConnection ssc;
        ssc = (ServerSocketConnection)Connector.open("socket://8080");
        while (true) {
            SocketConnection sc = (SocketConnection)ssc.acceptAndOpen();
            //process and verify socket input
        }
        */

        TransportManager tm = new TransportManager(new RmsStorage());
        ITransportDestination d = new HttpTransportDestination(TEST_SERVER_URL);
        
        String data = new String(TEST_DATA);
        ByteArrayPayload bap = new ByteArrayPayload(TEST_DATA.getBytes(),null,0);
        TransportMessage message = new TransportMessage(bap, d, "HttptransportMethodTest", 2);
        
        /* Use the following code to transmit a whole file.
         * Note that to run this in an emulator, you need to place the bmp in the appropriate storage root
         * (This information is printed to the console when launching the emulator)
        IDataPointer p = new FileDataPointer("file:///root1/547mb.bmp");
        DataPointerPayload dpp = new DataPointerPayload(p);
        TransportMessage message = new TransportMessage(dpp, d, "HttptransportMethodTest", 2);
        */
        
        (new ReliableHttpTransportMethod()).transmit(message, tm); 
    }    
}
