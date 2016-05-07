package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import model.RoverQueue;

/* NOTE TO SELF: Need to implement method that filters rovers by tool that is needed either to harvets or drill
 * and another method that searches from that filtered list the closest rover to my location */

public class RoverClient implements Runnable{
	private String roverName;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private RoverQueue queue;

	/* This constructor will be used when you want to send a request to establish a connection,
	 * unlike the other constructor in this class you create the socket in the constructor
	 * to try to extablish a connection with another rover using the destination IP and 
	 * destination port.
	 * */
	public RoverClient(String ip, int port, String name, RoverQueue queue) throws IOException{

		this.socket = new Socket(ip, port);
		this.queue = queue;
		roverName = name;
	} 

	/* This constructor will be used when someone sent a request to establish a connection,
	 * the socket argment it takes is the socket that is created by serverSocket.accept()
	 * which extablishes the connection. So instead of trying to establish a connection in
	 * this constructor you are just storing a reference to the socket that was created
	 * by using serverSocket.accept() in the RoverServer class.
	 * */
	public RoverClient(Socket socket, String name, RoverQueue queue) throws IOException{

		this.socket = socket;
		this.queue = queue;
		roverName = name;

	} 

	// Return rover name
	public String getRoverName() {
		return roverName;
	}

	// Send message to other rover
	public void send(String message){
		output.println(message);
	}

	// return IP address for other rover in the form /xxx.xx.xx.x
	public InetAddress getIP(){
		return socket.getInetAddress();
	}

	// returns port number of other rover
	public int getPort(){
		return socket.getPort();
	}

	@Override
	public void run() {	// Thread that will listen for incoming messages
		try {
			// Instatiates buffered reader and print writer which will be used to send and receive messages through this socket
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Sends message to other rover so they know a connection has been extablished
		//send("You are now connected to " + roverName); CAUSES AN ISSUE NEED TO FIX THIS

		while(true){

			try {
				if(input.ready()){
					String loc = input.readLine();
					String parts[] = loc.split(" ");
					if(!parts[0].equals("ROCK")){
						queue.addLocation("LOC " + parts[2] + " " + parts[3]);
						System.out.println("LOC " + parts[2] + " " + parts[3]);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
}

