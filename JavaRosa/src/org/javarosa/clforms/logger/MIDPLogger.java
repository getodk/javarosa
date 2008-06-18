/****************************************************************************
 * MIDPLogger.java
 *
 * Copyright (C) Martin Woolley 2001
 *
 *
 * MIDPLogger is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MIDPLogger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MIDPLogger; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @author martin.woolley@woolleynet.com
 * http://www.woolleynet.com
 * http://mobilelandscape.co.uk 
 *
 ****************************************************************************/

package org.javarosa.clforms.logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Date;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class MIDPLogger
{
   // LOG_LEVELs 0=debug,1=information,2=warning,3=error, 4=express
   public static final byte  DEBUG   = 0;
   public static final byte  INFO    = 1;
   public static final byte  WARNING = 2;
   public static final byte  ERROR   = 3;  
   public static final byte  EXPRESS = 4; 
   public static final byte  NONE    = 5;
   
   protected static final String [] levels = {"DEBUG  ","INFO   ","WARNING","ERROR  ","EXPRESS", "NONE"};
   
   int log_level;
   boolean log_to_console;
   boolean include_level_name = true;
   
   private RecordStore db;
   private static int recno;   
      
   public MIDPLogger(int this_log_level, 
                     boolean this_log_to_console,
                     boolean this_include_level_name) throws RecordStoreException
   {
       log_level = this_log_level;
       log_to_console = this_log_to_console;
       include_level_name = this_include_level_name;
       try {
         db = RecordStore.openRecordStore("MIDPLog", false);
       }
       catch (RecordStoreNotFoundException e) {
              db = RecordStore.openRecordStore("MIDPLog" , true);
       }
     
   }
   
   public void includeLevelName(boolean active)
   {
     include_level_name = active;
   } 

   public void write(String entry, int level)
   {
     Date now;
     StringBuffer log_message = new StringBuffer();
     if (!(log_level > level)) {
        if (include_level_name) {
            log_message.append(levels[level]);
            log_message.append(" ");
        }
                
        log_message.append(entry);
        addRecord(new String(log_message));
        if (log_to_console)
          System.out.println(log_message);
     }
        
   }
    
   private void addRecord(String record)
   {
     try
     {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       DataOutputStream dos = new DataOutputStream(baos);
       dos.writeUTF(record);
       byte[] b = baos.toByteArray();
       recno = db.addRecord(b, 0, b.length);       
     }
     catch (Exception e)
     {
       System.out.println("Exception whilst adding record");
       e.printStackTrace();
     }
   }
  
   public void close()
   {
     try {
       db.closeRecordStore();
     }
     catch (Exception e) {};
   }
   
}