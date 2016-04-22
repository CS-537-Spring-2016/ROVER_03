package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
<<<<<<< HEAD
import common.ScanMap;

/*
 * This is just an idea for the moment but ROVER_03 might have to implement
 * runnable in order to allow for multithreading. The reason we might need this 
 * is because in order to communicate with the other rovers in blue corporation 
 * we will most likely need to create a P2P network connection between the rovers.
 * */
public class ROVER_03 implements Runnable {
	
	// Line 16 to 32 don't need to be changed, got these from sample rovers
=======
import common.MapTile;
import common.ScanMap;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_03 {

>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;

	public ROVER_03() {
<<<<<<< HEAD
		System.out.println("ROVER_03 rover object constructed");
		rovername = "ROVER_03";
		SERVER_ADDRESS = "localhost";
		sleepTime = 10000;	// Changed sleep time to decrease number of request sent to server but will have to modify this
	}
	
	private void start() throws IOException, InterruptedException {

		// Make connection and initialize streams
=======
		// constructor
		System.out.println("ROVER_03 rover object constructed");
		rovername = "ROVER_03";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server will cut connection if it is too small
	}
	
	public ROVER_03(String serverAddress) {
		// constructor
		System.out.println("ROVER_03 rover object constructed");
		rovername = "ROVER_03";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 200; // in milliseconds - smaller is faster, but the server will cut connection if it is too small
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		// Make connection and initialize streams
		//TODO - need to close this socket
>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

<<<<<<< HEAD

		// Process all messages from server, wait until server requests Rover ID name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); /* This sets the name of this instance
										 * of a swarmBot for identifying the
										 *thread to the server*/
=======
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// Process all messages from server, wait until server requests Rover ID
		// name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); // This sets the name of this instance
										// of a swarmBot for identifying the
										// thread to the server
>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
				break;
			}
		}

		// ******** Rover logic *********
<<<<<<< HEAD
		// We do not need a stuck boolean because we have threads and will not get stuck on anything
		
		String line = "";
		boolean blocked = false;

		String[] cardinals = {"N", "E", "S", "W"};

		String currentDir = null;
		Coord currentLoc = null;
		Coord previousLoc = null;

		// start Rover controller process
		while (true) {		
=======
		// int cnt=0;
		String line = "";

		int counter = 0;
		
		boolean goingSouth = false;
		boolean goingEast = false;
		
		boolean stuck = false; // just means it did not change locations between requests,
								// could be velocity limit or obstruction etc.
		boolean blocked = false;

		String[] cardinals = new String[4];
		cardinals[0] = "N";
		cardinals[1] = "E";
		cardinals[2] = "S";
		cardinals[3] = "W";

		String currentDir = cardinals[0];
		Coord currentLoc = null;
		Coord previousLoc = null;
		

		// start Rover controller process
		while (true) {

			// currently the requirements allow sensor calls to be made with no
			// simulated resource cost
			
>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
			
			// **** location call ****
			out.println("LOC");
			line = in.readLine();
            if (line == null) {
            	System.out.println("ROVER_03 check connection to server");
            	line = "";
            }
			if (line.startsWith("LOC")) {
				// loc = line.substring(4);
				currentLoc = extractLOC(line);
			}
<<<<<<< HEAD
			if(!currentLoc.equals(previousLoc)){
=======
>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
			System.out.println("ROVER_03 currentLoc at start: " + currentLoc);
			
			// after getting location set previous equal current to be able to check for stuckness and blocked later
			previousLoc = currentLoc;
<<<<<<< HEAD
				
=======
			
			
			
>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
			// **** get equipment listing ****			
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			//System.out.println("ROVER_03 equipment list results drive " + equipment.get(0));
			System.out.println("ROVER_03 equipment list results " + equipment + "\n");
<<<<<<< HEAD
	
=======
			
	

>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
			// ***** do a SCAN *****
			//System.out.println("ROVER_03 sending SCAN request");
			this.doScan();
			scanMap.debugPrintMap();
<<<<<<< HEAD
			System.out.println("ROVER_03 ------------ bottom process control --------------"); 
			}

			Thread.sleep(sleepTime); // We need to have thread sleep until signal is received from another rover ****
		}
		


	}
	
