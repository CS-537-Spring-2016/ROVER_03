package communication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class RoverServer implements Runnable{
	
	// Rover name and listerning port
	private final static String NAME = "ROVER_03";
	private final static int PORT = 8000;
	
	// List of connected rovers
	private List<RoverClient> rovers;
	
	
	private ServerSocket serverSocket;
	
	// Will be used to get input from console
	private InputStreamReader cin;
	private StringBuilder message;
	
	public RoverServer() throws IOException{
		// Creates a server socket at specified port
		serverSocket = new ServerSocket(PORT);
		
		System.out.println("ROVER_03 server online...");
		System.out.println("Waiting for other rovers to connect...");
		
		cin = new InputStreamReader(System.in);
		rovers = new ArrayList<>();
	}

	@Override
	public void run() {
		while(true){
			try {
				RoverClient client = new RoverClient(serverSocket.accept(), NAME);
				Thread newThread = new Thread(client);
				rovers.add(client);
				newThread.start();
				getClientList();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public  void getClientList(){
		System.out.println("****************** Rover List **********************");
		for(RoverClient r : rovers){
			System.out.println("Address: " + r.getIP() + " , Port:" + r.getPort());
		}
	}

	public static void main(String args[]) throws IOException{
		Thread newThread = new Thread(new RoverServer());
		newThread.start();
	}
}
