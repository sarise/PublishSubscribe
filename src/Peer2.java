import java.net.InetAddress;
import java.net.UnknownHostException;

import message.Publication;
import message.SubscribeRequest;
import message.UnsubscribeRequest;
import message.Notification;

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

/**
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 6, 2011
 */
public class Peer2 extends ComponentDefinition {

	Positive<Network> network = requires(Network.class);
	private final int msgPeriod = 100;
	private Address myAddress = null;
	private Address serverAddress = null;

	Positive<Timer> timer = positive(Timer.class);

	public Peer2() {
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
			
			String topic = "Basketball";
			sendSubscribeRequest(topic);

			for (int i = 0; i < 1; i++) {
				publish("Football", "XYZASD");
			}

			//sendUnsubscribeRequest(topic);
			
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

	

	Handler<Notification> eventNotificationHandler = new Handler<Notification>() {
		public void handle(Notification msg) {
			System.out.println("Peer " + myAddress.getId() 
					+ " received a notification about " + msg.getTopic());
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