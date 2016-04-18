package testUtillities;

import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleBufferTest {
    public static void main (String args[]) throws IOException, InterruptedException {
    	InputStreamReader cin = new InputStreamReader(System.in);
    	while(true){
    	if(cin.ready()){
    		System.out.print((char)cin.read());
    	}
    	}
    }
}
