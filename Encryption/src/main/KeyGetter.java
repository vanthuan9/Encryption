package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyGetter {
	public static PrivateKey getPrivate(String file) throws Exception {
	    byte[] keyBytes = Files.readAllBytes(Paths.get(file));
		KeyFactory kf = KeyFactory.getInstance("RSA");
	    return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyBytes)));
	}
	
	public static PublicKey getPublic(String file) throws Exception {
	    byte[] keyBytes = Files.readAllBytes(Paths.get(file));
		KeyFactory kf = KeyFactory.getInstance("RSA");
	    return kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyBytes)));
	}
}
