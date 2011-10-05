import java.net.InetAddress;
import java.net.UnknownHostException;

import se.sics.kompics.address.Address;

/**
 * The <code>Configuration</code> class.
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 1, 2011
 *
 */

public class Configuration {
	
	public InetAddress ip = null;
	{
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
		}
	}
	
	public int networkPort = 12345;
	public int serverID = 0;
	public Address serverAddress  = new Address(ip, networkPort, serverID);
	
	public int peerID = 1;
	
	public Address getNewPeerAddress() {
		return new Address(ip, networkPort, peerID++);
	}
	
	public Address getServerAddress() {
		return serverAddress;
	}
	
	public int getNumberOfPeers() {
		return peerID;
	}

}
