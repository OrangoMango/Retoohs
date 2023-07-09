package com.orangomango.retoohs.ui;

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
import javafx.geometry.Point2D;

import dev.webfx.platform.resource.Resource;
import dev.webfx.platform.scheduler.Scheduler;

import java.util.*;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.game.*;

public class GameScreen{
	private static GameScreen instance = null;
	public static final Font FONT_45 = Font.loadFont(Resource.toUrl("/files/main_font.ttf", GameScreen.class), 45);
	public static final Font FONT_30 = Font.loadFont(Resource.toUrl("/files/main_font.ttf", GameScreen.class), 30);
	public static final Font FONT_15 = Font.loadFont(Resource.toUrl("/files/main_font.ttf", GameScreen.class), 15);
	private static final int BOSS_SCORE = 2000;
	
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
	public BonusPoint targetPoint;
	public Drop targetDrop;
	private long startTime;
	private Timeline loop;
	private MediaPlayer mediaPlayer;
	private boolean paused, tempStopped;
	private long lastPaused;
	private int pausedTime;
	private Image pausedImage;
	private Boss currentBoss;
	private int bossExtraScore, lastBossScore;
	private boolean shaking;
	private double cameraShakeX, cameraShakeY;
	private boolean autoShoot = true;
	public Enemy selectedEnemy;
	public boolean playsPlayer = true;
	private Reverser reverser;
	private List<MenuButton> pauseButtons = new ArrayList<>();
	
	private Image groundImage = MainApplication.loadImage("ground.png");
	private Image[] stoneGroundImages = new Image[]{MainApplication.loadImage("ground_stone_0.png"), MainApplication.loadImage("ground_stone_1.png")};
	private int[][] groundPattern;
	private Image reverseImage = MainApplication.loadImage("reverse.png");
	private volatile int reverseIndex = 0;
	
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
	
