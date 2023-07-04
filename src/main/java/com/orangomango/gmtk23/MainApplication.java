package com.orangomango.gmtk23;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.*;
import javafx.animation.Animation;

import com.orangomango.gmtk23.ui.HomeScreen;
import com.orangomango.gmtk23.game.Bullet;

public class MainApplication extends Application{
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 600;
	public static final int FPS = 40;
	public static Stage stage;
	
	private static boolean audioPlayed;
	public static AudioClip DAMAGE_SOUND;
	public static AudioClip RELOAD_SOUND;
	public static Media BACKGROUND_MUSIC;
	public static AudioClip DEATH_SOUND;
	public static AudioClip DROP_SOUND;
	public static AudioClip EXPLOSION_SOUND;
	public static AudioClip HEAL_SOUND;
	public static AudioClip NOAMMO_SOUND;
	public static AudioClip SCORE_SOUND;
	public static AudioClip SCORELOST_SOUND;
	
	@Override
	public void start(Stage stage){
		loadSounds();
		Bullet.loadGunSounds();
		MainApplication.stage = stage;

		HomeScreen hs = new HomeScreen();
		Scene scene = new Scene(hs.getLayout(), WIDTH, HEIGHT);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}
	
	private static void loadSounds(){
		DAMAGE_SOUND = new AudioClip(MainApplication.class.getResource("/audio/damage.wav").toExternalForm());
		RELOAD_SOUND = new AudioClip(MainApplication.class.getResource("/audio/ammo_reload.wav").toExternalForm());
		BACKGROUND_MUSIC = new Media(MainApplication.class.getResource("/audio/background.wav").toExternalForm());
		DEATH_SOUND = new AudioClip(MainApplication.class.getResource("/audio/death.wav").toExternalForm());
		DROP_SOUND = new AudioClip(MainApplication.class.getResource("/audio/drop.wav").toExternalForm());
		EXPLOSION_SOUND = new AudioClip(MainApplication.class.getResource("/audio/explosion.wav").toExternalForm());
		HEAL_SOUND = new AudioClip(MainApplication.class.getResource("/audio/extra_life.wav").toExternalForm());
		NOAMMO_SOUND = new AudioClip(MainApplication.class.getResource("/audio/no_ammo.wav").toExternalForm());
		SCORE_SOUND = new AudioClip(MainApplication.class.getResource("/audio/score.wav").toExternalForm());
		SCORELOST_SOUND = new AudioClip(MainApplication.class.getResource("/audio/score_lost.wav").toExternalForm());
	}
	
	public static void playSound(Media media, boolean rep){
		if (audioPlayed) return;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		MediaPlayer player = new MediaPlayer(media);
		if (rep) player.setCycleCount(Animation.INDEFINITE);
		else player.setOnEndOfMedia(() -> player.dispose());
		player.play();
	}
	
	public static void playSound(AudioClip player, boolean rep){
		if (audioPlayed) return;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
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
	
	public static Image loadImage(String name){
		return new Image(MainApplication.class.getResourceAsStream("/images/"+name));
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
