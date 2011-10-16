package centralized.system.peer.message;

import java.util.Random;

public class TopicList {
	
	public static String[] list = {
		"Football",
		"Basketball",
		"Cricket",
		"Crochet"
	};
	
	public static String getRandomTopic() {
		Random rand = new Random();
		return list[rand.nextInt(list.length)];
	}

}
