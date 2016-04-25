package communication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import model.Rover;
import model.RoverQueue;


/* NOTE TO SELF: Still need to implement method to reconnected to a rover if connection is lost.
 * Also make sure to send rover name list of tools and drive type to clients that connect to you.*/
public class RoverServer implements Runnable{

	/* Rover name and listerning port
	 * If this code is shared with a rovers in blue corporation this constant has to be changed
	 * This port number only applies to ROVER_03
	 */
	private final static int PORT = 9000;
	
	public RoverQueue roverQueue;
	
	private Rover rover;
	
	// List of connected rovers
	private List<RoverClient> rovers;

	private ServerSocket serverSocket;

	// Will be used to get input from console
	private InputStreamReader cin;
	private StringBuilder message = new StringBuilder();

	public RoverServer(Rover rover) throws IOException{
		// Creates a server socket at specified port and binds it to a specified port and address of local machine
		serverSocket = new ServerSocket(PORT);
		roverQueue = new RoverQueue();
		this.rover = rover;
		System.out.println(this.rover.getName() + " server online...");
		System.out.println("Waiting for other rovers to connect...");

		/* instatiates input stream reader object that will be used to capture any input written on the console.
		 * since the rovers are autonomous we will not be needing this for final implementation because there will be no
		 * human interaction with the rover but it is needed for testing purposes
		 * */
		cin = new InputStreamReader(System.in);
		
		// Array list that keeps track of connected rovers
		rovers = new ArrayList<>();

		// Begin messaging thread
		sendMessage();
	}

	@Override
	public void run() {	// Thread that continously listens for incoming connections
		while(true){
			try {
				RoverClient client = new RoverClient(serverSocket.accept(), rover.getName(), roverQueue);
				rovers.add(client);
				new Thread(client).start(); // instantiates and starts a new thread for a connecting client
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	// Will be used in ROVER_03 class to check if Rover Queue is empty
	//public void emptyQueue(){
	//	roverqueue
	//}

	/* This method sends messages that are written on the console to all rovers connected
	 * how ever again this thread will not be needed for the final implementation and is only for 
	 * testing purposes */
	public void sendMessage(){
		Thread messages = new Thread(){
			public void run(){
				while(true){
					try {
						if(cin.ready())					// Have to put it in a separate if statement because otherwise it does not work
														// not sure why though
							if(!rovers.isEmpty()){
								while(cin.ready()){
									message.append((char)cin.read());	// appends character by character to string builder
									System.out.println(cin.ready());
								}
								
								// iterates through connected client array list and calls the send message function
								for(RoverClient r : rovers){
									System.out.println(message.toString());
									System.out.println("sending to " + r.getPort() + " message: " + message );
									r.send(message.toString());
								}
								message.setLength(0); // Reset StringBuilder
							}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		messages.start();
	}
	
	/* Rovers receive LOC from server in the following form: "LOC xpos ypos"
	 * If a rover is trying to tell another rover to go get a piece of science at
	 * another location the exact string should be sent to the other rover. The 
	 * receiver of the coordinates will be responsible for extracting the coordinates
	 * not the sender. Therefore argument taken by this method is the exact same string
	 * given by the server when the LOC command is sent along with the index in the 
	 * arraylist of the rover that it is being sent to. Example: LOC 35 65 1 where
	 * LOC 35 65 is the string from the server and 1 is the index of the rover in 
	 * your array list.
	 * */
	public void sendLOC(String location){
		int index = getIndex(location);
		rovers.get(index).send(getLocation(location));
		roverQueue.addLocation(getLocation(location));
		roverQueue.displayLocation();
	}
	
	private int getIndex(String location){
		/* Splits the location string which has the form "LOC 35 65 1" into an array of
		 * four components the last one at index 3 is the index of the rover client in the
		 * array. Trim is used because to make sure you dont get a NumberFormatException when trying
		 * to parse the string into and integer.
		 */
		return Integer.parseInt(location.split(" ")[3].trim());
	}
	
	/* Extracts only the location portion of the message including the LOC so receiver rover can use the 
	 * extractLOC() method given provided Richard 
	 */
	private String getLocation(String location){
		String[] split = location.split(" "); 
		return split[0] + " " + split[1] + " " + split[2];
	}

	// prints list to console of connected rovers
	public void getClientList(){
		System.out.println("****************** Rover List **********************");
		for(RoverClient r : rovers){
			System.out.println("Address: " + r.getIP() + " , Port:" + r.getPort());
		}
	}
}
