package model;

import java.awt.Point;

public class BackTrack {
	Point [][] positionList;
	Point lastPosition;
	Point currentPosition;
	
	public BackTrack(){
		positionList = new Point[200][200];
	}
	
	public Point[][] getPositionList() {
		return positionList;
	}

	public void setPositionList(Point[][] positionList) {
		this.positionList = positionList;
	}

	public Point getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(Point lastPosition) {
		this.lastPosition = lastPosition;
	}

	public Point getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Point currentPosition) {
		this.currentPosition = currentPosition;
	}
	
	
	
	
}
