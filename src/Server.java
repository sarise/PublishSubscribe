import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import message.Publication;
import message.SubscribeRequest;
import message.UnsubscribeRequest;
import message.Notification;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.mina.MinaNetwork;
import se.sics.kompics.network.mina.MinaNetworkInit;
import se.sics.kompics.address.Address;

public class Server extends ComponentDefinition {

	private Negative<Network> network = negative(Network.class);
	private Hashtable subcriptionRepository = new Hashtable();
	private Vector<Publication> eventRepository = new Vector<Publication>();
	private Address serverAddress;
	
	public Server() {
	
		System.out.println("ServerComponent created.");
		
		subscribe(initHandler, control);		
		subscribe(startHandler, control);
		subscribe(subscribeHandler, network);
		subscribe(unsubscribeHandler, network);
		subscribe(eventPublicationHandler, network);
		System.out.println("Server subscribed to sub.");
	}

	Handler<ServerInit> initHandler = new Handler<ServerInit>() {
		public void handle(ServerInit init) {
			serverAddress = init.getServerAddress();
			
			System.out.println("Server " + serverAddress.getId() + " is initialized.");
		}
	};

	Handler<Start> startHandler = new Handler<Start>() {
		public void handle(Start event) {
			System.out.println("Server Component started.");
		}
	};

	Handler<SubscribeRequest> subscribeHandler = new Handler<SubscribeRequest>() {
		public void handle(SubscribeRequest msg) {
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

	
	Handler<UnsubscribeRequest> unsubscribeHandler = new Handler<UnsubscribeRequest>() {
		public void handle(UnsubscribeRequest msg) {
			System.out.println("Server received UnsubscribeRequest " + msg.getTopic());

			if (subcriptionRepository.containsKey(msg.getTopic())) {
				Vector<Address> subscriberlist = (Vector<Address>) subcriptionRepository.
						get(msg.getTopic());
				subscriberlist.remove(msg.getSource()); // Will this mutate the
														// instant in the object
														// inside?

				subcriptionRepository.remove(msg.getTopic());
				subcriptionRepository.put(msg.getTopic(), subscriberlist);

				System.out.println("Subscriber list for topic id "
						+ msg.getTopic() + " : " + subscriberlist.toString());

			} 
		}
	};

	
	Handler<Publication> eventPublicationHandler = new Handler<Publication>() {
		public void handle(Publication msg) {
			// EVENT REPOSITORY
			System.out.println("Server received publication from "
					+ msg.getTopic() + " " + msg.getInfo());
			eventRepository.add(msg);

			// EVENT NOTIFICATION SERVICE
			Notification notification = new Notification(msg.getTopic(),
					msg.getInfo(), serverAddress, null);
			Vector<Address> subscriberlist = (Vector<Address>) subcriptionRepository
					.get(msg.getTopic());
			System.out.println("subscriberlist: " + subscriberlist.toString());
			if (subscriberlist != null) {
				System.out.println("subscriberlist is not null.");
				for (Enumeration<Address> e = subscriberlist.elements(); e
						.hasMoreElements();) {
					Address subscriber = (Address) e.nextElement();
					notification.setDestination(subscriber);
					System.out.println("Notification: " + notification.getDestination());
					trigger(notification, network);
					System.out.println("Notified " + subscriber);
				}
			}
		}
	};
}
