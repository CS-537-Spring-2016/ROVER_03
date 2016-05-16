package movement;

import java.util.ArrayList;

import common.Coord;

public class TileNode {

	private ArrayList<TileNode> children;
	// the movement cost to move from the starting point A to a given square on the grid, following the path generated to get there. 
	private int g = 10;
	// the estimated movement cost to move from that given square on the grid to the final destination, also known as the heuristic
	private int h;
	public int f;
	public Coord coordinate;
	
	public TileNode(Coord coordinate){
		this.coordinate = coordinate;
	}
	
	public ArrayList<TileNode> getChildren(){
		return children;
	}
	
	public void addChild(TileNode node){
		if(children == null)
			children = new ArrayList<>();
		children.add(node);
	}
	
	public void setH(Coord destination){
		h = Math.abs(destination.xpos - coordinate.xpos) + Math.abs(destination.ypos - coordinate.ypos);
		setF();
	}
	
	private void setF(){
		f = g + h;
	}
}
