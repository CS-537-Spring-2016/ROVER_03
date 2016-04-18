package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class RoverClient implements Runnable{
	private Scanner in;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;

	public RoverClient(Socket socket) throws IOException{
		this.socket = socket;
		in = new Scanner(System.in); // Most likely wont need this but just going to use scanner for testing purposes
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);
	} 

	@Override
	public void run() {
		while(true){
			this.send();
			try {
				System.out.println(this.recv());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Will be used to send messages to other rovers
	public void send(String message){
		output.println(message);
	}

	// This will not be used either only for testing purposes
	public void send(){
		System.out.print("Message: ");
		send(in.nextLine());
	}

	public String recv() throws IOException{
		return input.readLine();
	}
}
