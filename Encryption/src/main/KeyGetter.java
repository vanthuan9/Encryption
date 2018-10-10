package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class KeyGetter {
	public static PrivateKey getPrivate(String file) throws Exception {
	    byte[] keyBytes = Files.readAllBytes(Paths.get(file));
	    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
	    return kf.generatePrivate(spec);
	}
	
	public static PublicKey getPublic(String file) throws Exception {
	    byte[] keyBytes = Files.readAllBytes(Paths.get(file));
	    //System.out.println(keyBytes);
	    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
	    return kf.generatePublic(spec);
	}
}
