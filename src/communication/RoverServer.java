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
	private StringBuilder message = new StringBuilder();

	public RoverServer() throws IOException{
		// Creates a server socket at specified port
		serverSocket = new ServerSocket(PORT);

		System.out.println("ROVER_03 server online...");
		System.out.println("Waiting for other rovers to connect...");

		cin = new InputStreamReader(System.in);
		rovers = new ArrayList<>();

		// Begin messaging thread
		sendMessage();
	}

	@Override
	public void run() {
		while(true){
			try {
				RoverClient client = new RoverClient(serverSocket.accept(), NAME);
				rovers.add(client);
				getClientList();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(){
		Thread messages = new Thread(){
			public void run(){
				while(true){
					//					try {
					//						if(cin.ready()){
					//							System.out.println((char)cin.read());
					//						}
					//					} catch (IOException e1) {
					//						// TODO Auto-generated catch block
					//						e1.printStackTrace();
					//					}

					try {
						//						System.out.println(cin.ready());
						//						Thread.sleep(5000);
						if(cin.ready())
							if(!rovers.isEmpty()){

								while(cin.ready()){
									message.append((char)cin.read());
									System.out.println(cin.ready());
								}

								for(RoverClient r : rovers){
									System.out.println(message.toString());
									System.out.println("sending to " + r.getPort() + " message: " + message );
									r.send(message.toString());
								}
								message.setLength(0); // Reset StringBuilder
							}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		messages.start();
	}

	public void getClientList(){
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
