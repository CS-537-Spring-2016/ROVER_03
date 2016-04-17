package testUtillities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PTest {
	private final static int PORT = 8000;
	
	@SuppressWarnings("resource")
	public static void main(String args[]) throws IOException{
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Waiting for handler");
		while(true){
			Socket socket = serverSocket.accept();	
			System.out.println("Request accepted!");
			Thread newThread = new Thread(new Handler(socket));
			newThread.start();
		}
		
	}
}
