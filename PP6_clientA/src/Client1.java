import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class Client1 {
	static final String DEFAULT_IP = "127.0.0.1";
	public static void main(String[] args) throws IOException {
	    	ServerSocket serverSocket = null;
			boolean listening = true;
			String address = null;
			//This client's server port is hard-coded to 5001
			int serverPort = 5001;
			String server;
			String action, fn;
			Scanner fromKeyboard = new Scanner(System.in);
			
			System.out.print("Enter server broker name or IP address or press return to use default: ");
			server = fromKeyboard.nextLine();
			if (server.length() == 0)
				server = DEFAULT_IP;
			
			//make a socket to the broker server (which is hard-coded to 5000)
			Socket brokerSocket = new Socket(server, 5000);
			
			//we also need to make a server that we host as a peer server
			try {
				serverSocket = new ServerSocket(serverPort);
			} catch (IOException e) {
				System.err.println("Could not listen on port.");
				System.exit(-1);
			}
			
			// get local address
			try {
				address = InetAddress.getLocalHost().getHostAddress();
				System.out.println("Your local address is "+ address);
			} catch (UnknownHostException e) {
				System.out.println("Could not find local address!");
			}
			
			//create thread to listen to requests in the background
			RequestHandlerThread reqThread = new RequestHandlerThread(serverSocket);
			Thread T = new Thread(reqThread);
			System.out.println("Starting thread to handle peer requests!");
			T.start();
			
        	//Now handle user inputs
	        while (listening){
	        	//ask user what they want to do
	        	System.out.println("What would you like to do? Type 'register' to offer up a file, 'request' to download a file, or 'quit' to quit.");
	        	action = fromKeyboard.nextLine();

	        	fileData toSend = new fileData();
	        	if (action.toLowerCase().equals("register")) {
	        		System.out.println("Enter local file name.");
	        		fn = fromKeyboard.nextLine();
	        		//quickly validate file
		        	boolean exists = false;
		        	File tempFile = new File(fn); 
	        		exists = tempFile.exists();
	        		while(!exists) {
	        			System.out.println("File called '"+ fn +"' not found. Please try a valid file or type 'quit'.");
	        			if(fn.equals("quit")) break;
	        			fn = fromKeyboard.nextLine();
	        			tempFile = new File(fn);
	        			exists = tempFile.exists();
	        		}
	        		if (!fn.equals("quit")) {
		        		//register to the broker with a port, address, and filename
		        		toSend.setReg(true);
		        		toSend.setFilename(fn);
		        		toSend.setAddress(address);
		        		toSend.setPort(serverPort);
		        		sendDataToClient(brokerSocket, toSend);
		        		System.out.println("File registered!");
	        		}
	        	}
	        	else if(action.toLowerCase().equals("request")) {
	        		System.out.println("What file would you like to request?");
	        		fn = fromKeyboard.nextLine();
	        		//send a file request to broker
	        		toSend.setRequest(true);
	        		toSend.setFilename(fn);
	        		sendDataToClient(brokerSocket, toSend);
	        		//if broker has attached port and address, use them to connect to another client
	        		fileData receive = receiveDataFromClient(brokerSocket);
	        		if(receive.getAddress() == null) {
	        			System.out.println("File not found on any peers. Try again later...");
	        		}
	        		else {
	        			System.out.println("File found on peer at " + receive.getAddress()+ "! Loading file...");
	        			//send the request to the peer
	        			Socket peerSocket = new Socket(InetAddress.getByName(receive.getAddress()), receive.getPort());
	        			sendDataToClient(peerSocket, toSend);
	        			//begin writing to file using the data we get from the peer
	        			try {
	        	            FileWriter fw = new FileWriter(fn);
	        	            BufferedWriter bw = new BufferedWriter(fw);
	        	            String lineFromFile;
	        	            receive = receiveDataFromClient(peerSocket);
	        	            while (receive.getFile() != null) {
	        	            	lineFromFile = receive.getFile();
	        	            	bw.write(lineFromFile + "\n");
	        	            	receive = receiveDataFromClient(peerSocket);
	        	            }
	        	            bw.close();				//close the write file
	        	        }catch(IOException ex) {
	        	            System.out.println("Exception occurred:");
	        	            ex.printStackTrace();
	        	        }
	        			System.out.println("Success!");
	        		}
	        	}
	        	else if(action.toLowerCase().equals("quit")) {
	        		//tell server we are done and close out
	        		toSend.setQuit(true);
	        		toSend.setAddress(address);
	        		sendDataToClient(brokerSocket, toSend);
	        		System.out.println("Goodbye!");
	        		T.interrupt();
	        		break;
	        	}
	        	
			}
	        brokerSocket.close();
			serverSocket.close();
	    }
	
	public static void sendDataToClient(Socket clntSock, fileData toClient ) throws IOException{
		try {
			OutputStream os = clntSock.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			//notice the need to create a new object to send
			oos.writeObject(toClient);

		}  catch (EOFException e) { // needed to catch when client is done
			System.out.println("in Send EOFException: goodbye client at " + clntSock.getRemoteSocketAddress() + " with port# " + clntSock.getPort());
			clntSock.close(); // Close the socket. We are done with this client!
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("in Send IOException: goodbye client at " + clntSock.getRemoteSocketAddress() + " with port# " + clntSock.getPort());
			clntSock.close(); //
		}
	}
	
	
	public static fileData receiveDataFromClient(Socket clntSock) throws IOException{
		//client transport and network info
		SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
		int port = clntSock.getPort();
		
		//client object
		fileData fromClient = null;
		
		try {
			InputStream is = clntSock.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			fromClient = (fileData) ois.readObject();
		}  catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EOFException e) { // needed to catch when client is done
			System.out.println("in receive EOF: goodbye client at " + clientAddress + " with port# " + port);
			clntSock.close(); // Close the socket. We are done with this client!
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("in receive IO: goodbye client at " + clientAddress + " with port# " + port);
			clntSock.close(); //
		}
		return fromClient;
		
	}
}
