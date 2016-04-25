package model;

import java.util.ArrayList;
import java.util.List;

public class RoverQueue {
	List<String> locationList = new ArrayList<String>();
	
	public RoverQueue(){
		//locationList.add("LOC 45 20");
	}
	
	public void addLocation (String location){
		locationList.add(location);
	}
	public void displayLocation(){
		for(String str: locationList)
		{
			System.out.println(str);
		}
		
	}
	
	public boolean isEmpty(){
		return locationList.isEmpty();
	}
	
	public void removeCompletedJob(){
		locationList.remove(0);
	}
	
	public String getJob(){
		return locationList.get(0);
	}
	
}
