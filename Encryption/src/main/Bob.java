package main;

import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;

public class Bob {
	
	//instance variables
	private PublicKey alicePubKey;
	private PublicKey bobPubKey;
	private PrivateKey bobPrivateKey;
	private String bobPort;
	private String config;
	private Base64.Decoder decoder = Base64.getDecoder();
	
	public Bob(String alicePubKeyFile, String bobPubKeyFile, String bobPrivateKeyFile, String bobPort, String config) throws Exception {
		
		//initialize instance variables
		alicePubKey = KeyGetter.getPublic(alicePubKeyFile);
		bobPubKey = KeyGetter.getPublic(bobPubKeyFile);
		bobPrivateKey = KeyGetter.getPrivate(bobPrivateKeyFile);
		this.bobPort = bobPort;
		this.config = config;
		
		//notify the identity of the server to the user
		System.out.println("This is Bob");
		
		//keep track of which countermeasure to employ; default if "No encryption"
		boolean encrypt = false;
		boolean macs = false;
		
		//Resolve the version
		resolveVersion(config, encrypt, macs);
		
		//attempt to create a server with the given port number
		int portNumber = Integer.parseInt(bobPort);
		try {
			System.out.println("Connecting to port "+portNumber+"...");
			ServerSocket bobServer = new ServerSocket(portNumber);
			System.out.println("Bob Server started at port "+portNumber);
			
			//accept the client(a.k.a. Mallory)
			Socket clientSocket = bobServer.accept();
			System.out.println("Client connected");
			DataInputStream streamIn = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			
			//Get the key exchange
			//String keyExchange = getKeyExchange(streamIn.readUTF().getBytes());
			
			
			boolean finished = false;
			
			//read input from Mallory
			while(!finished) {
				try {
					String[] msgParts = streamIn.readUTF().split(", ");
					System.out.println("Message from Mallory: "+ msgParts);
					
					Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
					Signature signer = Signature.getInstance("SHA256withRSA");
					
					SecureRandom random = new SecureRandom();
					cipher.init(Cipher.DECRYPT_MODE, bobPrivateKey, random);
					byte[] messageBytes = cipher.doFinal(decoder.decode(msgParts[0]));
					
					
					byte[] signedBytes = decoder.decode(msgParts[1]);
					byte[] cipherBytes = decoder.decode(msgParts[0]);
					System.out.println("Message is " + new String(messageBytes));
					System.out.println("Message is untampered with: " +  MySignature.verify(signer, alicePubKey, signedBytes, cipherBytes));
					
					finished = msgParts[0].equals("done");
				}
				catch(IOException ioe) {
					//disconnect if there is an error reading the input from Mallory
					finished = true;
				}
			}
			
			//clean up the connections before closing
			bobServer.close();
			streamIn.close();
			System.out.println("Bob closed");
		} 
		catch (IOException e) {
			//print error if the server fails to create itself
			System.out.println("Error in creating the server");
			System.out.println(e);
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
		//check for correct # of parameters
		if (args.length != 5) {
			System.out.println("Incorrect number of parameters");
			return;
		}
		
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		//create Bob
		try {
			Bob bob = new Bob(args[0], args[1], args[2], args[3], args[4]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
