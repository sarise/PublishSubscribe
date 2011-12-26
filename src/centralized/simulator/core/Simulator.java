package centralized.simulator.core;


import java.math.BigInteger;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import centralized.main.Configuration;
import centralized.simulator.core.event.AllPeerSubscribe;
import centralized.simulator.core.event.PeerFail;
import centralized.simulator.core.event.PeerJoin;
import centralized.simulator.core.event.PeerPublish;
import centralized.simulator.core.event.PeerSubscribe;
import centralized.simulator.core.event.PeerUnsubscribe;
import centralized.simulator.core.event.ServerStart;
import centralized.simulator.scenarios.Scenario1;
import centralized.simulator.snapshot.Snapshot;
import centralized.system.peer.Peer;
import centralized.system.peer.PeerAddress;
import centralized.system.peer.PeerConfiguration;
import centralized.system.peer.PeerInit;
import centralized.system.peer.PeerPort;
import centralized.system.peer.Server;
import centralized.system.peer.event.JoinPeer;
import centralized.system.peer.event.PublishPeer;
import centralized.system.peer.event.StartServer;
import centralized.system.peer.event.SubscribePeer;
import centralized.system.peer.event.SubscriptionInit;
import centralized.system.peer.event.UnsubscribePeer;

import se.sics.kompics.ChannelFilter;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;
import se.sics.kompics.network.Network;
import se.sics.kompics.p2p.bootstrap.BootstrapConfiguration;
import se.sics.kompics.p2p.fd.ping.PingFailureDetectorConfiguration;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

public final class Simulator extends ComponentDefinition {

	Positive<SimulatorPort> simulator = positive(SimulatorPort.class);
	Positive<Network> network = positive(Network.class);
	Positive<Timer> timer = positive(Timer.class);

	private int peerIdSequence;

	private Address peer0Address;
	private BigInteger idSpaceSize;
	private ConsistentHashtable<BigInteger> view;
	private final HashMap<BigInteger, Component> peers;
	private final HashMap<BigInteger, PeerAddress> peersAddress;
	
	private Component server;
	private PeerAddress serverPeerAddress;
	
	private BootstrapConfiguration bootstrapConfiguration;
	private PeerConfiguration peerConfiguration;	
	private PingFailureDetectorConfiguration fdConfiguration;
	
	private Vector<BigInteger>[] blocks;

//-------------------------------------------------------------------	
	public Simulator() {
		peers = new HashMap<BigInteger, Component>();
		peersAddress = new HashMap<BigInteger, PeerAddress>();
		view = new ConsistentHashtable<BigInteger>();

		idSpaceSize = BigInteger.valueOf(2).pow(Scenario1.NUMBER_OF_BITS);
		
		subscribe(handleInit, control);
		
		subscribe(handleGenerateReport, timer);
		
		subscribe(handlePeerJoin, simulator);
		subscribe(handlePeerFail, simulator);
		subscribe(handleServerStart, simulator);
		subscribe(handlePeerSubscribe, simulator);
		subscribe(handlePeerUnsubscribe, simulator);
		subscribe(handlePeerPublish, simulator);
		subscribe(handleAllPeerSubscribe, simulator);
	}

//-------------------------------------------------------------------	WE HAVE INITIALIZED THE INFORMATION IN CONSTRUCTOR
	Handler<SimulatorInit> handleInit = new Handler<SimulatorInit>() {
		public void handle(SimulatorInit init) {
			peers.clear();
			peerIdSequence = 0;

			peer0Address = init.getPeer0Address();
			bootstrapConfiguration = init.getBootstrapConfiguration();
			fdConfiguration = init.getFdConfiguration();
			peerConfiguration = init.getMSConfiguration();
			/*
			int snapshotPeriod = peerConfiguration.getSnapshotPeriod();			
			SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(snapshotPeriod, snapshotPeriod);
			spt.setTimeoutEvent(new GenerateReport(spt));
			trigger(spt, timer);
			*/
		}
	};

//-------------------------------------------------------------------	WE DON'T NEED THIS
	Handler<PeerJoin> handlePeerJoin = new Handler<PeerJoin>() {
		public void handle(PeerJoin event) {
			BigInteger id = event.getPeerId();
			
			// join with the next id if this id is taken
			BigInteger successor = view.getNode(id);
			while (successor != null && successor.equals(id)) {
				id = id.add(BigInteger.ONE).mod(idSpaceSize);
				successor = view.getNode(id);
			}

			Component newPeer = createAndStartNewPeer(id);
			view.addNode(id);

			trigger(new JoinPeer(id), newPeer.getPositive(PeerPort.class));
		}
	};

	
	//-------------------------------------------------------------------	WE DON'T NEED THIS
	Handler<ServerStart> handleServerStart = new Handler<ServerStart>() {
		public void handle(ServerStart event) {
			BigInteger id = event.getPeerId();
			
			// join with the next id if this id is taken
			BigInteger successor = view.getNode(id);
			while (successor != null && successor.equals(id)) {
				id = id.add(BigInteger.ONE).mod(idSpaceSize);
				successor = view.getNode(id);
			}

			Component server = createAndStartNewServer(id);
			//view.addNode(id);

			trigger(new StartServer(id), server.getPositive(PeerPort.class));
		}
	};
//-------------------------------------------------------------------	WE DON'T NEED THIS
	Handler<PeerFail> handlePeerFail = new Handler<PeerFail>() {
		public void handle(PeerFail event) {
			BigInteger id = view.getNode(event.getPeerId());

			if (view.size() == 0) {
				System.err.println("Empty network");
				return;
			}
			
			if (id.equals(Configuration.SOURCE_ID)) {
				System.err.println("Can not remove source ...");
				return;
			}

			view.removeNode(id);
			stopAndDestroyPeer(id);
		}
	};
	
