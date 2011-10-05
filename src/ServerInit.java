import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

/**
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 1, 2011
 *
 */
public class ServerInit extends Init {
	
	private final Address serverAddress;

//-------------------------------------------------------------------	
	public ServerInit(Address serverAddress) {
		super();
		this.serverAddress = serverAddress;
	}
	
	public Address getServerAddress() {
		return serverAddress;
	}
	
	

}
