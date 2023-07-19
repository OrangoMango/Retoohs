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
import javafx.geometry.Rectangle2D;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.function.Consumer;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.game.*;
import com.orangomango.retoohs.ui.UIBar;

public class GameScreen{
	private static GameScreen instance = null;
	public static final Font FONT_45 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 45);
	public static final Font FONT_30 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 30);
	public static final Font FONT_15 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 15);
	private static final int BOSS_SCORE = 1500;
	
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
	public Image pausedImage;
	private Boss currentBoss;
	private int bossExtraScore, lastBossScore;
	private boolean shaking;
	private double cameraShakeX, cameraShakeY;
	private boolean autoShoot = true;
	public Enemy selectedEnemy;
	public boolean playsPlayer = true;
	private Reverser reverser;
	private List<MenuButton> pauseButtons = new ArrayList<>();
	private int bossesKilled;
	private UIBar healthBar, exBar, restoreBar, bossBar;
	private boolean doingTutorial, touchControls = false;
	private Tutorial tutorial;
	private Runnable onResume;
	private JoyStick moveController, shootController;
	private MenuButton confirmCollectButton, explosionButton, healButton;
	private boolean nextExplosion;
	
	private Image groundImage = MainApplication.assetLoader.getImage("ground.png");
	private Image[] stoneGroundImages = new Image[]{MainApplication.assetLoader.getImage("ground_stone_0.png"), MainApplication.assetLoader.getImage("ground_stone_1.png")};
	private int[][] groundPattern;
	private Image reverseImage = MainApplication.assetLoader.getImage("reverse.png");
	private volatile int reverseIndex = 0;
	
	public GameScreen(boolean tutorial){
		if (instance != null){
			throw new IllegalStateException("instance != null");
		}
		instance = this;
		this.mediaPlayer = MainApplication.playSound(MainApplication.BACKGROUND_MUSIC, true);
		this.doingTutorial = tutorial;
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

	public Reverser getReverser(){
		return this.reverser;
	}

	public void setOnResume(Runnable r){
		this.onResume = () -> {
			r.run();
			this.onResume = null;
		};
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

		this.tutorial = new Tutorial(gc);

		this.player = new Player(gc, 300, 300);
		this.gameObjects.add(this.player);
		Bullet.applyConfiguration("normal_gun", null, null, 0, 0, 0, this.player);

		this.moveController = new JoyStick(gc, 50, 400);
		this.shootController = new JoyStick(gc, 800, 400);
		this.confirmCollectButton = new MenuButton(gc, 550, 500, 50, 50, MainApplication.assetLoader.getImage("button_confirmpick.png"), () -> this.keys.put(KeyCode.E, true));
		this.explosionButton = new MenuButton(gc, 625, 500, 50, 50, MainApplication.assetLoader.getImage("button_explosion.png"), () -> this.nextExplosion = true);
		this.healButton = new MenuButton(gc, 700, 500, 50, 50, MainApplication.assetLoader.getImage("button_heal.png"), () -> this.keys.put(KeyCode.Q, true));
		
		EventHandler<MouseEvent> mouseEvent = e -> {
			if (this.paused){
				for (MenuButton mb : this.pauseButtons){
					mb.click(e.getX(), e.getY());
				}
				return;
			}
			if (this.touchControls){
				this.moveController.onMousePressed(e);
				this.shootController.onMousePressed(e);
				this.confirmCollectButton.click(e.getX(), e.getY());
				this.explosionButton.click(e.getX(), e.getY());
				this.healButton.click(e.getX(), e.getY());
				if (this.shootController.isUsed()){
					playerShoot(this.nextExplosion, this.shootController.getAngle());
					this.nextExplosion = false;
				}
			} else if (e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY){
				playerShoot(e.getButton() == MouseButton.SECONDARY, Math.atan2(e.getY()/MainApplication.SCALE-this.player.getY(), e.getX()/MainApplication.SCALE-this.player.getX()));
			}
		};
		canvas.setOnMousePressed(mouseEvent);
		canvas.setOnMouseDragged(mouseEvent);
		canvas.setOnMouseMoved(e -> this.player.pointGun(Math.atan2(e.getY()-this.player.getY(), e.getX()-this.player.getX())));
		canvas.setOnMouseReleased(e -> {
			this.moveController.onMouseReleased();
			this.shootController.onMouseReleased();
		});

		// Start spawning the zombies
		if (!this.doingTutorial){
			startSpawner(gc);
		}
		
		this.bpoint1 = new BonusPoint(gc, 0, 0);
		this.bpoint2 = new BonusPoint(gc, 0, 0);
		this.bpoint1.relocate();
		this.bpoint2.relocate();

		Random random = new Random();		
		this.reverser = new Reverser(gc);

		this.groundPattern = new int[1000/64+1][600/64+1];
		for (int x = 0; x < this.groundPattern.length; x++){
			for (int y = 0; y < this.groundPattern[0].length; y++){
				this.groundPattern[x][y] = Math.random() > 0.75 ? random.nextInt(this.stoneGroundImages.length) : -1;
			}
		}
		
		this.startTime = System.currentTimeMillis();
		
		Image homeButtonImage = MainApplication.assetLoader.getImage("button_home.jpg");
		this.pauseButtons.add(new MenuButton(gc, 420, 450, 64, 64, homeButtonImage, () -> {
			quit();
			HomeScreen hs = new HomeScreen();
			MainApplication.stage.getScene().setRoot(hs.getLayout());
		}));
		Image resumeButtonImage = MainApplication.assetLoader.getImage("button_resume.jpg");
		this.pauseButtons.add(new MenuButton(gc, 520, 450, 64, 64, resumeButtonImage, () -> setPause(false)));

		// UI bars
		Image hpImage = MainApplication.assetLoader.getImage("hpbar.png");
		Image exImage = MainApplication.assetLoader.getImage("exbar.png");
		Image restoreImage = MainApplication.assetLoader.getImage("restorebar.png");
		Image bossbarImage = MainApplication.assetLoader.getImage("bossbar.png");
		this.healthBar = new UIBar(gc, 20, 60, 200, 30, hpImage, Color.GREEN, 23, 5, 175, 20);
		this.exBar = new UIBar(gc, 20, 95, 200, 30, exImage, Color.YELLOW, 23, 5, 175, 20);
		this.restoreBar = new UIBar(gc, 20, 130, 200, 30, restoreImage, Color.CYAN, 23, 5, 175, 20);
		this.bossBar = new UIBar(gc, 20, 165, 200, 30, bossbarImage, Color.PURPLE, 23, 5, 175, 20);
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();

		if (this.doingTutorial){
			this.tutorial.next();
		}
		
		pane.getChildren().add(canvas);
		return pane;
	}

	private void playerShoot(boolean explosion, double angle){
		if (this.currentBoss == null && !this.playsPlayer) return;
		long diff = System.currentTimeMillis()-this.lastExplosion;
		boolean exp = explosion && diff > 10000;
		if (exp){
			this.lastExplosion = System.currentTimeMillis();
			applyTutorial(t -> {
				if (t.getIndex() == 2){
					t.next();
				}
			});
		}
		if (Bullet.configs.get(this.player) != null && Bullet.configs.get(this.player).getAmmo() == 0){
			reloadAmmo();
		}
		this.player.shoot(angle, exp);
	}

	public void startSpawner(GraphicsContext gc){
		Thread spawner = new Thread(() -> {
			Random random = new Random();
			try {
				Thread.sleep(3000);
				while (this.gameRunning){
					if (this.paused) continue;
					int type = 0;
					/*if (this.score > 500){
						int delta = this.score-500;
						int n = delta/1000;
						type = random.nextInt(n+1);
					}
					if (type > 4) type = 4;*/
					Enemy e = new Enemy(gc, random.nextInt(800)+100, random.nextInt(400)+100, this.player, type);
					if (!this.playsPlayer){
						this.drops.add(new Drop(gc, random.nextInt(800)+100, random.nextInt(400)+100));
						if (this.selectedEnemy == null){
							this.selectedEnemy = e;
						}
					}
					this.gameObjects.add(e);
					Thread.sleep((random.nextInt(1000)+1000)*(this.currentBoss != null ? 2 : 1));
				}
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}
		});
		spawner.setDaemon(true);
		spawner.start();
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
	
	public void quit(){
		this.loop.stop();
		this.gameRunning = false;
		MainApplication.threadsRunning = false;
		MainApplication.schedule(() -> MainApplication.threadsRunning = true, 500);
		GameScreen.instance = null;
		this.player.stopAnimation();
		if (this.mediaPlayer != null){
			this.mediaPlayer.stop();
		}
	}

	public void applyTutorial(Consumer<Tutorial> cons){
		if (this.doingTutorial){
			cons.accept(this.tutorial);
		}
	}
	
	public void setPause(boolean p){
		this.paused = p;
		if (this.paused){
			this.lastPaused = System.currentTimeMillis();
			this.bpoint1.resetPausedTime();
			this.bpoint2.resetPausedTime();
			for (int i = 0; i < this.drops.size(); i++){
				this.drops.get(i).resetPausedTime();
			}
		} else {
			long diff = System.currentTimeMillis()-this.lastPaused;
			this.pausedTime += diff;
			this.pausedImage = null;
			if (this.onResume != null) this.onResume.run();
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
		gc.save();
		gc.scale(MainApplication.SCALE, MainApplication.SCALE);
		if (this.pausedImage != null){
			gc.drawImage(this.pausedImage, 0, 0, 1000, 600);
			if (this.tempStopped){
				gc.drawImage(this.reverseImage, 1+66*this.reverseIndex, 1, 64, 64, 500-100, 300-100, 200, 200);
			} else {
				gc.save();
				gc.setGlobalAlpha(0.6);
				gc.setFill(Color.BLACK);
				gc.fillRect(0, 0, 1000, 600);
				gc.restore();
				for (MenuButton mb : this.pauseButtons){
					mb.render();
				}
				String tutorialMessage = this.tutorial.getCurrentText();
				if (tutorialMessage != null){
					gc.save();
					gc.setFill(Color.WHITE);
					gc.setFont(FONT_30);
					gc.setTextAlign(TextAlignment.CENTER);
					gc.fillText(tutorialMessage, 500, 200);
					gc.restore();
				}
			}
			gc.restore();
			return;
		}
		
		if (this.score < 0) this.score = 0;
		if (this.score-this.bossExtraScore-this.lastBossScore >= BOSS_SCORE && this.currentBoss == null && this.playsPlayer){
			spawnBoss(gc);
			applyTutorial(t -> {
				if (t.getIndex() == 7){
					t.next();
				}
			});
		}
		
		gc.save();
		gc.translate(-this.cameraShakeX, -this.cameraShakeY);
		
		for (int x = 0; x < 1000; x += 64){
			for (int y = 0; y < 600; y += 64){
				int index = this.groundPattern[x/64][y/64];
				gc.drawImage(index >= 0 ? this.stoneGroundImages[index] : this.groundImage, x, y, 64, 64);
			}
		}
		
		// Render bonuspoint
		this.bpoint1.render();
		if (!this.doingTutorial || this.tutorial.getIndex() > 0) this.bpoint2.render();

		long diff = System.currentTimeMillis()-this.startTime-this.pausedTime;
		
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
						GameOverScreen gos = new GameOverScreen(diff, this.score, this.bossesKilled);
						MainApplication.stage.getScene().setRoot(gos.getLayout());
						return;
					} else {
						this.playsPlayer = true;
						obj.heal(100);
						this.reverser.allowStart();
						this.score += 250;
						tempstop(gc.getCanvas());
						this.player.setInvulnerable(true);
						this.drops.clear();
						MainApplication.schedule(() -> this.player.setInvulnerable(false), 1500);
						applyTutorial(t -> {
							if (t.getIndex() == 6){
								this.score = BOSS_SCORE-100;
								t.trigger();
							}
						});
					}
				}
				i--;
				if (!this.playsPlayer){
					this.score -= 5;
				}
				if (obj instanceof Enemy){
					if (this.gameObjects.size() == 1 && this.tutorial.getIndex() == 2){
						this.tutorial.next();
					}
					if (Math.random() > 0.75 || this.tutorial.getIndex() == 1){ // 25%
						this.drops.add(new Drop(gc, obj.getX(), obj.getY()));
					}
				}
				if (obj instanceof Boss){
					this.currentBoss = null;
					this.score += 400;
					this.bossesKilled++;
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
					MainApplication.schedule(() -> this.autoShoot = true, Bullet.getBulletConfig(this.player.getCurrentGun()).getInt("cooldown"));
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
				Point2D movement = new Point2D(0, 0);
				if (this.keys.getOrDefault(KeyCode.W, false)){
					movement = movement.add(0, -1);
				}
				if (this.keys.getOrDefault(KeyCode.A, false)){
					movement = movement.add(-1, 0);
				}
				if (this.keys.getOrDefault(KeyCode.S, false)){
					movement = movement.add(0, 1);
				}
				if (this.keys.getOrDefault(KeyCode.D, false)){
					movement = movement.add(1, 0);
				}
				if (this.touchControls && this.moveController.isUsed()){
					double moveAngle = this.moveController.getAngle();
					movement = new Point2D(Math.cos(moveAngle), Math.sin(moveAngle));
				}
				movement = movement.normalize().multiply(Enemy.SPEED*1.2);
				this.selectedEnemy.move(movement.getX(), movement.getY(), false);
				
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
			boolean idle = true;
			Point2D movement = new Point2D(0, 0);
			if (this.keys.getOrDefault(KeyCode.W, false)){
				movement = movement.add(0, -1);
				idle = false;
			}
			if (this.keys.getOrDefault(KeyCode.A, false)){
				movement = movement.add(-1, 0);
				idle = false;
			}
			if (this.keys.getOrDefault(KeyCode.S, false)){
				movement = movement.add(0, 1);
				idle = false;
			}
			if (this.keys.getOrDefault(KeyCode.D, false)){
				movement = movement.add(1, 0);
				idle = false;
			}
			if (this.touchControls && this.moveController.isUsed()){
				double moveAngle = this.moveController.getAngle();
				movement = new Point2D(Math.cos(moveAngle), Math.sin(moveAngle));
				idle = false;
			}
			movement = movement.normalize().multiply(playerSpeed);
			this.player.move(movement.getX(), movement.getY(), false);
			if (idle){
				this.player.setState(Player.State.IDLE);
			}
		}
		
		if ((this.keys.getOrDefault(KeyCode.Q, false) && this.player.getHP() < 90) || this.player.getHP() < 40){
			// heal
			long hdiff = System.currentTimeMillis()-this.lastHeal;
			if (hdiff > 30000){
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
		this.healthBar.setProgress(this.player.getHP()/100.0);
		this.healthBar.render();
		
		// Explosion bar
		this.exBar.setProgress(Math.min(1, (System.currentTimeMillis()-this.lastExplosion)/10000.0));
		this.exBar.render();
		
		// Heal bar
		this.restoreBar.setProgress(Math.min(1, (System.currentTimeMillis()-this.lastHeal)/30000.0));
		this.restoreBar.render();
		
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
			this.bossBar.setProgress(((this.score-this.bossExtraScore)%BOSS_SCORE/(double)(BOSS_SCORE)));
			this.bossBar.render();
		}
		
		gc.setGlobalAlpha(1);
		
		// Score
		gc.setFill(Color.BLACK);
		gc.setFont(FONT_45);
		gc.fillText(Integer.toString(this.score), 20, 40);
		
		// Time
		gc.setFill(Color.BLACK);
		gc.setFont(FONT_30);
		gc.fillText(String.format("%2d:%02d", diff/60000, diff/1000%60), 20, 580);
		
		this.reverser.render();

		if (this.touchControls){
			this.moveController.render();
			if (this.playsPlayer){
				this.shootController.render();
				if (this.shootController.isUsed()){
					this.player.pointGun(this.shootController.getAngle());
					gc.save();
					gc.setGlobalAlpha(0.6);
					gc.setFill(Color.WHITE);
					gc.translate(this.player.getX(), this.player.getY());
					gc.rotate(Math.toDegrees(this.shootController.getAngle()));
					double dist = Bullet.getBulletConfig(this.player.getCurrentGun()).getDouble("maxDistance");
					gc.fillRect(0, -3, dist, 6);
					gc.restore();
				}
				gc.save();
				gc.setGlobalAlpha(0.7);
				this.confirmCollectButton.render();
				this.explosionButton.render();
				this.healButton.render();
				gc.restore();
			}
		}
		gc.restore();
	}
}
