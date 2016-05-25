package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import model.Rover;
import model.RoverQueue;


public class RoverServer implements Runnable{

	/* Rover name and listening port
	 * This port number only applies to ROVER_03
	 */
	private final static int PORT = 53703;
	
	private RoverQueue roverQueue;
	private Rover rover;
	
	// List of connected rover
	private List<RoverClient> rovers;

	private ServerSocket serverSocket;

	public RoverServer(Rover rover) throws IOException{
		// Creates a server socket at specified port and binds it to a specified port and address of local machine
		serverSocket = new ServerSocket(PORT);
		roverQueue = new RoverQueue();
		this.rover = rover;
		System.out.println(this.rover.getName() + " server online...");
		System.out.println("Waiting for other rovers to connect...");
		
		// Array list that keeps track of connected rover
		rovers = new ArrayList<>();
	}

	@Override
	public void run() {	// Thread that continuously listens for incoming connections
		while(true){
			try {
				Socket socket = serverSocket.accept();
				RoverClient client = new RoverClient(socket, roverQueue);
				rovers.add(client);
				new Thread(client).start(); // instantiates and starts a new thread for a connecting client
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public RoverQueue getQueue(){
		return roverQueue;
	}

}
