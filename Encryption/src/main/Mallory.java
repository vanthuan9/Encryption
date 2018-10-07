package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Mallory {

	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Bob's public key
	 * args[2] ; port that Alice will connect to (Mallory's port)
	 * args[3] ; Bob's port
	 * args[4] ; program configuration
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//check for correct # of args
		if (args.length != 5) {
			return;
		}
		
		int portNumber = Integer.parseInt(args[2]);
		
//		try {
//			ServerSocket bobServer = new ServerSocket(portNumber);
//			Socket clientSocket = bobServer.accept();
//			
//			BufferedReader in = new BufferedReader(
//						new InputStreamReader(clientSocket.getInputStream()));
//			
//			String inputLine;
//			while ((inputLine = in.readLine()) != null) {
//				System.out.println(inputLine);
//			}
//			
//		} 
//		catch (IOException e) {
//			//print error or smthng
//		}
		
		int bobPortNumber = Integer.parseInt(args[3]);
		String serverAddress = "localhost";
		try{
			Socket bobSocket = new Socket(serverAddress, bobPortNumber);
			OutputStream os = bobSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			
			String message = "RANDOM MESSAGE HAHA";
			bw.write(message);
			bw.flush();
			System.out.println("Message Sent");
			
		
		}
		catch(IOException e) {
			//print error
		}
		
		
		
	}

}
