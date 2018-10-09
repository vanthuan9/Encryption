package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import javax.crypto.*;



/**
 *  Generates public-private key pairs and stores them in files
 */
public class Gen {
	
	private static final String ALGORITHM = "RSA";
	private static final int KEY_SIZE = 2048;
	private static final int EXPECTED_PARAM = 4; //must be even
	
	private KeyPairGenerator generator;
	
	public Gen(String algorithm, int keySize) {
		try {
			generator = KeyPairGenerator.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.out.println("Incorrect Key-Gen Algorithm was Provided");
		}
		generator.initialize(keySize);
	}
	
	private KeyPair genKeyPair() {
		return generator.generateKeyPair();
	}
	
	/**
	 * 
	 * args[0] ; File to store Alice's public signing key
	 * args[1] ; File to store Alice's private signing key
	 * args[2] ; File to store Bob's public cipher key
	 * args[3] ; File to store Bob's private cipher key
	 */
	public static void main(String[] args) {
		//check for correct # of args
		if (args.length != EXPECTED_PARAM) {
			System.out.println("Incorrect number of parameters");
			return;
		}

		Gen myGen = new Gen(ALGORITHM, KEY_SIZE);
		FileOutputStream writer;
		
		for(int i = 0; i < (EXPECTED_PARAM /2); i++) {
			KeyPair keys = myGen.genKeyPair();
			
			try {
				writer = new FileOutputStream(new File(args[2*i]));
				writer.write(keys.getPublic().getEncoded());
				writer.close();
				
				writer = new FileOutputStream(new File(args[2*i+1]));
				writer.write(keys.getPrivate().getEncoded());
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Finished Writing Keys");
	}

}
