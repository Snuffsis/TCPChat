/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import Shared.TcpConnection;

/**
 *
 * @author brom
 */
public class ServerConnection {

	private TcpConnection m_socket = null;
	private InetAddress m_serverAddress = null;
	private int m_serverPort = -1;
	private final static int PACKETSIZE = 1440;



	public ServerConnection(String hostName, int port) {
		m_serverPort = port;
		try {
			m_serverAddress = InetAddress.getByName(hostName);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public boolean handshake(String name) {
		String msg = name + " H";
		byte[] byteName = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(byteName, byteName.length, m_serverAddress, m_serverPort);
		try {
			m_socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		packet.setData(new byte[PACKETSIZE]);
		try {
			m_socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData());
		System.out.println(message);

		return true;
	}
	
	public String receiveChatMessage() {
		DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
		try {
			
			m_socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
		
		return message;
	}

	public void sendChatMessage(String message) {
		Random generator = new Random();
		double failure = generator.nextDouble();
		
		byte[] byteMessage = message.getBytes();
		DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, m_serverAddress, m_serverPort);
		if (failure > TRANSMISSION_FAILURE_RATE) {
			
			try {
				m_socket.send(packet);
			} catch (IOException e) {
				System.out.println("Connection closed.");
			}

		} else {
			// Message got lost
		}
	}
	
	// Metoden som används för att stänga uppkopplingen på socket
	public void disconnect(){
		m_socket.close();
	}
	
	public boolean isConnected() {
		return m_socket.isConnected();
	}

}