	// ################ Support Methods ###########################
	
		private void clearReadLineBuffer() throws IOException{
			while(in.ready()){
				//System.out.println("ROVER_03 clearing readLine()");
				String garbage = in.readLine();	
			}
		}
		

		// method to retrieve a list of the rover's equipment from the server
		private ArrayList<String> getEquipment() throws IOException {
			//System.out.println("ROVER_03 method getEquipment()");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.println("EQUIPMENT");
			
			String jsonEqListIn = in.readLine(); //grabs the string that was returned first
			if(jsonEqListIn == null){
				jsonEqListIn = "";
			}
			StringBuilder jsonEqList = new StringBuilder();
			//System.out.println("ROVER_03 incomming EQUIPMENT result - first readline: " + jsonEqListIn);
			
			if(jsonEqListIn.startsWith("EQUIPMENT")){
				while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
					if(jsonEqListIn == null){
						break;
					}
					//System.out.println("ROVER_03 incomming EQUIPMENT result: " + jsonEqListIn);
					jsonEqList.append(jsonEqListIn);
					jsonEqList.append("\n");
					//System.out.println("ROVER_03 doScan() bottom of while");
				}
			} else {
				// in case the server call gives unexpected results
				clearReadLineBuffer();
				return null; // server response did not start with "EQUIPMENT"
			}
			
