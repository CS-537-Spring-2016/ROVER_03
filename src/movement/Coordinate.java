package movement;

import java.awt.geom.Point2D;
import java.util.Comparator;

public class Coordinate implements Comparable<Coordinate>{

	public static enum TYPE{ABSOLUTE,LOCAL};
	// in map array
	private int localXPos;
	private int localYPos;

	// actual coordinates
	private int absoluteXPos;
	private int absoluteYPos;

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


	public int getAbsoluteX(){
		return absoluteXPos;
	}
	public int getAbsoluteY(){
		return absoluteYPos;
	}

	public int getLocalX(){
		return localXPos;
	}
	public int getLocalY(){
		return localYPos;
	}
	
	public void setLocalX( int x){
		localXPos = x;
	}
	public void setLocalY( int y){
		localYPos = y;
	}
	
	public void setAbsolute(int x, int y){
		absoluteXPos = x;
		absoluteYPos = y;
	}

	public int[] getOffset(Coordinate other){
		int[] offset = {other.getLocalX() - localXPos, other.getLocalY() - localYPos};
		//System.out.println("XOffset: " + offset[0] + " ,  YOffset: "  + offset[1]);
		return offset;
	}

	public double getDistance(Coordinate other, Coordinate.TYPE type){
		switch(type){
		case ABSOLUTE:
			return new Point2D.Double(absoluteXPos, absoluteYPos).distance(new Point2D.Double(other.getAbsoluteX(), other.getAbsoluteY()));
		case LOCAL:
			return new Point2D.Double(localXPos, localYPos).distance(new Point2D.Double(other.getLocalX(), other.getLocalY()));
		}
		return 0;
	}

	public boolean equals(Coordinate other) {
		if(absoluteXPos == other.getAbsoluteX() && absoluteYPos == other.getAbsoluteY())
			return true;
		return false;
	}

	public String toString(){
		return "ABSOLUTE: ( " + absoluteXPos + " , " + absoluteYPos + " ) ; LOCAL: ( " + localXPos + " , " + localYPos + " )";
	}


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

}
