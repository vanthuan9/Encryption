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
		String inputLine = "original template";
		System.out.println("Mallory Server started?");
		try {
			System.out.println(portNumber);;
			ServerSocket malloryServer = new ServerSocket(portNumber);
			System.out.println("Mallory Server started");
			Socket clientSocket = malloryServer.accept();
			System.out.println("Client connected");
			
			BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
			
			System.out.print("Client input: ");

			inputLine = in.readLine();

			malloryServer.close();
			System.out.println("Mallory Server closed");
			
		} 
		catch (IOException e) {
			//print error or smthng
		}
		
		int bobPortNumber = Integer.parseInt(args[3]);
		String serverAddress = "localhost";
		try{
			Socket bobSocket = new Socket(serverAddress, bobPortNumber);
			System.out.println("Connected to Bob's Server");

			OutputStream os = bobSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(inputLine);
			bw.flush();			
			System.out.println("Wrote to Bob's Server");
		
		}
		catch(IOException e) {
			//print error
		}
				
	}

}
