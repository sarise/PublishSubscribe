import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;


public class Notification extends Message{
	
	private final String topic;
	private String info;
	
	
	
	public Notification(String topic, String info, Address src, Address dest) {		
		super( src, dest);
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
}
