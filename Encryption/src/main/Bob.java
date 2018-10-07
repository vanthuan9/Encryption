package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Bob {
	
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
		
		//keep track of which countermeasure to employ; default if "No encryption"
		boolean encrypt = false;
		boolean macs = false;
		
		//Resolve the version
		resolveVersion(args[4], encrypt, macs);
		
		int portNumber = Integer.parseInt(args[3]);
		
		try {
			ServerSocket bobServer = new ServerSocket(portNumber);
			Socket clientSocket = bobServer.accept();
			
			BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
			
			
			
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
			}
			
			
			
		} 
		catch (IOException e) {
			//print error or smthng
		}
		

		
	}

}
