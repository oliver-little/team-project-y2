package teamproject.wipeout;

import java.net.InetSocketAddress;

public class Server
{
	private String serverName;
	private InetSocketAddress address;

	public Server(String name, InetSocketAddress address) {
		this.serverName = name;
		this.address = address;		
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}
	
	public String toString() {
		return serverName;
	}
}
