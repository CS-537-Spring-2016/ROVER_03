package model;

import java.util.ArrayList;
import java.util.List;

public class RoverQueue {
	List<String> locationList = new ArrayList<String>();
	
	public RoverQueue(){
		
	}
	
	public void addLocation (String location){
		locationList.add(location);
	}
	
	//set CurrentLocation method
	
	//traverse the list for nearer locations and set as target location
	
	public void displayLocation(){
		for(String str: locationList)
		{
			System.out.println("Location from Rover:"+str);
		}
		
	}
	
}
