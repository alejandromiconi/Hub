package hub;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import hub.commons.Log;

public class Service implements Runnable {

	private final Socket m_socket;
	private final int m_num;

	Service(Socket socket, int num) {
		
		m_socket = socket;
		m_num = num;

		Thread handler = new Thread(this, "handler-" + m_num);
		handler.start();
	}

	void print(String message) {
		
		try {

			out.write(message + "\n\r");
			out.flush();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	OutputStreamWriter out;
	
	public void run() {

		try {

			try {
				
				System.out.println(m_num + " Connected.");
				
				BufferedReader in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
				
				out = new OutputStreamWriter(m_socket.getOutputStream());
				
				print("Welcome hub #" + m_num);
	
                while ( true )
                {

					String line = in.readLine();
		
					switch (line) {
		
					case "dir": 
						
						File file = new File(".");
						print("Current working directory : " + file.getAbsolutePath());
						break;
					
					case "exit":
		
						System.out.println(m_num + " Closing Connection.");
						return;
		
					case "quit":
						
                    	System.out.println( m_num + " Quit Service..." );
                        Runtime.getRuntime().halt(0);
                        break;
               
					default:
		
						new Hub(line);
						
						print(Log.getLog("\n\r"));
						
						break;
		
					}
                }		
			}

			finally {
				
				m_socket.close();

			}
		}

		catch (IOException e) {
			System.out.println(m_num + " Error: " + e.toString());
		}

	}

	public static void main(String[] args) throws Exception {
		
		int port = 9000;
		
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		
		System.out.println("Accepting connections on port: " + port);
		
		int nextNum = 1;
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(port);
		
		while (true) {
			Socket socket = serverSocket.accept();
			new Service(socket, nextNum++);
		}
	}
}
