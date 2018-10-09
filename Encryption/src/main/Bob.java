package main;

import java.io.*;
import java.net.*;

public class Bob {
	
	
	private String alicePubKey;
	private String bobPubKey;
	private String bobPrivateKey;
	private String malPort;
	private String config;
	
	public Bob(String alicePubKey, String bobPubKey, String bobPrivateKey, String malPort, String config) {
		this.alicePubKey = alicePubKey;
		this.bobPubKey = bobPubKey;
		this.bobPrivateKey = bobPrivateKey;
		this.malPort = malPort;
		this.config = config;
		
		
		System.out.println("This is Bob");
		//keep track of which countermeasure to employ; default if "No encryption"
		boolean encrypt = false;
		boolean macs = false;
		
		//Resolve the version
		resolveVersion(config, encrypt, macs);
		
		int portNumber = Integer.parseInt(malPort);
		try {
			System.out.println("Connecting to port "+portNumber+"...");
			ServerSocket bobServer = new ServerSocket(portNumber);
			System.out.println("Bob Server started at port "+portNumber);
			Socket clientSocket = bobServer.accept();
			System.out.println("Client connected");
			
			DataInputStream streamIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			boolean finished = false;
			while(!finished) {
				try {
					String line = streamIn.readUTF();
					System.out.println("Message from Mallory: "+line);
					finished = line.equals("done");
				}
				catch(IOException ioe) {
					finished = true;
				}
			}
			
			bobServer.close();
			streamIn.close();
			System.out.println("Bob closed");
		} 
		catch (IOException e) {
			//print error or smthng
		}
		
	}
	
	private static void resolveVersion(String version, boolean encrypt, boolean macs) {
		if (version == "No cryptography") {
			//do nothing because this is the default version
		} else if (version == "Symmetric encryption only") {
			encrypt = true;
		} else if (version == "MACs only") {
			macs = true;
		} else if (version == "Symmetric encryption then MAC") {
			encrypt = true;
			macs = true;
		} else {
			//Throw exception here for unsupported version
			//remember to try-catch resolveVersion
		}
	}
	
	
	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Bob's public key
	 * args[2] ; Bob's secret key
	 * args[3] ; port that Mallory will connect to
	 * args[4] ; program configuration
	 */
	public static void main(String[] args) {
		//check for correct # of args
		if (args.length != 5) {
			System.out.println("Incorrect number of parameters");
			return;
		}
		
		Bob bob = new Bob(args[0], args[1], args[2], args[3], args[4]);
		
		
		

		
	}

}
