package centralized.system.peer;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import centralized.simulator.snapshot.Snapshot;
import centralized.system.peer.event.JoinPeer;
import centralized.system.peer.event.PublishPeer;
import centralized.system.peer.event.SubscribePeer;
import centralized.system.peer.event.SubscriptionInit;
import centralized.system.peer.event.UnsubscribePeer;
import centralized.system.peer.message.Notification;
import centralized.system.peer.message.Publication;
import centralized.system.peer.message.SubscribeRequest;
import centralized.system.peer.message.TopicList;
import centralized.system.peer.message.UnsubscribeRequest;


import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.p2p.bootstrap.BootstrapCompleted;
import se.sics.kompics.p2p.bootstrap.BootstrapRequest;
import se.sics.kompics.p2p.bootstrap.BootstrapResponse;
import se.sics.kompics.p2p.bootstrap.P2pBootstrap;
import se.sics.kompics.p2p.bootstrap.PeerEntry;
import se.sics.kompics.p2p.bootstrap.client.BootstrapClient;
import se.sics.kompics.p2p.bootstrap.client.BootstrapClientInit;
import se.sics.kompics.p2p.fd.FailureDetector;
import se.sics.kompics.p2p.fd.PeerFailureSuspicion;
import se.sics.kompics.p2p.fd.StartProbingPeer;
import se.sics.kompics.p2p.fd.StopProbingPeer;
import se.sics.kompics.p2p.fd.SuspicionStatus;
import se.sics.kompics.p2p.fd.ping.PingFailureDetector;
import se.sics.kompics.p2p.fd.ping.PingFailureDetectorInit;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

public final class Peer extends ComponentDefinition {
	Negative<PeerPort> msPeerPort = negative(PeerPort.class);

	Positive<Network> network = positive(Network.class);
	Positive<Timer> timer = positive(Timer.class);

	private Component fd, bootstrap;
	
	private Random rand;
	private Address myAddress;
	private Address serverAddress;
	private PeerAddress myPeerAddress;
	private PeerAddress serverPeerAddress;
	private Vector<PeerAddress> friends;
	private int msgPeriod;
	private int viewSize;
	private boolean bootstrapped;
	
	private HashMap<Address, UUID> fdRequests;
	private HashMap<Address, PeerAddress> fdPeers;
	
	private HashMap<BigInteger, BigInteger> mySubscriptions;				// <Topic ID, last sequence number>
	private HashMap<BigInteger, Vector<Notification>> eventRepository;		// <Topic ID, list of Notification>
	private HashMap<BigInteger, Vector<PeerAddress>> forwardingTable;		// <Topic ID, list of PeerAddress (your
	
	private BigInteger publicationSeqNum;

//-------------------------------------------------------------------
	public Peer() {
		fdRequests = new HashMap<Address, UUID>();
		fdPeers = new HashMap<Address, PeerAddress>();
		rand = new Random(System.currentTimeMillis());
		mySubscriptions =  new HashMap<BigInteger, BigInteger>();

		fd = create(PingFailureDetector.class);
		bootstrap = create(BootstrapClient.class);
		
		publicationSeqNum = BigInteger.ONE;
		
		connect(network, fd.getNegative(Network.class));
		connect(network, bootstrap.getNegative(Network.class));
		connect(timer, fd.getNegative(Timer.class));
		connect(timer, bootstrap.getNegative(Timer.class));
		
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		
//		subscribe(handleSendMessage, timer);
//		subscribe(handleRecvMessage, network);
		subscribe(handleJoin, msPeerPort);
		subscribe(handleSubscribe, msPeerPort);
		subscribe(handleUnsubscribe, msPeerPort);
		subscribe(handlePublish, msPeerPort);
		
		subscribe(handleSubscriptionInit, msPeerPort);

		
		subscribe(handleBootstrapResponse, bootstrap.getPositive(P2pBootstrap.class));
		subscribe(handlePeerFailureSuspicion, fd.getPositive(FailureDetector.class));
		
		
		subscribe(eventNotificationHandler, network);
		// subscribe(messageHandler, network);
		
//		System.out.println("Peer subscribed to initHandler, startHandler, and eventNotificationHandler.");
	}
	