			String jsonEqListString = jsonEqList.toString();		
			ArrayList<String> returnList;		
			returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>(){}.getType());		
			//System.out.println("ROVER_03 returnList " + returnList);
			
			return returnList;
		}
		

		// sends a SCAN request to the server and puts the result in the scanMap array
		public void doScan() throws IOException {
			//System.out.println("ROVER_03 method doScan()");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.println("SCAN");

			String jsonScanMapIn = in.readLine(); //grabs the string that was returned first
			if(jsonScanMapIn == null){
				System.out.println("ROVER_03 check connection to server");
				jsonScanMapIn = "";
			}
			StringBuilder jsonScanMap = new StringBuilder();
			System.out.println("ROVER_03 incomming SCAN result - first readline: " + jsonScanMapIn);
			
			if(jsonScanMapIn.startsWith("SCAN")){	
				while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
					//System.out.println("ROVER_03 incomming SCAN result: " + jsonScanMapIn);
					jsonScanMap.append(jsonScanMapIn);
					jsonScanMap.append("\n");
					//System.out.println("ROVER_03 doScan() bottom of while");
				}
			} else {
				// in case the server call gives unexpected results
				clearReadLineBuffer();
				return; // server response did not start with "SCAN"
			}
			//System.out.println("ROVER_03 finished scan while");

			String jsonScanMapString = jsonScanMap.toString();
			// debug print json object to a file
			//new MyWriter( jsonScanMapString, 0);  //gives a strange result - prints the \n instead of newline character in the file

			//System.out.println("ROVER_03 convert from json back to ScanMap class");
			// convert from the json string back to a ScanMap object
			scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);		
		}
		

		// this takes the LOC response string, parses out the x and x values and
		// returns a Coord object
		public static Coord extractLOC(String sStr) {
			sStr = sStr.substring(4);
			if (sStr.lastIndexOf(" ") != -1) {
				String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
				//System.out.println("extracted xStr " + xStr);

				String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
				//System.out.println("extracted yStr " + yStr);
				return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
			}
			return null;
		}
	
	public static void main(String args[]) throws IOException, InterruptedException{
		ROVER_03 client = new ROVER_03();
		client.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
=======
			
			
			

			
			// MOVING

			// try moving east 5 block if blocked
			if (blocked) {
				for (int i = 0; i < 5; i++) {
					out.println("MOVE S");
					//System.out.println("ROVER_00 request move E");
					Thread.sleep(1100);
				}
				blocked = false;
					//reverses direction after being blocked
					goingEast = !goingEast;
				
			} else {


				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();
				int centerIndex = (scanMap.getEdgeSize() - 1)/2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				if (goingEast) {
					// check scanMap to see if path is blocked to the south
					// (scanMap may be old data by now)
					if (scanMapTiles[centerIndex][centerIndex +1].getHasRover() 
							|| scanMapTiles[centerIndex +1][centerIndex].getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex +1][centerIndex].getTerrain() == Terrain.NONE) {
						blocked = true;
					} else {
						// request to server to move
						out.println("MOVE E");
						System.out.println("ROVER_03 request move E");
					}
					
				} else {
					// check scanMap to see if path is blocked to the north
					// (scanMap may be old data by now)
					System.out.println("ROVER_03 scanMapTiles[2][1].getHasRover() " + scanMapTiles[2][1].getHasRover());
					System.out.println("ROVER_03 scanMapTiles[2][1].getTerrain() " + scanMapTiles[2][1].getTerrain().toString());
					
					if (scanMapTiles[centerIndex][centerIndex -1].getHasRover() 
							|| scanMapTiles[centerIndex -1][centerIndex].getTerrain() == Terrain.ROCK
							|| scanMapTiles[centerIndex -1][centerIndex].getTerrain() == Terrain.NONE) {
						blocked = true;
					} else {
						// request to server to move
						out.println("MOVE W");
						System.out.println("ROVER_03 request move W");
					}
					
				}

			}

			// another call for current location
			out.println("LOC");
			line = in.readLine();
			if (line.startsWith("LOC")) {
				currentLoc = extractLOC(line);
			}

			System.out.println("ROVER_03 currentLoc after recheck: " + currentLoc);
			System.out.println("ROVER_03 previousLoc: " + previousLoc);

			// test for stuckness
			stuck = currentLoc.equals(previousLoc);

			System.out.println("ROVER_03 stuck test " + stuck);
			System.out.println("ROVER_03 blocked test " + blocked);

			
			Thread.sleep(sleepTime);
			
			System.out.println("ROVER_03 ------------ bottom process control --------------"); 

		}

	}

	// ################ Support Methods ###########################
	
	private void clearReadLineBuffer() throws IOException{
		while(in.ready()){
			//System.out.println("ROVER_03 clearing readLine()");
			String garbage = in.readLine();	
		}
	}
	

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		//System.out.println("ROVER_03 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");
		
		String jsonEqListIn = in.readLine(); //grabs the string that was returned first
		if(jsonEqListIn == null){
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		//System.out.println("ROVER_03 incomming EQUIPMENT result - first readline: " + jsonEqListIn);
		
		if(jsonEqListIn.startsWith("EQUIPMENT")){
			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
				if(jsonEqListIn == null){
					break;
				}
				//System.out.println("ROVER_03 incomming EQUIPMENT result: " + jsonEqListIn);
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
				//System.out.println("ROVER_03 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}
		
		String jsonEqListString = jsonEqList.toString();		
		ArrayList<String> returnList;		
		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>(){}.getType());		
		//System.out.println("ROVER_03 returnList " + returnList);
		
		return returnList;
	}
	

	// sends a SCAN request to the server and puts the result in the scanMap array
	public void doScan() throws IOException {
		//System.out.println("ROVER_03 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); //grabs the string that was returned first
		if(jsonScanMapIn == null){
			System.out.println("ROVER_03 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_03 incomming SCAN result - first readline: " + jsonScanMapIn);
		
		if(jsonScanMapIn.startsWith("SCAN")){	
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				//System.out.println("ROVER_03 incomming SCAN result: " + jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				//System.out.println("ROVER_03 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		//System.out.println("ROVER_03 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file
		//new MyWriter( jsonScanMapString, 0);  //gives a strange result - prints the \n instead of newline character in the file

		//System.out.println("ROVER_03 convert from json back to ScanMap class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);		
	}
	

	// this takes the LOC response string, parses out the x and x values and
	// returns a Coord object
	public static Coord extractLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			//System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			//System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}
	
	

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_03 client = new ROVER_03();
		client.run();
	}
}
>>>>>>> e0f90a14f65bc292e7f1b8d224fb07d17747a386
