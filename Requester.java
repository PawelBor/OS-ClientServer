package client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Requester{
	
	Socket requestSocket;
	ObjectOutputStream out;
 	ObjectInputStream in;
 	String message;
 	String username;
 	String ipaddress;
 	
 	Scanner reader = new Scanner(System.in);
	
	public void run() throws IOException
	{
		//creating a socket to connect to the server
				System.out.println("Please Enter your IP Address");
				ipaddress = reader.next();
				requestSocket = new Socket(ipaddress, 2004);
				System.out.println("Connected to "+ipaddress+" in port 2004");
				//get Input and Output streams
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(requestSocket.getInputStream());
		
		try{	
			System.out.println("server> " + getMessage());
			
			String serverResponse = "";
			
			//login
			do{
				System.out.println("Logins: Admin/User");
				System.out.print("User Name: ");
				username = reader.next();
				sendMessage(username);
			}
			while(getMessage().equals("wrong username"));
			
			System.out.println("WELCOME " + username);
			
			System.out.println("Commands Available ");
			System.out.println("mkdir (inputfilename) - Creates Folder");
			System.out.println("list - shows files");
			//logged in - take commands
			while(!serverResponse.equals("end")){
				
				System.out.print(username+"@server> ");
				String command = reader.next();
				sendMessage(command);
				serverResponse = (String)in.readObject();
				while(serverResponse.startsWith("Connected as"))
				serverResponse = (String)in.readObject();
				System.out.println(serverResponse);
			}
					
		}catch(UnknownHostException unknownHost){
			System.err.println("unknown host");
		}catch(ClassNotFoundException classNot){
			System.err.println("wrong format");
		}
		
		in.close();
		out.close();
		requestSocket.close();
	}
	
	private String getMessage() throws ClassNotFoundException, IOException {
		message = (String)in.readObject();
		return message;
	}

	void sendMessage(String msg) throws IOException
	{
		out.writeObject(msg);
		out.flush();
	}
	
	public static void main(String args[]) throws IOException
	{
		Requester client = new Requester();
		client.run();
	}
}