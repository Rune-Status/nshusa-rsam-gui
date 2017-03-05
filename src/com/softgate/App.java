package com.softgate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class App extends Application {

	private static Stage stage;

	private static Scene scene;

	@Override
	public void init() {
		
		new Thread(new Runnable() {

			@Override
			public void run() {

				if (!Files.exists(AppData.resourcePath)) {
					AppData.resourcePath.toFile().mkdirs();
				}
				
				if (!Files.exists(AppData.storeResourcePath)) {
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.storeResourcePath.toFile()))) {
						writer.println("0:archive");
						writer.println("1:model");
						writer.println("2:animation");
						writer.println("3:music");
						writer.println("4:map");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.resourcePath.toFile(), "stores.txt")))) {			
					
					String line;
					
					while((line = reader.readLine()) != null) {
						
						String[] split = line.split(":");
						
						AppData.storeNames.put(Integer.parseInt(split[0]), split[1]);
						
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				if (!Files.exists(AppData.resourcePath)) {
					AppData.resourcePath.toFile().mkdirs();
				}
				
				if (!Files.exists(AppData.archiveResourcePath)) {
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.archiveResourcePath.toFile()))) {
						writer.println("0:empty");
						writer.println("1:title screen.jag");
						writer.println("2:config.jag");
						writer.println("3:interface.jag");
						writer.println("4:2d graphics.jag");
						writer.println("5:version list.jag");
						writer.println("6:textures.jag");
						writer.println("7:chat system.jag");
						writer.println("8:sound effects.jag");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.resourcePath.toFile(), "archives.txt")))) {			
					
					String line;
					
					while((line = reader.readLine()) != null) {
						
						String[] split = line.split(":");
						
						AppData.archiveNames.put(Integer.parseInt(split[0]), split[1]);
						
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			
		}).start();

	}

	@Override
	public void start(Stage stage) {
		App.stage = stage;
		try {
			Parent root = FXMLLoader.load(App.class.getResource("/Main.fxml"));
			Scene scene = new Scene(root);
			App.scene = scene;
			scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
			stage.setScene(scene);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon_128.png")));
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setResizable(false);
			stage.centerOnScreen();
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static Stage getStage() {
		return stage;
	}

	public static Scene getScene() {
		return scene;
	}

}
