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

/**
 * This class is used for communication between rovers. Originally it had been set up to keep connections
 * to other rovers alive. However blue corporation decided it was better to close connection immediately after
 * sending a string. Therefore it closes sockets and input ports immediately. This rover server class is built only
 * to receive communications from others not to send. No output stream is used. 
 * @author Carlos Galdamez
 */
public class RoverServer implements Runnable{

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
		
		while(true){
			try {
				Socket socket = serverSocket.accept();
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				if(input.ready()){
					String loc = input.readLine();
					String parsedInput[] = loc.split(" ");
					/* I will receive string in the following format : ROCK RADIOACTIVE 2 3
					 * The last two numbers are the coordinates x and y respectively first word is the terrain and
					 * second word is the type of science */
					if(!parsedInput[0].equals("ROCK")){ /*Since I have threads if I receive anything with ROCK in it I will just ignore it */
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
	
	/**
	 * Gets rover queue
	 * @return rover queue object
	 */
	public RoverQueue getQueue(){
		return roverQueue;
	}

}
