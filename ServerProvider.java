//help thanks to...
//www.coderanch.com
//www.stackoverflow.com
//www.javaprogrammingforum.com
// /prog/ boards
import java.io.*;
import java.net.*;

public class ServerProvider implements Runnable{
	
	ServerSocket providerSocket;
	Socket connection = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	String cmd;
	String message;
	String cDir = null;
	String newcDir = null;
	String userID = null;
	String newUserID = null;
	BufferedWriter writer = null;
	
	public void run()
	{	
		try {
			
			providerSocket = new ServerSocket(2004, 10);
			//Wait for connection
			System.out.println("Waiting for connection");
			connection = providerSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			//get Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			sendMessage("CONNECTION SUCCESSFUL");
			
			
			//makes folder called ServerUsers on drive D + text file containing usernames 
			//admin/user
			String path = "C:" + File.separator + "ServerUsers" + File.separator + "Usernames.txt";
			File file = new File(path);
			//gets file if already exists and if not makes new file
			if(!file.exists()) { 
				file.getParentFile().mkdirs(); 
				file.createNewFile();
				new File("C:/ServerUsers/admin").mkdirs();
				new File("C:/ServerUsers/user").mkdirs();
				//list of users
				writer = new BufferedWriter(new FileWriter(file));
				writer.write("admin");
				writer.newLine();
				writer.write("user");
				writer.close();
			}
			compareUsers(obtainUsers());
			cmdCompare();
			//disconnecting
			in.close();
			out.close();
			providerSocket.close();
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	private void cmdCompare() throws ClassNotFoundException, IOException {
		
			//new thread for user commands
			Thread thread = new Thread(new Runnable() {
			     public void run() {
			    	 
			    	//Get user commands
					try {
						cmd = (String)in.readObject();
					} catch (ClassNotFoundException | IOException e2) {
						e2.printStackTrace();
					}
					
					//Valid user commands
					if(cmd.equalsIgnoreCase("list")){
						if(cDir == null)
							cDir = "C:/ServerUsers/" + userID;
						//client gets position in which we are in now
						showDirectoryPos();
					}else if(cmd.equals("mkdir")){
						try {
							sendMessage("Folder Created:");
							try {
								cmd = (String)in.readObject();
							
								String paths = cDir+"/"+cmd;
								File file = new File(paths);
								//makes directory for users if non existent else notify it already exists.
								if(!file.exists()) { 
									new File(paths).mkdirs();
									showDirectoryPos();
								}else{
									sendMessage("please use different name, folder exists already");
								}
							} catch (Exception e) {
								sendMessage("error-");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else{
						try {
							sendMessage("Wrong Command ");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			     }			
		});  
		while(true){
			thread.run();
		}
	}

	
	
	private void compareUsers(String[] names) throws ClassNotFoundException, IOException {
		
		if(userID == null)
			userID = (String)in.readObject();
		else{
			newUserID = (String)in.readObject();
			userID = newUserID;
		}
		
		//if username is right let it if not ask again
		if(newUserID == null)
		if(userID.equals(names[0]) || userID.equals(names[1]))
			sendMessage("Connected as " + userID);
		else{
			sendMessage("wrong username");
			compareUsers(obtainUsers());
		}
		if(newUserID != null){

			if(newUserID.equals(names[0]) || newUserID.equals(names[1]))
				sendMessage("Connected as " + userID);
			else{
				sendMessage("wrong username");
				compareUsers(obtainUsers());
			}	
			
		}
	}
	//gets the users from text file located on server
	private String[] obtainUsers() throws IOException {
		
		String path = "C:" + File.separator + "ServerUsers" + File.separator + "Usernames.txt";
		BufferedReader br = new BufferedReader(new FileReader(path));
		String names[] = {br.readLine(),br.readLine(),br.readLine()};
		br.close();
		return names;
		
	}
	
	private void showDirectoryPos() {
		String messagge = "";
		String messageupdt2 = "";
		//tells the client what is the current directory.
		try{
			File folder = new File(cDir);
			File[] listFile = folder.listFiles();
			

		    for (int i = 0; i < listFile.length; i++) {
		       if (listFile[i].isDirectory()) {
		    	  
		        System.out.print(" " + listFile[i].getName()+"-dir");
		        messagge = " " + listFile[i].getName()+"-dir";
		        
		      }
		      messageupdt2 += " "+messagge;
		       
		    }
		}catch(Exception e){
			try {
				sendMessage("no named folder!");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			cDir = newcDir;
		}
	    try {
			sendMessage(messageupdt2);
		} catch (IOException e) {
			System.out.print("message not sent");
		}
		
	}	
	void sendMessage(String message) throws IOException
	{
		out.writeObject(message);
		out.flush();
		System.out.println("server> " + message);
	}
	
	public static void main(String args[]) throws ClassNotFoundException, IOException
	{
		ServerProvider server = new ServerProvider();
		Thread runnerThread = new Thread(server);
		
		while(true){
			runnerThread.run();
		}
	}
}