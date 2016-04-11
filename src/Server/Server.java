package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//
// Source file for the server side. 
//
// Created by Sanny Syberfeldt
// Maintained by Marcus Brohede
//

import java.net.*;
import java.text.SimpleDateFormat;
//import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import Client.ChatMessage;
import Client.Client;

public class Server {

	private static int uniqueID;
	private ArrayList<ClientThread> ct;
	private boolean runServer;
	private int portNumber;
	private SimpleDateFormat date;
	
	private ArrayList<ClientConnection> m_connectedClients;

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			Server instance = new Server(Integer.parseInt(args[0]));
			instance.listenForClientMessages();
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Server(int portNumber) {
		this.portNumber = portNumber;

		date = new SimpleDateFormat("HH:mm:ss");
		
		m_connectedClients = new ArrayList<ClientConnection>();

		// TODO: create a socket, attach it to port based on portNumber, and
		// assign it to m_socket
	}

	private void listenForClientMessages() {
		runServer = true;
		System.out.println("Waiting for client messages... ");

		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			
			while(runServer){
				event("Waiting for messages...");
				
				Socket socket = serverSocket.accept();
				
				if(!runServer)
					break;
				ClientConnect = new ClientConnection(socket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		do {
			// TODO: Listen for client messages.
			// On reception of message, do the following:
			// * Unmarshal message
			// * Depending on message type, either
			// - Try to create a new ClientConnection using addClient(), send
			// response message to client detailing whether it was successful
			// - Broadcast the message to all connected users using broadcast()
			// - Send a private message to a user using sendPrivateMessage()
		} while (true);
	}

	public boolean addClient(String name, InetAddress address, int port) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return false; // Already exists a client with this name
			}
		}
		m_connectedClients.add(new ClientConnection(name, address, port));
		return true;
	}

	public void sendPrivateMessage(String message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				c.sendMessage(message, m_socket);
			}
		}
	}

	public void broadcast(String message) {
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message, m_socket);
		}
	}
	class ClientThread extends Thread{
		Socket socket;
		ObjectInputStream msgInput;
		ObjectOutputStream msgOutput;
		//unique id for easier disonnects
		int id;
		//Username for client
		String username;
		//Message
		ChatMessage cm;
		//Date and time for connection
		String date;
		
		
		ClientThread(Socket socket){
			id = ++uniqueID;
			this.socket = socket;
			
			System.out.println("Thread trying to connect for output/input");
			try
			{
				//create output
				msgOutput = new ObjectOutputStream(socket.getOutputStream());
				msgInput = new ObjectInputStream(socket.getInputStream());
				//Check username
				username = (String) msgInput.readObject();
				event(username + " Has connected");
			}
			catch (IOException e){
				event("Exception during creation of input/output: " + e);
				return;
			}
			catch (ClassNotFoundException e){
				event("Exception " + e);
			}
			date = new Date().toString() + "\n";
			
		}
		
		public void message(){
			runServer = true;
			
			while(runServer){
				try {
					cm = (ChatMessage) msgInput.readObject();
				} catch (ClassNotFoundException e) {
					break;
				} catch (IOException e) {
					event(username + "Error reading object" + e);
					break;
				}
				String message = cm.getMessage();
				
				switch(cm.getType()){
				
				case ChatMessage.MESSAGE:
						
				}
				
			}
		}
		
		
	}
}
