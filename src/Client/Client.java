package Client;

import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import Shared.ChatMessage;
import Shared.TcpConnection;

public class Client implements ActionListener {

	private static String m_name = null;
	private final ChatGUI m_GUI;
	private static TcpConnection m_connection = null;
	private static ChatMessage chatMsg = null;
	private static boolean connectionStatus = false;


	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: java Client serverhostname serverportnumber username");
			System.exit(-1);
		}
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				if (connectionStatus == true) {
					System.out.println("clientpingtest");
					ping();
				}

			}

		};
		timer.schedule(timerTask, 0, 1000);
		try {
			Client instance = new Client(args[2]);
			instance.connectToServer(args[0], Integer.parseInt(args[1]));
		} catch (NumberFormatException e) {
			System.err.println("Error: port number must be an integer.");
			System.exit(-1);
		}
	}

	public Client(String userName) {
		m_name = userName;

		// Start up GUI (runs in its own thread)
		m_GUI = new ChatGUI(this, m_name);
	}

	private void connectToServer(String hostName, int port) {
		// Create a new server connection
		try {
			m_connection = new TcpConnection(InetAddress.getByName(hostName), port, m_name);
			m_connection.start();
			chatMsg = new ChatMessage("H", m_name);
			m_connection.sendMessage(chatMsg);
			System.out.println("sent");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m_connection.isRun()) {
			connectionStatus = true;
			listenForServerMessages();
			
		} else {
			System.err.println("Unable to connect to server");
		}
	}

	private void listenForServerMessages() {
		do {
			if (m_connection.gotMessage()) {
				chatMsg = m_connection.getMsg();
				System.out.println(chatMsg.getMessage());
				String message = chatMsg.getMessage();
				m_GUI.displayMessage(message);
			}
		} while (true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String input = m_GUI.getInput();
		try {
			//If it is a command
			//if (input.startsWith("/")) {
				if (input.startsWith("/list")){
					System.out.println("/list");
					String[] splitInput = input.split(" ", 1);
					String msgType = splitInput[0];
					chatMsg = new ChatMessage(msgType, m_name);
					m_connection.sendMessage(chatMsg);
				}
				else if(input.startsWith("/exit")||input.startsWith("/quit")||input.startsWith("/leave")){
					System.out.println("/exit");
					input += " ";
					String[] splitInput = input.split(" ", 2);
					String msgType = splitInput[0];
					String msg = splitInput[1];
					chatMsg = new ChatMessage(msgType, msg);
					m_connection.sendMessage(chatMsg);
					System.exit(0);
				}
				else if(input.startsWith("/tell")){
					System.out.println("/tell");
					String[] splitInput = input.split(" ", 3);
					String msgType = splitInput[0];
					String msg = splitInput[2];
					String name = splitInput[1];
					chatMsg = new ChatMessage(msgType, msg, name);
					m_connection.sendMessage(chatMsg);
				}
				else if(input.startsWith("/tuna")){
					String[] splitInput = input.split(" ", 2);
					String msgType = splitInput[0];
					String name = splitInput[1];
					chatMsg = new ChatMessage(msgType, name);
					m_connection.sendMessage(chatMsg);
				}
				else if (input.startsWith("/help")){
					input += " ";
					String[] splitInput = input.split(" ", 2);
					String msgType = splitInput[0];
					String msg = splitInput[1];
					chatMsg = new ChatMessage(msgType, msg);
					m_connection.sendMessage(chatMsg);
				}
				
			//}
			//If it is a regular message
			else{
				String msgType = "M";
				String msg = input;
				chatMsg = new ChatMessage(msgType, msg);
				m_connection.sendMessage(chatMsg);
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		m_GUI.clearInput();
	}

	public String getName(String name) {
		return m_name;
	}

	public boolean hasName(String testName) {
		return testName.equals(m_name);
	}
	// Metod för att skicka ping meddelandet
	private static void ping() {
		chatMsg = new ChatMessage("ping", "ping");
		try {
			m_connection.sendMessage(chatMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
