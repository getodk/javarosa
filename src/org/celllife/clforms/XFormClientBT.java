/*
 * BTImageClient.java
 *
 * Created on 2007/10/24, 10:16:26
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.celllife.clforms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
// jsr082 API
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
// midp/cldc API
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import org.celllife.clforms.api.Form;
import org.celllife.clforms.xml.XMLUtil;


final class XFormClientBT implements Runnable, DiscoveryListener
{

    private static final UUID XFORM_SERVER_UUID = new UUID("F0E0D0C0B0A000908070605040302010", false);
    private static final int XFORMS_NAMES_ATTRIBUTE_ID = 0x4321;
    private static final int READY = 0;
    private static final int DEVICE_SEARCH = 1;
    private static final int SERVICE_SEARCH = 2;
    private int state = READY;
    private DiscoveryAgent discoveryAgent;
    private VisualXFormClient visualXFormClient;
    private boolean isClosed;
    private Thread runner;
    private Vector devices = new /* RemoteDevice */ Vector();
    private Vector records = new /* ServiceRecord */ Vector();
    private int discType;
    private int[] searchIDs;
    private String xformRefToLoad;
    private Hashtable base = new Hashtable();
    private boolean isDownloadCanceled;
    private UUID[] uuidSet;
    private int[] attrSet;

    public XFormClientBT(VisualXFormClient visualXFormClient)
    {
        this.visualXFormClient = visualXFormClient;

        runner = new Thread(this);
        runner.start();
    }

    public void run()
    {
        boolean isBTReady = false;

        try
        {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            discoveryAgent = localDevice.getDiscoveryAgent();

            isBTReady = true;
        }
        catch (Exception e)
        {
            System.out.println("Error when initialising bluetooth : " + e);
        }

        visualXFormClient.initialise(isBTReady);

        if (!isBTReady)
        {
            return;
        }

        uuidSet = new UUID[2];

        uuidSet[0] = new UUID(0x1101);

        uuidSet[1] = XFORM_SERVER_UUID;

        attrSet = new int[1];

        attrSet[0] = XFORMS_NAMES_ATTRIBUTE_ID;

        processXFormSearchDownload();
    }

    public void deviceDiscovered(RemoteDevice remoteBTDevice,
                                 DeviceClass deviceClass)
    {
        if (devices.indexOf(remoteBTDevice) == -1)
        {
            devices.addElement(remoteBTDevice);
        }
    }

    public void inquiryCompleted(int discType)
    {
        this.discType = discType;

        synchronized (this)
        {
            notify();
        }
    }

    public void servicesDiscovered(int transID,
                                   ServiceRecord[] servRecord)
    {
        for (int i = 0; i < servRecord.length; i++)
        {
            records.addElement(servRecord[i]);
        }
    }

    public void serviceSearchCompleted(int transID, int respCode)
    {
        int index = -1;

        for (int i = 0; i < searchIDs.length; i++)
        {
            if (searchIDs[i] == transID)
            {
                index = i;

                break;
            }
        }

        if (index == -1)
        {
            System.err.println("Unexpected transaction index: " + transID);
        }
        else
        {
            searchIDs[index] = -1;
        }

        for (int i = 0; i < searchIDs.length; i++)
        {
            if (searchIDs[i] != -1)
            {
                return;
            }
        }

        synchronized (this)
        {
            notify();
        }
    }

    void requestSearch()
    {
        synchronized (this)
        {
            notify();
        }
    }

    void cancelSearch()
    {
        synchronized (this)
        {
            if (state == DEVICE_SEARCH)
            {
                discoveryAgent.cancelInquiry(this);
            }
            else if (state == SERVICE_SEARCH)
            {
                for (int i = 0; i < searchIDs.length; i++)
                {
                    discoveryAgent.cancelServiceSearch(searchIDs[i]);
                }
            }
        }
    }

    void requestLoad(String name)
    {
        synchronized (this)
        {
            xformRefToLoad = name;
            notify();
        }
    }

    void cancelLoad()
    {
        isDownloadCanceled = true;
    }

    void destroy()
    {
        synchronized (this)
        {
            isClosed = true;
            isDownloadCanceled = true;
            notify();
        }

        try
        {
            runner.join();
        }
        catch (InterruptedException e)
        {
        } // ignore
    }

    private synchronized void processXFormSearchDownload()
    {
        while (!isClosed)
        {
            state = READY;

            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                System.err.println("Unexpected interruption: " + e);

                return;
            }

            if (isClosed)
            {
                return;
            }

            if (!searchDevices())
            {
                return;
            }
            else if (devices.size() == 0)
            {
                continue;
            }

            // search for services now
            if (!searchServices())
            {
                return;
            }
            else if (records.size() == 0)
            {
                continue;
            }
            if (!presentUserSearchResults())
            {
                continue;
            }

            while (true)
            {
                isDownloadCanceled = false;

                try
                {
                    wait();
                }
                catch (InterruptedException e)
                {
                    System.err.println("Unexpected interruption: " + e);

                    return;
                }

                if (isClosed)
                {
                    return;
                }

                if (xformRefToLoad == null)
                {
                    break;
                }

                Form form = this.loadForm();
                if (isClosed)
                {
                    return;
                }

                if (isDownloadCanceled)
                {
                    continue;
                }


                visualXFormClient.writeDownloadedXFormToRMS(form);

                continue;
            }
        }
    }

    private boolean searchDevices()
    {
        state = DEVICE_SEARCH;
        devices.removeAllElements();

        try
        {
            discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
        }
        catch (BluetoothStateException e)
        {
            System.err.println("Can't start inquiry now: " + e);
            visualXFormClient.showSearchError("Can't start device search");

            return true;
        }

        try
        {
            wait(); // until devices are found
        }
        catch (InterruptedException e)
        {
            System.err.println("Unexpected interruption: " + e);

            return false;
        }

        if (isClosed)
        {
            return false;
        }

        switch (discType)
        {
            case INQUIRY_ERROR:
                visualXFormClient.showSearchError("Device discovering error...");
            case INQUIRY_TERMINATED:
                devices.removeAllElements();
                break;
            case INQUIRY_COMPLETED:

                if (devices.size() == 0)
                {
                    visualXFormClient.showSearchError("No devices in range");
                }
                break;
            default:
                System.err.println("system error:" + " unexpected device discovery code: " + discType);
                destroy();

                return false;
        }

        return true;
    }

    private boolean searchServices()
    {
        state = SERVICE_SEARCH;
        records.removeAllElements();
        searchIDs = new int[devices.size()];

        boolean isSearchStarted = false;

        for (int i = 0; i < devices.size(); i++)
        {
            RemoteDevice rd = (RemoteDevice) devices.elementAt(i);

            try
            {
                searchIDs[i] = discoveryAgent.searchServices(attrSet, uuidSet, rd, this);
            }
            catch (BluetoothStateException e)
            {
                System.err.println("Can't search services for: " + rd.getBluetoothAddress() + " due to " + e);
                searchIDs[i] = -1;

                continue;
            }

            isSearchStarted = true;
        }

        if (!isSearchStarted)
        {
            visualXFormClient.showSearchError("Can't search services.");

            return true;
        }

        try
        {
            wait(); // until services are found
        }
        catch (InterruptedException e)
        {
            System.err.println("Unexpected interruption: " + e);

            return false;
        }

        if (isClosed)
        {
            return false;
        }

        if (records.size() == 0)
        {
            visualXFormClient.showSearchError("No proper services were found");
        }

        return true;
    }

    private boolean presentUserSearchResults()
    {
        base.clear();

        for (int i = 0; i < records.size(); i++)
        {
            ServiceRecord sr = (ServiceRecord) records.elementAt(i);

            DataElement de = sr.getAttributeValue(XFORMS_NAMES_ATTRIBUTE_ID);

            if (de == null)
            {
                System.err.println("Unexpected service - missed attribute");

                continue;
            }

            // get the images names from this attribute
            Enumeration deEnum = (Enumeration) de.getValue();

            while (deEnum.hasMoreElements())
            {
                de = (DataElement) deEnum.nextElement();

                String name = (String) de.getValue();

                // name may be stored already
                Object obj = base.get(name);

                // that's either the ServiceRecord or Vector
                if (obj != null)
                {
                    Vector v;

                    if (obj instanceof ServiceRecord)
                    {
                        v = new Vector();
                        v.addElement(obj);
                    }
                    else
                    {
                        v = (Vector) obj;
                    }

                    v.addElement(sr);
                    obj = v;
                }
                else
                {
                    obj = sr;
                }

                base.put(name, obj);
            }
        }

        return visualXFormClient.showXFormNames(base);
    }

    private Form loadForm()
    {
        if (xformRefToLoad == null)
        {
            System.err.println("Error: xformRefToLoad=null");

            return null;
        }

        ServiceRecord[] sr = null;
        Object obj = base.get(xformRefToLoad);

        if (obj == null)
        {
            System.err.println("Error: no record for: " + xformRefToLoad);

            return null;
        }
        else if (obj instanceof ServiceRecord)
        {
            sr = new ServiceRecord[]{(ServiceRecord) obj};
        }
        else
        {
            Vector v = (Vector) obj;
            sr = new ServiceRecord[v.size()];

            for (int i = 0; i < v.size(); i++)
            {
                sr[i] = (ServiceRecord) v.elementAt(i);
            }
        }

        for (int i = 0; i < sr.length; i++)
        {
            StreamConnection conn = null;
            String url = null;

            if (isDownloadCanceled)
            {
                return null;
            }

            try
            {
                url = sr[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                conn = (StreamConnection) Connector.open(url);
            }
            catch (Exception e)
            {
                System.err.println("Note: can't connect to: " + e + " : "+ url);

                // ignore
                continue;
            }

            try
            {
                OutputStream out = conn.openOutputStream();
                out.write(xformRefToLoad.length()); // length is 1 byte
                out.write(xformRefToLoad.getBytes());
                out.flush();
                out.close();
            }
            catch (Exception e)
            {
                System.err.println("Can't write to server for: " + e + ":" + url);

                try
                {
                    conn.close();
                }
                catch (IOException ee)
                {
                } // ignore
                continue;
            }

            // then open a steam and read an image
            byte[] xformData = null;

            try
            {
                InputStream in = conn.openInputStream();

                DataInputStream is = new DataInputStream(in);
                int length = is.readInt();
                //int length = in.read();
                System.out.println("Length : " + length);
                xformData = new byte[length];
                xformData = is.readUTF().getBytes();
                String xf = new String(xformData);
                System.out.println("XFORM DATA " + xf);
                in.close();
            }
            catch (Exception e)
            {
                System.err.println("Can't read from server for(2) : " + e + ":" + url);

                continue;
            }
            finally
            {
                // close stream connection anyway
                try
                {
                    conn.close();
                }
                catch (IOException e)
                {
                } // ignore
            }

            Form form = new Form();
            try
            {
                
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(xformData));
                InputStreamReader isr = new InputStreamReader(dis);
                XMLUtil.parseForm(isr, form);
                //Serializer.deserialize(xformData, form);
                String str = new String(xformData);
                System.out.println("FORMS : \n" + str);
            }
            catch (Exception e)
            {
                System.out.println("Exception : " + e.toString());
            }
            System.out.println("Returned FORM.");
            return form;
        }

        return null;
    }
} 