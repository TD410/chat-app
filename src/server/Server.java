package server;
import java.net.*;
import java.util.ArrayList;
import client.Message;

/**
 * Server -- implements Runnable
 * @author: 1410532
 */
public class Server implements Runnable {		
	public ServerSocket serverSocket = null;
	public Thread thread = null;
	public ArrayList<ServerThread> clientList = null;	
	
	public static void main(String args[]){
		new Server(5000);
	}
	
	/**
	 * Constructor -- starts the server.
	 * creates a new thread to run the server
	 * @param port port for server to communicate
	 */	
	public Server(int port){
		try{
			serverSocket = new ServerSocket(port);		
			thread = new Thread(this);
			thread.start();
			clientList = new ArrayList<ServerThread>();
			System.out.println("Server started on port " + port + ".");
		}
		catch(Exception ex){
			System.err.println(ex);
		}
	}
	
	/**
	 * run -- runs non stop and waits for client to connect.
	 * calls addClient when a Client connects
	 */
	@Override
	public void run() {
		while (thread != null){ //while there is at least 1 thread (the thread running the server)
			try{
				addClient(serverSocket.accept()); //serverSocket.accept() returns a Socket object -- that is the client's Socket				
			}
			catch(Exception ex){
				System.err.println(ex);
			}
		}
	}
	
	/**
	 * addClient -- add a new ServerThread class instance to serve one client
	 * @param clientSocket the client's Socket
	 */
	public void addClient(Socket clientSocket){
		ServerThread client = new ServerThread(this, clientSocket);
		clientList.add(client); //add the client to list
		client.start(); //start the thread running a client
	}	
	
	/**
	 * Receive a chat from sender then send it to other users
	 * @param message
	 */
	public synchronized void broadcast(Message message){
		System.out.println("Broadcasting " + message.type + " from "+ message.sender +" to "+ message.receiver);
		for (int i =  clientList.size() - 1; i >= 0; i--){
			ServerThread client = clientList.get(i);			
			if (client.running){
					if (message.receiver.equals("All"))	client.send(message);	//If receiver is All			
					else if (message.receiver.equals(client.userName) || message.sender.equals(client.userName)) 
						client.send(message);	
			}
			else{
				clientList.remove(i);
			}			
		}
	}
	
	public String[] getUserList(){
		ArrayList<String> userList = new ArrayList<String>();
		userList.add("All");
		for (int i = 0; i < clientList.size(); i++){
			ServerThread client = clientList.get(i);			
			if (client.running && client.userName != null)
				userList.add(client.userName);
		}
		return userList.toArray(new String[userList.size()]);
	}
}

