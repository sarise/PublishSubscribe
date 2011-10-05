import se.sics.kompics.Init;
import se.sics.kompics.address.Address;
import se.sics.kompics.p2p.bootstrap.BootstrapConfiguration;
import se.sics.kompics.p2p.fd.ping.PingFailureDetectorConfiguration;


public class PeerInit extends Init {
	
	private final Address myAddress;
	private final Address serverAddress;

//-------------------------------------------------------------------	
	public PeerInit(Address myAddress,
			Address serverAddress) {
		super();
		this.myAddress = myAddress;
		this.serverAddress = serverAddress;
	}
	
	public Address getMyAddress() {
		return myAddress;
	}
	
	public Address getServerAddress() {
		return serverAddress;
	}
	
	

}
