package hub.commons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class License {

	enum Expired { Not , FileNotExists , Corrupted , Exceeds }
	
	public static final String fileLicense = "hub.cfg";

	Expired expired = Expired.Not;
	
	long from, to;
	public int steps = 0;
	
	public boolean isExpired() {
		return expired != Expired.Not;
	}
	
	String buffer;
	
	public License() {


		File license = new File(fileLicense);

		if (!license.exists()) {
			System.out.println("Configuration undefined!");
			this.expired = Expired.FileNotExists;
			return;
		}
		
		try {

			
			// Read file!
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(fileLicense));
			buffer = reader.readLine();
			reader.close();
	
			// 0-2 : Posición to
			// 3-4 : Steps
			// 5   : From
			// to

			int pto = getint(0, 3);

			this.from = getlong(5, pto);
			this.to = getlong(pto , buffer.length());
			this.steps = getint(3, 5);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Date df = new Date(from);
		Date dt = new Date(to);
		Date lm = new Date(license.lastModified());
		
		Date now = new Date();
		if (!df.before(now)) this.expired = Expired.Exceeds;
		if (!dt.after(now)) this.expired = Expired.Exceeds;
		if (!df.equals(lm)) this.expired = Expired.Corrupted; 
		
	}

	public void save(int months, int steps) {

		//Date d = new Date(license.lastModified());
		//DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss");
	    //System.out.println(dateFormat.format(d));
		

		Calendar cal = Calendar.getInstance();
		this.from = cal.getTimeInMillis();
		String sfrom = String.valueOf(from);

		cal.add(Calendar.MONTH, months);
		this.to = cal.getTimeInMillis();
		String sto = String.valueOf(to);
		
		this.steps = steps;
		
		buffer = 
				String.format("%03d", 3 + 2 + sfrom.length())
				+ String.format("%02d", steps)
				+ sfrom + sto;
		
		try {
			
			FileWriter fw = new FileWriter(fileLicense);
			BufferedWriter writer = new BufferedWriter(fw);
			writer.write(buffer);
			writer.close();
			

			File license = new File(fileLicense);
			license.setLastModified(from);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void view() {

		System.out.println("License");
		System.out.println("From " + new Date(from));
		System.out.println("To " + new Date(to));
		System.out.println("Steps " + steps);
		
		String expired = "";
		switch(this.expired) {
		
		case Not:
			
			expired = "Not Expired";
			break;
			
		case Exceeds:
			
			expired = "Exceeds";
			break;
			
		case Corrupted:
			
			expired = "Corrupted";
			break;
			
		case FileNotExists:
			
			expired = "File not exists";
			break;
			
		default:
			
			expired = "Expired (" + this.expired + ")";
			break;
		}
		
		System.out.println(this.isExpired() ? expired : expired);
	}
	
	int getint(int from , int to) {

		int token;

		try {
			token = Integer.parseInt(buffer.substring(from, to));
		}
		
		catch(Exception e) {
			token = 0;
		}
		
		return token;
	}

	long getlong(int from , int to) {
		
		long token;
		
		try {
			token = Long.parseLong(buffer.substring(from, to));
		}
		
		catch(Exception e) {
			token = 0;
		}
		
		return token;
	}
}
