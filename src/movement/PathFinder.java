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
	private ArrayList<TileNode> path;
	private Coord og;

	public PathFinder(){
		open = new ArrayList<TileNode>();
		closed = new ArrayList<TileNode>();
		path = new ArrayList<TileNode>();
	}

	public ArrayList<TileNode> generatePath(Coord origin, Coord destination, ScanMap map, int iteration) throws InterruptedException{
		path.clear();
		og = origin;
		return pathHelper(new TileNode(origin), destination,map,iteration);
	}

	public ArrayList<TileNode> pathHelper(TileNode tile, Coord destination, ScanMap map, int iteration) throws InterruptedException{
		open.clear();
		
		if(iteration == 3){
			System.out.println("\n\n");
			return path;
		}
		else if(closed.contains(tile)){
			if(tile.getChildren().isEmpty())
				return pathHelper(tile.getParent(),destination,map,iteration);
			else{
				TileNode lowestCost = tile.getChildren().get(0);
				for(TileNode node: tile.getChildren()){
					if(node.f < lowestCost.f && !node.visited){
						lowestCost = node;
					}
				}
				return pathHelper(lowestCost,destination,map,iteration + 1);
			}
		}

		TileNode curr = tile;
		if(iteration != 0)
			path.add(curr);
		closed.add(curr);
		System.out.println(curr.coordinate);


		TileNode north,south,east,west;
		north = south = east = west = null;
		if(!blocked(1,0,map,tile.coordinate)){
			east = new TileNode(new Coord(tile.coordinate.xpos + 1, tile.coordinate.ypos));
			east.setH(destination);
			east.setParent(curr);
			curr.getChildren().add(east);
		}
		if(!blocked(0,-1,map,tile.coordinate)){
			north = new TileNode(new Coord(tile.coordinate.xpos, tile.coordinate.ypos - 1));
			north.setH(destination);
			north.setParent(curr);
			curr.getChildren().add(north);
		}
		if(!blocked(0,1,map,tile.coordinate)){
			south = new TileNode(new Coord(tile.coordinate.xpos, tile.coordinate.ypos + 1));
			south.setH(destination);
			south.setParent(curr);
			curr.getChildren().add(south);
		}
		if(!blocked(-1,0,map,tile.coordinate)){
			west = new TileNode(new Coord(tile.coordinate.xpos - 1, tile.coordinate.ypos));
			west.setH(destination);
			west.setParent(curr);
			curr.getChildren().add(west);
		}

		addAll(curr.getChildren());

		if(open.isEmpty()){
			return pathHelper(curr.getParent(),destination,map,iteration);
		}

		TileNode lowestFCost = open.get(0);
		for(TileNode node: open){
			if(node.f < lowestFCost.f && !node.visited){
				lowestFCost = node;
			}
			System.out.println("Node coordinate: " + node.coordinate + " has f cost of: " + node.f);
		}

		lowestFCost.visited = true;
		System.out.println("MOVE TO: [" + (- tile.coordinate.xpos + lowestFCost.coordinate.xpos) + " , " + (- tile.coordinate.ypos + lowestFCost.coordinate.ypos) + "]");
		//	Thread.sleep(3000);
		return pathHelper(lowestFCost,destination,map,iteration + 1);

	}

	public void addAll(ArrayList<TileNode> nodes){
		for(TileNode node: nodes){
			if(node != null && !closedContains(node)){
				System.out.println("Adding " + node.coordinate + " to open list");
				open.add(node);
			}
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


	private boolean blocked(int xOffset, int yOffset, ScanMap m, Coord position){
		MapTile[][] map = m.getScanMap();
		int centerIndexX = 3 + (- og.xpos + position.xpos);
		int centerIndexY = 3 + (- og.ypos + position.ypos);
		return map[centerIndexX + xOffset][centerIndexY + yOffset].getHasRover() 
				|| map[centerIndexX + xOffset][centerIndexY + yOffset].getTerrain() == Terrain.ROCK
				|| map[centerIndexX + xOffset][centerIndexY + yOffset].getTerrain() == Terrain.NONE;
	}
}
