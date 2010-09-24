package org.javarosa.demo.debug;

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


// Define the logging levels we need. Adapted from
// the java.util.logging.Level class.
// See http://developers.sun.com/techtopics/mobility/midp/ttips/logging/

public class Level {

    public static final Level FINE = new Level( 1, "FINE" );
    public static final Level INFO = new Level( 2, "INFO" );
    public static final Level WARNING = new Level( 3, "WARNING" );
    public static final Level SEVERE = new Level( 4, "SEVERE" );
    public static final Level OFF = new Level( 100, "OFF" );

    private Level( int level, String name ){
        _level = level;
	_name = "[" + name + "]";
    }

    public int intValue(){
        return _level;
    }

    public String toString(){
	return _name;
    }

    private int _level;
    private String _name;
}
