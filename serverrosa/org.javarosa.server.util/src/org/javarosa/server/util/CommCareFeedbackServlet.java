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
		
		out.print("Body Read: " + body.toString());

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
		
		  PasswordAuthentication auth = new PasswordAuthentication("commcare@dimagi.com","commcare4dimagi");
		  
		    Properties props = new Properties();
		    props.put("mail.smtp.auth", "true");
		    props.put("mail.smtp.host", "smtp.gmail.com");
		    props.put("mail.smtp.port", "465");
		    props.put("mail.smtp.ssl.protocols", "tls");
		    Session session = Session.getInstance(props, null);
		    session.setPasswordAuthentication(new URLName("http://smtp.google.com:465"), auth);

		    try {
		        MimeMessage msg = new MimeMessage(session);
		        msg.setFrom(new InternetAddress("no-reply: CommCare Automated Feedback Gateway <blackhole@dimagi.com>"));
		        msg.setRecipients(Message.RecipientType.TO,
		                          "commcare@dimagi.com");
		        msg.setSubject("Subject: CommCare Automated Feedback");
		        msg.setSentDate(new Date());
		        msg.setText(body.toString());
		        Transport.send(msg);
		    } catch (MessagingException mex) {
		        System.out.println("send failed, exception: " + mex);
		        out.println("Error! " + mex);
		    }

	      out.println("Thanks for the submission...");
	      
	}
}
