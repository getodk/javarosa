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

package org.javarosa.services.transport.impl.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import org.javarosa.services.transport.TransportResult;
import org.javarosa.services.transport.impl.StreamsUtil;

public class HttpTransport {

	private String url;

	/**
	 * 
	 * 
	 * @param url
	 */
	public HttpTransport(String url) {
		this.url = url;
	}

	/**
	 * 
	 * 
	 * 
	 * @param s
	 * @return
	 */
	public HttpTransportResult send(String s) {
		return send(s.getBytes());
	}

	/**
	 * 
	 * 
	 * 
	 * @param bytes
	 * @return
	 */
	private HttpTransportResult send(byte[] bytes) {
		HttpTransportResult result = new HttpTransportResult();

		try {
			HttpConnectionPOSTText conn = new HttpConnectionPOSTText(this.url);

			writeToConnection(conn, bytes);

			readResponse(conn, result);

			conn.close();
		} catch (ClassCastException e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.setFailureReason(e.getMessage());
			result.incrementFailureCount();

		} catch (IOException e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.setFailureReason(e.getMessage());
			result.incrementFailureCount();
		}

		return result;

	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * @param conn
	 * @param bytes
	 * @throws IOException
	 */
	private void writeToConnection(HttpConnectionPOSTText conn, byte[] bytes)
			throws IOException {
		OutputStream out = null;
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(bytes);
			out = conn.getConnection().openOutputStream();

			StreamsUtil.writeFromInputToOutput(in, out);
		} catch (IOException e) {
			throw e;

		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
			if (in != null) {
				in.close();
			}
		}

	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * @param conn
	 * @param result
	 * @return
	 * @throws IOException
	 * @throws ClassCastException
	 */
	private TransportResult readResponse(HttpConnectionPOSTText conn,
			HttpTransportResult result) throws IOException, ClassCastException {

		int responseCode = conn.getConnection().getResponseCode();
		if (responseCode == HttpConnection.HTTP_OK) {
			result.setSuccess(true);
			// TODO: what to do with the response body?
			readResponseBody(conn);
		} else {
			result.setResponseCode(responseCode);
		}

		return result;

	}

	/**
	 * 
	 * 
	 * 
	 * @param conn
	 * @return
	 * @throws IOException
	 */
	private String readResponseBody(HttpConnectionPOSTText conn)
			throws IOException {
		InputStream in = null;
		String r = null;
		try {
			in = conn.getConnection().openInputStream();
			int len = (int) conn.getConnection().getLength();
			byte[] response = StreamsUtil.readFromStream(in, len);
			r = new String(response);
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return r;
	}

}
