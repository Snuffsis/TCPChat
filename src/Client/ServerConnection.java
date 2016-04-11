/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author brom
 */
public class ServerConnection {
	
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;

	private InetAddress m_serverAddress = null;
	private int m_serverPort = -1;

	public ServerConnection(String hostName, int port) {
		m_serverPort = port;
		try {
			m_serverAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean handshake(String name) {
		
		try {
			socket = new Socket(m_serverAddress, m_serverPort);
		} catch (IOException e) {
			System.out.println("Error connection to server: "+e);
			return false;
		}
		
		String msg = "Connection accepted "+ socket.getInetAddress() + ":" + socket.getPort();
		event(msg);
		try{
		sInput = new ObjectInputStream(socket.getInputStream());
		sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch(IOException e){
			event("Exception creating input/output: "+e);
			return false;
		}
		new RecieveChatMessage().start();
		// TODO:
		// * marshal connection message containing user name
		// * send message via socket
		// * receive response message from server
		// * unmarshal response message to determine whether connection was
		// successful
		// * return false if connection failed (e.g., if user name was taken)
		return true;
	}
	
	public String receiveChatMessage() {
		// TODO:
		// * receive message from server
		// * unmarshal message if necessary

		// Note that the main thread can block on receive here without
		// problems, since the GUI runs in a separate thread

		// Update to return message contents
		return "";
	}

	public void sendChatMessage(String message) {
		try {
			Socket client = new Socket(m_serverAddress, m_serverPort);
			
			

			OutputStream outToServer = client.getOutputStream();

			DataOutputStream out = new DataOutputStream(outToServer);

			out.writeUTF(message);

			InputStream inFromServer = client.getInputStream();

			DataInputStream in = new DataInputStream(inFromServer);

			in.readUTF();

			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	class ListenFromServer() extends Thread{
		
	}
	
}
