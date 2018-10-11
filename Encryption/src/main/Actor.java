package main;

import java.util.Base64;

public abstract class Actor {
	public boolean macs;
	public boolean encrypt;
	public Base64.Encoder encoder = Base64.getEncoder();
	public Base64.Decoder decoder = Base64.getDecoder();
	
	public void resolveConfig(String config) throws IllegalArgumentException{
		System.out.println("Config is: " + config);
		if (config.compareTo("noCrypt") == 0) {
			//do nothing because this is the default version
		} else if (config.compareTo("encOnly") == 0) {
			encrypt = true;
		} else if (config.compareTo("macOnly") == 0) {
			macs = true;
		} else if (config.compareTo("mac&Enc") == 0) {
			encrypt = true;
			macs = true;
		} else {
			throw new IllegalArgumentException("configuration is not supported");
		}
	}
}
