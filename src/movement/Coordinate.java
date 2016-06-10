package movement;

import java.awt.geom.Point2D;

/**
 * This coordinate class provides user with information about two coordinates the "local" and "absolute" coordinates.
 * In order to use this class it is not necessary to set both "local" and "absolute". The PathFinder makes extensive
 * use of this class, and in that case it IS necessary to set both. That is taken care of in the PathFinder class. 
 * This class provides everything necessary to manipulate coordinates and extrapolate other coordinates from known coordinates. 
 * @author Carlos Galdamez
 * @see Comparable
 * @see Point2D
 */
public class Coordinate implements Comparable<Coordinate>{

	public static enum TYPE{ABSOLUTE,LOCAL};
	
	/* Refer to x and y position in the scan area, if scan area is 7 x 7 then the lowest value for
	 * x and y is 0 and the highest is 6*/ 
	private int localXPos;
	private int localYPos;

	/* Refer to x and y position in the map */
	private int absoluteXPos;
	private int absoluteYPos;

	/*-------------------------------------------------------------- CONSTRUCTOR ---------------------------------------------------*/
	
	/**
	 * Constructor either sets the absolute or local positions based on type that was
	 * entered by the user.
	 * @param xPos - rover x position
	 * @param yPos - rover y position
	 * @param type - type of x position and y position given, they are either ABSOLUTE or LOCAL
	 */
	public Coordinate(int xPos, int yPos, Coordinate.TYPE type){
		switch(type){
		case ABSOLUTE:
			absoluteXPos = xPos;
			absoluteYPos = yPos;
			break;
		case LOCAL:
			localXPos = xPos;
			localYPos = yPos;
			break;
		default: break;
		}
	}

	/*------------------------------------------------------------ GETTERS --------------------------------------------------------*/
	
	/**
	 * Gets absolute x position
	 * @return absolute x position
	 */
	public int getAbsoluteX(){
		return absoluteXPos;
	}
	
	/**
	 * Gets absolute y position
	 * @return absolute y position
	 */
	public int getAbsoluteY(){
		return absoluteYPos;
	}
	
	/**
	 * Gets local x position
	 * @return local x position
	 */
	public int getLocalX(){
		return localXPos;
	}
	
	/**
	 * Gets local y position
	 * @return local y position
	 */
	public int getLocalY(){
		return localYPos;
	}
	
	/*------------------------------------------------------------ SETTERS --------------------------------------------------------*/

	/**
	 * Sets local x and y position for rover, these values should not
	 * be less than 0 and should not be greater that size - 1 of your 
	 * scan area. Example: id scan area is 7 X 7 then values should not be greater
	 * than 6.
	 * @param x - x position
	 * @param y - y position
	 */
	public void setLocal(int x, int y){
		localXPos = x;
		localYPos = y;
	}
	
	/**
	 * Sets absolute x and y position for rover
	 * @param x - x position
	 * @param y - y position
	 */
	public void setAbsolute(int x, int y){
		absoluteXPos = x;
		absoluteYPos = y;
	}

	/*---------------------------------------------------- OTHER HELPER METHODS ---------------------------------------------------*/
	
	/**
	 * Only used to get difference between LOCAL x positions and LOCAL y positions. Offset
	 * is no necessary for absolute positions. This method is only used in 
	 * PathFinder.
	 * @param other - coordinate object you want to get offset from
	 * @return array of two values the x offset and the y offset
	 */
	public int[] getOffset(Coordinate other){
		int[] offset = {other.getLocalX() - localXPos, other.getLocalY() - localYPos};
		//System.out.println("XOffset: " + offset[0] + " ,  YOffset: "  + offset[1]);
		return offset;
	}

	/**
	 * Calculates the distance between any two Coordinate objects. LOCAL or ABSOLUTE position values
	 * can be used.
	 * @param other - coordinate object user wishes to get distance from
	 * @param type - type of values that will be used to get the distance: LOCAL or ABSOLUTE
	 * @return distance from specified coordinate object
	 */
	public double getDistance(Coordinate other, Coordinate.TYPE type){
		/* Need to change coordinate x and y values to Point2D objects to use Point2D distance method */
		switch(type){
		case ABSOLUTE:
			return new Point2D.Double(absoluteXPos, absoluteYPos).distance(new Point2D.Double(other.getAbsoluteX(), other.getAbsoluteY()));
		case LOCAL:
			return new Point2D.Double(localXPos, localYPos).distance(new Point2D.Double(other.getLocalX(), other.getLocalY()));
		}
		return 0;
	}

	/**
	 * Checks if two coordinate objects have the same absolute y and x positions
	 * @param other - coodinate object user wishes to compare this instance to
	 * @return true or false
	 */
	public boolean equals(Coordinate other) {
		if(absoluteXPos == other.getAbsoluteX() && absoluteYPos == other.getAbsoluteY())
			return true;
		return false;
	}
	
	/**
	 * Creates an exact copy of this instance
	 * @return new coordinate object 
	 */
	public Coordinate clone(){
		Coordinate clone = new Coordinate(absoluteXPos, absoluteYPos, Coordinate.TYPE.ABSOLUTE);
		clone.setLocal(localXPos, localYPos);
		return clone;
	}

	/* This method will tell rover in what quadrant destination is. There are 4 quadrants as specified below */
	@Override
	public int compareTo(Coordinate other) {
		if(other.getAbsoluteX() > absoluteXPos && other.getAbsoluteY() > absoluteYPos){
			System.out.println("DESTINATION IS SOUTH EAST OF YOUR LOCATION");
			return 4; // SOUTH EAST IS 4TH QUADRANT
		}
		if(other.getAbsoluteX() > absoluteXPos && other.getAbsoluteY() < absoluteYPos){
			System.out.println("DESTINATION IS NORTH EAST OF YOUR LOCATION");
			return 3; // NORTH EAST IS 3TH QUADRANT
		}
		if(other.getAbsoluteX() < absoluteXPos && other.getAbsoluteY() > absoluteYPos){
			System.out.println("DESTINATION IS SOUTH WEST OF YOUR LOCATION");
			return 2; // SOUTH WEST IS 2ND QUADRANT
		}
		if(other.getAbsoluteX() < absoluteXPos && other.getAbsoluteY() < absoluteYPos){
			System.out.println("DESTINATION IS NORTH WEST OF YOUR LOCATION");
			return 1; // NORTH WEST IS 1ST QUADRANT
		}
		return 0;
	}
	
	/*---------------------------------------------------- TO STRING METHOD -----------------------------------------------------*/
	
	public String toString(){
		return "ABSOLUTE: ( " + absoluteXPos + " , " + absoluteYPos + " ) ; LOCAL: ( " + localXPos + " , " + localYPos + " )";
	}

}
