package testUtillities;

import java.io.IOException;

import communication.RoverClient;

public class Test3 {
	public static void main(String args[]) throws IOException{
	new RoverClient("127.0.0.1",8000, "ROVER_200", null);
	}
}