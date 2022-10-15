package client;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable {
	//Type login, message, user_list, filename
	public String sender;
	public String receiver;
	public String type;
	public Object data;
	public Message(String sender, String receiver, String type, Object data){
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
		this.data = data;
	}
}
