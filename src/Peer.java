import java.net.InetAddress;
import java.net.UnknownHostException;

import message.Publication;
import message.SubscribeRequest;
import message.UnsubscribeRequest;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Event;
import se.sics.kompics.Init;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.*;
import se.sics.kompics.network.mina.MinaNetwork;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timer;

public class Peer extends ComponentDefinition {

	Positive<Network> network = requires(Network.class);
	private final int msgPeriod = 100;
	private Address myAddress = null;
	private Address serverAddress = null;

	Positive<Timer> timer = positive(Timer.class);

	public Peer() {
		subscribe(initHandler, control);
		subscribe(startHandler, control);
		subscribe(eventNotificationHandler, network);
		// subscribe(messageHandler, network);
		
		System.out.println("Peer subscribed to initHandler, startHandler, and eventNotificationHandler.");

	}

	Handler<PeerInit> initHandler = new Handler<PeerInit>() {
		public void handle(PeerInit init) {
			myAddress = init.getMyAddress();
			serverAddress = init.getServerAddress();
			
			System.out.println("Peer " + myAddress.getId() + " is initialized.");
		}
	};

	Handler<Start> startHandler = new Handler<Start>() {
		public void handle(Start event) {
			System.out.println("Peer " + myAddress.getId() + " is started.");
			
			String topic = "Football";
			sendSubscribeRequest(topic);

			for (int i = 0; i < 1; i++) {
				publish("Basketball", "XYZASD");
			}

			sendUnsubscribeRequest(topic);
			
			/*
			 * SchedulePeriodicTimeout spt = new
			 * SchedulePeriodicTimeout(msgPeriod, msgPeriod);
			 * spt.setTimeoutEvent(new SendMessage(spt)); trigger(spt, timer);
			 */
		}
	};

	// -------------------------------------------------------------------
	// This handler is called periodically, every msgPeriod milliseconds.
	// -------------------------------------------------------------------
	Handler<SendMessage> handleSendMessage = new Handler<SendMessage>() {
		public void handle(SendMessage event) {
			publish("Basketball", "AJDAkj");
		}
	};

	

	Handler<Publication> eventNotificationHandler = new Handler<Publication>() {
		public void handle(Publication msg) {
			// messages++;
			// msg.source.compareTo(new ())
			System.out.println("Peer " + myAddress.getId() + " is received mesage from " + msg.getTopic());
		}
	};
	
	// -------------------------------------------------------------------------
	private void sendSubscribeRequest(String topic) {
		System.out.println("Peer " + myAddress.getId() + " is triggering subscription.");
		SubscribeRequest sub = new SubscribeRequest(topic, myAddress, serverAddress);
		trigger(sub, network);
	}
	
	private void sendUnsubscribeRequest(String topic) {
		System.out.println("Peer " + myAddress.getId() + " is triggering subscription.");
		UnsubscribeRequest unsub = new UnsubscribeRequest(topic, myAddress, serverAddress);
		trigger(unsub, network);
	}
	
	private void publish(String topic, String info) {
		System.out.println("Peer " + myAddress.getId() + " is publishing an event.");
		
		trigger(new Publication(topic, info, myAddress, serverAddress), network);
	}

}
