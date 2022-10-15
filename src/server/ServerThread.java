package server;
import java.net.*;
import java.util.UUID;
import java.io.*;
import client.Message;
/**
 * ServerThread -- inherit Thread.
 * creates and maintain a single thread for each client
 * @author 1410523
 */
public class ServerThread extends Thread {	
	public Server server = null;
	public Socket socket = null;
	public String ID = UUID.randomUUID().toString();
	public ObjectOutputStream output = null;
	public ObjectInputStream input = null;	
	public Boolean running = true;
	
	/**
	 * ClientThread Constructor
	 * @param ser an instance of Server class
	 * @param soc a client's Socket
	 */
	public ServerThread(Server ser, Socket soc){
		try{
			server = ser;
			socket = soc;		
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			System.out.println("Client connected.");
		}
		catch(Exception ex){
			System.err.println(ex);
		}
	}
	
	public void run(){
		while(running){
			try{
				Message message = (Message) input.readObject();			
				if (message.type.equals("login")){
					//Check if name is available
					Boolean nameAvailable = true;
					for (String name : server.getUserList()){
						if (name.equals((String) message.data)){
							nameAvailable = false;
							send(new Message("Server", "", "login", "NO"));
							break;
						}						
					}
					if (nameAvailable){
						userName = (String) message.data;
						send(new Message("Server", userName, "login", "OK"));
						server.broadcast(new Message("Server","All", "message", userName + " has joined the chat."));
						server.broadcast(new Message("Server","All", "user_list", server.getUserList())); //update user list
					}
				}
				else {
					server.broadcast(message);
				}
			}
			catch(Exception ex){
				System.err.println(ex);
				close(); //close connection
				terminate(); //stop running thread
				System.out.println("Disconnected " + userName + ".");
				server.broadcast(new Message("Server","All", "user_list", server.getUserList())); //update user list
			}
		}
	}
	
	/**
	 * Send the message back to 1 client that this thread is managing
	 * @param sender the sender's user name
	 * @param message the message
	 */
	public void send(Message message){
		try{
				output.writeObject(message);
				output.flush();
				System.out.println("Sent " + message.type + " to " + userName + ".");
			}
		catch(Exception ex){
				System.err.println("Failed send " + message.type + " to " + userName + ": " + ex + ".");
			}		
	}
	
	/**
	 * Close the connection
	 */
	public void close(){
		try{
			if (socket != null) socket.close();
			if (input != null) input.close();
			if (output != null) output.close();
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	
	/**
	 * Stop running thread
	 */
	public void terminate(){
		running = false;
	}
}
