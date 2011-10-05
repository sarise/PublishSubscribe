import se.sics.kompics.*;

import se.sics.kompics.network.Network;


/**
 * How to set my own port? How to send to one node?
 */
public class Main extends ComponentDefinition {

	public Main() {
		Simulator sim = new Simulator();
		sim.run();
	}

	public static void main(String[] args) {
		Kompics.createAndStart(Main.class);
		Kompics.shutdown();
	}

}
