package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Alice {

	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Alice's private key
	 * args[2] ; Bob's public key
	 * args[3] ; port that Alice will connect to (Mallory's port)
	 * args[4] ; program configuration
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//check for correct # of args
		if (args.length != 5) {
			return;
		}
		System.out.println("This is Alice");
		
		int malPortNumber = Integer.parseInt(args[3]);
		String serverAddress = "localhost";
		try{
			System.out.println("Connecting to Mallory at ("+malPortNumber+", "+serverAddress +")...");
			Socket malSocket = new Socket(serverAddress, malPortNumber);
			System.out.println("Connected to Mallory");
			
			Scanner console = new Scanner(System.in);
			DataOutputStream streamOut = new DataOutputStream(malSocket.getOutputStream());
			
			String line = "";
			while(!line.equals("done")) {
				try
		         {  System.out.print("Type message: ");
					line = console.nextLine();
					
		            streamOut.writeUTF(line);
		            streamOut.flush();
		            System.out.println("Message sent");
		         }
		         catch(IOException ioe)
		         {  
		        	 System.out.println("Sending error: " + ioe.getMessage());
		         }
				
			}
			
			console.close();
			streamOut.close();
			malSocket.close();
			System.out.println("Farewell traveler");
			
//			OutputStream os = malSocket.getOutputStream();
//			OutputStreamWriter osw = new OutputStreamWriter(os);
//			BufferedWriter bw = new BufferedWriter(osw);
//			
//			String message; 
//			Scanner reader = new Scanner(System.in);
//			
//			System.out.println("What should Alice tell Bob?");
//			message = reader.nextLine();
//			while(message.compareTo("done") != 0) {
//				bw.write(message);
//				bw.flush();
//				System.out.println("Message Sent");
//				System.out.println("Type new message");
//				message = reader.nextLine();
//			}
//			
//			System.out.println("Farewell traveler");
//			
//			malSocket.close();
//			reader.close();
		}
		catch(IOException e) {
			//print error
		}

	}
}
