package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Mallory {

	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Bob's public key
	 * args[2] ; port that Alice will connect to (Mallory's port)
	 * args[3] ; Bob's port
	 * args[4] ; program configuration
	 */

	public static String modify(String s) {
		
		Scanner console = new Scanner(System.in);
		System.out.println("Hehe how should we modifie this message? : "+s);
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
		console.close();
		return modifiedMessage;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//check for correct # of args
		if (args.length != 5) {
			return;
		}
		
		int portNumber = Integer.parseInt(args[2]);
		String inputLine = "original template";
		System.out.println("This is Mallory");
		
		try {
			int bobPortNumber = Integer.parseInt(args[3]);
			String serverAddress = "localhost";
			
			System.out.println("Connecting to Bob at ("+bobPortNumber+", "+serverAddress +")...");
			Socket bobSocket = new Socket(serverAddress, bobPortNumber);
			System.out.println("Connected to Bob");
			
			System.out.println("Connecting to port "+portNumber +"...");
			ServerSocket malloryServer = new ServerSocket(portNumber);
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
			System.out.println("Mallory closed");
			
//			BufferedReader in = new BufferedReader(
//						new InputStreamReader(clientSocket.getInputStream()));
//			
//			System.out.print("Client input: ");
//
//			inputLine = in.readLine();
//			System.out.println(inputLine);
//			malloryServer.close();
//			System.out.println("Mallory Server closed");
			
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
