package centralized.simulator.scenarios;

import java.io.FileInputStream;
import java.util.Properties;

import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

@SuppressWarnings("serial")
public class Scenario1 extends Scenario {
	
	public static  int NUMBER_OF_PEERS;
	public static  int NUMBER_OF_SUBCRIPTIONS;
	public static  int NUMBER_OF_PUBLICATIONS;
	public static  int NUMBER_OF_UNSUBSCRIPTIONS;
	public static int NUMBER_OF_BITS;
	public static String subscriptionsModel;
	public static String publicationsModel;
	public static int PUBLICATION_INTERVAL;
	
	
	public static Properties configFile = new Properties();
	
	private static SimulationScenario scenario = new SimulationScenario() {{
		
		try {
			//configFile.load(this.getClass().getClassLoader().getResourceAsStream("simulation.properties"));
			configFile.load(new FileInputStream("config/simulation.properties"));
		} catch (Exception e) {
			System.err.println("Error: couldn't load the properties file in Scenario1.java");
		}
		//Integer.parseInt("0");
		
		NUMBER_OF_PEERS = Integer.parseInt(configFile.getProperty("NumberOfNodes"));
		NUMBER_OF_SUBCRIPTIONS = Integer.parseInt(configFile.getProperty("NumberOfSubscriptions"));
		NUMBER_OF_PUBLICATIONS = Integer.parseInt(configFile.getProperty("NumberOfPublication"));
		NUMBER_OF_UNSUBSCRIPTIONS = Integer.parseInt(configFile.getProperty("NumberOfUnsubscriptions"));
		
		subscriptionsModel = configFile.getProperty("SubscriptionsModel");
		publicationsModel = configFile.getProperty("PublicationsModel");
		
		NUMBER_OF_BITS = Integer.parseInt(configFile.getProperty("NumberOfBits"));
		PUBLICATION_INTERVAL = Integer.parseInt(configFile.getProperty("PublicationsInterval"));
			
		StochasticProcess serverstart = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(1, Operations.serverStart, uniform(NUMBER_OF_BITS));
		}};

		// Joining
		StochasticProcess joining = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(NUMBER_OF_PEERS, Operations.peerJoin, uniform(NUMBER_OF_BITS));
		}};
		
		StochasticProcess subscribing = null;
		if (subscriptionsModel.equals("random")) {
			// Subscription
			subscribing = new StochasticProcess() {{
				eventInterArrivalTime(constant(100));
				raise(NUMBER_OF_SUBCRIPTIONS, Operations.peerSubscribe, uniform(NUMBER_OF_BITS));
			}};
		} 
		else if (subscriptionsModel.equals("correlated")) { 
			// Subscription
			subscribing = new StochasticProcess() {{
				eventInterArrivalTime(constant(100));
				raise(1, Operations.allPeerSubscribe);
			}};
		}
		
		// Publication
		StochasticProcess publishing = new StochasticProcess() {{
			eventInterArrivalTime(constant(PUBLICATION_INTERVAL));
			raise(NUMBER_OF_PUBLICATIONS, Operations.peerPublish, uniform(NUMBER_OF_BITS));
		}};
		
		// Unsubscribe 
		StochasticProcess unsubscribing = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(NUMBER_OF_UNSUBSCRIPTIONS, Operations.peerUnsubscribe, uniform(NUMBER_OF_BITS));
		}};
		

		StochasticProcess termination = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
			raise(NUMBER_OF_PEERS, Operations.peerFail, uniform(NUMBER_OF_BITS));
		}};

		serverstart.start();
		joining.startAfterTerminationOf(8000, serverstart);
		subscribing.startAfterTerminationOf(5000, joining);
		publishing.startAfterTerminationOf(8000, subscribing);
		unsubscribing.startAfterTerminationOf(5000, publishing); 
		// TODO: ask Amir why starting the unsubcribing process after 
		// the subscribing process will make the execution stops without no clear reason.
		// 
		
		//termination.startAfterTerminationOf(500000, subscribing);
	}};
	
//-------------------------------------------------------------------
	public Scenario1() {
		super(scenario);
	} 
}
