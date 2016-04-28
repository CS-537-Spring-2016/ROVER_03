package model;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class RoverQueue {
	
	private ArrayList<Point2D> positionList;
	private Double xPosition;
	private Double yPosition;
	private Point2D closestPoint;
	 
	
	public RoverQueue(){
		closestPoint = new Point2D.Double();
		
		// These are hard coded cordinates but will not be there for final implementation..will be receiving these from other rovers or server target location
		addLocation("LOC 13 18");
		addLocation("LOC 4 25");
		addLocation("LOC 5 32");
		addLocation("LOC 15 36");
		addLocation("LOC 17 36");
		addLocation("LOC 30 21");
		addLocation("LOC 19 0");
		addLocation("LOC 24 13");
		addLocation("LOC 17 12");
		addLocation("LOC 4 20");
		addLocation("LOC 10 23");

		positionList = new ArrayList<Point2D>();

	}
	
	public void addLocation (String location){
		xPosition = Double.parseDouble(location.split(" ")[1].trim());
		yPosition = Double.parseDouble(location.split(" ")[2].trim());
		Point2D point = new Point2D.Double(xPosition, yPosition);
		positionList.add(point);	
	}
	
	//finds the closest location from the current location
	public String closestTargetLocation(String currentLocation)
	{
		Double currentXPosition = Double.parseDouble(currentLocation.split(" ")[1].trim());
		Double currentYPosition = Double.parseDouble(currentLocation.split(" ")[2].trim());
		Point2D point = new Point2D.Double(currentXPosition, currentYPosition);
		
		ArrayList<Double> closestArray = new ArrayList<Double>();
		ArrayList<Point2D> PointArray = new ArrayList<Point2D>();
		Double closest = 10000.0;
		
		for(Point2D pt: positionList)
		{
			System.out.println("The distance from :"+point+" to: "+pt+" is: "+point.distance(pt)); 
			closestArray.add(point.distance(pt));
			PointArray.add(pt);
		}
		
		for(int j=0;j<closestArray.size();j++) // finds the shortest distance
		{
			if(closestArray != null)
			if(closestArray.get(j) < closest) 
			{
				closest = closestArray.get(j);
				closestPoint = PointArray.get(j); 
			}
		}
		System.out.println("The closest distance is:" + closest+ " which is at: "+ closestPoint);
		// if the x and y coordinates are required to be extracted to Integer
		int x = (int)closestPoint.getX();    
		int y = (int)closestPoint.getY();
		//String [] closestLocation = closestPoint.toString().split(" ");
		
		return "LOC " + x + " " + y;
	}

	public boolean isEmpty(){
		return positionList.isEmpty();
	}
	
	// need to get correct index
	public void removeCompletedJob(){
		positionList.remove(closestPoint);
	}
	

	public ArrayList<Point2D> getPositionList() {
		return positionList;
	}

	public void setPositionList(ArrayList<Point2D> positionList) {
		this.positionList = positionList;
	}

	public void displayLocation(){
		for(Point2D str: positionList)
		{
			System.out.println("Location from Rover:"+str);
		}
	}	
	
}
