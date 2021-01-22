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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Scanner;

public class RequestHandlerThread implements Runnable{
	ServerSocket servSock;
	Socket clntSock;
	fileData request;
	SocketAddress clientAddress;
	int port;
	String name;

	public RequestHandlerThread(ServerSocket connectionSocket) {

		this.servSock = connectionSocket;
	}
	
	
	@Override
	public void run() {	
		try {
			//receive request
			clntSock = servSock.accept();
			clientAddress = clntSock.getRemoteSocketAddress();
			port = clntSock.getPort();	
			request = recieveDataFromClient(clntSock);
			fileData toSend = new fileData();
			String inputFileName = request.getFilename();
			String lineFromInputFile;
			
			//fulfill request load file to output by sending line by line
			File infile = new File(inputFileName);  //open file to for reading
			Scanner fromInputFile = new Scanner(infile).useDelimiter("\\n|\\r");//use this to read a line, line has delimiter of , and \n and \r
            while (fromInputFile.hasNextLine()) {
            	lineFromInputFile = fromInputFile.nextLine();
            	toSend.setFile(lineFromInputFile);
            	sendDataToClient(clntSock, toSend);
            }
            fromInputFile.close();  //need to close the read file
            // tell peer that we're done here
            toSend.setFile(null);
            sendDataToClient(clntSock, toSend);
            clntSock.close();
            
		}catch(FileNotFoundException ex) {
            System.out.println("Exception occurred: the input file does not exist");
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
	
	
	public static fileData recieveDataFromClient(Socket clntSock) throws IOException{
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
