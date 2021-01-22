public class Registration{
	private String address;
	private int port;
	private String fileName;
	
	Registration(String add, int portNum, String file){
		this.address = add;
		this.port = portNum;
		this.fileName = file;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getFileName() {
		return fileName;
	}
		
}
