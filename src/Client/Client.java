package Client;

import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

import Shared.ChatMessage;
import Shared.TcpConnection;

public class Client implements ActionListener {
	
	private static String m_name = null;
	private final ChatGUI m_GUI;
	private static TcpConnection m_connection = null;
	private static ChatMessage chatMsg = null;

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: java Client serverhostname serverportnumber username");
			System.exit(-1);
		}
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
			m_connection = new TcpConnection(hostName, port, m_name);
			String handShake_name = m_name;
			String handShake_type = "H";
			chatMsg = new ChatMessage(handShake_type, handShake_name);
			m_connection.sendMessage(chatMsg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (m_connection.isRun()) {
			listenForServerMessages();
		} else {
			System.err.println("Unable to connect to server");
		}
	}

	private void listenForServerMessages() {
		do {
			try {
				m_GUI.displayMessage(m_connection.readObject().getMessage());
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String msg = m_GUI.getInput();
		String[] splitMsg = msg.split(" ", 1);
		String msgType = splitMsg[0];
		String message = splitMsg[1];
		
		chatMsg = new ChatMessage(msgType, message);
		
		try {
			m_connection.sendMessage(chatMsg);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		m_GUI.clearInput();
	}
	public String getName(String name){
		return m_name;
	}
	public boolean hasName(String testName) {
		return testName.equals(m_name);
	}
}
