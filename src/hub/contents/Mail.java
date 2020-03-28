package hub.contents;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.w3c.dom.Element;

import hub.Hub;
import hub.commons.Log;

public class Mail extends Concentra {

	public static final String sendErrors = "errors";
	//public static final String sendFile = "file";
	public static final String sendLog = "log";

	String sendemail, host, port, from, password, to;
	
	public Mail(Element e) {

		super(e);
		
		this.sendemail = e.getAttribute("sendemail").toLowerCase();
		this.host = e.getAttribute("host");
		this.port = e.getAttribute("port");
		this.from = e.getAttribute("from");
		this.password = e.getAttribute("password");
		this.to = e.getAttribute("to");
	}

	public boolean process() {

		if (sendemail.isEmpty()) return true;
		
		if (sendemail.equals(sendErrors)) {
			if (!Log.hasErrors()) 
				return true;
		}
		
		// Get system properties
		Properties props = System.getProperties();

		// Setup mail server
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port); //TLS Port
		props.put("mail.smtp.auth", "true"); //enable authentication
		//props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
		//props.put("mail.debug", "true");
		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		};
		
		// Get the default Session object.
		Session session = Session.getDefaultInstance(props, auth);

		try {
			
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			String [] tos = to.split(";");
			for( String to : tos ) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
			}
			
			// Set Subject: header field
			message.setSubject(Hub.App + " Lot " + this.props.lot);

			
			String body = Log.getLog();

			// Generamos la salida para enviar 
			if (!this.props.contentId.isEmpty()) {
				
				String filename = "file_" + this.props.lot + "_" + this.props.contentId + ".csv";
				
				this.getCurrent(false);
				this.getConsolidations();
				
				this.saveTextFile(filename);
				
				Multipart mp = new MimeMultipart();

				// Now set the actual message
	            BodyPart pt = new MimeBodyPart();
	            pt.setText(body);
	            mp.addBodyPart(pt);
	            
	            // Now set the file!
				BodyPart pf = new MimeBodyPart();
				DataSource f = new FileDataSource(filename);
				pf.setDataHandler(new DataHandler(f));
				pf.setFileName(filename);
				mp.addBodyPart(pf);
				
				message.setContent(mp);
			}

			else {

				// Now set the actual message
				message.setText(body);

			}

			// Send message
			Transport.send(message);
			
			this.logMessage("Sent message successfully to " + to);

		} catch (MessagingException mex) {
			
			mex.printStackTrace();

			this.logErrorMessage("Message not sent to " + to + " " + mex.getMessage());
		}
		
		
		return true;
	}
}
