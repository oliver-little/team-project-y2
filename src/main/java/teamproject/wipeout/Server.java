package teamproject.wipeout;

import java.net.InetSocketAddress;

public class Server {
	private final String serverName;
	private final InetSocketAddress address;

	public Server(String name, InetSocketAddress address) {
		this.serverName = name;
		this.address = address;		
	}
	
	public String getServerName() {
		return this.serverName;
	}
	
	public InetSocketAddress getAddress() {
		return this.address;
	}
	
	public String toString() {
		return this.serverName;
	}
}
