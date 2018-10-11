package main;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Alice extends Actor {
	
	//instance variables
	private PublicKey alicePubKey;
	private PrivateKey alicePrivateKey;
	private PublicKey bobPubKey;
	
	private String MACKey;
	private int counter = 0;

	public Alice(String alicePubKeyFile, String alicePrivateKeyFile, 
			String bobPubKeyFile, String malPort, String config) throws Exception {
		
		//Retrieve keys
	    alicePubKey = KeyGetter.getPublic(alicePubKeyFile);
	    alicePrivateKey = KeyGetter.getPrivate(alicePrivateKeyFile);
	    bobPubKey = KeyGetter.getPublic(bobPubKeyFile);		    
		
		//Apply configuration
		resolveConfig(config);
		System.out.println(macs);
		
		Scanner console = new Scanner(System.in);
		System.out.println("This is Alice"); //notify the identity of this server to the user
		
		//obtain Mallory's port number and connect to it
		int malPortNumber = Integer.parseInt(malPort);
		String serverAddress = "localhost";
		
		try{
			System.out.println("Connecting to Mallory at ("+malPortNumber+", "+serverAddress +")...");
			Socket malSocket = new Socket(serverAddress, malPortNumber);
			System.out.println("Connected to Mallory");
			
			DataOutputStream streamOut = new DataOutputStream(malSocket.getOutputStream());
			
			if(macs) {
				//Exchange mac key with Bob and initiates the conversation
				initializeMACKey();
				streamOut.writeUTF(new String(macKeyExchangeMsg()));
				streamOut.flush();
			}
			
			//obtain the message from the user and send it to Mallory
			//the communication ends when the user inputs "done"
			String line = "";
			while(!line.equals("done")) {
				try
		         {  System.out.print("Type message: ");
					line = console.nextLine();
					
					if(macs&&encrypt) {
						streamOut.writeUTF(exchangeMsgWithEncAndMAC(line));
			            streamOut.flush();
			            System.out.println("Message sent");
					}
					else if(macs) {
						streamOut.writeUTF(exchangeMsgWithMAC(line));
			            streamOut.flush();
			            System.out.println("Message sent");
					}
					else if(encrypt) {
						streamOut.writeUTF(exchangeMsgWithEnc(line));
			            streamOut.flush();
			            System.out.println("Message sent");
					}
					else {
						streamOut.writeUTF(line);
			            streamOut.flush();
			            System.out.println("Message sent");
					}
		            
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
	
	private String macKeyExchangeMsg() 
		throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, bobPubKey, new SecureRandom());

		String cipherKey = encoder.encodeToString(cipher.doFinal(MACKey.getBytes("UTF-8")));
		String msg = newMessage("Bob", "MacExchange") + ", " + cipherKey;
		
		//Implements PCKS 1.5 signature; assumes that oracle attack has been screened for
		Signature signer = Signature.getInstance("SHA256withRSA");
		byte[] signedBytes = MySignature.sign(signer, alicePrivateKey, encoder.encode(msg.getBytes("UTF-8")));
		String signedMsg = encoder.encodeToString(signedBytes);
		
		System.out.println("mac key is: " + MACKey);
		System.out.println(msg + ", " + signedMsg);
	    return (msg + ", " + signedMsg);
	}
	
	private void initializeMACKey() throws NoSuchAlgorithmException {
		// get a key generator for the HMAC-MD5 keyed-hashing algorithm
	    KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
	    keyGen.init(new SecureRandom());
	    
	    // generate a key from the generator
	    SecretKey key = keyGen.generateKey();
	    MACKey = encoder.encodeToString(key.getEncoded());
	}
	
	//Returns the parts of every message save the string the user wrote
	private String newMessage(String receiver, String typeOfMsg) {
		counter++;
		return (receiver + ", " + typeOfMsg + ", " + (counter - 1) + 
					", " + System.currentTimeMillis());
	}
	
	private String exchangeMsgWithEnc(String message) throws Exception {
		String aesKey = generateAESKey();
		
		String encryptedMsg = encryptMsg(message, aesKey);
		
		Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
			
		//Implements PCKS 1.5 signature; assumes that oracle attack has been screened for
		Signature signer = Signature.getInstance("SHA256withRSA");
			
		SecureRandom random = new SecureRandom();
		cipher.init(Cipher.ENCRYPT_MODE, bobPubKey, random);
			
		String sessionKey = encoder.encodeToString(cipher.doFinal(aesKey.getBytes("UTF-8")));
		
		counter++;
		System.out.println("session key: "+sessionKey);
		
		return ("Bob, "+counter+", "+sessionKey +", "+ encryptedMsg);
	}
	
	private String exchangeMsgWithMAC(String message) throws Exception {
		counter++;
		String newMessage = counter+", "+message;
		String MAC = generateMAC(newMessage);
		return newMessage+", "+MAC;
	}
	
	private String exchangeMsgWithEncAndMAC(String message) throws Exception {
		String encryptedMsg = exchangeMsgWithEnc(message);
		
		String MAC = generateMAC(encryptedMsg);
		
		return encryptedMsg+", " +MAC;
	}
	
	private String generateAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);
		SecretKey secretKey = keyGenerator.generateKey();
		String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		return encodedKey;
	}
	
	private String encryptMsg(String originalText, String AESKey) throws Exception {
		
		Cipher cipher = Cipher.getInstance("AES");
		// decode the base64 encoded string
		byte[] decodedKey = decoder.decode(AESKey);
		// rebuild key using SecretKeySpec
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		byte[] plainTextByte = originalText.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedByte = cipher.doFinal(plainTextByte);
		String encryptedText = encoder.encodeToString(encryptedByte);
		return encryptedText;
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
