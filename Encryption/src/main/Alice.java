package main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Alice {

	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Alice's private key
	 * args[2] ; Bob's public key
	 * args[3] ; port that Alice will connect to (Mallory's port)
	 * args[4] ; program configuration
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//check for correct # of args
		if (args.length != 5) {
			return;
		}
		
		int malPortNumber = Integer.parseInt(args[3]);
		String serverAddress = "localhost";
		try{
			Socket malSocket = new Socket(serverAddress, malPortNumber);
			OutputStream os = malSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			
			String message; 
			Scanner reader = new Scanner(System.in);
			
			System.out.println("What should Alice tell Bob?");
			message = reader.nextLine();
			bw.write(message);
			bw.flush();
			System.out.println("Message Sent");
			
			malSocket.close();
			reader.close();
		}
		catch(IOException e) {
			//print error
		}
		
		
		
	}
}
