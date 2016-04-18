package communication;

import java.io.IOException;
import java.net.ServerSocket;

public class RoverServer implements Runnable{
	
	private final static String NAME = "ROVER_03";
	private final static int PORT = 8000;
	private ServerSocket serverSocket;
	
	public RoverServer() throws IOException{
		// Creates a server socket
		serverSocket = new ServerSocket(PORT);
		System.out.println("ROVER_03 server online...");
		System.out.println("Waiting for other rovers to connect...");
	}

	@Override
	public void run() {
		while(true){
			try {
				Thread newThread = new Thread(new RoverClient(serverSocket.accept(), NAME));
				newThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public static void main(String args[]) throws IOException{
		Thread newThread = new Thread(new RoverServer());
		newThread.start();
	}
}
