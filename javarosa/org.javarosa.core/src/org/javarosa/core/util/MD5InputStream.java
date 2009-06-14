package org.javarosa.core.util;

import java.io.IOException;
import java.io.InputStream;

public class MD5InputStream {
    InputStream in;
    
    public MD5InputStream( InputStream i ){
        in = i;
    }
    
    public final String getHashCode ()
        throws IOException {

        MD5 md5 = new MD5(null);
        byte[] bytes = new byte[8192];
        
        int bytesRead = 0;
        while( (bytesRead = in.read(bytes)) != -1){
            md5.update(bytes,0,bytesRead);
        }
        return MD5.toHex( md5.doFinal() );
    }
}

