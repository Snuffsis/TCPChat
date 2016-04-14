package Server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import Shared.ChatMessage;
import Shared.TcpConnection;

public class Server {

	private static ArrayList<TcpConnection> m_connectedClients = new ArrayList<TcpConnection>();
	private static ServerSocket s_socket;
	private String motd = "Be friendly!";
	private static String[] helplist = { "/help - will display a list of commands",
			"/tell <recipient> <msg> - will send a message to the specified recipient",
			"/leave, /exit or /quit - will exit the chatroom, you may add a farewell message",
			"/tuna <recipient> - will honor the great IRC chat client by slapping a fellow participant with a smelly tuna",
			"/list - will display a list of connected clients" };

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		// timer för att, var 4:e sekund, kolla om en användare ska
		// disconnectas.
		Timer timer = new Timer();
		TimerTask timertask = new TimerTask() {

			@Override
			public void run() {
				checkIfDisconnect();
				removePing();

			}

		};
		timer.schedule(timertask, 0, 5000);
		try {
			Server instance = new Server(Integer.parseInt(args[0]));
			while (true) {
				instance.connectingClients();
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

		ChatMessage message = null;
		for (TcpConnection c : m_connectedClients) {
			if (c.gotMessage()) {
				message = c.getMsg();
			}
			if (message != null) {
				String msg = message.getMessage();
				String msgType = message.getMsgType();

				if(msgType.equalsIgnoreCase("ping")){
					System.out.println("pingtest");
					clientPing(c.retName());
				}
				// Om det är ett meddelande från en klient
				// Om användaren vill lista alla aktiva klienter
				if (msgType.equalsIgnoreCase("/list")) {
					System.out.println("/list");
					listClients("Connected clients: \n", c.retName());
				}
				// If the user wants to disconnect from the client the server
				// will first broadcast a message, and then close the thread
				else if (msgType.equalsIgnoreCase("/quit") || msgType.equalsIgnoreCase("/leave")
						|| msgType.equalsIgnoreCase("/exit")) {
					System.out.println("/leaving");
					broadcast(c.retName() + " has left! " + msg);
					disconnectClient(c.retName());
					c.setRun(false);
				}
				// In honor of the greatest chatclient of them all, IRC, users
				// have the
				// ability to slap their fellow peers with a smelly tuna
				else if (msgType.equalsIgnoreCase("/tuna")) {
					broadcast("* " + c.retName() + " slaps " + msg + " with a smelly tuna *");
				}
				// Om användaren vill skicka ett private meddelande
				// till
				// en
				// annan användare.
				else if (msgType.equalsIgnoreCase("/tell")) {
					String name = message.getName();
					sendPrivateMessage(c.retName() + " (private): " + msg, name);
					sendPrivateMessage("You tell " + name + ": " + msg, c.retName());
				}
				// The statements regarding the help commands

				else if (msgType.equalsIgnoreCase("/help")) {
					System.out.println(msgType + " " + msg);
					if (msg.equalsIgnoreCase("exit ") || msg.equalsIgnoreCase("quit ")
							|| msg.equalsIgnoreCase("leave ")) {
						System.out.println("exit");
						sendPrivateMessage(helplist[2], c.retName());
					} else if (msg.equalsIgnoreCase("list ")) {
						System.out.println("list");
						sendPrivateMessage(helplist[4], c.retName());
					} else if (msg.equalsIgnoreCase("tell ")) {
						System.out.println("tell");
						sendPrivateMessage(helplist[1], c.retName());
					} else if (msg.equalsIgnoreCase("tuna ")) {
						System.out.println("tuna");
						sendPrivateMessage(helplist[3], c.retName());
					} else if (msg.equalsIgnoreCase("help ")) {
						System.out.println("help");
						sendPrivateMessage(helplist[0], c.retName());
					} else if (msg.equalsIgnoreCase("")) {
						System.out.println("empty");
						listHelp("The list of commands are: \n", c.retName());
					}
				}

				// För att skicka ett meddelande till alla aktiva
				// klienter

				else if (msgType.equals("M")) {
					broadcast(c.retName() + ": " + msg);
				}
				
			}
			message = null;
		}
	}

	// Metod för att lägga till klienter i arraylist
	
	public void connectingClients() {
		try {
			TcpConnection TcpC = null;
			TcpC = new TcpConnection(s_socket.accept());
			TcpC.start();
			ChatMessage message = null;
			while (!TcpC.gotMessage()) {
			}
			message = TcpC.getMsg();
			if (message != null) {
				if (message.getMsgType().equals("H")) {
					TcpC.setUsername(message.getMessage());
					System.out.println(TcpC.retName());
					addClient(message.getMessage(), TcpC);
					sendPrivateMessage("Welcome, " + motd, message.getMessage());
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	public boolean addClient(String name, TcpConnection TcpSocket) {
		TcpConnection c;
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.retName().equals(name)) {
				String message = "Name Taken, please reconnect";
				ChatMessage chatMsg = new ChatMessage(message);
				try {
					TcpSocket.sendMessage(chatMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false; // Already exists a client with this name
			}
		}
		m_connectedClients.add(TcpSocket);
		broadcast(TcpSocket.retName() + " has connected!");
		return true;
	}

	// Metod för att skicka privata meddelanden
	public void sendPrivateMessage(String message, String name) {
		ChatMessage chatMsg = new ChatMessage(message);
		TcpConnection c;
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.retName().equals(name)) {
				try {
					c.sendMessage(chatMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// metod för att skriva ut meddelanden
	public void broadcast(String message) {
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			try {
				itr.next().sendMessage(new ChatMessage(message));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Metod för att lista klienter som är uppkopplade, i ett privat meddelande
	public void listClients(String message, String name) {
		TcpConnection v;
		String clist = message;
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			v = itr.next();
			clist += v.retName() + "\n";
		}
		sendPrivateMessage(clist, name);
	}

	public void listHelp(String message, String name) {
		String hlist = message;
		for (int i = 0; i < helplist.length; i++) {
			hlist += helplist[i] + "\n";
		}
		sendPrivateMessage(hlist, name);
	}

	// Metod för att ta bort klient ifrån arraylisten när denne inte längre är
	// uppkopplad
	public static void disconnectClient(String name) {
		TcpConnection c;
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.retName().equals(name)) {
				m_connectedClients.remove(c);
				break;
			}
		}
	}
	// Metod som används i samband med ping-metoden, för att se om pingtaggen är
	// 0, och isåfall då ta bort den ur listan
	public static void checkIfDisconnect() {
		TcpConnection c;
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			if (c.getPing() == 0) {
				System.out.println("client current id " + c.getId());
				disconnectClient(c.retName());
				break;
			}
		}
	}
	public static void removePing() {
		TcpConnection c;
		for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
			c = itr.next();
			c.removePing();
		}
	}
	// Metod för att hantera ping-taggen för varje klient
		public void clientPing(String name) {
			TcpConnection c;
			for (Iterator<TcpConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
				c = itr.next();
				if (c.retName().equalsIgnoreCase(name)) {
					c.ping();
				}
			}
		}
	

}
