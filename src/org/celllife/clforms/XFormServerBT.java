/*
 * BTImageServer.java
 *
 * Created on 2007/10/24, 10:16:36
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.celllife.clforms;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.ServiceRegistrationException;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import org.celllife.clforms.api.Form;
import org.celllife.clforms.storage.DummyForm;
import org.celllife.clforms.storage.Serializer;
//import org.celllife.clforms.storage.WritableString;
//import org.celllife.clforms.storage.WritableString;
import org.celllife.clforms.storage.XFormMetaData;
import org.celllife.clforms.storage.XFormRMSUtility;


public class XFormServerBT implements Runnable
{

    private static final UUID XFORM_SERVER_UUID = new UUID("F0E0D0C0B0A000908070605040302010", false);
    private static final int XFORM_ATTRIBUTE_ID = 0x4321;
    private LocalDevice localDevice;
    private StreamConnectionNotifier notifier;
    private ServiceRecord record;
    private VisualXFormServer visualXFormServer;
    private boolean isClosed;
    private Thread runner;
    private ProcessClientMessaging processor;
    private final Hashtable dataElements = new Hashtable();
    private XFormRMSUtility xformRMSUtility;

    public XFormServerBT(VisualXFormServer visualXFormServer)
    {
        this.visualXFormServer = visualXFormServer;
        this.xformRMSUtility = visualXFormServer.getXFormRMSUtility();

        runner = new Thread(this);
        runner.start();
    }

    public void run()
    {
        boolean isReady = false;

        try
        {
            localDevice = LocalDevice.getLocalDevice();
            if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC))
            {
            }

            StringBuffer url = new StringBuffer("btspp://");

            url.append("localhost").append(':');

            url.append(XFORM_SERVER_UUID.toString());

            url.append(";name=Picture Server");

            url.append(";authorize=false");

            notifier = (StreamConnectionNotifier) Connector.open(url.toString());

            record = localDevice.getRecord(notifier);

            DataElement base = new DataElement(DataElement.DATSEQ);
            record.setAttributeValue(XFORM_ATTRIBUTE_ID, base);


            isReady = true;
        }
        catch (Exception e)
        {
            System.err.println("Error initialising bluetooth: " + e);
        }

        visualXFormServer.initialise(isReady);


        if (!isReady)
        {
            return;
        }

        processor = new ProcessClientMessaging();

        while (!isClosed)
        {
            StreamConnection conn = null;

            try
            {
                conn = notifier.acceptAndOpen();
            }
            catch (IOException e)
            {
                continue;
            }

            processor.addConnection(conn);
        }
    }

    boolean changeXFormInfo(String name, boolean publish)
    {
    	//LOG
    	System.out.println("PUBLISHED: "+publish);
        DataElement base = record.getAttributeValue(XFORM_ATTRIBUTE_ID);

        DataElement de = (DataElement) dataElements.get(name);

        if (de == null)
        {
            de = new DataElement(DataElement.STRING, name);
            dataElements.put(name, de);
        }

        if (publish)
        {
            base.addElement(de);
        }
        else
        {
            if (!base.removeElement(de))
            {
                System.err.println("Error: item was not removed for: " + name);

                return false;
            }
        }

        record.setAttributeValue(XFORM_ATTRIBUTE_ID, base);

        try
        {
            localDevice.updateRecord(record);
        }
        catch (ServiceRegistrationException e)
        {
            System.err.println("Can't update record now for: " + name);

            return false;
        }

        return true;
    }

    void destroy()
    {
        isClosed = true;

        if (notifier != null)
        {
            try
            {
                notifier.close();
            }
            catch (IOException e)
            {
            } // ignore
        }

        try
        {
            runner.join();
        }
        catch (InterruptedException e)
        {
        } // ignore
        if (processor != null)
        {
            processor.destroy(true);
        }

        processor = null;
    }

    private void processConnection(StreamConnection conn)
    {
        String xformRef = readXFormName(conn);

        int recordId = visualXFormServer.getXFormReference(xformRef);

        byte[] xformData = getXFormData(recordId);

        sendXFormData(xformData, conn);

        try
        {
            conn.close();
        }
        catch (IOException e)
        {
        } // ignore
    }

    private void sendXFormData(byte[] xformData,
                               StreamConnection conn)
    {
        if (xformData == null)
        {
            return;
        }

        OutputStream out = null;

        try
        {
            out = conn.openOutputStream();
            DataOutputStream os = new DataOutputStream(out);
            os.writeInt(xformData.length);
            os.writeUTF(new String(xformData));
            out.flush();
        }
        catch (IOException e)
        {
            System.err.println("Cant send XForm data: " + e);
        }

        // close output stream anyway
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
            } // ignore
        }
    }

    private String readXFormName(StreamConnection conn)
    {
        String xformRef = null;
        InputStream in = null;

        try
        {
            in = conn.openInputStream();

            int length = in.read(); // 'name' length is 1 byte
            if (length <= 0)
            {
                throw new IOException("Can't read name length");
            }

            byte[] nameData = new byte[length];
            length = 0;

            while (length != nameData.length)
            {
                int n = in.read(nameData, length, nameData.length - length);

                if (n == -1)
                {
                    throw new IOException("Can't read name data");
                }

                length += n;
            }

            xformRef = new String(nameData);
        }
        catch (IOException e)
        {
            System.err.println(e);
        }

        // close input stream anyway
        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
            } // ignore
        }

        return xformRef;
    }

    private byte[] getXFormData(int recordId)
    {
        byte[] data = "FAILED".getBytes();///DummyForm.testXForm;
        
        ////
        
        try
        {
       //     return this.xformRMSUtility.retrieveByteDataFromRMS(recordId);
            //   WritableString ws = new WritableString();
   //         XFormMetaData md = new XFormMetaData();
  //          this.xformRMSUtility.retrieveMetaDataFromRMS(recordId, md);
  //          ws.setSizeOfData(md.getSize());
  //          this.xformRMSUtility.retrieveFromRMS(recordId, ws);
            
 //           System.out.println("SIZE : " + md.getSize() + " :::" + ws.getString());// " + ws.getString());
            return this.xformRMSUtility.retrieveByteDataFromRMS(recordId);//ws.getString().getBytes();
            //this.visualXFormServer.getMainShell().controllerLoadForm(form);
            //data = ws.getString().getBytes(); //Serializer.serialize(form);
           // System.out.println("Serialised data is : " + ws.getString());
        }
        catch (Exception e)
        {
            System.out.println("Error in getting XForm data :" + e.toString());
        }
        return data;
    }

    private class ProcessClientMessaging implements Runnable
    {

        private Thread runner;
        private Vector queue = new Vector();
        private boolean isOk = true;

        ProcessClientMessaging()
        {
            runner = new Thread(this);
            runner.start();
        }

        public void run()
        {
            while (!isClosed)
            {
                // wait for new task to be processed
                synchronized (this)
                {
                    if (queue.size() == 0)
                    {
                        try
                        {
                            wait();
                        }
                        catch (InterruptedException e)
                        {
                            System.err.println("Unexpected exception: " + e);
                            destroy(false);

                            return;
                        }
                    }
                }

                // send the image to specified connection
                StreamConnection conn;

                synchronized (this)
                {
                    // may be awaked by "destroy" method.
                    if (isClosed)
                    {
                        return;
                    }

                    conn = (StreamConnection) queue.firstElement();
                    queue.removeElementAt(0);
                    processConnection(conn);
                }
            }
        }

        /** Adds the connection to queue and notifies the thread. */
        void addConnection(StreamConnection conn)
        {
            synchronized (this)
            {
                queue.addElement(conn);
                notify();
            }
        }

        /** Closes the connections and . */
        void destroy(boolean needJoin)
        {
            StreamConnection conn;

            synchronized (this)
            {
                notify();

                while (queue.size() != 0)
                {
                    conn = (StreamConnection) queue.firstElement();
                    queue.removeElementAt(0);

                    try
                    {
                        conn.close();
                    }
                    catch (IOException e)
                    {
                    } // ignore
                }
            }

            // wait until dispatching thread is done
            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
            } // ignore
        }
    }
} // end of class 'BTImageServer' definition