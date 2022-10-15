package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Dimension;
import net.miginfocom.swing.MigLayout;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.ScrollPaneConstants;
import java.awt.Insets;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

/**
 * Client -- implements Runnable
 * @author 1410532
 */
public class Client implements Runnable {

	private JFrame frmClient;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frmClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Client() {		
		initialize();
		serverSetting();
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmClient = new JFrame();
		frmClient.setTitle("Simple Chat Client");
		frmClient.setMinimumSize(new Dimension(400, 300));
		frmClient.setName("mainWindow");
		frmClient.setSize(new Dimension(616, 417));
		frmClient.setBounds(100, 100, 600, 400);
		frmClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClient.getContentPane().setLayout(new MigLayout("", "[500.00,grow][100px:100px:200px,grow]", "[350.00,grow][50px:50px:50px,grow]"));
		
		JScrollPane scrollPane_Chat = new JScrollPane();
		scrollPane_Chat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frmClient.getContentPane().add(scrollPane_Chat, "cell 0 0,grow");
		
		txtrChat = new JTextArea();
		txtrChat.setMargin(new Insets(15, 15, 15, 15));
		txtrChat.setEditable(false);
		txtrChat.setWrapStyleWord(true);
		txtrChat.setLineWrap(true);
		scrollPane_Chat.setViewportView(txtrChat);
		
		JScrollPane scrollPane_User = new JScrollPane();
		frmClient.getContentPane().add(scrollPane_User, "cell 1 0,grow");
		
		listUserModel = new DefaultListModel<String>();
		listUser = new JList<String>(listUserModel);
		listUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_User.setViewportView(listUser);
		
		JScrollPane scrollPane_Type = new JScrollPane();
		frmClient.getContentPane().add(scrollPane_Type, "cell 0 1,grow");
		
		txtrType = new JTextArea();
		txtrType.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				 if(e.getKeyCode() == KeyEvent.VK_ENTER && enterIsSend){
					 e.consume();
					 send(new Message(userName, listUser.getSelectedValue() , "message", txtrType.getText()));
					 txtrType.setText("");
				 }
			}
		});
		txtrType.setMargin(new Insets(15, 15, 15, 15));
		txtrType.setLineWrap(true);
		txtrType.setWrapStyleWord(true);
		scrollPane_Type.setViewportView(txtrType);
		
		JButton btnSend = new JButton(">");
		btnSend.setPreferredSize(new Dimension(42, 23));
		btnSend.setMinimumSize(new Dimension(42, 23));
		btnSend.setMaximumSize(new Dimension(42, 23));
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(new Message(userName, listUser.getSelectedValue() , "message", txtrType.getText()));
				txtrType.setText("");
			}
		});
		frmClient.getContentPane().add(btnSend, "flowx,cell 1 1");
		
		JButton btnFile = new JButton("File");
		btnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser browse = new JFileChooser();
				browse.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = browse.showOpenDialog(browse);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = browse.getSelectedFile();
		            sendFile(file);		            
		        }
			}
		});
		btnFile.setPreferredSize(new Dimension(55, 23));
		btnFile.setMinimumSize(new Dimension(55, 23));
		btnFile.setMaximumSize(new Dimension(55, 23));
		frmClient.getContentPane().add(btnFile, "cell 1 1");
		
		JMenuBar menuBar = new JMenuBar();
		frmClient.setJMenuBar(menuBar);
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		
		JMenuItem mntmServerSettings = new JMenuItem("Server Settings");		
		mntmServerSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serverSetting();
			}
		});
		mnSettings.add(mntmServerSettings);
		
		JMenuItem mntmGeneralSettings = new JMenuItem("General Settings");
		mntmGeneralSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generalSetting();
			}
		});
		mnSettings.add(mntmGeneralSettings);
		
		JMenu mnAbout = new JMenu("About");
		menuBar.add(mnAbout);
		
		JMenuItem mntmSimpleChatClient = new JMenuItem("About Simple Chat Client");
		mntmSimpleChatClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				credits();
			}
		});
		mnAbout.add(mntmSimpleChatClient);
	}
	
	
	/**
	 * Client code
	 */
	public Socket socket = null;
	public ObjectOutputStream output = null;
	public ObjectInputStream input = null;		
	public Thread thread = null;
	public Boolean connected = false;
	
	
	public JTextArea txtrChat, txtrType = null; //text boxes for output/input
	public JList<String> listUser = null; //Jlist for user
	public DefaultListModel<String> listUserModel;
		
	
	public String userName = "User"; //Default Name
	public String serverIP = "localhost"; //Default server
	public int port = 5000; //Default port	
	public Boolean enterIsSend = true;
	public String DOWNLOAD_LOCATION =  System.getProperty("user.home") + "\\Desktop"; //Default download location
	
	/**
	 * Set up server IP, port, username
	 */
	public void serverSetting(){
		JTextField _host = new JTextField(15);
		_host.setText(serverIP);
	    JTextField _port = new JTextField(4);
	    _port.setText(Integer.toString(port));
	    JTextField _name = new JTextField(12);	  
	    _name.setText(userName);
	    JPanel p = new JPanel();
	    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	    p.add(new JLabel("Host"));
	    p.add(_host);
	    p.add(new JLabel("Port"));
	    p.add(_port);
	    p.add(new JLabel("Username"));
	    p.add(_name);
		int result = JOptionPane.showConfirmDialog(null, p, "Server Settings", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			connected = false;
			close();
			serverIP = _host.getText();
			port = Integer.parseInt(_port.getText());
			userName = _name.getText();
			connectToServer(serverIP, port);
			send(new Message("", "" , "login", userName));
		}
	}
	
	
	public void credits(){
		JOptionPane.showMessageDialog(null, "Simple Chat Client by group Intel\n October 2016\n \n 1. 1410532 Đỗ Thùy Dung\n 2. 1413591 Trần Tiến Thành\n 3. 1412032 Nguyễn Thị Thùy Linh\n 4. 1411548 Lê Tất Hùng\n 5. 1414558 Trần Sơn Tùng");
	}
	
	/**
	 * Set up download location, enter key
	 */
	public void generalSetting(){
		
		JCheckBox enter = new JCheckBox("Enter key is send");
		enter.setSelected(enterIsSend);
		JTextField download = new JTextField();		
		download.setEditable(false);
		download.setText(DOWNLOAD_LOCATION);
		JButton browse = new JButton("Browse");
		
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(chooser);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            download.setText(chooser.getSelectedFile().toString());
		        }
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		panel.add(enter, "wrap");
		panel.add(new JLabel("Download location:"), "wrap");
		panel.add(download);
		panel.add(browse);
		
		int result = JOptionPane.showConfirmDialog(null, panel, "General Settings", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION){
			enterIsSend = enter.isSelected();
			DOWNLOAD_LOCATION = download.getText();
		}
	}
	
	/**
	 * Connect
	 */	
	public void connectToServer(String serverIP, int port){
		try{
			socket = new Socket(serverIP, port);
			input = new ObjectInputStream(socket.getInputStream());		
			output = new ObjectOutputStream(socket.getOutputStream());	
			txtrChat.setText(userName + " connected to "+ serverIP + " on port " + port + ".\n");		
			connected = true;		
		}
		catch(Exception ex){
			txtrChat.setText("Failed to connect to server: "+ ex +".\n");
		}
	}
	
	/**
	 * Send a message to server
	 * @param message message to send
	 */
	public void send(Message message){
		try {
			output.writeObject(message);
			output.flush();
		}
		catch (Exception ex){
			txtrChat.append("Failed to send: "+ ex +".\n");
		}		
	}
	
	/**
	 * Send a file to server
	 * @param file
	 */
	public void sendFile(File file){
		try{
			String receiver = listUser.getSelectedValue();
			txtrChat.append("Uploading "+ file.getName() +" to " + receiver +" ...\n");
			Path path = Paths.get(file.getAbsolutePath());
			byte [] data  = Files.readAllBytes(path);				
			output.writeObject(new Message(userName, receiver , file.getName(), data));
			output.flush();
			txtrChat.append("Upload Completed: "+ file.getName() +"\n");
		}
		catch (Exception ex){
			txtrChat.append("Failed to send file: "+ ex +".\n");
		}
	}
	
	/**
	 * Get data from server and puts on text area
	 */
	@Override
	public void run(){
		while(connected){
			try {
				Message message = (Message) input.readObject();								
				//type == "login"
				if (message.type.equals("login")){
					if (message.data.equals("NO")){						
						txtrChat.append(userName + " is unavailable. Please choose another name.\n");
						serverSetting();
					}
				}
				
				//type == "message"
				else if (message.type.equals("message"))
					if (message.receiver.equals("All")) 
						if (message.sender.equals("Server")) txtrChat.append((String) message.data + "\n");
						else txtrChat.append(message.sender + ": " + (String) message.data + "\n");
					else	
						txtrChat.append("[PM] " + message.sender + " > " + message.receiver + ": " + (String) message.data + "\n");
									
				//type == "user_list"
				else if (message.type.equals("user_list")){
					listUserModel = new DefaultListModel<String>();
					for (String s : (String[]) message.data){
						listUserModel.addElement(s);
					}
					listUser.setModel(listUserModel);	
					listUser.setSelectedIndex(0);
				}
				
				//type == "filename"
				else {	
					if (!message.sender.equals(userName)){
						new Thread() {
				            public void run() {
								txtrChat.append("Downloading "+ message.type +" from " + message.sender + " ...\n");
								try {
									Files.write(Paths.get(DOWNLOAD_LOCATION, message.type), (byte[]) message.data);
								} catch (IOException e) {
									e.printStackTrace();
								}
								txtrChat.append("Download Completed: " + DOWNLOAD_LOCATION +"\\"+ message.type + "\n");
				            }
						}.start();     
					}
				}
			}
			
			catch (SocketException ex){
				//txtrChat.append("Disconnected from server.\n");
			}
			catch (Exception ex){
				txtrChat.append(ex +".\n");
			}
		}
	}
	
	
	public void close(){
		try{
			connected = false;
			if (socket != null) socket.close();
			if (input != null) input.close();
			if (output != null) output.close();
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	
}
