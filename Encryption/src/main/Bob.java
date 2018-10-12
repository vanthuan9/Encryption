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
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Bob extends Actor {
	
	//instance variables
	private PublicKey alicePubKey;
	private PublicKey bobPubKey;
	private PrivateKey bobPrivateKey;
	
	private Cipher keyDecrypter;
	
	private String MACKey;
	private int counter = 0;

	public Bob(String alicePubKeyFile, String bobPubKeyFile, String bobPrivateKeyFile, String bobPort, String config) throws Exception {
		
		//initialize instance variables
		alicePubKey = KeyGetter.getPublic(alicePubKeyFile);
		bobPubKey = KeyGetter.getPublic(bobPubKeyFile);
		bobPrivateKey = KeyGetter.getPrivate(bobPrivateKeyFile);
		
		//notify the identity of the server to the user
		System.out.println("This is Bob");
		

		//Resolve the version
		resolveConfig(config);
		
		keyDecrypter = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
		keyDecrypter.init(Cipher.DECRYPT_MODE, bobPrivateKey, new SecureRandom());
		
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
			if (macs) {
				String incomingMac = streamIn.readUTF();
				String[] msgParts = incomingMac.split(", ");
 				
				if(msgParts.length == 6 && msgParts[1].compareTo("MacExchange") == 0
						&& verifyCounter(msgParts)) {

					Signature signer = Signature.getInstance("SHA256withRSA");					
					System.out.println(extractSignedMsg(msgParts));
					
					byte[] signedBytes = decoder.decode(msgParts[msgParts.length - 1]);
					byte[] cipherBytes = encoder.encode(extractSignedMsg(msgParts).getBytes("UTF-8"));

					if (!MySignature.verify(signer, alicePubKey, signedBytes, cipherBytes)) {
						System.out.println("MAC key has been tampered with by Mallory!");
					}
					
					byte[] macKeyBytes = keyDecrypter.doFinal(decoder.decode(msgParts[4]));
					MACKey = new String(macKeyBytes);
					System.out.println("MAC key from Alice is: " + MACKey);
				} else {
					System.out.println("MAC key has been tampered with by Mallory!");
				}
			}
			
			boolean finished = false;
			
			//read input from Mallory
			while(!finished) {
				try {
					
					String incomingMsg = streamIn.readUTF();
					System.out.println(incomingMsg);
					String[] msgParts = incomingMsg.split(", ");
					String message = "";
					System.out.println("Message from Mallory of array length "+ msgParts.length);
					
					//Encrypt and MAC
					if(msgParts.length==7 && macs && encrypt) {
						String MAC = msgParts[6];
						
						if(generateMAC(extractSignedMsg(msgParts)).compareTo(MAC)==0) {
							if (verifyCounter(msgParts)) {
								String decryptedMsg = decrypt(msgParts[5],decryptRSA(msgParts[4]));
								message = decryptedMsg;
								System.out.println("Message from Alice: " +decryptedMsg);
							} else {
								System.out.println("Counter is off, Mallory has deleted previous message(s)");
							}
						}
						else {
							System.out.println("MAC doesn't correspond. Message has been tampered with");
						}
					}
					//Encrypt only
					else if(msgParts.length == 6 && !macs && encrypt) {
						if (verifyCounter(msgParts)) {
							String decryptedMsg = decrypt(msgParts[5],decryptRSA(msgParts[4]));
							message = decryptedMsg;
							System.out.println("Message from Alice: " +decryptedMsg);
						}
						else {
							System.out.println("Counter is off, Mallory has tampered with this and/or previous message(s)");
						}

					}
					//MAC only
					else if(msgParts.length == 6 && macs&&!encrypt) {
						if(generateMAC(extractSignedMsg(msgParts)).compareTo(msgParts[5]) == 0) {
							System.out.println("Message from Alice: "+msgParts[4]);
						}
						else {
							
							System.out.println("MAC doesn't correspond. Message has been tampered with");
						}
					} 
					else if(msgParts.length == 5 && !macs&&!encrypt) {
						if (verifyCounter(msgParts)) {
							System.out.println("Message from Alice: "+ msgParts[4]);
						} else {
							System.out.println("Counter is off, Mallory has tampered with this and/or previous message(s)");
						}
					}
				 
					counter++;
				
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
	
	private boolean verifyCounter(String[] msgParts) {
		int msgCounter = Integer.parseInt(msgParts[2]);
		int msgTimeDif = (int) (System.currentTimeMillis() 
									- Long.parseLong(msgParts[3]));

		return (msgCounter == counter) && (msgTimeDif < 120000);
	}
	
	private String extractSignedMsg(String[] msgParts) {
		StringBuilder acc = new StringBuilder();
		
		acc.append(msgParts[0]);
		for (int i = 1; i < msgParts.length - 1; i++) {
			acc.append(", ");
			acc.append(msgParts[i]);
		}
		
		return acc.toString();
	}
	
	public String decryptRSA(String rsaEncTex) throws Exception {
		byte[] messageBytes = keyDecrypter.doFinal(decoder.decode(rsaEncTex));

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
	    String MAC = Base64.getEncoder().encodeToString(digest);
		return MAC;
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
