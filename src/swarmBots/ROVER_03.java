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
import common.ScanMap;

public class ROVER_03{
	
	// Line 16 to 32 don't need to be changed, got these from sample rovers
	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;

	public ROVER_03() {
		System.out.println("ROVER_03 rover object constructed");
		rovername = "ROVER_03";
		SERVER_ADDRESS = "localhost";
		sleepTime = 10000;	// Changed sleep time to decrease number of request sent to server but will have to modify this
	}
	
	private void start() throws IOException, InterruptedException {

		// Make connection and initialize streams
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);


		// Process all messages from server, wait until server requests Rover ID name
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); /* This sets the name of this instance
										 * of a swarmBot for identifying the
										 *thread to the server*/
				break;
			}
		}

		// ******** Rover logic *********
		// We do not need a stuck boolean because we have threads and will not get stuck on anything
		
		String line = "";
		boolean blocked = false;

		String[] cardinals = {"N", "E", "S", "W"};

		String currentDir = null;
		Coord currentLoc = null;
		Coord previousLoc = null;

		// start Rover controller process
		while (true) {		
			
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
			if(!currentLoc.equals(previousLoc)){
			System.out.println("ROVER_03 currentLoc at start: " + currentLoc);
			
			// after getting location set previous equal current to be able to check for stuckness and blocked later
			previousLoc = currentLoc;
				
			// **** get equipment listing ****			
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			//System.out.println("ROVER_03 equipment list results drive " + equipment.get(0));
			System.out.println("ROVER_03 equipment list results " + equipment + "\n");
	
			// ***** do a SCAN *****
			//System.out.println("ROVER_03 sending SCAN request");
			this.doScan();
			scanMap.debugPrintMap();
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
		

		/* this takes the LOC response string, parses out the x and x values and returns a Coord 
		 * object */
		public static Coord extractLOC(String sStr) {
				String[] coordinates = sStr.split(" ");
				return new Coord(Integer.parseInt(coordinates[1].trim()), Integer.parseInt(coordinates[2].trim()));
		}
	
	public static void main(String args[]) throws IOException, InterruptedException{
		ROVER_03 client = new ROVER_03();
		client.start();
	}
}
