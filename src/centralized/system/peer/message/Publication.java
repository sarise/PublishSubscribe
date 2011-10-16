package centralized.system.peer.message;
import centralized.system.peer.PeerAddress;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

/**
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 1, 2011
 *
 */
public class Publication extends Message{
	

	private static final long serialVersionUID = 6781177817829311117L;
	private final String topic;
	private String content;
	private long seqNumber;

	
	public Publication(String topic, String content, Address source, Address destination, long seqNumber) {		
		super(source, destination);
		this.topic = topic;
		this.content = content;
		this.seqNumber = seqNumber;
	}

	
public void setInfo(String s) {
		
		content = s;
	}

	public String getTopic() {
		return topic;
	}

	public String getInfo() {
		return content;
	}
}
