import se.sics.kompics.Event;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

public class Subscription extends Message {
	
	private final String topic;

	public Subscription(String s, Address src, Address dest) {		
		super( src, dest);
		topic = s;
	}

	public String getTopic() {
		return topic;
	}

}
