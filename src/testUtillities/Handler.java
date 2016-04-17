package testUtillities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Handler implements Runnable{
	private PrintWriter out;
	private BufferedReader in;

	public Handler(Socket socket) throws IOException{
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(),true);
	}

	@Override
	public void run() {

		out.println("Request Accepted");
		while(true){
			String message = null;
			try {
				message = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Message: " + message);
		}
	}

}
