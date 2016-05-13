package missionControl;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import common.MapTile;
import common.ScanMap;
import enums.Terrain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.RoverQueue;
import swarmBots.ROVER_03;
import trackingUtility.Tracker;

public class MissionControl extends Application{
	private Label[][] labels = new Label[7][7];
	private ROVER_03 client;
	private RoverQueue queue;
	private Tracker tracker;
	private ScanMap map;
	private Map<String,Integer> cargoList;

	@Override
	public void start(Stage primaryStage) throws IOException, InterruptedException{
		client = new ROVER_03();

		new Thread (new Runnable(){

			@Override
			public void run() {
				try {
					client.start();					
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}			
			}

		}).start();

		map = client.getScanMap();
		queue = client.getQueue();
		tracker = client.getTracker();
		cargoList = client.getCargoList();


		BorderPane bp = new BorderPane();
		bp.getStyleClass().add("gui");
		Scene scene = new Scene(bp, 800, 750);

		bp.getStylesheets().add("missionControl/style.css");
		Label sceneTitle = new Label("Mission Control\n");
		sceneTitle.getStyleClass().add("missionControl");
		HBox hb = new HBox();
		hb.getChildren().add(sceneTitle);
		hb.setAlignment(Pos.BOTTOM_CENTER);

		bp.setTop(hb);
		//sceneTitle.getStyleClass().add("titleText");
		bp.setLeft(setTaskModule());
		HBox console = new HBox();

		VBox cargo = new VBox();
		HBox titleCargo = new HBox();
		Label ti = new Label("\nROVER_03 CARGO:\n");
		ti.getStyleClass().add("destination");
		titleCargo.getChildren().add(ti);
		titleCargo.setAlignment(Pos.CENTER);
		cargo.setMinWidth(350);
		HBox hb4 = new HBox();
		hb4.setAlignment(Pos.CENTER);
		hb4.setSpacing(20);
		cargo.getChildren().addAll(titleCargo, hb4);

		HBox destination = new HBox();
		destination.setMinWidth(225);

		//while(client.getRover().getTools() == null || client.getRover().getDriveType() == null);
		
		VBox equipment = new VBox();
		Label t = new Label("\nROVER_03 EQUIPMENT:\n");
		t.getStyleClass().add("destination");
		Label drive = new Label("DRIVE TYPE: " + client.getRover().getDriveType());
		drive.getStyleClass().add("destination");
		Label tool1 = new Label("TOOL 1: " + client.getRover().getTools().get(0));
		tool1.getStyleClass().add("destination");
		Label tool2 = new Label("TOOL 2: " + client.getRover().getTools().get(1));
		tool2.getStyleClass().add("destination");
		equipment.getChildren().addAll(t,drive,tool1,tool2);
		equipment.setMinWidth(225);

		console.setMinWidth(800);
		console.setMinHeight(125);
		console.getStyleClass().add("console");
		console.getChildren().addAll(cargo, destination,equipment);
		bp.setBottom(console);

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//queue = client.getQueue();
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							hb4.getChildren().clear();
							VBox one = new VBox();
							VBox two = new VBox();
							Label organic = new Label("ORGANIC: " + cargoList.get("ORGANIC"));
							organic.getStyleClass().add("destination");
							Label radioactive = new Label("RADIOACTIVE: " + cargoList.get("RADIOACTIVE"));
							radioactive.getStyleClass().add("destination");
							Label crystal = new Label("CRYSTAL: " + cargoList.get("CRYSTAL"));
							crystal.getStyleClass().add("destination");
							Label mineral = new Label("MINERAL: " + cargoList.get("MINERAL"));
							mineral.getStyleClass().add("destination");
							one.getChildren().addAll(organic,radioactive);
							two.getChildren().addAll(crystal,mineral);
							hb4.getChildren().addAll(one,two);
						}
					});
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//queue = client.getQueue();
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							destination.getChildren().clear();
							VBox v = new VBox();
							Label title = new Label("\nROVER_03 TRACKER:\n");
							title.getStyleClass().add("destination");
							Label origin = new Label("ORIGIN: [ " + tracker.getStartingPoint().xpos + " , " + tracker.getStartingPoint().ypos + " ]");
							origin.getStyleClass().add("destination");
							Label destin = new Label("DESTINATION: [ " + tracker.getDestination().xpos + " , " + tracker.getDestination().ypos + " ]");
							destin.getStyleClass().add("destination");
							Label distance = new Label("DISTANCE LEFT: [ " + tracker.getDistanceTracker().xpos + " , " + tracker.getDistanceTracker().ypos + " ]");
							distance.getStyleClass().add("destination");
							v.getChildren().addAll(title,origin,destin,distance);
							destination.getChildren().add(v);
						}
					});
				}
			}
		}).start();

		GridPane gp = new GridPane();
		gp.setAlignment(Pos.BOTTOM_LEFT);

		while (map == null){;			// Will loop until there is map 
		map = client.getScanMap();
		}

		MapTile[][] tiles = map.getScanMap(); 
		for(int row = 0; row < 7; row++){
			for(int column = 0; column < 7; column++){
				Label tile = new Label("");
				labels[row][column] = tile;
				tile.setPrefSize(70, 70);
				tile.getStyleClass().add("grid");

				if(tiles[row][column].getTerrain() == Terrain.ROCK){
					tile.getStyleClass().add("rock");
				}
				else if(tiles[row][column].getTerrain() == Terrain.NONE){
					tile.getStyleClass().add("abyss");
				}
				else if(tiles[row][column].getTerrain() == Terrain.SAND){
					tile.getStyleClass().add("sand");
				}
				else if(tiles[row][column].getTerrain() == Terrain.GRAVEL){
					tile.getStyleClass().add("gravel");
				}
				else{
					tile.getStyleClass().add("dirt");
				}

				if(row == 3 && column == 3){
					// Image image = new Image(getClass().getResourceAsStream("ROVER_03.png"));
					labels[row][column].setText("R3");
				}

				gp.add(labels[row][column], row, column);
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true){
					map = client.getScanMap();
					MapTile[][] tiles = map.getScanMap();

					for(int row = 0; row < 7; row++){
						for(int column = 0; column < 7; column++){
							if(tiles[row][column].getTerrain() == Terrain.ROCK){
								labels[row][column].getStyleClass().clear();
								labels[row][column].getStyleClass().addAll("rock","grid");
							}
							else if(tiles[row][column].getTerrain() == Terrain.NONE){
								labels[row][column].getStyleClass().clear();
								labels[row][column].getStyleClass().addAll("abyss","grid");	
							}
							else if(tiles[row][column].getTerrain() == Terrain.SAND){
								labels[row][column].getStyleClass().clear();
								labels[row][column].getStyleClass().addAll("sand","grid");
							}
							else if(tiles[row][column].getTerrain() == Terrain.GRAVEL){
								labels[row][column].getStyleClass().clear();
								labels[row][column].getStyleClass().addAll("gravel","grid");
							}
							else{
								labels[row][column].getStyleClass().clear();
								labels[row][column].getStyleClass().addAll("dirt","grid");
							}
						}	
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		bp.setCenter(gp);

		primaryStage.setTitle("Mission Control");
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private VBox setTaskModule(){
		/* Contains module title and module contents */
		VBox module = new VBox();
		module.setMinHeight(500);
		module.setMinWidth(350);
		module.setAlignment(Pos.CENTER);

		HBox title = new HBox();
		Label taskTitle = new Label("ROVER_03: ONLINE");
		taskTitle.getStyleClass().add("taskTitle");
		/* Adds label to HBox */
		title.getChildren().add(taskTitle);
		title.setMinWidth(350);
		/* Sets background to #000000 */
		title.getStyleClass().add("console");
		
		/* Will contain all the coordinates received from other Rovers if any */
		VBox tasks = new VBox();
		/* Scroll Pane needed in case list of coordinates is long
		 * The tasks VBox is inside the scroll pane */
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(tasks);
		scrollPane.setPrefSize(350, 475);
		
		/* Place a contents inside main VBox*/
		module.getChildren().addAll(title,scrollPane);

		new Thread(new Runnable() {
			@Override
			public void run(){
				while (true){
					/* While the rover is online we will have to constantly update the task list on mission control
					 * however, since this particular piece o code is running on a separate thread we would not be able
					 * to access the Application thread. That is why we have to use Platform.runLater */
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							/* Clears contents inside of the scroll pane */
							tasks.getChildren().clear();
							if(queue.isEmpty()){
								/* This message will be shown if there is nothing in the queue */
								Label task = new Label("WAITING FOR NEW TASKS...");
								task.getStyleClass().add("tasks");
								tasks.getChildren().add(task);
							}
							else
								for(Point2D destination: queue.getPositionList()){
									/* Coordinates will be shown for every coordinate in the queue */
									String display = "RECEIVED COORDINATE: [ " + (int)destination.getX() + " , " + (int)destination.getY() + " ]";
									Label task = new Label(display);
									task.getStyleClass().add("tasks");
									tasks.getChildren().add(task);
								}
						}
					});
					
					try {
						/* This thread will execute every second */
						Thread.sleep(1000);
					} catch (Exception e1) {
						continue;
					}
				}
			}
		}).start();

		return module;
	}
	
	public static void main(String args[]) throws IOException, InterruptedException{
		Application.launch();
	}
}
