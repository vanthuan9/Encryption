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
	
	private Cipher keyEncrypter;
	
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
			
			initialize();
			
			if(macs) {
				//Exchange mac key with Bob and initiates the conversation
				streamOut.writeUTF(new String(macKeyExchangeMsg()));
				streamOut.flush();
			}
			
			//obtain the message from the user and send it to Mallory
			//the communication ends when the user inputs "done"
			String line = "";
			while(!line.equals("done")) {
				try {  
					System.out.print("Type message: ");
					line = console.nextLine();
					
					String packagedMsg = packageMessage(line);
					streamOut.writeUTF(packagedMsg);
		            streamOut.flush();
		            System.out.println("Message sent");
					
		         } catch(IOException ioe) {  
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
	
	private void initialize() throws Exception {
		keyEncrypter = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
		keyEncrypter.init(Cipher.ENCRYPT_MODE, bobPubKey, new SecureRandom());
		
		if (macs) {
			initializeMACKey();
		} 
	}
	
	private String macKeyExchangeMsg() 
		throws GeneralSecurityException, UnsupportedEncodingException {
		
		String cipherKey = encoder.encodeToString(keyEncrypter.doFinal(MACKey.getBytes("UTF-8")));
		String msg = newMessage("MacExchange") + ", " + cipherKey;
		
		//Implements PCKS 1.5 signature; assumes that oracle attack has been screened for
		Signature signer = Signature.getInstance("SHA256withRSA");
		byte[] signedBytes = MySignature.sign(signer, alicePrivateKey, encoder.encode(msg.getBytes("UTF-8")));
		String signedMsg = encoder.encodeToString(signedBytes);
		
		System.out.println("mac key is: " + MACKey);
		System.out.println(msg + ", " + signedMsg);
	    return (msg + ", " + signedMsg);
	}
	
	private void initializeMACKey() throws NoSuchAlgorithmException {
		// get a key generator for the HMAC-SHA256 keyed-hashing algorithm
	    KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
	    keyGen.init(new SecureRandom());
	    
	    // generate a key from the generator
	    SecretKey key = keyGen.generateKey();
	    MACKey = encoder.encodeToString(key.getEncoded());
	}
	
	//Returns the parts of every message save the string the user wrote
	private String newMessage(String typeOfMsg) {
		StringBuilder acc = new StringBuilder();
		
		//Bob is the default msg receiver, though this can be changed to a parameter
		acc.append("Bob, ");

		acc.append(typeOfMsg);
		acc.append(", ");
		
		acc.append(counter);
		acc.append(", ");
		
		acc.append(System.currentTimeMillis());
		
		counter++;		
		return acc.toString();
	}
	
	private String packageMessage(String message) throws Exception {
		StringBuilder acc = new StringBuilder();
		acc.append(newMessage("NewMessage"));

		if(encrypt) {
			//Generate new session key
			String aesKey = generateAESKey();
			String encryptedKey = encoder.encodeToString(keyEncrypter.doFinal(aesKey.getBytes("UTF-8")));
			String encryptedMsg = encryptMsg(message, aesKey); //Check this method
			
			acc.append(", ");
			acc.append(encryptedKey);
			
			acc.append(", ");
			acc.append(encryptedMsg);
		} else {
			acc.append(", ");
			acc.append(message);
		}
		
		if(macs) {
			String hashedMsg = generateMAC(acc.toString());
			acc.append(", ");
			acc.append(hashedMsg);
		}
		
		return acc.toString();
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
		byte[] plainTextByte = originalText.getBytes("UTF-8");
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
		byte[] decodedKey = decoder.decode(MACKey);
		// rebuild key using SecretKeySpec
		SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");

	    // create a MAC and initialize with the above key
	    Mac mac = Mac.getInstance(secretKey.getAlgorithm());
	    mac.init(secretKey);
	    
	    // get the string as UTF-8 bytes
	    byte[] b = originalText.getBytes("UTF-8");
	    
	    // create a digest from the byte array
	    byte[] digest = mac.doFinal(b);
	    String MAC = encoder.encodeToString(digest);
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
