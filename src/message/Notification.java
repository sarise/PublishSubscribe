package message;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

/**
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 1, 2011
 *
 */
public class Notification extends Message{
	
	private static final long serialVersionUID = -9199390927629685995L;
	private final String topic;
	private String info;
	
	// flag unread??
	
	public Notification(String topic, String info, Address src, Address dest) {		
		super(src, dest);
		this.topic = topic;
		this.info = info;
	}

	
	public void setInfo(String s) {
		info = s;
	}

	public String getTopic() {
		return topic;
	}

	public String getInfo() {
		return info;
	}
	
	
	public String toString() {
		return "{" + topic + "|" + info + "|" + this.getDestination().toString() + "}";
	}
}
