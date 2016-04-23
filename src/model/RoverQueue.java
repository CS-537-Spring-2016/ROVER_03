package model;

import java.util.ArrayList;
import java.util.List;

public class RoverQueue {
	List<String> locationList = new ArrayList<String>();
	
	RoverQueue(){
		
	}
	
	public void addLocation (String location){
		locationList.add(location);
	}
	
	
}