	public boolean isPaused(){
		return this.paused;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public Boss getCurrentBoss(){
		return this.currentBoss;
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
			if (this.paused){
				for (MenuButton mb : this.pauseButtons){
					mb.click(e.getX(), e.getY());
				}
				return;
			}
			if (this.currentBoss == null && !this.playsPlayer) return;
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

		MainApplication.schedule(() -> {
			Random random = new Random();
			Scheduler.schedulePeriodic(1500, scheduled -> {
				if (this.gameRunning && MainApplication.threadsRunning){
					if (this.paused) return;
					int type = 0;
                                       	if (this.score > 500){
                                                int delta = this.score-500;
                       	                        int n = delta/1000;
                               	                type = random.nextInt(n+1);
                                        }
                       	                if (type > 4) type = 4;
                               	        Enemy e = new Enemy(gc, random.nextInt(MainApplication.WIDTH-200)+100, random.nextInt(MainApplication.HEIGHT-200)+100, this.player, type);
                  	                if (this.selectedEnemy == null && !this.playsPlayer){
                       	                        this.selectedEnemy = e;
                               	        }
                                        this.gameObjects.add(e);
				} else {
					scheduled.cancel();
				}
			});
		}, 6000);

		this.bpoint1 = new BonusPoint(gc, 0, 0);
		this.bpoint2 = new BonusPoint(gc, 0, 0);
		this.bpoint1.relocate();
		this.bpoint2.relocate();

		Random random = new Random();		
		this.reverser = new Reverser(gc);

		this.groundPattern = new int[MainApplication.WIDTH/64+1][MainApplication.HEIGHT/64+1];
		for (int x = 0; x < this.groundPattern.length; x++){
			for (int y = 0; y < this.groundPattern[0].length; y++){
				this.groundPattern[x][y] = Math.random() > 0.75 ? random.nextInt(this.stoneGroundImages.length) : -1;
			}
		}
		
		this.startTime = System.currentTimeMillis();
		
		Image homeButtonImage = MainApplication.loadImage("warning.png");
		this.pauseButtons.add(new MenuButton(gc, 420, 300, 64, 64, homeButtonImage, () -> {
			quit();
			HomeScreen hs = new HomeScreen();
			MainApplication.stage.getScene().setRoot(hs.getLayout());
		}));
		Image resumeButtonImage = MainApplication.loadImage("warning.png");
		this.pauseButtons.add(new MenuButton(gc, 520, 300, 64, 64, resumeButtonImage, () -> setPause(false)));
		
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
	
	public void tempstop(Canvas canvas){
		setPause(true);
		this.tempStopped = true;
		this.pausedImage = canvas.snapshot(null, new WritableImage(MainApplication.WIDTH, MainApplication.HEIGHT));
		MainApplication.playSound(MainApplication.SWOOSH_SOUND, false);
		Timeline anim = new Timeline(new KeyFrame(Duration.millis(100), e -> this.reverseIndex++));
		anim.setCycleCount(5);
		anim.setOnFinished(e -> this.reverseIndex = 0);
		anim.play();
		MainApplication.schedule(() -> {
			setPause(false);
			this.tempStopped = false;
		}, 500);
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
	
	private GameObject findNearestEnemy(double x, double y, double dist){
		GameObject found = null;
		double minDistance = Double.POSITIVE_INFINITY;
		Point2D pos = new Point2D(x, y);
		for (int i = 0; i < this.gameObjects.size(); i++){
			GameObject obj = this.gameObjects.get(i);
			if (obj instanceof Player) continue;
			double d = pos.distance(new Point2D(obj.getX(), obj.getY()));
			if (d <= dist && d < minDistance){
				minDistance = d;
				found = obj;
			}
		}
		return found;
	}
	
	/**
	 * @param axis x(true) y(false)
	 * @param min min(true) max(false)
	 */
	private Enemy selectEnemy(Enemy e, boolean axis, boolean min){
		Point2D start = new Point2D(e.getX(), e.getY());
		double minDistance = Double.POSITIVE_INFINITY;
		GameObject found = null;
		for (int i = 0; i < this.gameObjects.size(); i++){
			GameObject obj = this.gameObjects.get(i);
			if (obj == e || !(obj instanceof Enemy)) continue;
			Point2D ePos = new Point2D(obj.getX(), obj.getY());
			double distance = ePos.distance(start);
			if (distance < minDistance){
				if (axis){
					if (min){
						if (obj.getX() <= e.getX()){
							found = obj;
							minDistance = distance;
						}
					} else {
						if (obj.getX() >= e.getX()){
							found = obj;
							minDistance = distance;
						}
					}
				} else {
					if (min){
						if (obj.getY() <= e.getY()){
							found = obj;
							minDistance = distance;
						}
					} else {
						if (obj.getY() >= e.getY()){
							found = obj;
							minDistance = distance;
						}
					}
				}
			}
		}
		return found == null ? e : (Enemy)found;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.LIME);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		Random random = new Random();
		
		if (this.pausedImage != null){
			gc.drawImage(this.pausedImage, 0, 0);
			if (this.tempStopped){
				gc.drawImage(this.reverseImage, 1+66*this.reverseIndex, 1, 64, 64, MainApplication.WIDTH/2-64, MainApplication.HEIGHT/2-64, 128, 128);
			} else {
				gc.save();
				gc.setGlobalAlpha(0.6);
				gc.setFill(Color.BLACK);
				gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
				gc.restore();
				for (MenuButton mb : this.pauseButtons){
					mb.render();
				}
			}
			return;
		}
		
		if (this.score < 0) this.score = 0;
		if (this.score-this.bossExtraScore-this.lastBossScore >= BOSS_SCORE && this.currentBoss == null && this.playsPlayer){
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
			if (obj instanceof Enemy && this.selectedEnemy == null && !this.playsPlayer){
				this.selectedEnemy = (Enemy)obj;
			}
			if (obj.getHP() <= 0){
				if (!(obj instanceof Player) || this.playsPlayer){
					this.gameObjects.remove(obj);
					Bullet.configs.remove(obj);
				}
				if (this.selectedEnemy == obj){
					this.selectedEnemy = null;
				}
				if (obj instanceof Player){
					if (this.playsPlayer){
						MainApplication.playSound(MainApplication.DEATH_SOUND, false);
						quit();
						GameOverScreen gos = new GameOverScreen();
						MainApplication.stage.getScene().setRoot(gos.getLayout());
						return;
					} else {
						this.playsPlayer = true;
						obj.heal(100);
						this.reverser.allowStart();
						this.score += 250;
						tempstop(gc.getCanvas());
					}
				}
				i--;
				if (!this.playsPlayer){
					this.score -= 5;
				}
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
		
		// There are no selected enemies when the player is the user.
		if (this.playsPlayer){
			this.selectedEnemy = null;
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
		
		final double playerSpeed = this.playsPlayer ? 4 : 3;
		if (this.currentBoss == null && !this.playsPlayer){
			GameObject nearestEnemy = findNearestEnemy(this.player.getX(), this.player.getY(), Bullet.getBulletConfig(this.player.getCurrentGun()).getDouble("maxDistance"));
			if (nearestEnemy != null && !nearestEnemy.isInvulnerable()){
				Point2D pPos = new Point2D(this.player.getX(), this.player.getY());
				Point2D ePos = new Point2D(nearestEnemy.getX(), nearestEnemy.getY());
				if (this.autoShoot){
					this.autoShoot = false;
					double angle = Math.atan2(ePos.getY()-pPos.getY(), ePos.getX()-pPos.getX());
					this.player.shoot(angle, false);
					this.player.pointGun(angle);
					if (Bullet.configs.get(this.player).getAmmo() == 0){
						reloadAmmo();
					}
					MainApplication.schedule(() -> this.autoShoot = true, Bullet.getBulletConfig(this.player.getCurrentGun()).getInteger("cooldown"));
				}
				double eDistance = pPos.distance(ePos);
				if (eDistance < Bullet.getBulletConfig(this.player.getCurrentGun()).getDouble("maxDistance")/3){
					double deltaX = ePos.getX()-pPos.getX();
					double deltaY = ePos.getY()-pPos.getY();
					if (Math.abs(deltaX) > playerSpeed){
						this.player.move(-playerSpeed*(deltaX > 0 ? 1 : -1), 0, false);
					} else {
						this.player.move(0, -playerSpeed*(deltaY > 0 ? 1 : -1), false);
					}
				}
			} else {
				// Nearest bonus/drop point
				BonusPoint bPoint = this.targetPoint;
				if (bPoint == null){
					Point2D pPos = new Point2D(this.player.getX(), this.player.getY());
					Point2D b1Pos = new Point2D(this.bpoint1.getX(), this.bpoint1.getY());
					Point2D b2Pos = new Point2D(this.bpoint2.getX(), this.bpoint2.getY());
					double b1dist = pPos.distance(b1Pos);
					double b2dist = pPos.distance(b2Pos);
					bPoint = b1dist < b2dist ? this.bpoint1 : this.bpoint2;
					this.targetPoint = bPoint;
				}
				Point2D tPoint = new Point2D(bPoint.getX(), bPoint.getY());
				if (this.targetDrop != null){
					tPoint = new Point2D(this.targetDrop.getX(), this.targetDrop.getY());
				}
				double deltaX = tPoint.getX()-this.player.getX();
				double deltaY = tPoint.getY()-this.player.getY();
				if (Math.abs(deltaX) > playerSpeed){
					this.player.move(playerSpeed*(deltaX > 0 ? 1 : -1), 0, false);
				} else {
					this.player.move(0, playerSpeed*(deltaY > 0 ? 1 : -1), false);
				}
			}
			if (this.selectedEnemy != null){
				if (this.keys.getOrDefault(KeyCode.W, false)){
					this.selectedEnemy.move(0, -Enemy.SPEED*1.2, false);
				} else if (this.keys.getOrDefault(KeyCode.A, false)){
					this.selectedEnemy.move(-Enemy.SPEED*1.2, 0, false);
				} else if (this.keys.getOrDefault(KeyCode.S, false)){
					this.selectedEnemy.move(0, Enemy.SPEED*1.2, false);
				} else if (this.keys.getOrDefault(KeyCode.D, false)){
					this.selectedEnemy.move(Enemy.SPEED*1.2, 0, false);
				}
				
				if (this.keys.getOrDefault(KeyCode.UP, false)){
					this.selectedEnemy = selectEnemy(this.selectedEnemy, false, true);
					this.keys.put(KeyCode.UP, false);
				} else if (this.keys.getOrDefault(KeyCode.DOWN, false)){
					this.selectedEnemy = selectEnemy(this.selectedEnemy, false, false);
					this.keys.put(KeyCode.DOWN, false);
				} else if (this.keys.getOrDefault(KeyCode.RIGHT, false)){
					this.selectedEnemy = selectEnemy(this.selectedEnemy, true, false);
					this.keys.put(KeyCode.RIGHT, false);
				} else if (this.keys.getOrDefault(KeyCode.LEFT, false)){
					this.selectedEnemy = selectEnemy(this.selectedEnemy, true, true);
					this.keys.put(KeyCode.LEFT, false);
				}
			}
		} else {
			if (this.keys.getOrDefault(KeyCode.W, false)){
				this.player.move(0, -playerSpeed, false);
			} else if (this.keys.getOrDefault(KeyCode.A, false)){
				this.player.move(-playerSpeed, 0, false);
			} else if (this.keys.getOrDefault(KeyCode.S, false)){
				this.player.move(0, playerSpeed, false);
			} else if (this.keys.getOrDefault(KeyCode.D, false)){
				this.player.move(playerSpeed, 0, false);
			} else {
				this.player.setState(Player.State.IDLE);
			}
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
		gc.fillText((diff/60000)+":"+(diff/1000%60), 20, MainApplication.HEIGHT-20);
		
		this.reverser.render();
	}
}