	//-------------------------------------------------------------------	
	Handler<PeerSubscribe> handlePeerSubscribe = new Handler<PeerSubscribe>() {
		public void handle(PeerSubscribe event) {
			BigInteger id = view.getNode(event.getPeerId());
			Component peer = peers.get(id);
//			System.out.println(view + ", id: " + id + ", event id:" + event.getPeerId());

			Positive pos = peer.getPositive(PeerPort.class);
			SubscribePeer sp = new SubscribePeer();
			
			if (sp == null)
				System.err.println("Event SubscribePeer is null");
			if (pos == null) 
				System.err.println("Port is null");
			
			if (sp != null && pos != null)
				trigger(sp, pos);
		}
	};
	
	Handler<AllPeerSubscribe> handleAllPeerSubscribe = new Handler<AllPeerSubscribe>() {
		public void handle(AllPeerSubscribe event) {
			
			Properties configFile = new Properties();
			try {
				configFile.load(this.getClass().getClassLoader().getResourceAsStream("simulation.properties"));
			} catch (Exception e) {
				System.err.println("Error: couldn't load the properties file in Scenario1.java");
			}
			
			int peersPerBlock = Integer.parseInt(configFile.getProperty("SubscriptionsPerNode"));
			
			determineSubscription(true, peersPerBlock);
		
		}
	};
	
	//-------------------------------------------------------------------	
	Handler<PeerUnsubscribe> handlePeerUnsubscribe = new Handler<PeerUnsubscribe>() {
		public void handle(PeerUnsubscribe event) {
			// TODO: change the implementation
			System.err.println("PeerUnsubscribe -- not implemented yet.");
			
			
			BigInteger id = view.getNode(event.getPeerId());
			Component peer = peers.get(id);
			Positive pos = peer.getPositive(PeerPort.class);
			UnsubscribePeer sp = new UnsubscribePeer();
			
			if (sp == null)
				System.err.println("Event UnsubscribePeer is null");
			if (pos == null) 
				System.err.println("Port is null");
			
			if (sp != null && pos != null)
				trigger(sp, pos);
				
		}
	};
	
