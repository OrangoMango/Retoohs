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
	public static final int WIDTH = 500; //1000;
	public static final int HEIGHT = 300; //600;
	public static final double SCALE = 0.5;
	public static final int FPS = 40;
	public static Stage stage;
	public static boolean threadsRunning = true;
	
	public static boolean audioPlayed;
	public static AudioClip DAMAGE_SOUND;
	public static AudioClip RELOAD_SOUND;
	public static Media BACKGROUND_MUSIC;
	public static Media MENU_BACKGROUND_MUSIC;
	public static Media BOSS_BACKGROUND_MUSIC;
	public static Media GAMEOVER_BACKGROUND_MUSIC;
	public static AudioClip DEATH_SOUND;
	public static AudioClip DROP_SOUND;
	public static AudioClip EXPLOSION_SOUND;
	public static AudioClip HEAL_SOUND;
	public static AudioClip NOAMMO_SOUND;
	public static AudioClip SCORE_SOUND;
	public static AudioClip SCORELOST_SOUND;
	public static AudioClip BOSSHIT_SOUND;
	public static AudioClip BOSSSUPER_SOUND;
	public static AudioClip WARNING_SOUND;
	public static AudioClip SWOOSH_SOUND;
	public static double musicVolume = 1;
	public static double sfxVolume = 1;

	public static AssetLoader assetLoader;

	static {
		assetLoader = new AssetLoader();
		assetLoader.loadImages();
	}
	
	@Override
	public void start(Stage stage){
		loadSounds();
		Bullet.loadGunSounds();
		MainApplication.stage = stage;

		HomeScreen hs = new HomeScreen();
		Scene scene = new Scene(hs.getLayout(), WIDTH, HEIGHT);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle("Retoohs by OrangoMango");
		stage.getIcons().add(assetLoader.getImage("icon.png"));
		stage.show();
	}
	
	private static void loadSounds(){
		DAMAGE_SOUND = new AudioClip(MainApplication.class.getResource("/audio/damage.wav").toExternalForm());
		RELOAD_SOUND = new AudioClip(MainApplication.class.getResource("/audio/ammo_reload.wav").toExternalForm());
		BACKGROUND_MUSIC = new Media(MainApplication.class.getResource("/audio/background.wav").toExternalForm());
		MENU_BACKGROUND_MUSIC = new Media(MainApplication.class.getResource("/audio/menu_background.wav").toExternalForm());
		GAMEOVER_BACKGROUND_MUSIC = new Media(MainApplication.class.getResource("/audio/gameover_background.wav").toExternalForm());
		BOSS_BACKGROUND_MUSIC = new Media(MainApplication.class.getResource("/audio/boss_battle_background.wav").toExternalForm());
		DEATH_SOUND = new AudioClip(MainApplication.class.getResource("/audio/death.wav").toExternalForm());
		DROP_SOUND = new AudioClip(MainApplication.class.getResource("/audio/drop.wav").toExternalForm());
		EXPLOSION_SOUND = new AudioClip(MainApplication.class.getResource("/audio/explosion.wav").toExternalForm());
		HEAL_SOUND = new AudioClip(MainApplication.class.getResource("/audio/extra_life.wav").toExternalForm());
		NOAMMO_SOUND = new AudioClip(MainApplication.class.getResource("/audio/no_ammo.wav").toExternalForm());
		SCORE_SOUND = new AudioClip(MainApplication.class.getResource("/audio/score.wav").toExternalForm());
		SCORELOST_SOUND = new AudioClip(MainApplication.class.getResource("/audio/score_lost.wav").toExternalForm());
		BOSSHIT_SOUND = new AudioClip(MainApplication.class.getResource("/audio/boss_hit.wav").toExternalForm());
		BOSSSUPER_SOUND = new AudioClip(MainApplication.class.getResource("/audio/boss_super.wav").toExternalForm());
		WARNING_SOUND = new AudioClip(MainApplication.class.getResource("/audio/warning.wav").toExternalForm());
		SWOOSH_SOUND = new AudioClip(MainApplication.class.getResource("/audio/swoosh.wav").toExternalForm());
	}
	
	public static MediaPlayer playSound(Media media, boolean rep){
		if (audioPlayed) return null;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		MediaPlayer player = new MediaPlayer(media);
		player.setVolume(musicVolume);
		if (rep) player.setCycleCount(Animation.INDEFINITE);
		else player.setOnEndOfMedia(() -> player.dispose());
		player.play();
		return player;
	}
	
	public static void playSound(AudioClip player, boolean rep){
		if (audioPlayed) return;
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
