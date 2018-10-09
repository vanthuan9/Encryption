package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Alice {
	
	private String alicePubKey;
	private String alicePrivateKey;
	private String bobPubKey;
	private String malPort;
	private String config;
	
	
	public Alice(String alicePubKey, String alicePrivateKey, String bobPubKey, String malPort, String config) {

		this.alicePubKey = alicePubKey;
		this.alicePrivateKey = alicePrivateKey;
		this.bobPubKey = bobPubKey;
		this.malPort = malPort;
		this.config = config;
		
		System.out.println("This is Alice");
		
		int malPortNumber = Integer.parseInt(malPort);
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
			
		}
		catch(IOException e) {
			//print error
		}
	}

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
		if(args.length != 5) System.out.println("incorrect number of arguments");
		else {
			Alice alice = new Alice(args[0], args[1], args[2], args[3], args[4]);
		}

	}
}
