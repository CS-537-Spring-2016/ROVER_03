package communication;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class RoverServer implements Runnable{

	private List<RoverClient> rovers;
	private final static String NAME = "ROVER_03";
	private final static int PORT = 8000;
	private ServerSocket serverSocket;
	private InputStreamReader cin;
	public RoverServer() throws IOException{
		// Creates a server socket
		serverSocket = new ServerSocket(PORT);
		System.out.println("ROVER_03 server online...");
		System.out.println("Waiting for other rovers to connect...");
		//InputStreamReader to read from console
		cin = new InputStreamReader(System.in);
		rovers = new ArrayList<>();

		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					while(true){
						Thread.sleep(4000);
						if(!rovers.isEmpty()){
							System.out.println(rovers.size());// checking if they are actually connecting
							while(cin.ready())
								for(RoverClient r:rovers){
									r.send((char)cin.read()+"");
								}
						}

					}
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void run() {
		while(true){
			try {
				RoverClient client = new RoverClient(serverSocket.accept(), NAME);
				Thread newThread = new Thread(client);
				rovers.add(client);
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
