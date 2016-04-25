package testUtillities;
import java.io.IOException;
import java.net.UnknownHostException;

import communication.RoverClient;

public class Test {

	public static void main(String[] args) throws UnknownHostException, IOException {

		new RoverClient("127.0.0.1", 8000,"ROVER_100",null);
		
		}
}
