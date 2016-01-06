/**
 * 
 */
package org.javarosa.core.services.locale;

import org.javarosa.core.util.OrderedMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author ctsims
 *
 */
public class LocalizationUtils {
	/**
	 * @param is A path to a resource file provided in the current environment
	 *
	 * @return a dictionary of key/value locale pairs from a file in the resource directory 
	 * @throws IOException 
	 */
	public static OrderedMap<String, String> parseLocaleInput(InputStream is) throws IOException {
			// TODO: This might very well fail. Best way to handle?
			OrderedMap<String, String> locale = new OrderedMap<String, String>();
			int chunk = 100;
			InputStreamReader isr;
			isr = new InputStreamReader(is,"UTF-8");
			boolean done = false;
			char[] cbuf = new char[chunk];
			int offset = 0;
			int curline = 0;

				String line = "";
				while (!done) {
					int read = isr.read(cbuf, offset, chunk - offset);
					if(read == -1) {
						done = true;
						if(line.length() != 0) {
							parseAndAdd(locale, line, curline);
						}
						break;
					}
					String stringchunk = String.valueOf(cbuf,offset,read);
					
					int index = 0;
					
					while(index != -1) {
						int nindex = stringchunk.indexOf('\n',index);
						//UTF-8 often doesn't encode with newline, but with CR, so if we 
						//didn't find one, we'll try that
						if(nindex == -1) { nindex = stringchunk.indexOf('\r',index); }
						if(nindex == -1) {
							line += stringchunk.substring(index);
							break;
						}
						else {
							line += stringchunk.substring(index,nindex);
							//Newline. process our string and start the next one.
							curline++;
							parseAndAdd(locale, line, curline);
							line = "";
						}
						index = nindex + 1;
					}
				}
				is.close();
			return locale;
		}

		private static void parseAndAdd(OrderedMap<String, String> locale, String line, int curline) {

			//trim whitespace.
			line = line.trim();
			
			//clear comments
			while(line.indexOf("#") != -1) {
				line = line.substring(0, line.indexOf("#"));
			}
			if(line.indexOf('=') == -1) {
				// TODO: Invalid line. Empty lines are fine, especially with comments,
				// but it might be hard to get all of those.
				if(line.trim().equals("")) {
					//Empty Line
				} else {
					System.out.println("Invalid line (#" + curline + ") read: " + line);
				}
			} else {
				//Check to see if there's anything after the '=' first. Otherwise there
				//might be some big problems.
				if(line.indexOf('=') != line.length()-1) {
					String value = line.substring(line.indexOf('=') + 1,line.length());
					locale.put(line.substring(0, line.indexOf('=')), value);
				}
				 else {
					System.out.println("Invalid line (#" + curline + ") read: '" + line + "'. No value follows the '='.");
				}
			}
		}
}