	//-------------------------------------------------------------------	
	Handler<PeerPublish> handlePeerPublish = new Handler<PeerPublish>() {
		public void handle(PeerPublish event) {
			// TODO: change the implementation
			//System.err.println("PeerPublish -- not implemented yet.");
			
			
			BigInteger id = view.getNode(event.getPeerId());
			Component peer = peers.get(id);
//			System.out.println(view.getTree() + ", id: " + id + ", event id:" + event.getPeerId());
			if (peer == null)
				System.out.println("---------------------");
			Positive pos = peer.getPositive(PeerPort.class);
			PublishPeer sp = new PublishPeer();
			
			if (sp == null)
				System.err.println("Event SubscribePeer is null");
			if (pos == null) 
				System.err.println("Port is null");
			
			if (sp != null && pos != null)
				trigger(sp, pos);
				
		}
	};

//-------------------------------------------------------------------	
	Handler<GenerateReport> handleGenerateReport = new Handler<GenerateReport>() {
		public void handle(GenerateReport event) {
			Snapshot.report();
		}
	};

//-------------------------------------------------------------------	
	private final Component createAndStartNewPeer(BigInteger id) {
		Component peer = create(Peer.class);
		int peerId = ++peerIdSequence;
		Address peerAddress = new Address(peer0Address.getIp(), peer0Address.getPort(), peerId);

		PeerAddress msPeerAddress = new PeerAddress(peerAddress, id);
		
		connect(network, peer.getNegative(Network.class), new MessageDestinationFilter(peerAddress));
		connect(timer, peer.getNegative(Timer.class));

		trigger(new PeerInit(msPeerAddress, serverPeerAddress, peerConfiguration, bootstrapConfiguration, fdConfiguration), 
				peer.getControl());

		trigger(new Start(), peer.getControl());
		peers.put(id, peer);
		peersAddress.put(id, msPeerAddress);
		
		return peer;
	}

	//-------------------------------------------------------------------	
	private final void determineSubscription(boolean isFixed, int peersPerBlock) {
		int numOfBlocks = peers.size() / peersPerBlock;
		
		// give the leftover to the last block
		
		
		this.blocks = new Vector[numOfBlocks];
		
		Iterator it = this.peersAddress.entrySet().iterator();
		loop:
		for (int i = 0; i < numOfBlocks; i++) {
			// for each block
			blocks[i] = new Vector<BigInteger>();
			for (int j = 0; j < peersPerBlock; j++) {
				if (!it.hasNext())
					break loop;
				
				Map.Entry entry = (Map.Entry) it.next();
				BigInteger topicID = (BigInteger) entry.getKey();
				blocks[i].add(topicID);
			}	
		}
		
		// handle the leftover: spread them to the first few blocks
		int count = 0;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			BigInteger topicID = (BigInteger) entry.getKey();
			
			blocks[count++].add(topicID);			
		}

		Random rand = new Random();
		Iterator it2 = peers.entrySet().iterator();
		while(it2.hasNext()) {
			Map.Entry entry = (Map.Entry) it2.next();
			Component peer = (Component) entry.getValue();
			
			
			SubscriptionInit si = new SubscriptionInit(blocks[rand.nextInt(numOfBlocks)]);
			Positive pos = peer.getPositive(PeerPort.class);
			trigger(si, pos);
		}		
		
	}
//-------------------------------------------------------------------	
	private final void stopAndDestroyPeer(BigInteger id) {
		Component peer = peers.get(id);

		trigger(new Stop(), peer.getControl());
		System.out.println("Triggering STOP event to a peer.");
		

		disconnect(network, peer.getNegative(Network.class));
		disconnect(timer, peer.getNegative(Timer.class));

		Snapshot.removePeer(peersAddress.get(id));

		peers.remove(id);
		peersAddress.remove(id);

		destroy(peer);
	}
	
	//-------------------------------------------------------------------	
	private final Component createAndStartNewServer(BigInteger id) {
		Component tmpServer = create(Server.class);
		int peerId = ++peerIdSequence;
		Address serverAddress = new Address(peer0Address.getIp(), peer0Address.getPort(), peerId);

		PeerAddress tmpServerPeerAddress = new PeerAddress(serverAddress, id);
		
		connect(network, tmpServer.getNegative(Network.class), new MessageDestinationFilter(serverAddress));
		connect(timer, tmpServer.getNegative(Timer.class));

		trigger(new PeerInit(tmpServerPeerAddress, tmpServerPeerAddress, peerConfiguration, bootstrapConfiguration, fdConfiguration), 
				tmpServer.getControl());

		trigger(new Start(), tmpServer.getControl());
		server = tmpServer;
		serverPeerAddress = tmpServerPeerAddress;
		
		return tmpServer;
	}


//-------------------------------------------------------------------	
	private final static class MessageDestinationFilter extends ChannelFilter<Message, Address> {
		public MessageDestinationFilter(Address address) {
			super(Message.class, address, true);
		}

		public Address getValue(Message event) {
			return event.getDestination();
		}
	}
}

