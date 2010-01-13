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

package org.javarosa.server.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * This function requires the existence of the directory
 * _storageRoot="/usr/share/tomcat5.5/webapps/jrtest/store/" to store temporary files.
 * It also creates proprietary data chunks labelled *.data
 * Do not put other files labelled *.data in _storageRoot
 */
public class CommCareFeedbackServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp
				.getOutputStream()
				.write(
						"Submit POST's to this URL in order to have them sent along to commcare@dimagi.com, the fogbugz inbox for CommCare bugs and feature requests"
								.getBytes());
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
	    PrintWriter out = resp.getWriter();

		// do something smart here
		// we don't care about headers

		int bufSize = 1400;
		byte[] temp = new byte[bufSize];
		InputStream stream = req.getInputStream();
		int bytesRead = stream.read(temp);
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		
		out.println("Body Read: " + body.toString());

		// FileWriter always assumes default encoding is OK!
		while (bytesRead != -1) {
			// If we didn't read in a full buffer.
			if (bytesRead < bufSize) {
				byte[] newTemp = new byte[bytesRead];
				for (int i = 0; i < bytesRead; i++) {
					newTemp[i] = temp[i];
				}
				body.write(newTemp);
			}
			// We did read in a full buffer.
			else {
				body.write(temp);
			}
			bytesRead = req.getInputStream().read(temp);
		}
		
		  //PasswordAuthentication auth = new PasswordAuthentication("commcare@dimagi.com","commcare4dimagi");
		  
		    Properties props = new Properties();
		    props.put("mail.smtp.from", "no-reply: CommCare Automated Feedback Gateway <blackhole@dimagi.com>");
		    props.put("mail.smtp.starttls.enable", "true");
		    //props.put("mail.smtp.auth", "true");
		    props.put("mail.smtp.host", "smtp.gmail.com");
		    props.put("mail.smtp.port", "465");
		    props.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
		    Session session = Session.getInstance(props, null);
		    //session.setPasswordAuthentication(new URLName("smtp.gmail.com"), auth);

		    try {
		        MimeMessage msg = new MimeMessage(session);
		        msg.setRecipients(Message.RecipientType.TO,
		                          "csims@dimagi.com");
		        msg.setSubject("Subject: CommCare Automated Feedback");
		        msg.setSentDate(new Date());
		        msg.setText(body.toString());
		        //Transport.send(msg);
		        Transport t = session.getTransport("smtp");
		        t.connect("commcare@dimagi.com","commcare4dimagi");
		        t.sendMessage(msg, msg.getAllRecipients());
		    } catch (MessagingException mex) {
		        System.out.println("send failed, exception: " + mex.getMessage());
		        out.println("Error! " + mex);
		    }

	      out.println("Thanks for the submission...");
	      
	}
}
