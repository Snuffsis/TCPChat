package Server;

import java.io.IOException;

import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import Client.Client;
import Shared.TcpConnection;

public class Server {

	private static ArrayList<TcpConnection> m_connectedClients = new ArrayList<TcpConnection>();
	private static TcpConnection m_socket;
	private static ServerSocket s_socket;
	private String motd = "Be friendly!";

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		
		try {
			Server instance = new Server(Integer.parseInt(args[0]));
			while(true){
				m_socket = new TcpConnection(s_socket.accept());
				m_socket.start();
				instance.listenForClientMessages();
			}
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	private Server(int portNumber) throws IOException {
		try {
			s_socket = new ServerSocket(portNumber);
			s_socket.setSoTimeout(5);
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void listenForClientMessages() {
		System.out.println("Waiting for client messages... ");
		
		do {
			String msgType = m_socket.readObject().getMsgType();
			String msg = m_socket.readObject().getMessage();
			if (msgType.equals("H")) {
				if (addClient(msg, /*SOCKET*/)) {
					String connectionMsg = msg + " has connected.";

					System.out.println(connectionMsg);
					broadcast(msg + " has connected to the chat!\n");
						sendPrivateMessage("Welcome!" + motd, msg);
					
//				TODO 
//				Remove legasy
//				fix connection
//				cange client to fit my needs
				}
				// Om namnet �r taget, s� misslyckas handshake
				else {
					String connectionMsg = "Name is taken, try reconnecting.";
					sendPrivateMessage(id + " " + connectionMsg, recieved[0]);
				}
			}
			// Om det �r ett meddelande fr�n en klient
			else {
				// Om anv�ndaren vill lista alla aktiva klienter
				if (msgType.equalsIgnoreCase("/list")) {
					listClients("Connected clients: \n", m_socket.retName());
				}
				// Om anv�ndaren vill l�mna chatten
				else if (messageType.equalsIgnoreCase("/leave")) {
					setID(name, id);
					disconnectClient(name + " " + id);

				}
				// Om anv�ndaren vill skicka ett private meddelande till en
				// annan anv�ndare.
				else if (messageType.equalsIgnoreCase("/tell")) {
					setID(name, id);
					String[] recievedPrivate = message.split(" ", 5);
					sendPrivateMessage(id + " " + recievedPrivate[1] + " (private): " + recievedPrivate[4],
							recievedPrivate[3]);
					sendPrivateMessage(id + " " + "You tell " + recievedPrivate[3] + " " + recievedPrivate[4],
							recievedPrivate[1]);
				}

				// F�r att skicka ett meddelande till alla aktiva klienter

				else if (messageType.equals("M")) {
					setID(name, id);
					System.out.println("test id " + id);
					broadcast(id + " " + name + ": " + recieved[3]);
				}
				// Om det �r ett ping meddelande.
				else if (recieved[2].equalsIgnoreCase("/ping")) {
					clientPing(recieved[1]);
					System.out.println(id + " " + recieved[1] + " ping");
				}

			}

		} while (true);

	}


//	// Metod f�r att hantera ping-taggen f�r varje klient
//	public void clientPing(String name) {
//		Client c;
//		for (Iterator<Client> itr = m_connectedClients.iterator(); itr.hasNext();) {
//			c = itr.next();
//			if (c.hasName(name)) {
//				c.ping();
//			}
//		}
//	}

//	// Metod f�r att ta bort ping-taggen f�r varje klient
//	public static void removePing() {
//		Client c;
//		for (Iterator<Client> itr = m_connectedClients.iterator(); itr.hasNext();) {
//			c = itr.next();
//			c.removePing();
//
//		}
//	}

	// Metod f�r att l�gga till klienter i arraylist
	public boolean addClient(String name, Socket socket) {
		Client c;
		for (Iterator<Client> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				return false; // Already exists a client with this name
			}
		}
		m_connectedClients.add(new Client(name, socket));

		return true;
	}

	// Metod f�r att skicka privata meddelanden
	public void sendPrivateMessage(String message, String name) {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {

				c.sendMessage(message, m_socket);
			}
		}
	}

	// metod f�r att sskriva ut meddelanden
	public static void broadcast(String message) {
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			itr.next().sendMessage(message, m_socket);

		}
	}

	// Metod f�r att lista klienter som �r uppkopplade, i ett privat meddelande
	public void listClients(String message, String name) {
		ClientConnection v;
		String clist = message;
		// String clist = "Connected Clients: \n";
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			v = itr.next();
			clist += v.getName() + "\n";
		}
		sendPrivateMessage(clist, name);
	}

	// Metod f�r att ta bort klient ifr�n arraylisten n�r denne inte l�ngre �r
	// uppkopplad
	public static void disconnectClient(String nameStr) {
		String[] test = nameStr.split(" ", 2);
		String name = test[0];
		int id = Integer.parseInt(test[1]);

		for (int i = 0; i < 15; i++) {
			broadcast(id + " " + name + " has disconnected ");
		}
		System.out.println(name + " has disconnected");
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.hasName(name)) {
				m_connectedClients.remove(c);
				break;
			}
		}
	}

	// Metod som anv�nds i samband med ping-metoden, f�r att se om pingtaggen �r
	// 0, och is�fall d� ta bort den ur listan
	public static void checkIfDisconnect() {
		ClientConnection c;
		for (Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.getPing() == 0) {
				System.out.println("client current id " + c.getId());
				disconnectClient(c.getName() + " " + c.getId());
				break;
			}
		}
	}
}
