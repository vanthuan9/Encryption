package main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class Alice {
	
	//instance variables
	private PublicKey alicePubKey;
	private PrivateKey alicePrivateKey;
	private PublicKey bobPubKey;
	private String malPort;
	private String config;

	private String macExchangeMsg() 
		throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
		
		//Implements PCKS 1.5 signature; assumes that oracle attack has been screened for
		Signature signer = Signature.getInstance("SHA256withRSA");
		
		String msg = "Stand-in for actual key later";
		
		SecureRandom random = new SecureRandom();
		cipher.init(Cipher.ENCRYPT_MODE, bobPubKey, random);
		
		String cipherMsg = Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
		byte[] signedBytes = MySignature.sign(signer,alicePrivateKey,Base64.getDecoder().decode(cipherMsg));
		String signedMsg = Base64.getEncoder().encodeToString(signedBytes);
		
	    return (cipherMsg + ", " + signedMsg);
	}

	public Alice(String alicePubKeyFile, String alicePrivateKeyFile, 
			String bobPubKeyFile, String malPort, String config) throws Exception {

	    alicePubKey = KeyGetter.getPublic(alicePubKeyFile);
	    alicePrivateKey = KeyGetter.getPrivate(alicePrivateKeyFile);
	    bobPubKey = KeyGetter.getPublic(bobPubKeyFile);		    

	    
		this.malPort = malPort;
		this.config = config;
		
		System.out.println("This is Alice"); //notify the identity of this server to the user
		
		//obtain Mallory's port number and connect to it
		int malPortNumber = Integer.parseInt(malPort);
		String serverAddress = "localhost";
		try{
			System.out.println("Connecting to Mallory at ("+malPortNumber+", "+serverAddress +")...");
			Socket malSocket = new Socket(serverAddress, malPortNumber);
			System.out.println("Connected to Mallory");
			
			Scanner console = new Scanner(System.in);
			DataOutputStream streamOut = new DataOutputStream(malSocket.getOutputStream());
			
			//Exchange mac key with Bob and initiates the conversation
			streamOut.writeUTF(new String(macExchangeMsg()));
			streamOut.flush();
			
			//obtain the message from the user and send it to Mallory
			//the communication ends when the user inputs "done"
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
			
			//close all the sockets and console when the user is done communicating
			console.close();
			streamOut.close();
			malSocket.close();
			System.out.println("Farewell traveler");
			
		}
		catch(IOException e) {
			//print error
			System.out.println("Connection failed due to following reason");
			System.out.println(e);
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

		//check for correct # of parameters
		if(args.length != 5) System.out.println("Incorrect number of parameters");
		else {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			
			//create Alice to start communication
			try {
				Alice alice = new Alice(args[0], args[1], args[2], args[3], args[4]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
