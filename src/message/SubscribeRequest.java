package message;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

public class SubscribeRequest extends Message {
	
	private static final long serialVersionUID = 2876631073644631897L;
	private final String topic;

	public SubscribeRequest(String s, Address src, Address dest) {		
		super( src, dest);
		topic = s;
	}

	public String getTopic() {
		return topic;
	}

}