package teamproject.wipeout;

import java.net.InetSocketAddress;

public class ServerListItem {
	private final String serverName;
	private final InetSocketAddress address;

	public ServerListItem(String name, InetSocketAddress address) {
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
