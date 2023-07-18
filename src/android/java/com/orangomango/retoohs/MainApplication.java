package com.orangomango.retoohs;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.animation.Animation;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;

import com.orangomango.retoohs.ui.HomeScreen;
import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.game.Bullet;

import javafxports.android.FXActivity;
import android.media.MediaPlayer;
import android.os.Build;
import android.media.AudioManager;
import android.os.Vibrator;
import android.content.Context;
import android.view.View;

public class MainApplication extends Application{
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 600;
	public static final int FPS = 40;
	public static Stage stage;
	public static boolean threadsRunning = true;
	
	public static boolean audioPlayed;
	public static String DAMAGE_SOUND = "damage.wav";
	public static String RELOAD_SOUND = "ammo_reload.wav";
	public static String BACKGROUND_MUSIC = "background.wav";
	public static String MENU_BACKGROUND_MUSIC = "menu_background.wav";
	public static String BOSS_BACKGROUND_MUSIC = "boss_battle_background.wav";
	public static String GAMEOVER_BACKGROUND_MUSIC = "gameover_background.wav";
	public static String DEATH_SOUND = "death.wav";
	public static String DROP_SOUND = "drop.wav";
	public static String EXPLOSION_SOUND = "explosion.wav";
	public static String HEAL_SOUND = "extra_life.wav";
	public static String NOAMMO_SOUND = "no_ammo.wav";
	public static String SCORE_SOUND = "score.wav";
	public static String SCORELOST_SOUND = "score_lost.wav";
	public static String BOSSHIT_SOUND = "boss_hit.wav";
	public static String BOSSSUPER_SOUND = "boss_super.wav";
	public static String WARNING_SOUND = "warning.wav";
	public static String SWOOSH_SOUND = "swoosh.wav";
	public static double musicVolume = 1;
	public static double sfxVolume = 1;

	private static Map<String, MediaPlayer> players = new HashMap<>();
	public static Vibrator vibrator = (Vibrator)FXActivity.getInstance().getSystemService(Context.VIBRATOR_SERVICE);

	public static AssetLoader assetLoader;

	static {
		assetLoader = new AssetLoader();
		assetLoader.loadImages();
	}
	
	@Override
	public void start(Stage stage) throws Exception{
		if (Build.VERSION.SDK_INT >= 29){
			Method forName = Class.class.getDeclaredMethod("forName", String.class);
			Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
			Class vmRuntimeClass = (Class) forName.invoke(null, "dalvik.system.VMRuntime");
			Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
			Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[] { String[].class} );
			Object vmRuntime = getRuntime.invoke(null);
			setHiddenApiExemptions.invoke(vmRuntime, (Object[])new String[][]{new String[]{"L"}});
		}

		loadSounds();
		Bullet.loadGunSounds();
		MainApplication.stage = stage;

		HomeScreen hs = new HomeScreen();
		Scene scene = new Scene(hs.getLayout(), WIDTH, HEIGHT);
		stage.setScene(scene);
		stage.show();
	}

	public static void copyFile(String name){
		File file = new File(FXActivity.getInstance().getFilesDir().getAbsolutePath(), name.split("/")[2]);
		if (!file.exists()){
			try {
				Files.copy(MainApplication.class.getResourceAsStream(name), file.toPath());
			} catch (IOException ioe){
				ioe.printStackTrace();
			}
		}
	}
	
	private static void loadSounds(){
		copyFile("/audio/damage.wav");
		copyFile("/audio/ammo_reload.wav");
		copyFile("/audio/background.wav");
		copyFile("/audio/menu_background.wav");
		copyFile("/audio/boss_battle_background.wav");
		copyFile("/audio/gameover_background.wav");
		copyFile("/audio/death.wav");
		copyFile("/audio/drop.wav");
		copyFile("/audio/explosion.wav");
		copyFile("/audio/extra_life.wav");
		copyFile("/audio/no_ammo.wav");
		copyFile("/audio/score.wav");
		copyFile("/audio/score_lost.wav");
		copyFile("/audio/boss_hit.wav");
		copyFile("/audio/boss_super.wav");
		copyFile("/audio/warning.wav");
		copyFile("/audio/swoosh.wav");
	}
	
	public static void playSound(String media, boolean rep){
		if (audioPlayed) return;
		audioPlayed = true;
		schedule(() -> audioPlayed = false, 50);
		MediaPlayer mp = players.getOrDefault(media, new MediaPlayer());
		boolean first = false;
		if (!players.containsKey(media)){
			players.put(media, mp);
			first = true;
		}
		try {
			if (first){
				mp.setDataSource(FXActivity.getInstance().getFilesDir().getAbsolutePath()+"/"+media);
				mp.prepare();
				mp.setLooping(rep);
				mp.setOnCompletionListener(player -> {
					player.release();
					players.remove(media);
				});
			}
			mp.start();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void removeMediaPlayer(String media){
		MediaPlayer mp = players.getOrDefault(media, null);
		if (mp != null){
			mp.stop();
			mp.release();
			players.remove(media);
		}
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
