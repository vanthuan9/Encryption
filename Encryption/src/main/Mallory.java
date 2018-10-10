package main;

import java.io.*;
import java.net.*;
import java.util.*;

public class Mallory {

	//instance variables
	private Scanner console = new Scanner(System.in);
	private Socket bobSocket;
	private ServerSocket malloryServer;
	private String alicePubKey;
	private String bobPubKey;
	private String malPort;
	private String bobPort;
	private String config;
	
	//list of previous intercepts
	private List<String> interceptHistory;

	public Mallory(String alicePubKey, String bobPubKey, String malPort, String bobPort, String config) {

		//initialize instance variables
		this.alicePubKey = alicePubKey;
		this.bobPubKey = bobPubKey;
		this.malPort = malPort;
		this.bobPort = bobPort;
		this.config = config;
		
		interceptHistory = new ArrayList<String>();
		
		System.out.println("This is Mallory"); //notify the identity of this server to the user
		
		//obtain its port number and start a server
		//also connect to Bob using Bob's port number
		int portNumber = Integer.parseInt(malPort);	
		int bobPortNumber = Integer.parseInt(bobPort);
		String serverAddress = "localhost";
		try {
			//connect to Bob
			System.out.println("Connecting to Bob at ("+bobPortNumber+", "+serverAddress +")...");
			bobSocket = new Socket(serverAddress, bobPortNumber);
			System.out.println("Connected to Bob");
			
			//start its server
			System.out.println("Connecting to port "+portNumber +"...");
			malloryServer = new ServerSocket(portNumber);
			System.out.println("Mallory Server started at "+ portNumber);
			Socket clientSocket = malloryServer.accept();
			System.out.println("Client connected");
			
			//initialize input/output streams
			DataInputStream streamIn = 
					new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			DataOutputStream streamOut = new DataOutputStream(bobSocket.getOutputStream());
			
			//read input from the client(a.k.a. Alice) and modify it before sending it to Bob
			boolean finished = false;
			while(!finished) {
				try {
					//read input from Alice and print it out
					String line = streamIn.readUTF();
					System.out.println("");
					System.out.println("Message from Alice: "+line);
					
					 //add Alice's message to the intercept history and display it
					interceptHistory.add(line);
					System.out.println("Interception history:");
					for(int i=0; i<interceptHistory.size(); i++) {
						System.out.println((i+1) +". "+interceptHistory.get(i));
					}
					
					//if the input was "done", meaning that Alice will disconnect, disconnect itself
					if(line.equals("done")) {
						finished = true;
					}
					else {
						//modify the message otherwise unless the user wants to delete the message
						String messageToSend = modify(line);
						if(messageToSend!=null) {
							streamOut.writeUTF(messageToSend);
							streamOut.flush();
							System.out.println("Message sent to Bob");
						}
					}
				}
				catch(IOException e) {
					//if there is an error in reading input from the client, print out the error
					System.out.println("Error in reading messages from the clinet");
					System.out.println(e);
					finished = true;
				}
			}
			
			//clean up all the connections before ending
			clientSocket.close();
			streamIn.close();
			streamOut.close();
			malloryServer.close();
			console.close();
			System.out.println("Mallory closed");
			
		} 
		catch (IOException e) {
			//if there is an error in creating/connecting to the server, print out the error
			System.out.println("Error in creating/connecting to the server");
			System.out.println(e);
			return;
		}		
	}
	
	/**
	 * This method modifies input message from the client before sending it to Bob.
	 * The method gives 4 options (send, delete, modify, replay) for the user to choose,
	 * and modifies the message accordingly.
	 * 
	 * @param s input string from the client to modify
	 * @return modified message
	 */
	public String modify(String s) {
		
		System.out.println("How should we modifie this message?");
		System.out.println
		("Type 'send' to send, 'delete' to delete, 'modify' to modify, 'replay' to replay previous interceptions");
		
		boolean validInput = false;
		String input;
		
		while(!validInput) {
			input = console.nextLine(); //read input from the user
			
			//if the user simply wants to send the message, send the message
			if(input.compareTo("send")==0) {
				validInput = true;
				return s;
			}
			//if the user wants to delete the message, return null value
			else if(input.compareTo("delete")==0) {
				validInput = true;
				System.out.println("Message deleted");
				return null;
			}
			//if the user wants to modify message, obtain an input from the user and return that string instead
			else if(input.compareTo("modify")==0) {
				System.out.println("What should we send Bob instead?");
				System.out.print("Type new message to send: ");
				String modifiedMessage = console.nextLine();
				validInput = true;
				return modifiedMessage;
			}
			//if the user wants to replay the message, make the user select the number that corresponds
			//to the past intercepts and send the corresponding previous message instead
			else if(input.compareTo("replay")==0) {
				boolean validNumber = false;
				int interceptNumber=-1;
				System.out.println("Which of the preivous intercepts should we replay?");
				while(!validNumber) {
					System.out.print("Type the corresponding number: ");
					String interceptNumberString = console.nextLine();
					
					//interceptNumber will stay as -1 if the user input is not a number
					try {
						interceptNumber = Integer.parseInt(interceptNumberString);
					}
					catch(NumberFormatException e){
					}
					
					//continue only if the user gave a correct number
					if(interceptNumber<=interceptHistory.size() && interceptNumber>0) {
						validNumber = true;
					}
					else {
						System.out.println("Wrong input. Please type a number within the range.");
					}
				}
				
				validInput = true;
				//return the corresponding previous intercept
				return interceptHistory.get(interceptNumber-1);
			}
			else {
				System.out.println("Invalid input, please type another input");
			}

		}
		//this line is not reachable
		return null;
	}
	
	
	/**
	 * args[0] ; Alice's public key
	 * args[1] ; Bob's public key
	 * args[2] ; port that Alice will connect to (Mallory's port)
	 * args[3] ; Bob's port
	 * args[4] ; program configuration
	 */
	public static void main(String[] args) {
		//check for correct # of parameters
		if(args.length != 5) System.out.println("Incorrect number of parameters");
		else {
			//create Mallory
			Mallory mallory = new Mallory(args[0], args[1], args[2], args[3], args[4]);
		}
	}

}
