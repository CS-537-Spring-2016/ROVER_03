package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class RoverClient implements Runnable{
	private String roverName;
	private Scanner in;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;

	public RoverClient(Socket socket, String name) throws IOException{
		this.socket = socket;
		roverName = name;
		in = new Scanner(System.in); // Most likely wont need this but just going to use scanner for testing purposes
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);
	} 

	@Override
	public void run() {
		//while(true){
			new Thread(new Runnable(){
				@Override
				public void run() {
					while(true)
					send();	
				}

				// Will be used to send messages to other rovers
				public void send(String message){
					output.println(message);
				}

				// This will not be used either only for testing purposes
				public void send(){
					send(roverName + ": " + in.nextLine());
				}
			}).start();

			new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						while(true)
						recv();
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}

				public void recv() throws IOException{
					String message;
					if((message = input.readLine()) != null)
						System.out.println(message);
				}
			}).start();

		//}
	}

}

