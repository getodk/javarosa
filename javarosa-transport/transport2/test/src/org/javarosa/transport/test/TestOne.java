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

package org.javarosa.transport.test;

import java.io.IOException;

import org.javarosa.services.transport.TransportService;
import org.javarosa.services.transport.impl.http.SimpleHttpTransportMessage;

public class TestOne {
	public static final String TEST_SERVER_URL = "http://localhost:90";
	public static final String TEST_DATA = "testdata";

	private TransportService svc = new TransportService();

	// public TestOne(int arg0, String arg1) {
	// super(arg0, arg1);
	//		 
	// }

	public void test(int i) {
		String url = "http://www.google.co.tz/search?hl=en&q=";
		SimpleHttpTransportMessage message = new SimpleHttpTransportMessage(
				"Hellow World", url);

		try {
			svc.send(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new TestOne().test(1);
	}
}
