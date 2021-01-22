import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.List;

public class Server {
	
    public static void main(String[] args) throws IOException {
    	ServerSocket servSock = null;
		boolean listening = true;
		List<Registration> registrations = new CopyOnWriteArrayList<>();
		//Just use hard-coded server port
		int serverPort = 5000;
		try {
			servSock = new ServerSocket(serverPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port.");
			System.exit(-1);
		}
        System.out.println("File transfer broker is up and running...");
        SocketAddress clientAddress;
        int port;
        while (listening)
		{
        	Socket clntSock = servSock.accept(); // Get client connection	  
			clientAddress = clntSock.getRemoteSocketAddress();
			port = clntSock.getPort();
			System.out.println("Handling client at " + clientAddress + " with port# " + port);
			ClientServiceThread clientHandler = new ClientServiceThread(clntSock, registrations);
			Thread conectToClient= new Thread(clientHandler);
			new Thread(conectToClient).start();  //create and run a server thread to service client
			System.out.println("Now loop back and wait for the next registration or request " );
		}
		servSock.close();
    }
    
}
