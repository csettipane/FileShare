import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Scanner;

public class ClientServiceThread implements Runnable{
	Socket clntSock;
	fileData request;
	List<Registration> registrations;
	SocketAddress clientAddress;
	int port;
	String name;

	public ClientServiceThread(Socket connectionSocket, List<Registration> regs) {

		this.clntSock = connectionSocket;
		this.clientAddress = clntSock.getRemoteSocketAddress();
		this.port = clntSock.getPort();	
		this.registrations = regs;
	}
	
	
	@Override
	public void run() {
		try {
			while(true) {
				//get something from the client over this channel
				fileData fromClient = receiveDataFromClient(clntSock);
				//check if client is registering or asking for a file (we don't need to check if there is a file since that shouldn't happen), or quitting
				if(fromClient.isReg()) {
					//simply add the port, address, and filename to the list
					Registration newReg = new Registration(fromClient.getAddress(), fromClient.getPort(), fromClient.getFilename());
					registrations.add(newReg);
				}
				if(fromClient.isRequest()) {
					//prepare to send something back
					fileData toSend = new fileData();
					String fn = fromClient.getFilename();
					//loop over registrations, looking for something with the same filename
					for(Registration reg: registrations) {
						if(reg.getFileName().equals(fn)) {
							toSend.setPort(reg.getPort());
							toSend.setAddress(reg.getAddress());
							break;
						}
					}
					//report back to client
					sendDataToClient(clntSock, toSend);
				}
				if(fromClient.isQuit()) {
					//loop over registrations and remove the ones from this client
					System.out.println("Client at " + fromClient.getAddress()+ "has quit.");
					for (Registration reg: registrations) {
						if(reg.getAddress().equals(fromClient.getAddress().toString())) {
							registrations.remove(reg);
							System.out.println("Removing deprecated client file.");
						}
					}
					//then we're done with this client
					break;
				}
			}
		}catch (EOFException e) { // needed to catch when client is done
			System.out.println("goodbye client with IP: " + clientAddress + " and port# " + port);
		try {
			clntSock.close(); // Close the socket. We are done with this client!
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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