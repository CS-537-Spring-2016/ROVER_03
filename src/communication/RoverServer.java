package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import common.Coord;
import model.Rover;
import model.RoverQueue;
import movement.Coordinate;
import tasks.Task;


public class RoverServer implements Runnable{

	/* Rover name and listening port
	 * This port number only applies to ROVER_03
	 */
	private final static int PORT = 53703;
	
	private RoverQueue roverQueue;
	private Rover rover;

	private ServerSocket serverSocket;

	public RoverServer(Rover rover) throws IOException{
		// Creates a server socket at specified port and binds it to a specified port and address of local machine
		serverSocket = new ServerSocket(PORT);
		roverQueue = new RoverQueue();
		this.rover = rover;
		System.out.println(this.rover.getName() + " server online...");
		System.out.println("Waiting for other rovers to connect...");
	}

	@Override
	public void run() {	// Thread that continuously listens for incoming connections
		
		/* I do not need to create a thread when I receive a connection request because a new connection request is send from other rover
		 * when a message needs to be sent */
		while(true){
			try {
				Socket socket = serverSocket.accept();
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				if(input.ready()){
					String loc = input.readLine();
					String parsedInput[] = loc.split(" ");
					if(!parsedInput[0].equals("ROCK")){
						Coordinate destination = new Coordinate(Integer.parseInt(parsedInput[2]),Integer.parseInt(parsedInput[3]), Coordinate.TYPE.ABSOLUTE);
						Task task = new Task("ROVER", parsedInput[0],parsedInput[1],destination);
						roverQueue.addTask(task);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public RoverQueue getQueue(){
		return roverQueue;
	}

}
