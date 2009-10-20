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

package org.javarosa.communication.sms.trigger;

import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IDaemon;

/**
 * 
 * @author Clayton Sims
 *
 */
public class SmsTriggerDaemon implements IDaemon {
	
	public static final String DAEMON_NAME = "SMS Trigger Daemon";
	
	boolean running = false;
	String port;
	SmsTriggerService service;
	
	public SmsTriggerDaemon() {
		String propPort = JavaRosaServiceProvider.instance().getPropertyManager().getSingularProperty(SmsTriggerProperties.TRIGGER_DEFAULT_PORT);
		if(propPort == null) {
			propPort = "16361"; 
		}
		init(propPort);
	}
	
	public SmsTriggerDaemon(String port) {
		init(port);
	}
	
	private void init(String port) {
		this.port = port;
		service = new SmsTriggerService();
	}

	public String getName() {
		return DAEMON_NAME;
	}

	public boolean isRunning() {
		return running;
	}

	public void restart() {
		stop();
		start();
	}

	public void start() {
		if(!running) {
			running = service.start(port);
		}
	}

	public void stop() {
		if(running) {
			running = service.stop();
		}
	}
	
	public void addTrigger(ISmsTrigger trigger) {
		service.addTrigger(trigger);
	}
}
