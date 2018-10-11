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

public class Alice {
	
	//instance variables
	private PublicKey alicePubKey;
	private PrivateKey alicePrivateKey;
	private PublicKey bobPubKey;
	private String malPort;
	private String config;
	private String MACKey;
	private boolean MAC=false;
	private boolean encrypt=false;
	private int counter = 0;

	private String macExchangeMsg() 
		throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
		
		//Implements PCKS 1.5 signature; assumes that oracle attack has been screened for
		Signature signer = Signature.getInstance("SHA256withRSA");
		
		String msg = MACKey;
		
		SecureRandom random = new SecureRandom();
		cipher.init(Cipher.ENCRYPT_MODE, bobPubKey, random);
		
		String cipherMsg = Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
		byte[] signedBytes = MySignature.sign(signer,alicePrivateKey,Base64.getDecoder().decode(cipherMsg));
		String signedMsg = Base64.getEncoder().encodeToString(signedBytes);
		
	    return (cipherMsg + ", " + signedMsg);
	}
	
	private String exchangeMsgWithEnc(String message) throws Exception {
		
		String aesKey = generateAESKey();
		
		String encryptedMsg = encryptMsg(message, aesKey);
		
		Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
			
		//Implements PCKS 1.5 signature; assumes that oracle attack has been screened for
		Signature signer = Signature.getInstance("SHA256withRSA");
			
		SecureRandom random = new SecureRandom();
		cipher.init(Cipher.ENCRYPT_MODE, bobPubKey, random);
			
		String sessionKey = Base64.getEncoder().encodeToString(cipher.doFinal(aesKey.getBytes("UTF-8")));
		
		counter++;
		
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
		byte[] decodedKey = Base64.getDecoder().decode(AESKey);
		// rebuild key using SecretKeySpec
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		byte[] plainTextByte = originalText.getBytes();
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedByte = cipher.doFinal(plainTextByte);
		Base64.Encoder encoder = Base64.getEncoder();
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
	
	private void initializeMACKey() throws NoSuchAlgorithmException {
		
		// get a key generator for the HMAC-MD5 keyed-hashing algorithm
	    KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
	    
	    // generate a key from the generator
	    SecretKey key = keyGen.generateKey();
	    MACKey = Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public Alice(String alicePubKeyFile, String alicePrivateKeyFile, 
			String bobPubKeyFile, String malPort, String config) throws Exception {

	    alicePubKey = KeyGetter.getPublic(alicePubKeyFile);
	    alicePrivateKey = KeyGetter.getPrivate(alicePrivateKeyFile);
	    bobPubKey = KeyGetter.getPublic(bobPubKeyFile);		    

	    
	    
		this.malPort = malPort;
		this.config = config;
		Scanner console = new Scanner(System.in);
		System.out.println("This is Alice"); //notify the identity of this server to the user
		System.out.println("Should I encrypt the message?");
		System.out.println("Type yes or no");
		boolean validSetup = false;
		String input = "";
		while(!validSetup) {
			input = console.nextLine();
			if(input.compareTo("yes")==0) {
				encrypt = true;
				validSetup = true;
			}
			else if(input.compareTo("no")==0){
				encrypt = false;
				validSetup = true;
			}
			else {
				System.out.println("Please type either 'yes' or 'no'");
			}
		}
		validSetup = false;
		
		System.out.println("Should I add MAC to the message?");
		System.out.println("Type yes or no");
		while(!validSetup) {
			input = console.nextLine();
			if(input.compareTo("yes")==0) {
				MAC = true;
				//initializes MAC key
			    initializeMACKey();
			    System.out.println(MACKey);
				validSetup = true;
			}
			else if(input.compareTo("no")==0){
				MAC = false;
				validSetup = true;
			}
			else {
				System.out.println("Please type either 'yes' or 'no'");
			}
		}
		//obtain Mallory's port number and connect to it
		int malPortNumber = Integer.parseInt(malPort);
		String serverAddress = "localhost";
		try{
			System.out.println("Connecting to Mallory at ("+malPortNumber+", "+serverAddress +")...");
			Socket malSocket = new Socket(serverAddress, malPortNumber);
			System.out.println("Connected to Mallory");
			
			
			DataOutputStream streamOut = new DataOutputStream(malSocket.getOutputStream());
			
			if(MAC) {
				//Exchange mac key with Bob and initiates the conversation
				streamOut.writeUTF(new String(macExchangeMsg()));
				streamOut.flush();
			}
			
			
			//obtain the message from the user and send it to Mallory
			//the communication ends when the user inputs "done"
			String line = "";
			while(!line.equals("done")) {
				try
		         {  System.out.print("Type message: ");
					line = console.nextLine();
					
					if(MAC&&encrypt) {
						streamOut.writeUTF(exchangeMsgWithEncAndMAC(line));
			            streamOut.flush();
			            System.out.println("Message sent");
					}
					else if(MAC) {
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
