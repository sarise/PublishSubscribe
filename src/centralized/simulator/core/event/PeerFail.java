package centralized.simulator.core.event;

import java.math.BigInteger;

import se.sics.kompics.Event;

public final class PeerFail extends Event {

	private final BigInteger peerId;

//-------------------------------------------------------------------	
	public PeerFail(BigInteger peerId) {
		this.peerId = peerId;
	}

//-------------------------------------------------------------------	
	public BigInteger getPeerId() {
		return peerId;
	}
}
