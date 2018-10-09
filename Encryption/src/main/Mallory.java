package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Mallory {


	private Scanner console = new Scanner(System.in);
	private Socket bobSocket;
	private ServerSocket malloryServer;
	private String alicePubKey;
	private String bobPubKey;
	private String malPort;
	private String bobPort;
	private String config;

	public Mallory(String alicePubKey, String bobPubKey, String malPort, String bobPort, String config) {

		this.alicePubKey = alicePubKey;
		this.bobPubKey = bobPubKey;
		this.malPort = malPort;
		this.bobPort = bobPort;
		this.config = config;
		
		int portNumber = Integer.parseInt(malPort);
		System.out.println("This is Mallory");
		
		try {
			int bobPortNumber = Integer.parseInt(bobPort);
			String serverAddress = "localhost";
			
			System.out.println("Connecting to Bob at ("+bobPortNumber+", "+serverAddress +")...");
			bobSocket = new Socket(serverAddress, bobPortNumber);
			System.out.println("Connected to Bob");
			
			System.out.println("Connecting to port "+portNumber +"...");
			malloryServer = new ServerSocket(portNumber);
			System.out.println("Mallory Server started at "+ portNumber);
			Socket clientSocket = malloryServer.accept();
			System.out.println("Client connected");
			
			DataInputStream streamIn = 
					new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			
			DataOutputStream streamOut = new DataOutputStream(bobSocket.getOutputStream());
			
			boolean finished = false;
			
			while(!finished) {
				try {
					String line = streamIn.readUTF();
					System.out.println("Message from Alice: "+line);
					if(line.equals("done")) {
						finished = true;
					}
					else {
						String messageToSend = modify(line);
						if(messageToSend!=null) {
							streamOut.writeUTF(messageToSend);
							streamOut.flush();
							System.out.println("Message sent to Bob");
						}
					}
				}
				catch(IOException ioe) {
					finished = true;
				}
				
			}
			clientSocket.close();
			streamIn.close();
			streamOut.close();
			malloryServer.close();
			console.close();
			System.out.println("Mallory closed");
			
		} 
		catch (IOException e) {
			//print error or smthng
		}		
	}
	


	public String modify(String s) {
		
		System.out.println("How should we modifie this message? : "+s);
		System.out.println("Type 'send' to send, 'delete' to delete, 'modify' to modify");
		
		boolean validInput = false;
		String input;
		while(!validInput) {
			input = console.nextLine();
			if(input.compareTo("send")==0) {
				validInput = true;
				return s;
				
			}
			else if(input.compareTo("delete")==0) {
				validInput = true;
				System.out.println("Message deleted");
				return null;
			}
			else if(input.compareTo("modify")==0) {
				validInput = true;
			}
			else {
				System.out.println("Invalid input, please type another input");
			}

		}
		
		System.out.println("What should we send Bob?");
		String modifiedMessage = console.nextLine();
		return modifiedMessage;
		
	}
	
	
	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Bob's public key
	 * args[2] ; port that Alice will connect to (Mallory's port)
	 * args[3] ; Bob's port
	 * args[4] ; program configuration
	 */
	public static void main(String[] args) {
		Mallory mallory = new Mallory(args[0], args[1], args[2], args[3], args[4]);
				
	}

}
