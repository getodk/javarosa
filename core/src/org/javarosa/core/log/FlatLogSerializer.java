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
package org.javarosa.core.log;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009
 *
 */
public class FlatLogSerializer implements IFullLogSerializer<String> {

	/* (non-Javadoc)
	 * @see org.javarosa.core.log.ILogSerializer#serializeLog(org.javarosa.core.log.IncidentLog)
	 */
	private String serializeLog(LogEntry log) {
		return "[" + log.getType() + "] " +log.getTime().toString() + ": " +  log.message+ "\n";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.log.ILogSerializer#serializeLogs(org.javarosa.core.log.IncidentLog[])
	 */
	public String serializeLogs(LogEntry[] logs) {
		StringBuilder log = new StringBuilder();
		for(int i = 0; i < logs.length; ++i ) {
			log.append(this.serializeLog(logs[i]));
		}
		return log.toString();
	}

}
