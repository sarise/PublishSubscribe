import java.net.InetAddress;

import java.net.UnknownHostException;

import message.Publication;
import message.Subscription;

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

public class ClientComponent extends ComponentDefinition {

	// Positive<MyNetwork> myNetwork = positive(MyNetwork.class);
	Positive<Network> network = requires(Network.class);
	//Negative<Network> myNetwork = negative(Network.class);
	private final int msgPeriod = 100;
	private Address srcAddr = null;
	private Address destAddr = null;
	private int myPort;
	private int myId;
	Component mina;
	// int messages;

	Positive<Timer> timer = positive(Timer.class);

	public ClientComponent() throws InterruptedException {
		myPort = 1234;
		myId = 1;

		// System.out.println("Client Component created.");
		// messages = 0;
		// subscribe(initH, control);
		
		mina = create(MinaNetwork.class); 
		connect(this.negative(Network.class), mina.getPositive(Network.class));
		
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		srcAddr = new Address(inet, myPort, myId);
		destAddr = new Address(inet, 1111, 0);

		subscribe(startHandler, control);
		subscribe(eventNotificationHandler, network);
		// subscribe(messageHandler, network);
		System.out.println("Client subscribed to start.");

	}

	Handler<Event> initH = new Handler<Event>() {
		public void handle(Event init) {
			System.out.println("Client Component initialized.");
		}
	};

	Handler<Start> startHandler = new Handler<Start>() {
		public void handle(Start event) {
			System.out.println("Client Component started.");
			Subscription sub = new Subscription("Football", srcAddr, destAddr);

		
		
			System.out.println("Client triggering event.");
			trigger(sub, network);
			//trigger(sub, mina.getNegative(Network.class));

			for (int i = 0; i < 1; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				publish("Basketball", "XYZASD");
				System.out.println("Client published.");

			}

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

	void publish(String topic, String info) {
		System.out.println("Client is sending a publication.");
		trigger(new Publication(topic, info, srcAddr, destAddr), network);
		System.out.println("Publication sent.");
	}

	Handler<Publication> eventNotificationHandler = new Handler<Publication>() {
		public void handle(Publication msg) {
			// messages++;
			// msg.source.compareTo(new ())
			System.out.println("MyComp received msg from " + msg.getTopic());
		}
	};

}
