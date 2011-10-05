import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;


public class Simulator extends ComponentDefinition {
	
	private static final int NUMBER_OF_PEERS = 3;
	
	Configuration conf;
	Component server = null;
	
	public Simulator() {
		this.conf = new Configuration();
	}
	
	public void run() {
		createServer();
		
		for (int i = 0; i < NUMBER_OF_PEERS; i++) {
			createNewPeer();
		}
	}
	
	/**
	 * A function to create a new peer
	 * @return
	 */
	public Component createNewPeer() {
		if (server == null)
			return null;
		
		Component peer = create(Peer.class);
		connect(peer.getNegative(Network.class),
				server.getPositive(Network.class));
		
		trigger(new PeerInit(conf.getNewPeerAddress(), conf.getServerAddress()), 
				peer.getControl());
		trigger(new Start(), peer.getControl());
		
		return peer;
		
	}
	
	public Component createServer() {
		server = create(Server.class);
		return server;
		
	}

}
