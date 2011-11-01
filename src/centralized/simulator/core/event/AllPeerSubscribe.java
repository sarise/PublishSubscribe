package centralized.simulator.core.event;

import java.math.BigInteger;

import se.sics.kompics.Event;

public final class AllPeerSubscribe extends Event {
	private final BigInteger peerId;

//-------------------------------------------------------------------	
	public AllPeerSubscribe(BigInteger peerId) {
		this.peerId = peerId;
	}
	
	//-------------------------------------------------------------------	
	
}
