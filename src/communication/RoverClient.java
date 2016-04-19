package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


// NOTE: Might need to send request back to client...
public class RoverClient{
	private String roverName;
	//private Scanner in;
	private Socket socket;
	private static PrintWriter output;
	private BufferedReader input;


	public RoverClient(String ip, int port, String name) throws IOException{
		this.socket = new Socket(ip, port);
		roverName = name;
		//in = new Scanner(System.in); // Most likely wont need this but just going to use scanner for testing purposes
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);
		
		Thread read = new Thread(){
			public void run() {
				while(true){
					try {
						if(input.ready())
							System.out.println(input.readLine());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		};
		
		read.start();
	} 

	public RoverClient(Socket socket, String name) throws IOException{
		this.socket = socket;
		roverName = name;
		//in = new Scanner(System.in); // Most likely wont need this but just going to use scanner for testing purposes
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);

		Thread read = new Thread(){
			public void run() {
				while(true){
					try {
						if(input.ready())
							System.out.println(input.readLine());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		};
		
		read.start();
	} 


	public String getRoverName() {
		return roverName;
	}

	// Will be used to send messages to other rovers
	public void send(String message){
		output.println(message);
	}


//	public void recv() throws IOException{
//		if(input.ready())
//			System.out.println(input.readLine());
//	}

	public InetAddress getIP(){
		return socket.getInetAddress();
	}

	public int getPort(){
		return socket.getPort();
	}
}

