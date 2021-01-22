import java.io.Serializable;
import java.net.InetAddress;

public class fileData implements Serializable{
	private static final long serialVersionUID = 1L;
	//if the message is a request, then we need a filename to ask broker/client about 
	private boolean isRequest;
	private String filename;
	//if the message is to register with the broker, we should send along our local IP and port
	private boolean isReg;
	private int port;
	private String address;
	//if the message is a file we're sending, we need to send the file
	private boolean isFile;
	private String file;
	//if the message is to quit, that's all the server needs to know
	private boolean isQuit;
	
	//just use default constructor and edit it in place, since most values will be null anyway
	fileData(){
	}

	public boolean isRequest() {
		return isRequest;
	}

	public void setRequest(boolean isRequest) {
		this.isRequest = isRequest;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isReg() {
		return isReg;
	}

	public void setReg(boolean isReg) {
		this.isReg = isReg;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public boolean isQuit() {
		return isQuit;
	}

	public void setQuit(boolean isQuit) {
		this.isQuit = isQuit;
	}
}
