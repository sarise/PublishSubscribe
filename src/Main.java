import se.sics.kompics.*;

import se.sics.kompics.network.Network;


/**
 * How to set my own port? How to send to one node?
 */
public class Main extends ComponentDefinition {
	Component client1, client2, server, clientNet, serverNet; // subcomponents
	Channel channel1, channel2; // channels

	// Negative<Network> network = negative(Network.class);

	public Main() { // . constructor
		client1 = create(ClientComponent.class);
		// client2 = create(ClientComponent.class);
		server = create(ServerComponent.class);
		// clientNet = create(MyNetwork.class);
		// serverNet = create(MyNetwork.class);

		/*
		 * channel1 = connect(clientNet.getPositive(Network.class),
		 * client.getNegative(Network.class));
		 * 
		 * channel2 = connect(serverNet.getPositive(Network.class),
		 * client.getNegative(Network.class));
		 */
	//	System.out.println("Creating channel 1");
	//	channel1 = connect(client1.getNegative(Network.class),
		//		server.getPositive(Network.class));

		// channel2 = connect(client2.getNegative(MyNetwork.class),
		// server.getPositive(MyNetwork.class));

		System.out.println("Channel created");

	}

	public static void main(String[] args) {
		Kompics.createAndStart(Main.class);
		Kompics.shutdown();
	}

}