	Handler<Notification> eventNotificationHandler = new Handler<Notification>() {
		@Override
		public void handle(Notification msg) {
			System.out.println("Peer " + myAddress.getId() 
					+ " received a notification about " + msg.getTopic());
		}
	};
	
	// Helper methods
	
	// -------------------------------------------------------------------------
	private void sendSubscribeRequest(BigInteger topicID, BigInteger lastSequenceNum) {
		System.out.println("Peer " + myAddress.getId() + " is triggering subscription.");
		
		mySubscriptions.put(topicID, lastSequenceNum);
		
		SubscribeRequest sub = new SubscribeRequest(topicID, lastSequenceNum, myAddress, serverAddress);
//		System.out.println("ServerAddress" + serverAddress );
		trigger(sub, network);
	}
	
	private void sendUnsubscribeRequest(BigInteger topicID) {
		System.out.println("Peer " + myAddress.getId() + " is triggering subscription.");
		UnsubscribeRequest unsub = new UnsubscribeRequest(topicID, myAddress, serverAddress);
		trigger(unsub, network);
	}
	
	private void publish(BigInteger topicID, String content) {
		System.out.println("Peer " + myAddress.getId() + " is publishing an event.");
		
		trigger(new Publication(topicID, publicationSeqNum, content, myAddress, serverAddress), network);
		publicationSeqNum.add(BigInteger.ONE);
	}
	
	Handler<Start> handleStart = new Handler<Start>() {
		@Override
		public void handle(Start event) {
//			System.out.println("Peer -- inside the handleStart()");
			/*
			System.out.println("Peer " + myAddress.getId() + " is started.");
			Address add = new Address(myAddress.getIp(), myAddress.getPort(), myAddress.getId()-1);
			Notification notification = new Notification("test",
					"nothing", myAddress, myAddress);
			trigger(notification, network);
			String topic = "Football";
			sendSubscribeRequest(topic);
			
*/
			
	
			//sendUnsubscribeRequest(topic);
		}
	};
	
	Handler<SubscriptionInit> handleSubscriptionInit = new Handler<SubscriptionInit>() {
		@Override
		public void handle(SubscriptionInit si) {
			Vector<BigInteger> topicIDs = si.getTopicIDs();
			
			Iterator it = topicIDs.iterator();
			while(it.hasNext()) {
				BigInteger topicID = (BigInteger) it.next();			
				sendSubscribeRequest(topicID, BigInteger.ZERO);
				
			}
			

		}
	};


//-------------------------------------------------------------------
// This handler initiates the Peer component.	
//-------------------------------------------------------------------
	Handler<PeerInit> handleInit = new Handler<PeerInit>() {
		@Override
		public void handle(PeerInit init) {
			myPeerAddress = init.getMSPeerSelf();
			myAddress = myPeerAddress.getPeerAddress();
			serverPeerAddress = init.getServerPeerAddress();
			serverAddress = serverPeerAddress.getPeerAddress();
			friends = new Vector<PeerAddress>();
			msgPeriod = init.getMSConfiguration().getSnapshotPeriod();

			viewSize = init.getMSConfiguration().getViewSize();

			trigger(new BootstrapClientInit(myAddress, init.getBootstrapConfiguration()), bootstrap.getControl());
			trigger(new PingFailureDetectorInit(myAddress, init.getFdConfiguration()), fd.getControl());
			
			System.out.println("Peer " + myAddress.getId() + " is initialized.");
		}
	};

//-------------------------------------------------------------------
// Whenever a new node joins the system, this handler is triggered
// by the simulator.
// In this method the node sends a request to the bootstrap server
// to get a pre-defined number of existing nodes.
// You can change the number of requested nodes through peerConfiguration
// defined in Configuration.java.
// Here, the node adds itself to the Snapshot.
//-------------------------------------------------------------------
	Handler<JoinPeer> handleJoin = new Handler<JoinPeer>() {
		@Override
		public void handle(JoinPeer event) {
			Snapshot.addPeer(myPeerAddress);
			BootstrapRequest request = new BootstrapRequest("Lab0", viewSize);
			trigger(request, bootstrap.getPositive(P2pBootstrap.class));			
		}
	};
	
