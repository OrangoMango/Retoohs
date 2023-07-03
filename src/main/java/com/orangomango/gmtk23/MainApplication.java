package com.orangomango.gmtk23;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import com.orangomango.gmtk23.ui.GameScreen;

public class MainApplication extends Application{
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 600;
	public static final int FPS = 40;
	
	@Override
	public void start(Stage stage){
		GameScreen gs = new GameScreen();
		Scene scene = new Scene(gs.getLayout(), WIDTH, HEIGHT);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}
	
	public static void schedule(Runnable r, int time){
		new Thread(() -> {
			try {
				Thread.sleep(time);
				r.run();
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}
		}).start();
	}
	
	public static Image loadImage(String name){
		return new Image(MainApplication.class.getResourceAsStream("/images/"+name));
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
