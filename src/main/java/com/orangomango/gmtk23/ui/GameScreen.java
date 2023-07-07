package com.orangomango.gmtk23.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.*;
import javafx.scene.text.Font;
import javafx.animation.*;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.media.Media;

import java.util.*;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.game.*;

public class GameScreen{
	private static GameScreen instance = null;
	public static final Font FONT_45 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 45);
	public static final Font FONT_30 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 30);
	public static final Font FONT_15 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 15);
	private static final int BOSS_SCORE = 4000;
	
	private List<GameObject> gameObjects = new ArrayList<>();
	private List<FloatingText> fTexts = new ArrayList<>();
	private List<Drop> drops = new ArrayList<>();
	private List<Explosion> explosions = new ArrayList<>();
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private Player player;
	public int score;
	private long lastExplosion;
	private long lastHeal;
	private volatile boolean gameRunning = true;
	private BonusPoint bpoint1, bpoint2;
	private long startTime;
	private Timeline loop;
	private MediaPlayer mediaPlayer;
	private boolean paused;
	private long lastPaused;
	private int pausedTime;
	private Image pausedImage;
	private Boss currentBoss;
	private int bossExtraScore, lastBossScore;
	private boolean shaking;
	private double cameraShakeX, cameraShakeY;
	
	private Image groundImage = MainApplication.loadImage("ground.png");
	private Image[] stoneGroundImages = new Image[]{MainApplication.loadImage("ground_stone_0.png"), MainApplication.loadImage("ground_stone_1.png")};
	private int[][] groundPattern;
	
	public GameScreen(){
		if (instance != null){
			throw new IllegalStateException("instance != null");
		}
		instance = this;
		this.mediaPlayer = MainApplication.playSound(MainApplication.BACKGROUND_MUSIC, true);
	}
	
	public static GameScreen getInstance(){
		return instance;
	}
	
	public List<GameObject> getGameObjects(){
		return this.gameObjects;
	}
	
	public List<FloatingText> getFloatingTexts(){
		return this.fTexts;
	}
	
	public List<Explosion> getExplosions(){
		return this.explosions;
	}
	
	public Map<KeyCode, Boolean> getKeys(){
		return this.keys;
	}
	
	public int getPausedTime(){
		return this.pausedTime;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.P){
				setPause(!this.paused);
				if (this.paused){
					this.pausedImage = canvas.snapshot(null, new WritableImage(MainApplication.WIDTH, MainApplication.HEIGHT));
				}
			} else {
				this.keys.put(e.getCode(), true);
			}
		});
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		GraphicsContext gc = canvas.getGraphicsContext2D();

		this.player = new Player(gc, 300, 300);
		this.gameObjects.add(this.player);
		Bullet.applyConfiguration("normal_gun", null, null, 0, 0, 0, this.player);
		
		EventHandler<MouseEvent> mouseEvent = e -> {
			if (this.paused) return;
			if (e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY){
				long diff = System.currentTimeMillis()-this.lastExplosion;
				boolean exp = e.getButton() == MouseButton.SECONDARY && diff > 10000;
				if (exp){
					this.lastExplosion = System.currentTimeMillis();
				}
				if (Bullet.configs.get(this.player).getAmmo() == 0){
					reloadAmmo();
				}
				this.player.shoot(Math.atan2(e.getY()-this.player.getY(), e.getX()-this.player.getX()), exp);
			}
		};
		canvas.setOnMousePressed(mouseEvent);
		canvas.setOnMouseDragged(mouseEvent);
		
		canvas.setOnMouseMoved(e -> this.player.pointGun(Math.atan2(e.getY()-this.player.getY(), e.getX()-this.player.getX())));
		
		Thread spawner = new Thread(() -> {
			Random random = new Random();
			try {
				Thread.sleep(3000);
				while (this.gameRunning){
					if (this.paused) continue;
					int type = 0;
					if (this.score > 1500){
						int delta = this.score-1500;
						int n = delta/2000;
						type = random.nextInt(n+1);
					}
					if (type > 4) type = 4;
					this.gameObjects.add(new Enemy(gc, random.nextInt(MainApplication.WIDTH-200)+100, random.nextInt(MainApplication.HEIGHT-200)+100, this.player, type));
					Thread.sleep((random.nextInt(1000)+1000)*(this.currentBoss != null ? 2 : 1));
				}
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}
		});
		spawner.setDaemon(true);
		spawner.start();
		
		this.bpoint1 = new BonusPoint(gc, 0, 0);
		this.bpoint2 = new BonusPoint(gc, 0, 0);
		this.bpoint1.relocate();
		this.bpoint2.relocate();
		
		Random random = new Random();
		this.groundPattern = new int[MainApplication.WIDTH/64+1][MainApplication.HEIGHT/64+1];
		for (int x = 0; x < this.groundPattern.length; x++){
			for (int y = 0; y < this.groundPattern[0].length; y++){
				this.groundPattern[x][y] = Math.random() > 0.75 ? random.nextInt(this.stoneGroundImages.length) : -1;
			}
		}
		
		this.startTime = System.currentTimeMillis();
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void changeMusic(Media music){
		if (this.mediaPlayer != null) this.mediaPlayer.stop();
		MainApplication.audioPlayed = false;
		this.mediaPlayer = MainApplication.playSound(music, true);
	}
	
	public void screenShake(){
		if (this.shaking) return;
		this.shaking = true;
		MainApplication.schedule(() -> this.cameraShakeX = -5, 50);
		MainApplication.schedule(() -> this.cameraShakeX = 0, 150);
		MainApplication.schedule(() -> this.cameraShakeY = -5, 200);
		MainApplication.schedule(() -> this.cameraShakeY = 0, 250);
		MainApplication.schedule(() -> this.shaking = false, 300);
	}
	
	private void spawnBoss(GraphicsContext gc){
		this.currentBoss = new Boss(gc, 600, 300);
		this.gameObjects.add(this.currentBoss);
		this.lastBossScore = this.score;
		changeMusic(MainApplication.BOSS_BACKGROUND_MUSIC);
	}
	
	private void quit(){
		this.loop.stop();
		this.gameRunning = false;
		MainApplication.threadsRunning = false;
		MainApplication.schedule(() -> MainApplication.threadsRunning = true, 500);
		MainApplication.schedule(() -> GameScreen.instance = null, 500);
		this.player.stopAnimation();
		if (this.mediaPlayer != null){
			this.mediaPlayer.stop();
		}
	}
	
	private void setPause(boolean p){
		this.paused = p;
		if (this.paused){
			this.lastPaused = System.currentTimeMillis();
		} else {
			long diff = System.currentTimeMillis()-this.lastPaused;
			this.pausedTime += diff;
			this.pausedImage = null;
		}
	}
	
	private void reloadAmmo(){
		Bullet.configs.get(this.player).reload();
		MainApplication.playSound(MainApplication.RELOAD_SOUND, false);
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.LIME);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		
		if (this.pausedImage != null){
			gc.drawImage(this.pausedImage, 0, 0);
			gc.save();
			gc.setGlobalAlpha(0.6);
			gc.setFill(Color.BLACK);
			gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
			gc.restore();
			return;
		}
		
		if (this.score < 0) this.score = 0;
		if (this.score-this.bossExtraScore-this.lastBossScore >= BOSS_SCORE && this.currentBoss == null){
			spawnBoss(gc);
		}
		
		gc.save();
		gc.translate(-this.cameraShakeX, -this.cameraShakeY);
		
		for (int x = 0; x < MainApplication.WIDTH; x += 64){
			for (int y = 0; y < MainApplication.HEIGHT; y += 64){
				int index = this.groundPattern[x/64][y/64];
				gc.drawImage(index >= 0 ? this.stoneGroundImages[index] : this.groundImage, x, y, 64, 64);
			}
		}
		
		// Render bonuspoint
		this.bpoint1.render();
		this.bpoint2.render();
		
		// Render gameObjects
		for (int i = 0; i < this.gameObjects.size(); i++){
			GameObject obj = this.gameObjects.get(i);
			obj.render();
			if (obj.getHP() <= 0){
				this.gameObjects.remove(obj);
				Bullet.configs.remove(obj);
				if (obj instanceof Player){
					MainApplication.playSound(MainApplication.DEATH_SOUND, false);
					quit();
					GameOverScreen gos = new GameOverScreen();
					MainApplication.stage.getScene().setRoot(gos.getLayout());
					return;
				}
				i--;
				if (obj instanceof Enemy && Math.random() > 0.85){ // 85%
					this.drops.add(new Drop(gc, obj.getX(), obj.getY()));
				}
				if (obj instanceof Boss){
					this.currentBoss = null;
					this.score += 400;
					this.bossExtraScore += this.score-this.lastBossScore;
					changeMusic(MainApplication.BACKGROUND_MUSIC);
				}
			}
		}
		
		// Render explosion
		for (int i = 0; i < this.explosions.size(); i++){
			Explosion explosion = this.explosions.get(i);
			explosion.render();
			if (!explosion.exists()){
				this.explosions.remove(explosion);
				i--;
			}
		}
		
		// Render floatingTexts
		for (int i = 0; i < this.fTexts.size(); i++){
			FloatingText ft = this.fTexts.get(i);
			ft.render();
			if (ft.getDeltaY() > 40){
				this.fTexts.remove(ft);
				i--;
			}
		}
		
		// Render drops
		for (int i = 0; i < this.drops.size(); i++){
			Drop dr = this.drops.get(i);
			dr.render();
			if (!dr.exists()){
				this.drops.remove(dr);
				i--;
			}
		}
		
		final double playerSpeed = 4;
		if (this.keys.getOrDefault(KeyCode.W, false)){
			this.player.move(0, -playerSpeed, false);
			this.player.setState(Player.State.MOVING_UP);
		} else if (this.keys.getOrDefault(KeyCode.A, false)){
			this.player.move(-playerSpeed, 0, false);
			this.player.setState(Player.State.MOVING_LEFT);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){
			this.player.move(0, playerSpeed, false);
			this.player.setState(Player.State.MOVING_DOWN);
		} else if (this.keys.getOrDefault(KeyCode.D, false)){
			this.player.move(playerSpeed, 0, false);
			this.player.setState(Player.State.MOVING_RIGHT);
		} else {
			this.player.setState(Player.State.IDLE);
		}
		
		if ((this.keys.getOrDefault(KeyCode.Q, false) && this.player.getHP() < 90) || this.player.getHP() < 40){
			// heal
			long diff = System.currentTimeMillis()-this.lastHeal;
			if (diff > 30000){
				MainApplication.playSound(MainApplication.HEAL_SOUND, false);
				this.player.heal(60);
				this.lastHeal = System.currentTimeMillis();
			}
			this.keys.put(KeyCode.Q, false);
		}
		int am = Bullet.configs.get(this.player).getAmmo();
		int dam = Bullet.configs.get(this.player).getDefaultAmmo();
		if (this.keys.getOrDefault(KeyCode.R, false) && am != dam){
			reloadAmmo();
			this.keys.put(KeyCode.R, false);
		}
		
		if (this.keys.getOrDefault(KeyCode.ESCAPE, false)){
			quit();
			HomeScreen hs = new HomeScreen();
			MainApplication.stage.getScene().setRoot(hs.getLayout());
			return;
		}
		
		gc.restore();
		
		gc.setLineWidth(3);
		gc.setGlobalAlpha(0.7);
		gc.setStroke(Color.BLACK);
		
		// HP bar
		gc.setFill(Color.GREEN);
		gc.fillRect(20, 60, 200*this.player.getHP()/100.0, 30);
		gc.strokeRect(20, 60, 200, 30);
		
		// Explosion bar
		gc.setFill(Color.YELLOW);
		gc.fillRect(20, 100, 200*Math.min(1, (System.currentTimeMillis()-this.lastExplosion)/10000.0), 20);
		gc.strokeRect(20, 100, 200, 20);
		
		// Heal bar
		gc.setFill(Color.CYAN);
		gc.fillRect(20, 130, 200*Math.min(1, (System.currentTimeMillis()-this.lastHeal)/30000.0), 20);
		gc.strokeRect(20, 130, 200, 20);
		
		// Ammo
		int an = Bullet.configs.get(this.player).getAmmoAmount();
		int dan = Bullet.configs.get(this.player).getDefaultAmount();
		gc.setFill(Color.ORANGE);
		double ah = 65*((double)am/dam);
		gc.fillRect(230, 60+(65-ah), 30, ah);
		gc.strokeRect(230, 60, 30, 65);
		gc.fillRect(230, 130, 30*((double)an/dan), 20);
		gc.strokeRect(230, 130, 30, 20);
		
		// Player has no ammo and can't reload
		if (am == 0 && an == 0){
			this.player.setGun("small_gun");
		}

		if (this.currentBoss != null){
			// Boss health bar
			gc.setFill(Color.ORANGE);
			gc.fillRect(400, 40, 450*((double)this.currentBoss.getHP()/Boss.HEALTH), 40);
			gc.strokeRect(400, 40, 450, 40);
		} else {
			// Boss bar
			gc.setFill(Color.PURPLE);
			gc.fillRect(20, 160, 200*((this.score-this.bossExtraScore)%BOSS_SCORE/(double)(BOSS_SCORE)), 20);
			gc.strokeRect(20, 160, 200, 20);
		}
		
		gc.setGlobalAlpha(1);
		
		// Score
		gc.setFill(Color.BLACK);
		gc.setFont(FONT_45);
		gc.fillText(Integer.toString(this.score), 20, 40);
		
		// Time
		gc.setFill(Color.BLACK);
		gc.setFont(FONT_30);
		long diff = System.currentTimeMillis()-this.startTime-this.pausedTime;
		gc.fillText(String.format("%2d:%02d", diff/60000, diff/1000%60), 20, MainApplication.HEIGHT-20);
	}
}
