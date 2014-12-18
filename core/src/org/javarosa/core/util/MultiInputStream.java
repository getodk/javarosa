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

/**
 *
 */
package org.javarosa.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * MultiInputStream allows for concatenating multiple
 * input streams together to be read serially in the
 * order that they were added.
 *
 * A MultiInputStream must have all of its component
 * streams added to it before it can be read from. Once
 * the stream is ready, it should be prepare()d before
 * the first read.
 *
 * @author Clayton Sims
 * @date Dec 18, 2008
 *
 */
public class MultiInputStream extends InputStream {

	/** InputStream **/
   List<InputStream> streams = new ArrayList<InputStream>(1);

	int currentStream = -1;

	public void addStream(InputStream stream) {
		streams.add(stream);
	}

	/**
	 * Finalize the stream and allow it to be read
	 * from.
	 *
	 * @return True if the stream is ready to be read
	 * from. False if the stream couldn't be prepared
	 * because it was empty.
	 */
	public boolean prepare() {
		if(streams.size() == 0) {
			return false;
		}
		else {
			currentStream = 0;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if(currentStream == -1) {
			throw new IOException("Cannot read from unprepared MultiInputStream!");
		}
		InputStream cur = streams.get(currentStream);
		int next = cur.read();

		if(next != -1 ) {
			return next;
		}

		//Otherwise, end of Stream

		//Loop through the available streams until we read something that isn't
		//an end of stream
		while(next == -1 && currentStream + 1 < streams.size()) {
			currentStream++;
			cur = ((InputStream)streams.get(currentStream));
			next = cur.read();
		}

		//Will be either a valid value or -1 if we've run out of streams.
		return next;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		if(currentStream == -1) {
			throw new IOException("Cannot read from unprepared MultiInputStream!");
		}
		return streams.get(currentStream).available();
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		if(currentStream == -1) {
			throw new IOException("Cannot read from unprepared MultiInputStream!");
		}
      for (InputStream stream : streams) {
         stream.close();
      }
	}

}
