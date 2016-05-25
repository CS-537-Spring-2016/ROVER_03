package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import common.Coord;
import model.RoverQueue;
import tasks.Task;


public class RoverClient implements Runnable{
	private String roverName;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private RoverQueue queue;

	/* This constructor will be used when someone sent a request to establish a connection,
	 * the socket argment it takes is the socket that is created by serverSocket.accept()
	 * which extablishes the connection. So instead of trying to establish a connection in
	 * this constructor you are just storing a reference to the socket that was created
	 * by using serverSocket.accept() in the RoverServer class.
	 * */
	public RoverClient(Socket socket, RoverQueue queue) throws IOException{

		this.socket = socket;
		this.queue = queue;
		roverName = "ROVER_" + (getPort() + "").substring(3);     /* Rover number are the last two digits of the port */ 
		System.out.println("CONNECTION HAS BEEN ESTABLISHED WITH " + roverName);
	} 

	// Send message to other rover
	public void send(String message){
		output.println(message);
	}

	@Override
	public void run() {	// Thread that will listen for incoming messages
		try {
			// Instantiates buffered reader and print writer which will be used to send and receive messages through this socket
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while(true){

			try {
				if(input.ready()){
					String loc = input.readLine();
					String parsedInput[] = loc.split(" ");
					if(!parsedInput[0].equals("ROCK")){
						Task task = new Task(roverName, parsedInput[0],parsedInput[1],new Coord(Integer.parseInt(parsedInput[2]),Integer.parseInt(parsedInput[3])));
						queue.addTask(task);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	
	
	/*-------------------------------------------------------------- GETTERS ------------------------------------------------------------*/ 
		
	/* Return IP address for other rover in the form /xxx.xx.xx.x */
	private InetAddress getIP(){
		return socket.getInetAddress();
	}

	/* Returns port number of other rover */
	private int getPort(){
		return socket.getPort();
	}
}

