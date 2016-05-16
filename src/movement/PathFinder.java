package movement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Terrain;

public class PathFinder {

	private ArrayList<TileNode> open;
	private ArrayList<TileNode> closed;

	public PathFinder(){
		open = new ArrayList<TileNode>();
		closed = new ArrayList<TileNode>();
	}

	public ArrayList<TileNode> generatePath(Coord origin, Coord destination, ScanMap map, int iteration){
		int count = iteration;
		open.clear();

		if(count == 1)
			closed.clear();
		
		if(iteration == 4){
			return closed;
		}
		
		TileNode curr = new TileNode(origin);
		closed.add(curr);

		TileNode north,south,east,west;
		north = south = east = west = null;
		if(!blocked(1,0,map)){
			east = new TileNode(new Coord(origin.xpos + 1, origin.ypos));
			east.setH(destination);
		}
		if(!blocked(0,-1,map)){
			north = new TileNode(new Coord(origin.xpos, origin.ypos - 1));
			north.setH(destination);
		}
		if(!blocked(0,1,map)){
			south = new TileNode(new Coord(origin.xpos, origin.ypos + 1));
			south.setH(destination);
		}
		if(!blocked(-1,0,map)){
			west = new TileNode(new Coord(origin.xpos - 1, origin.ypos));
			west.setH(destination);
		}

		addAll(east,north,south,west);
		
		TileNode lowestFCost = open.get(0);
		for(TileNode node: open){
			if(node.f < lowestFCost.f)
				lowestFCost = node;
		}
		
		return generatePath(lowestFCost.coordinate,destination,map,count + 1);
	}

	public void addAll(TileNode ... nodes){
		for(TileNode node: nodes){
			if(node != null && !closedContains(node) && !openContains(node))
				open.add(node);
		}
	}
	
	public boolean openContains(TileNode node){
		for (TileNode o : open)
			if(o.coordinate.equals(node.coordinate))
				return true;
		return false;
	}
	
	public boolean closedContains(TileNode node){
		for (TileNode o : closed)
			if(o.coordinate.equals(node.coordinate))
				return true;
		return false;
	}


	private boolean blocked(int xOffset, int yOffset, ScanMap m){
		MapTile[][] map = m.getScanMap();
		int centerIndex = (m.getEdgeSize() - 1)/2;
		return map[centerIndex + xOffset][centerIndex + yOffset].getHasRover() 
				|| map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.ROCK
				|| map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.NONE;
	}
}
