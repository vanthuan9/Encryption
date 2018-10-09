package main;

import java.io.*;
import java.net.*;
import java.util.*;

public class Mallory {


	private Scanner console = new Scanner(System.in);
	private Socket bobSocket;
	private ServerSocket malloryServer;
	private String alicePubKey;
	private String bobPubKey;
	private String malPort;
	private String bobPort;
	private String config;
	
	private List<String> interceptHistory;

	public Mallory(String alicePubKey, String bobPubKey, String malPort, String bobPort, String config) {

		this.alicePubKey = alicePubKey;
		this.bobPubKey = bobPubKey;
		this.malPort = malPort;
		this.bobPort = bobPort;
		this.config = config;
		
		interceptHistory = new ArrayList<String>();
		
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
					System.out.println("");

					interceptHistory.add(line);
					System.out.println("Message from Alice: "+line);
					if(!interceptHistory.isEmpty()) System.out.println("Interception history:");
					for(int i=0; i<interceptHistory.size(); i++) {
						System.out.println((i+1) +". "+interceptHistory.get(i));
					}
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
		
		System.out.println("How should we modifie this message?");
		System.out.println("Type 'send' to send, 'delete' to delete, 'modify' to modify, 'replay' to replay previous interceptions");
		
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
				System.out.println("What should we send Bob instead?");
				System.out.print("Type new message to send: ");
				String modifiedMessage = console.nextLine();
				validInput = true;
				return modifiedMessage;
			}
			else if(input.compareTo("replay")==0) {
				boolean validNumber = false;
				int interceptNumber=-1;
				System.out.println("Which of the preivous intercepts should we replay?");
				while(!validNumber) {
					System.out.print("Type the corresponding number: ");
					String interceptNumberString = console.nextLine();
					try {
						interceptNumber = Integer.parseInt(interceptNumberString);
					}
					catch(NumberFormatException e){
					}
					
					if(interceptNumber<=interceptHistory.size() && interceptNumber>0) {
						validNumber = true;
					}
					else {
						System.out.println("Wrong input. Please type a number within the range.");
					}
				}
				
				validInput = true;
				
				return interceptHistory.get(interceptNumber-1);
			}
			else {
				System.out.println("Invalid input, please type another input");
			}

		}
		System.out.println("What should we send Bob instead?");
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
