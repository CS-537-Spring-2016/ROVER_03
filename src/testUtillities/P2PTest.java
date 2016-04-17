package testUtillities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PTest {
	private final static int PORT = 8000;
	
	@SuppressWarnings("resource")
	public static void main(String args[]) throws IOException{
		PrintWriter out;
		BufferedReader in;
		
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Waiting for clients...");
		Socket socket = serverSocket.accept();	
		System.out.println("Request accepted!");
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(),true);
		out.println("Request Accepted");

		while(true){
			String message = in.readLine();
			System.out.println("Message: " + message);
		}
		
	}
}
