import se.sics.kompics.Event;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;


public class Publication extends Message{
	
	private final String topic;
	private String info;
	

	
	public Publication(String s, String info, Address src, Address dest) {		
		super( src, dest);
		topic = s;
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
