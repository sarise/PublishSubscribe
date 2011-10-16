package centralized.system.peer.message;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

/**
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 1, 2011
 *
 */
public class UnsubscribeRequest extends Message {
	
	private static final long serialVersionUID = -3963584253785169395L;
	private final String topic;

	public UnsubscribeRequest(String s, Address src, Address dest) {		
		super( src, dest);
		topic = s;
	}

	public String getTopic() {
		return topic;
	}

}
