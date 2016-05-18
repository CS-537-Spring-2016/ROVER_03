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
	private ScanMap map;
	public Coord destination = null;

	public PathFinder(){
		open = new ArrayList<TileNode>();
		closed = new ArrayList<TileNode>();
		path = new ArrayList<TileNode>();
	}

	public ArrayList<TileNode> generatePath(Coord origin, Coord destination, ScanMap map, int iteration) throws InterruptedException{
		if(this.destination == null)
			closed.clear();
		this.map = map;
		path.clear();
		og = origin;
		this.destination = destination;
		return pathHelper(new TileNode(origin), destination,map,iteration);
	}

	public void nextMove(TileNode parent, Coord next, int iteration){
		TileNode curr = new TileNode(next);
		curr.setH(destination);
		curr.setParent(parent);
		parent.getChildren().add(curr);
		if(curr.coordinate.equals(destination)) return;
		getNext(curr,--iteration);
	}
	public void getNext(TileNode tile, int iteration){

		if(iteration >= 0){
			if(!blocked(1,0,map,tile.coordinate) && !closedContains(new TileNode(new Coord(tile.coordinate.xpos + 1, tile.coordinate.ypos)))){
				nextMove(tile,new Coord(tile.coordinate.xpos + 1, tile.coordinate.ypos),iteration);
			}
			if(!blocked(0,-1,map,tile.coordinate) && !closedContains(new TileNode(new Coord(tile.coordinate.xpos, tile.coordinate.ypos - 1)))){
				nextMove(tile,new Coord(tile.coordinate.xpos, tile.coordinate.ypos - 1),iteration);
			}
			if(!blocked(0,1,map,tile.coordinate) && !closedContains(new TileNode(new Coord(tile.coordinate.xpos, tile.coordinate.ypos + 1)))){
				nextMove(tile,new Coord(tile.coordinate.xpos, tile.coordinate.ypos + 1),iteration);
			}
			if(!blocked(-1,0,map,tile.coordinate) && !closedContains(new TileNode(new Coord(tile.coordinate.xpos - 1, tile.coordinate.ypos)))){
				nextMove(tile,new Coord(tile.coordinate.xpos - 1, tile.coordinate.ypos),iteration);
			}
		}	
	}

	public ArrayList<TileNode> pathHelper(TileNode t, Coord destination, ScanMap map, int iteration) throws InterruptedException{
		TileNode tile = t;
		tile.setH(destination);
		
		if(!blocked(1,0,map,tile.coordinate)){
			nextMove(tile,new Coord(tile.coordinate.xpos + 1, tile.coordinate.ypos),2);
		}
		if(!blocked(0,-1,map,tile.coordinate)){
			nextMove(tile,new Coord(tile.coordinate.xpos, tile.coordinate.ypos - 1),2);
		}
		if(!blocked(0,1,map,tile.coordinate)){
			nextMove(tile,new Coord(tile.coordinate.xpos, tile.coordinate.ypos + 1),2);
		}
		if(!blocked(-1,0,map,tile.coordinate)){
			nextMove(tile,new Coord(tile.coordinate.xpos - 1, tile.coordinate.ypos),2);
		}


		filterByPathLength(tile);
		
		for(int i = 0; i < tile.getChildren().size();i++){
			filterByPathCost(tile, i);
			for(int x = 0; x < tile.getChildren().get(i).getChildren().size();x++){
				filterSecondLayer(tile,x);
			}
		}


		filterFirstLayer(tile);


		TileNode next = tile;

	
		while(next.hasChildren()){
			next = next.getChildren().get(0);
			path.add(next);
			closed.add(next);
		}

		return path;
	}

	public void filterByPathCost(TileNode parent,int index){
		if(parent.h <= 2) return;
		ArrayList<TileNode> currList = parent.getChildren().get(index).getChildren(); //SECOND LAYER CHILDREN
		int smallestCostIndex = 0;
		int sum = 0;
		for(int i = 0; i < currList.size(); i++){
			smallestCostIndex = 0;
			for(int x = 0; x < currList.get(i).getChildren().size(); x++){
				sum = 0;
				sum += parent.getChildren().get(index).f + currList.get(i).f + currList.get(i).getChildren().get(x).f;
				if(x == 0)
					currList.get(i).bestF = sum;
				if(x > 0 && sum < currList.get(i).bestF){
					currList.get(i).bestF = sum;
					smallestCostIndex = x;
				}
			}
			for(int y = 0; y < currList.get(i).getChildren().size(); y++){
				if(y != smallestCostIndex){
					currList.get(i).getChildren().remove(y);
					smallestCostIndex --; // Need to do this because indexes shifted
					y--;
				}
			}
		}
	}

	public void filterByPathLength(TileNode parent){
		if(parent.h <= 2) return;
		ArrayList<TileNode> currList = parent.getChildren();
		for(int i = 0; i < currList.size(); i++){
			TileNode currTile = currList.get(i);
			if(currTile.getChildren().isEmpty()){
				currList.remove(currTile);
				i--;
			}
			else{
				for(int x = 0; x < currTile.getChildren().size(); x++){
					if(currTile.getChildren().get(x).getChildren().isEmpty()){
						currTile.getChildren().remove((currTile.getChildren().get(x)));
						x--;
					}
				}
			}
		}

	}

	public void filterSecondLayer(TileNode parent, int index){
		if(parent.h <= 2) return;
		ArrayList<TileNode> currList = parent.getChildren().get(index).getChildren();
		int smallestCostIndex = 0;
		for(int i = 0; i < currList.size(); i++){
			if(i != 0 && currList.get(i).bestF < currList.get(smallestCostIndex).bestF){
				smallestCostIndex = i;
			}
		}

		parent.getChildren().get(index).bestF = parent.getChildren().get(index).getChildren().get(smallestCostIndex).bestF;

		for(int i = 0; i < currList.size(); i++){
			if(i != smallestCostIndex){
				currList.remove(i);
				smallestCostIndex --; // Need to do this because indexes shifted
				i--;
			}
		}

	}

	public void filterFirstLayer(TileNode parent){
		ArrayList<TileNode> currList = parent.getChildren();
		int smallestCostIndex = 0;
		for(int i = 0; i < currList.size(); i++){
			if(i != 0 && currList.get(i).bestF < currList.get(smallestCostIndex).bestF){
				smallestCostIndex = i;
			}
		}


		for(int i = 0; i < currList.size(); i++){
			if(i != smallestCostIndex){
				currList.remove(i);
				smallestCostIndex --; // Need to do this because indexes shifted
				i--;
			}
		}

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
