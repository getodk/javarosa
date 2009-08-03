/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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

