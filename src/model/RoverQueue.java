package model;

import java.util.ArrayList;

import movement.Coordinate;
import tasks.Task;

/**
 * RoverQueue is used to store tasks for the rover. Additionally it it helps the
 * rover decide what task to execute next based on the current distance from the task.
 * @author Antonio Bang
 * @author Carlos Galdamez
 * @see Task
 * @see Coordinate
 */
public class RoverQueue {
	
	private ArrayList<Task> tasks;
	private Task closestTask = null;
	
	/*----------------------------------------------------------- CONSTRUCTOR ------------------------------------------------------------*/
	
	/** Constructor instatiates the array list that will hold all of the rover tasks */
	public RoverQueue(){
		tasks = new ArrayList<Task>();
	}
	
	/*------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * Finds closest task to rover's current location
	 * @param currentLocation - Coordinate object for rover's currents location
	 * @return task object for closest task
	 */
	public Task closestTask(Coordinate currentLocation){
	
		Double closest = Double.POSITIVE_INFINITY; /* Set closest distance to infinity */

		/* Iterate through list to find which task is closest to current location 
		 * closest task will be store in closestTask global variable */
		for(Task t: tasks){
			Double distanceFromCurr = currentLocation.getDistance(t.getDestination(), Coordinate.TYPE.ABSOLUTE);
			if(distanceFromCurr < closest) {
				closest = distanceFromCurr;
				closestTask = t;
			}
			System.out.println("The distance from :" + currentLocation + " to: " + t.getDestination() + " is: " + distanceFromCurr); 
		}
		System.out.println("The closest distance is:" + closest + " which is at: " + closestTask);
		
		return closestTask;
	}
	
	/*------------------------------------------- METHODS FOR LIST MANIPULATION AND ACCESS ---------------------------------------------*/
	
	/**
	 * Adds a task to the array list of tasks
	 * @param task - task object to be added to task list
	 */
	public void addTask (Task task){
		if(!contains(task.getDestination())) /* Check if coordinate for the task is already in the list to prevent duplicates */
			tasks.add(task);		
	}
	
	/**
	 * Removes the current closestTask object from the list of tasks
	 */
	public void removeCompletedJob(){
		tasks.remove(closestTask);
	}
	
	/**
	 * Checks if task list is empty
	 * @return true or false
	 */
	public boolean isEmpty(){
		return tasks.isEmpty();
	}
	
	/**
	 * Gets list of tasks
	 * @return list of tasks
	 */
	public ArrayList<Task> getTasks() {
		return tasks;
	}
	
	/**
	 * Checks if the specified coordinate is already in the task list
	 * @param c - Coordinate object
	 * @return true or false
	 */
	private boolean contains(Coordinate c){
		for(Task t: tasks)
			if(t.getDestination().equals(c))
				return true;
		return false;
		
	}	
}
