package model;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import common.Coord;
import movement.Coordinate;
import tasks.Task;

public class RoverQueue {
	
	private ArrayList<Task> tasks;
	private Task closestTask = null;
	 
	public RoverQueue(){
		tasks = new ArrayList<Task>();
	}
	
	public void addTask (Task task){
		if(!contains(task.getDestination()))
			tasks.add(task);		
	}
	
	//Finds the closest task from the current location
	public Task closestTask(Coordinate currentLocation){
	
		/* Makes current location into a Point2D object */
		Point2D point = new Point2D.Double(currentLocation.getAbsoluteX(), currentLocation.getAbsoluteY());
		Double closest = Double.POSITIVE_INFINITY; 
		
		for(Task t: tasks){
			Point2D pt = new Point2D.Double(t.getDestination().getAbsoluteX(), t.getDestination().getAbsoluteY());
			Double distanceFromCurr = point.distance(pt);
			if(distanceFromCurr < closest) {
				closest = distanceFromCurr;
				closestTask = t; 
			}
			System.out.println("The distance from :" + point + " to: " + pt + " is: " + distanceFromCurr); 
		}
		
		System.out.println("The closest distance is:" + closest + " which is at: " + closestTask);
		
		return closestTask;
	}

	public boolean isEmpty(){
		return tasks.isEmpty();
	}
	
	public void removeCompletedJob(){
		tasks.remove(closestTask);
	}
	
	public ArrayList<Task> getTasks() {
		return tasks;
	}

	public void setPositionList(ArrayList<Task> tasks) {
		this.tasks = tasks;
	}
	
	private boolean contains(Coordinate c){
		for(Task t: tasks)
			if(t.getDestination().equals(c))
				return true;
		return false;
		
	}

	public void displayLocation(){
		for(Task task: tasks)
			System.out.println("Location from Rover:" + task.getDestination());
	}	
	
}
