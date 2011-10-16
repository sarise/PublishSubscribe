package centralized.simulator.scenarios;

import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

@SuppressWarnings("serial")
public class Scenario1 extends Scenario {
	
	public static final int NUMBER_OF_PEERS = 3;
	public static final int NUMBER_OF_SUBCRIPTIONS = 3;
	public static final int NUMBER_OF_PUBLICATIONS = 10;
	public static final int NUMBER_OF_UNSUBSCRIPTIONS = 3;
	
	private static SimulationScenario scenario = new SimulationScenario() {{
		StochasticProcess serverstart = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(1, Operations.serverStart, uniform(13));
		}};

		// Joining
		StochasticProcess joining = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(NUMBER_OF_PEERS, Operations.peerJoin, uniform(13));
		}};
		
		// Subscription
		StochasticProcess subscribing = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(NUMBER_OF_SUBCRIPTIONS, Operations.peerSubscribe, uniform(13));
		}};
		
		// Publication
		StochasticProcess publishing = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(NUMBER_OF_PUBLICATIONS, Operations.peerPublish, uniform(13));
		}};
		
		// Unsubscribe 
		StochasticProcess unsubscribing = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(NUMBER_OF_UNSUBSCRIPTIONS, Operations.peerUnsubscribe, uniform(13));
		}};
		

		StochasticProcess termination = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
			raise(NUMBER_OF_PEERS, Operations.peerFail, uniform(13));
		}};

		serverstart.start();
		joining.startAfterTerminationOf(8000, serverstart);
		subscribing.startAfterTerminationOf(5000, joining);
		publishing.startAfterTerminationOf(8000, subscribing);
		unsubscribing.startAfterTerminationOf(10000, subscribing);
		//termination.startAfterTerminationOf(500000, subscribing);
	}};
	
//-------------------------------------------------------------------
	public Scenario1() {
		super(scenario);
	} 
}
