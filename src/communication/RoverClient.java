package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


// NOTE: Might need to send request back to client...
public class RoverClient implements Runnable{
	private String roverName;
	private Scanner in;
	private Socket socket;
	private static PrintWriter output;
	private BufferedReader input;

	
	public RoverClient(String ip, int port, String name) throws IOException{
		this.socket = new Socket(ip, port);
		roverName = name;
		in = new Scanner(System.in); // Most likely wont need this but just going to use scanner for testing purposes
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);
	} 
	
	public RoverClient(Socket socket, String name) throws IOException{
		this.socket = socket;
		roverName = name;
		in = new Scanner(System.in); // Most likely wont need this but just going to use scanner for testing purposes
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);
	} 

	@Override
	public void run() {
		while(true){
			try {
				this.recv();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public String getRoverName() {
		return roverName;
	}

	// Will be used to send messages to other rovers
	public void send(String message){
		output.println(message);
	}

	// This will not be used either only for testing purposes
	public void send(){
		send(roverName + ": " + in.nextLine());
	}


	public void recv() throws IOException{
		if(input.ready())
			System.out.println(input.readLine());
	}
	
	public void connect(String ip, int port){
		
	}

}

