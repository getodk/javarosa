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

package org.javarosa.services.transport.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.StreamsUtil;
import org.javarosa.services.transport.TransportResult;

public class HttpTransport {

	private HttpConnectionROSA conn;
	private String url;

	public HttpTransport(String url) {
		this.url = url;
	}

	public TransportResult send(String s) {
		return send(s.getBytes());
	}

	private TransportResult send(byte[] payload) {

		HttpTransportResult result = new HttpTransportResult();
		result.setPayload(payload);

		try {
			this.conn = new HttpConnectionROSA(this.url);

			InputStream in = new ByteArrayInputStream(payload);
			StreamsUtil.writeFromInputToOutput(in, this.conn.getOut());

			int responseCode = this.conn.getConnection().getResponseCode();
			if (responseCode == HttpConnection.HTTP_OK) {

				byte[] response = this.conn.readResponse();
				System.out.println(response);

				result.setSuccess(true);
			}else{
				result.setResponseCode(responseCode);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			result.setSuccess(false);

		} finally {
			// cleanStreams();
		}
		return result;
	}

}
