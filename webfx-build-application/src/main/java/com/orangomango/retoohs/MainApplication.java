package com.orangomango.retoohs;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.*;
import javafx.animation.Animation;

import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.scheduler.Scheduler;

import com.orangomango.retoohs.ui.HomeScreen;
import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.game.Bullet;

public class MainApplication extends Application{
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 600;
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
		DAMAGE_SOUND = new AudioClip(Resource.toUrl("/audio/damage.wav", MainApplication.class));
		RELOAD_SOUND = new AudioClip(Resource.toUrl("/audio/ammo_reload.wav", MainApplication.class));
		BACKGROUND_MUSIC = new Media(Resource.toUrl("/audio/background.wav", MainApplication.class));
		MENU_BACKGROUND_MUSIC = new Media(Resource.toUrl("/audio/menu_background.wav", MainApplication.class));
		GAMEOVER_BACKGROUND_MUSIC = new Media(Resource.toUrl("/audio/gameover_background.wav", MainApplication.class));
		BOSS_BACKGROUND_MUSIC = new Media(Resource.toUrl("/audio/boss_battle.wav", MainApplication.class));
		DEATH_SOUND = new AudioClip(Resource.toUrl("/audio/death.wav", MainApplication.class));
		DROP_SOUND = new AudioClip(Resource.toUrl("/audio/drop.wav", MainApplication.class));
		EXPLOSION_SOUND = new AudioClip(Resource.toUrl("/audio/explosion.wav", MainApplication.class));
		HEAL_SOUND = new AudioClip(Resource.toUrl("/audio/extra_life.wav", MainApplication.class));
		NOAMMO_SOUND = new AudioClip(Resource.toUrl("/audio/no_ammo.wav", MainApplication.class));
		SCORE_SOUND = new AudioClip(Resource.toUrl("/audio/score.wav", MainApplication.class));
		SCORELOST_SOUND = new AudioClip(Resource.toUrl("/audio/score_lost.wav", MainApplication.class));
		BOSSHIT_SOUND = new AudioClip(Resource.toUrl("/audio/boss_hit.wav", MainApplication.class));
		BOSSSUPER_SOUND = new AudioClip(Resource.toUrl("/audio/boss_super.wav", MainApplication.class));
		WARNING_SOUND = new AudioClip(Resource.toUrl("/audio/warning.wav", MainApplication.class));
		SWOOSH_SOUND = new AudioClip(Resource.toUrl("/audio/swoosh.wav", MainApplication.class));
	}
	
	public static MediaPlayer playSound(Media media, boolean rep){
		if (audioPlayed) return null;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		MediaPlayer player = new MediaPlayer(media);
		if (rep) player.setCycleCount(Animation.INDEFINITE);
		else player.setOnEndOfMedia(() -> player.dispose());
		player.play();
		return player;
	}
	
	public static void playSound(AudioClip player, boolean rep){
		if (audioPlayed) return;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		if (rep) player.setCycleCount(Animation.INDEFINITE);
		player.play();
	}
	
	public static void schedule(Runnable r, int time){
		Scheduler.scheduleDelay(time, r);
	}
	
	public static void schedulePeriodic(Runnable r, int time){
		Scheduler.schedulePeriodic(time, scheduled -> {
			if (threadsRunning){
				r.run();
			} else {
				scheduled.cancel();
			}
		});
	}
	
	public static Image loadImage(String name){
		return new Image(Resource.toUrl("/images/"+name, MainApplication.class));
	}
	
	public static void main(String[] args){
		launch(args);
	}
}
