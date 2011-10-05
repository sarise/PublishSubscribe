import se.sics.kompics.*;

import se.sics.kompics.network.Network;


/**
 * The <code>Main</code> class.
 * 
 * @author Sari Setianingsih
 * @author Jawad Manzoor
 * Created on Oct 1, 2011
 *
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
