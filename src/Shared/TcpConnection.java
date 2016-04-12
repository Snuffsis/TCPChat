package Shared;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Stack;
import java.util.concurrent.Semaphore;


public class TcpConnection extends Thread {

	private Socket _socket;
	private ObjectOutputStream _out;
	private ObjectInputStream _in;
	private ChatMessage msg = new ChatMessage(null, null);
	private boolean _gotMessage = false;
	private boolean _run = true;
	private boolean _remove = false;
	private Semaphore sem = new Semaphore(0);
	private String _name;
	
	
	public TcpConnection(String adr, int port, String name) throws IOException {
		_socket = new Socket(adr, port);
		_name = name;
		_out = new ObjectOutputStream(_socket.getOutputStream());
		_in = new ObjectInputStream(_socket.getInputStream());
	}
	
	public TcpConnection(Socket socket) throws IOException {
		_socket = socket;
		
		_out = new ObjectOutputStream(_socket.getOutputStream());
		_in = new ObjectInputStream(_socket.getInputStream());
	}
	
	public synchronized void sendMessage(ChatMessage msg) throws IOException {
		_out.writeObject(msg);
	}
	
	
	public synchronized boolean gotMessage() {
		return _gotMessage;
	}
	
	public synchronized boolean isRun() {
		return _run;
	}

	public synchronized ChatMessage getMsg() {
		sem.release();
		_gotMessage = false;
		return msg;
	}
	public synchronized String retName(){
		return _name;
	}

	public synchronized void setRun(boolean run) {
		_run = run;
	}

	public synchronized boolean getRemove() {
		return _remove;
	}

	public synchronized void setRemove(boolean remove) {
		_remove = remove;
	}
	
	public synchronized ChatMessage readObject() throws IOException, ClassNotFoundException {
		return (ChatMessage) _in.readObject();
	}
	

	public void run() {
		
		while(_socket.isConnected() && _run){
			try {
				msg = (ChatMessage) _in.readObject();
				_gotMessage = true;
				if(msg.getMsgType().equals("close")){
					_run = false;
					_socket.close();
				}
				try {
					sem.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				_run = false;
				try {
					_socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				_run = false;
				try {
					_socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					
				}
			}
		}
	}
}
