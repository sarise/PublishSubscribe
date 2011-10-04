import java.util.Enumeration;

import java.util.Hashtable;
import java.util.Vector;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.mina.MinaNetwork;
import se.sics.kompics.address.Address;

public class ServerComponent extends ComponentDefinition {

	// private Negative<MyNetwork> network = negative(MyNetwork.class);
	private Positive<Network> network = requires(Network.class);

	private Hashtable subcriptionRepository = new Hashtable();
	private Vector<Publication> eventRepository = new Vector<Publication>();
	Component mina;
	
	public ServerComponent() {
		
		mina = create(MinaNetwork.class); 
		connect(negative(Network.class), mina.getPositive(Network.class));
		System.out.println("ServerComponent created.");
		// messages = 0;
		// subscribe(initH, control);
		
		subscribe(startHandler, control);
		subscribe(subscriptionHandler, network);
		subscribe(eventPublicationHandler, network);
		System.out.println("Server subscribed to sub.");
	}

	/*
	 * Handler<Event> initH = new Handler<Event>() { public void handle(Event
	 * init) { System.out.println("Server Component initialized."); }};
	 */

	Handler<Start> startHandler = new Handler<Start>() {
		public void handle(Start event) {
			System.out.println("Server Component started.");
		}
	};
	/*
	 * Handler<Message> incomingSubscriptionHandler = new Handler<Message>() {
	 * public void handle(Message msg) { //messages++;
	 * System.out.println("Server received subscription "); } };
	 */

	Handler<Subscription> subscriptionHandler = new Handler<Subscription>() {
		public void handle(Subscription msg) {
			// messages++;
			System.out
					.println("Server received subscription " + msg.getTopic());

			if (subcriptionRepository.containsKey(msg.getTopic())) {
				Vector<Address> subscriberlist = (Vector<Address>) subcriptionRepository
						.get(msg.getTopic());
				subscriberlist.add(msg.getSource()); // Will this mutate the
														// instant in the object
														// inside?

				subcriptionRepository.remove(msg.getTopic());
				subcriptionRepository.put(msg.getTopic(), subscriberlist);

				System.out.println("Subscriber list for topic id "
						+ msg.getTopic() + " : " + subscriberlist.toString());

			} else {
				Vector<Address> subscriberlist = new Vector<Address>();
				subscriberlist.add(msg.getSource());
				System.out.println("Address source: " + msg.getSource());
				subcriptionRepository.put(msg.getTopic(), subscriberlist);

				System.out.println("Subscriber list for topic id "
						+ msg.getTopic() + " : " + subscriberlist.toString());
			}
		}
	};

	Handler<Publication> eventPublicationHandler = new Handler<Publication>() {
		public void handle(Publication msg) {
			// messages++;
			System.out.println("Server received publication from "
					+ msg.getTopic() + " " + msg.getInfo());
			eventRepository.add(msg);

			// Event Notification Service
			Vector<Address> subscriberlist = (Vector<Address>) subcriptionRepository
					.get(msg.getTopic());
			if (subscriberlist != null) {
				for (Enumeration<Address> e = subscriberlist.elements(); e
						.hasMoreElements();) {
					Address subscriber = (Address) e.nextElement();
					// msg.destination = subscriber;
					trigger(msg, network);
					System.out.println("Notified " + e);
				}
			}
		}
	};
}
