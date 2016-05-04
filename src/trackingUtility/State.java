package trackingUtility;

import common.Coord;

public class State {
	
	private Coord coordinate;
	private String direction;
	
	public State(Coord coordinate, String direction){
		this.coordinate = coordinate;
		this.direction = direction;
	}
	
	public String getDirection(){
		return direction;
	}
	
	public int getX(){
		return coordinate.xpos;
	}
	
	public int getY(){
		return coordinate.ypos;
	}
	
	
}
