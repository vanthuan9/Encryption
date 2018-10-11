package main;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Bob {
	
	//instance variables
	private PublicKey alicePubKey;
	private PublicKey bobPubKey;
	private PrivateKey bobPrivateKey;
	private String bobPort;
	private String config;
	private Base64.Decoder decoder = Base64.getDecoder();
	private String MACKey;
	
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
					String message = "";
					System.out.println("Message from Mallory: "+ msgParts);
					if(msgParts.length==2) {
						Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
						Signature signer = Signature.getInstance("SHA256withRSA");
						
						SecureRandom random = new SecureRandom();
						cipher.init(Cipher.DECRYPT_MODE, bobPrivateKey, random);
						byte[] messageBytes = cipher.doFinal(decoder.decode(msgParts[0]));
						
						
						byte[] signedBytes = decoder.decode(msgParts[1]);
						byte[] cipherBytes = decoder.decode(msgParts[0]);
						MACKey = new String(messageBytes);
						System.out.println("Message is " + new String(messageBytes));
						System.out.println("Message is untampered with: " +  MySignature.verify(signer, alicePubKey, signedBytes, cipherBytes));
						
					}
					//Encrypt and MAC
					else if(msgParts.length==5) {
						String MAC = msgParts[4];
						
						if(generateMAC(msgParts[0]+", "+msgParts[1]+", "+msgParts[2]+", "+msgParts[3]).compareTo(MAC)==0) {
							String decryptedMsg = decrypt(msgParts[3],decryptRSA(msgParts[2]));
							message = decryptedMsg;
							System.out.println("Message from Bob: " +decryptedMsg);
						}
						else {
							System.out.println("MAC doesn't correspond. Message has been tampered with");
						}
					}
					//Encrypt only
					else if(msgParts.length ==4) {
						String decryptedMsg = decrypt(msgParts[3],decryptRSA(msgParts[2]));
						message = decryptedMsg;
						System.out.println("Message from Bob: " +decryptedMsg);
					}
					//MAC only
					else if(msgParts.length ==3) {
						if(generateMAC(msgParts[0]+", "+msgParts[1]).compareTo(msgParts[2]) == 0) {
							System.out.println("Message from Alice: "+msgParts[1]);
							message = msgParts[1];
						}
						else {
							System.out.println("MAC doesn't correspond. Message has been tampered with");
						}
					}
					
					
					
					finished = message.equals("done");
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
	public String decryptRSA(String rsaEncTex) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
		
		SecureRandom random = new SecureRandom();
		cipher.init(Cipher.DECRYPT_MODE, bobPrivateKey, random);
		byte[] messageBytes = cipher.doFinal(decoder.decode(rsaEncTex));

		String message = new String(messageBytes);
		
		return message;
	}
	public String decrypt(String encryptedText, String encodedKey) throws Exception {
		Cipher cipher = Cipher.getInstance("AES");
		// decode the base64 encoded string
		byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
		// rebuild key using SecretKeySpec
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] encryptedTextByte = decoder.decode(encryptedText);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
		String decryptedText = new String(decryptedByte);
		return decryptedText;
	}
	
	private String generateMAC(String originalText) throws Exception {
		if(MACKey == null) {
			System.out.println("Initialize MAC Key first.");
			return null;
		}
	    
	    // decode the base64 encoded string
		byte[] decodedKey = Base64.getDecoder().decode(MACKey);
		// rebuild key using SecretKeySpec
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");

	    // create a MAC and initialize with the above key
	    Mac mac = Mac.getInstance(secretKey.getAlgorithm());
	    mac.init(secretKey);
	    
	    // get the string as UTF-8 bytes
	    byte[] b = originalText.getBytes("UTF-8");
	    
	    // create a digest from the byte array
	    byte[] digest = mac.doFinal(b);
	    String MAC = Base64.getEncoder().encodeToString(digest);
		return MAC;
	}
	
	private void initializeMACKey(String MACKey) {
		this.MACKey = MACKey;
	}
	
	private static void resolveVersion(String version, boolean encrypt, boolean macs) {
		if (version == "noCrypt") {
			//do nothing because this is the default version
		} else if (version == "encOnly") {
			encrypt = true;
		} else if (version == "macOnly") {
			macs = true;
		} else if (version == "mac&Enc") {
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
