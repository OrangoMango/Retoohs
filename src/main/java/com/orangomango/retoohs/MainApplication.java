package com.orangomango.retoohs;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.*;
import javafx.animation.Animation;

import com.orangomango.retoohs.ui.HomeScreen;
import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.game.Bullet;

public class MainApplication extends Application{
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 600;
	public static final double SCALE = 1;
	public static final int FPS = 40;
	public static Stage stage;
	public static boolean threadsRunning = true;
	
	public static boolean audioPlayed;
	public static double musicVolume = 1;
	public static double sfxVolume = 1;
	
	@Override
	public void start(Stage stage){
		MainApplication.stage = stage;
		HomeScreen hs = new HomeScreen();
		Scene scene = new Scene(hs.getLayout(), WIDTH, HEIGHT);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Retoohs by OrangoMango");
		stage.getIcons().add(AssetLoader.getInstance().getImage("icon.png"));
		stage.show();
	}
	
	public static MediaPlayer playMusic(String musicName){
		if (audioPlayed) return null;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		MediaPlayer player = new MediaPlayer(AssetLoader.getInstance().getMusic(musicName));
		player.setVolume(musicVolume);
		player.setCycleCount(Animation.INDEFINITE);
		player.play();
		return player;
	}
	
	public static void playSound(String audioName, boolean rep){
		if (audioPlayed) return;
		AudioClip player = AssetLoader.getInstance().getAudio(audioName);
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		player.setVolume(sfxVolume);
		if (rep) player.setCycleCount(Animation.INDEFINITE);
		player.play();
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
	
	public static void schedulePeriodic(Runnable r, int time){
		Thread loop = new Thread(() -> {
			while (threadsRunning){
				try {
					r.run();
					Thread.sleep(time);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		loop.setDaemon(true);
		loop.start();
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
