/**
 * 
 */
package org.javarosa.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of a Buffered Stream for j2me compatible libraries.
 * 
 * Very basic, no mark support (Pretty much only for web related streams
 * anyway).
 * 
 * @author ctsims
 *
 */
public class BufferedInputStream extends InputStream {
	
	//TODO: Better close semantics
	//TODO: Threadsafety
	
	private InputStream in;
	private byte[] buffer;
	
	private int position;
	private int count;
	
	public BufferedInputStream(InputStream in) {
		this(in, 2048);
	}
	
	public BufferedInputStream(InputStream in, int size) {
		this.in = in;
		this.buffer = new byte[size];
		cleanBuffer();
	}
	
	private void cleanBuffer() {
		this.position = 0;
		this.count = 0; 
	}
	

	/* (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		if(count == -1) { return 0; }
		//Size of our stream + the number of bytes we haven't yet read.
		return in.available() + (count - position);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		in.close();
		//clear up buffer
		buffer = null;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#mark(int)
	 */
	public void mark(int arg0) {
		//nothing
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		//If we've reached EOF, signal that.
		if(count == -1) { 
			return -1;
		}
		
		if(len == 0) {
			return 0;
		}
		
		if(off == -1 || len == -1 || off+len > b.length) {
			throw new IndexOutOfBoundsException("Bad inputs to input stream read");
		}
		
		int counter = 0;
		boolean quitEarly = false;
		while(counter != len && !quitEarly) {
			//TODO: System.arraycopy here?
			for(; position < count && counter < len ; ++position) {
				b[off + counter] = buffer[position];
				counter++;
			}
			
			//we read in as much as was requested;
			if(counter == len) {
				//don't need to do anything. We'll get bumped out of the loop
			} else if(position == count) {
				
				//If we didn't fill the buffer last time, we might be blocking on IO, so return
				//what we have and let the magic happen
				if(quitEarly) {
					continue;
				}
				
				//otherwise, try to fill that buffer 
				if(!fillBuffer()) {
					//Ok, so we didn't fill the whole thing. Either we're at the end of our stream (possible)
					//or there was an incomplete read.
					
					//EOF
					if(count == -1) {
						//We're at EOF. Two possible conditions here.
						
						//1) This was actually our first attempt on the end of stream. signal EOF 
						if(counter == 0) { return -1; }
						
						//2) This was the last pile of bits. Return the ones we read.
						else {
							return counter;
						}
					}
					
					//Incomplete read. Get the bits back. Hopefully the stream won't be blocked next time they try to read.  
					quitEarly = true;
				}
			}
		}
		return counter;
	}
	
	private boolean fillBuffer() throws IOException {
		if(count == -1) {
			//do nothing
			return false;
		}
		position = 0;
		count = in.read(buffer);
		return count == buffer.length;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#reset()
	 */
	public void reset() throws IOException {
		//mark is unsupported
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long len) throws IOException {
		//TODO: Something smarter here?
		long skipped= in.skip(len);
		if(skipped > count - position) {
			//need to reset our buffer positions, this buffer
			//is now expired.
			cleanBuffer();
		} else {
			//we skipped some number of bytes that just pushes us further
			//into the existing buffer
			
			//this has to be an integer-bound size, because it's smaller than
			//count - position, which is an integer size
			int bytesSkipped = (int)skipped;
			position += bytesSkipped;
		}
		return skipped;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		//If we've read all of the available buffer, fill
		//'er up.
		if(position == count) {
			//This has to return at _least_ 1 byte, unless
			//the stream has ended
			fillBuffer();
		}
		
		//either this was true when we got here, or it's true
		//now. Either way, signal EOF
		if(count == -1) { 
			return -1; 
		}
		
		//Otherwise, bump and return
		return buffer[position++] & 0xFF;
	}
}