	Handler<SubscribePeer> handleSubscribe = new Handler<SubscribePeer>() {
		@Override
		public void handle(SubscribePeer event) {
			BigInteger topicID = TopicList.getRandomTopic(); // TODO: randomization should come from the simulation class, so that for different simulation, we only need to modify the simulation class
			
			BigInteger lastSequenceNumber = BigInteger.ZERO;
			if (mySubscriptions.containsKey(topicID))
				lastSequenceNumber = mySubscriptions.get(topicID);
				
			sendSubscribeRequest(topicID, lastSequenceNumber);
			
		}
	};
	
	Handler<UnsubscribePeer> handleUnsubscribe = new Handler<UnsubscribePeer>() {
		@Override
		public void handle(UnsubscribePeer event) {
			
			System.out.println("Peer " + myAddress.getId() + " is unsubscribing an event.");

			/*
			if (!mySubscriptions.isEmpty()) {
				Set<BigInteger> topicIDs = mySubscriptions.keySet(); // TODO: we can randomize later. randomization should be done in the simulation class.
				    Iterator<BigInteger> it = topicIDs.iterator();
					BigInteger topicID = it.next();
					mySubscriptions.remove(topicID); 
					sendUnsubscribeRequest(topicID);
			}
			*/
		}
	};
	
