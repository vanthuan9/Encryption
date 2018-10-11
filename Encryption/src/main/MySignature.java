package main;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class MySignature {
	
	public static byte[] sign(Signature signer, PrivateKey privateKey, byte[] msg) 
	    throws SignatureException, UnsupportedEncodingException, InvalidKeyException {
		signer.initSign(privateKey);
		signer.update(msg);
		return signer.sign();
	}
	
	public static boolean verify(Signature signer, PublicKey publicKey, byte[] signature, byte[] msg) 
	    throws SignatureException, UnsupportedEncodingException, InvalidKeyException {
		signer.initVerify(publicKey);
		signer.update(msg);
		return signer.verify(signature);
	}
}
