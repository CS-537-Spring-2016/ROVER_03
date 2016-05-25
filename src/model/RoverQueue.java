package model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import common.Coord;
import tasks.Task;

public class RoverQueue {
	
	private HashSet<Task> tasks;
//	private Double xPosition;
//	private Double yPosition;
	private Task closestTask = null;
	 
	
	public RoverQueue(){
		//closestPoint = new Point2D.Double();
		tasks = new HashSet<Task>();  // Changed this to task instead
		
		// These are hard coded cordinates but will not be there for final implementation..will be receiving these from other rovers or server target location

//		addLocation("LOC 27 7");
//		addLocation("LOC 34 4");
//		addLocation("LOC 41 4");
//		addLocation("LOC 53 2");
//		addLocation("LOC 71 4");
//		addLocation("LOC 69 13");
//		addLocation("LOC 73 17");
//		addLocation("LOC 90 16");
	}
	
	public void addTask (Task task){
//		xPosition = (double) location.xpos;
//		yPosition = (double) location.ypos;
//		Point2D point = new Point2D.Double(xPosition, yPosition);
		tasks.add(task);
		
//		//Display all the location in the Rover Queue
//		Iterator<Point2D> iterator = positionList.iterator();
//		while(iterator.hasNext())
//		{
//			System.out.println("The location in the Rover Queue" + iterator.next());
//		}
			
	}
	
	//finds the closest location from the current location
	public Task closestTask(Coord currentLocation)
	{
//		Double currentXPosition = (double) currentLocation.xpos;
//		Double currentYPosition = (double) currentLocation.ypos;;
		Point2D point = new Point2D.Double(currentLocation.xpos, currentLocation.ypos);
		
//		ArrayList<Double> closestArray = new ArrayList<Double>();
//		ArrayList<Point2D> PointArray = new ArrayList<Point2D>();
//		Double closest = 10000.0;
		
		Double closest = Double.POSITIVE_INFINITY; 
		
		for(Task t: tasks){
			Point2D pt = new Point2D.Double(t.getDestination().xpos, t.getDestination().ypos);
			Double distanceFromCurr = point.distance(pt);
			if(distanceFromCurr < closest) {
				closest = distanceFromCurr;
				closestTask = t; 
			}
			System.out.println("The distance from :" + point + " to: " + " is: " + distanceFromCurr); 

			//closestArray.add(point.distance(pt));
			//PointArray.add(pt);
		}
		
//		for(int j=0;j<closestArray.size();j++) // finds the shortest distance
//		{
//			if(closestArray != null)
//			if(closestArray.get(j) < closest) 
//			{
//				closest = closestArray.get(j);
//				closestPoint = PointArray.get(j); 
//			}
//		}
		System.out.println("The closest distance is:" + closest + " which is at: " + closestTask);
//		if the x and y coordinates are required to be extracted to Integer
//		int x = (int)closestPoint.getX();    
//		int y = (int)closestPoint.getY();
		//String [] closestLocation = closestPoint.toString().split(" ");
		
		return closestTask;
	}

	public boolean isEmpty(){
		return tasks.isEmpty();
	}
	
	// need to get correct index
	public void removeCompletedJob(){
		tasks.remove(closestTask);
	}
	
	public HashSet<Task> getTasks() {
		return tasks;
	}

	public void setPositionList(HashSet<Task> tasks) {
		this.tasks = tasks;
	}

	public void displayLocation(){
		for(Task task: tasks)
			System.out.println("Location from Rover:" + task.getDestination());
	}	
	
}