	Handler<PublishPeer> handlePublish = new Handler<PublishPeer>() {
		@Override
		public void handle(PublishPeer event) {
			String info = "Test"; 
			//publish(TopicList.getRandomTopic(), info);	// Assumptions: we can publish something that we don't subscribe
			
			publish(myPeerAddress.getPeerId(), info);
		}
	};


//-------------------------------------------------------------------
// Whenever a node receives a response from the bootstrap server
// this handler is triggered.
// In this handler, the nodes adds the received list to its friend
// list and registers them in the failure detector.
// In addition, it sets a periodic scheduler to call the
// SendMessage handler periodically.	
//-------------------------------------------------------------------
	Handler<BootstrapResponse> handleBootstrapResponse = new Handler<BootstrapResponse>() {
		@Override
		public void handle(BootstrapResponse event) {
			if (!bootstrapped) {
				bootstrapped = true;
				PeerAddress peer;
				Set<PeerEntry> somePeers = event.getPeers();

				for (PeerEntry peerEntry : somePeers) {
					peer = (PeerAddress)peerEntry.getOverlayAddress();
					friends.addElement(peer);
					fdRegister(peer);
				}
				
				trigger(new BootstrapCompleted("Lab0", myPeerAddress), bootstrap.getPositive(P2pBootstrap.class));
				Snapshot.addFriends(myPeerAddress, friends);
				
				SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(msgPeriod, msgPeriod);
				spt.setTimeoutEvent(new SendMessage(spt));
				trigger(spt, timer);
			}
		}
	};
	
//-------------------------------------------------------------------
// This handler is called periodically, every msgPeriod milliseconds.
//-------------------------------------------------------------------
	Handler<SendMessage> handleSendMessage = new Handler<SendMessage>() {
		@Override
		public void handle(SendMessage event) {
			sendMessage();
		}
	};

//-------------------------------------------------------------------
// Whenever a node receives a PeerMessage from another node, this
// handler is triggered.
// In this handler the node, add the address of the sender and the
// address of another nodes, which has been sent by PeerMessage
// to its friend list, and updates its state in the Snapshot.
// The node registers the nodes added to its friend list and
// unregisters the node removed from the list.
//-------------------------------------------------------------------
	Handler<PeerMessage> handleRecvMessage = new Handler<PeerMessage>() {
		@Override
		public void handle(PeerMessage event) {
			PeerAddress oldFriend;
			PeerAddress sender = event.getMSPeerSource();
			PeerAddress newFriend = event.getNewFriend();

			// add the sender address to the list of friends
			if (!friends.contains(sender)) {
				if (friends.size() == viewSize) {
					oldFriend = friends.get(rand.nextInt(viewSize));
					friends.remove(oldFriend);
					fdUnregister(oldFriend);
					Snapshot.removeFriend(myPeerAddress, oldFriend);
				}

				friends.addElement(sender);
				fdRegister(sender);
				Snapshot.addFriend(myPeerAddress, sender);
			}

			// add the received new friend from the sender to the list of friends
			if (!friends.contains(newFriend) && !myPeerAddress.equals(newFriend)) {
				if (friends.size() == viewSize) {
					oldFriend = friends.get(rand.nextInt(viewSize));
					friends.remove(oldFriend);
					fdUnregister(oldFriend);
					Snapshot.removeFriend(myPeerAddress, oldFriend);
				}

				friends.addElement(newFriend);
				fdRegister(newFriend);
				Snapshot.addFriend(myPeerAddress, newFriend);				
			}			
		}
	};
	
//-------------------------------------------------------------------	
// If a node has registered for another node, e.g. P, this handler
// is triggered if P fails.
//-------------------------------------------------------------------	
	Handler<PeerFailureSuspicion> handlePeerFailureSuspicion = new Handler<PeerFailureSuspicion>() {
		@Override
		public void handle(PeerFailureSuspicion event) {
			Address suspectedPeerAddress = event.getPeerAddress();
			
			if (event.getSuspicionStatus().equals(SuspicionStatus.SUSPECTED)) {
				if (!fdPeers.containsKey(suspectedPeerAddress) || !fdRequests.containsKey(suspectedPeerAddress))
					return;
				
				PeerAddress suspectedPeer = fdPeers.get(suspectedPeerAddress);
				fdUnregister(suspectedPeer);
				
				friends.removeElement(suspectedPeer);
				System.out.println(myPeerAddress + " detects failure of " + suspectedPeer);
			}
		}
	};
	
//-------------------------------------------------------------------
// In this method a node selects a random node, e.g. randomDest,
// and sends it the address of another random node from its friend
// list, e.g. randomFriend.
//-------------------------------------------------------------------
	private void sendMessage() {
		if (friends.size() == 0)
			return;
		
		PeerAddress randomDest = friends.get(rand.nextInt(friends.size()));
		PeerAddress randomFriend = friends.get(rand.nextInt(friends.size()));
		
		if (randomFriend != null)
			trigger(new PeerMessage(myPeerAddress, randomDest, randomFriend), network);
	}
	
//-------------------------------------------------------------------
// This method shows how to register the failure detector for a node.
//-------------------------------------------------------------------
	private void fdRegister(PeerAddress peer) {
		Address peerAddress = peer.getPeerAddress();
		StartProbingPeer spp = new StartProbingPeer(peerAddress, peer);
		fdRequests.put(peerAddress, spp.getRequestId());
		trigger(spp, fd.getPositive(FailureDetector.class));
		
		fdPeers.put(peerAddress, peer);
	}

//-------------------------------------------------------------------	
// This method shows how to unregister the failure detector for a node.
//-------------------------------------------------------------------
	private void fdUnregister(PeerAddress peer) {
		if (peer == null)
			return;
			
		Address peerAddress = peer.getPeerAddress();
		trigger(new StopProbingPeer(peerAddress, fdRequests.get(peerAddress)), fd.getPositive(FailureDetector.class));
		fdRequests.remove(peerAddress);
		
		fdPeers.remove(peerAddress);
	}
}
