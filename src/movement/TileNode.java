package movement;

import java.util.ArrayList;

import common.Coord;

public class TileNode {

	public int bestF = 1000000;
	private ArrayList<TileNode> children;
	public boolean visited = false;
	private TileNode parent;
	// the movement cost to move from the starting point A to a given square on the grid, following the path generated to get there. 
	private int g = 10;
	// the estimated movement cost to move from that given square on the grid to the final destination, also known as the heuristic
	public int h;
	public int f;
	public Coord coordinate;
	
	public TileNode(Coord coordinate){
		this.coordinate = coordinate;
	}
	
	public ArrayList<TileNode> getChildren(){
		if(children == null)
			children = new ArrayList<>();
		return children;
	}
	
	public void addChild(TileNode node){
		children.add(node);
	}
	
	public void setH(Coord destination){
		h = Math.abs(destination.xpos - coordinate.xpos) + Math.abs(destination.ypos - coordinate.ypos);
		setF();
	}
	
	private void setF(){
		f = g + h;
	}
	
	public TileNode getParent(){
		return parent;
	}
	
	public void setParent(TileNode parent){
		this.parent = parent;
	}
	
	public int allVisited(){
		int count = 0;
		for(TileNode node : children){
			if(!node.visited)
				return count;
			count++;
		}	
		return -1;
	}
	
	public boolean hasChildren(){
		if(children == null)
			children = new ArrayList<>();
		return !children.isEmpty();
	}
}
