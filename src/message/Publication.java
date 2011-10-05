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
public class Publication extends Message{
	
	private static final long serialVersionUID = 6781177817829311117L;
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
