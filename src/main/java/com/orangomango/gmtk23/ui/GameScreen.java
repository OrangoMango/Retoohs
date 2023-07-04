package com.orangomango.gmtk23.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

import java.util.*;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.game.*;

public class GameScreen{
	private static GameScreen instance = null;
	public static final Font FONT_45 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 45);
	public static final Font FONT_30 = Font.loadFont(GameScreen.class.getResourceAsStream("/main_font.ttf"), 30);
	
	private List<GameObject> gameObjects = new ArrayList<>();
	private List<FloatingText> fTexts = new ArrayList<>();
	private List<Drop> drops = new ArrayList<>();
	private Map<KeyCode, Boolean> keys = new HashMap<>();
	private Player player;
	public int score;
	public Explosion explosion;
	private long lastExplosion;
	private long lastHeal;
	private boolean gameRunning = true;
	private BonusPoint bpoint1, bpoint2;
	private long startTime;
	
	public GameScreen(){
		if (instance != null){
			throw new IllegalStateException("instance != null");
		}
		instance = this;
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
	
	public boolean isGameRunning(){
		return this.gameRunning;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		canvas.setFocusTraversable(true);
		canvas.setOnKeyPressed(e -> this.keys.put(e.getCode(), true));
		canvas.setOnKeyReleased(e -> this.keys.put(e.getCode(), false));
		GraphicsContext gc = canvas.getGraphicsContext2D();

		this.player = new Player(gc, 300, 300);
		this.gameObjects.add(this.player);
		Bullet.applyConfiguration("normal_gun", null, null, 0, 0, 0, this.player);
		
		EventHandler<MouseEvent> mouseEvent = e -> {
			if (e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY){
				long diff = System.currentTimeMillis()-this.lastExplosion;
				boolean exp = e.getButton() == MouseButton.SECONDARY && diff > 15000;
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
		
		Thread spawner = new Thread(() -> {
			Random random = new Random();
			try {
				Thread.sleep(3000);
				while (this.gameRunning){
					int type = 0; //this.score > 5000 ? random.nextInt((this.score-5000)/5000+1)+1 : 0;
					Enemy e = new Enemy(gc, random.nextInt(MainApplication.WIDTH-200)+100, random.nextInt(MainApplication.HEIGHT-200)+100, this.player, type);
					if (!this.player.collided(e)){
						this.gameObjects.add(e);
						Thread.sleep(random.nextInt(1000)+1000);
					}
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
		
		this.startTime = System.currentTimeMillis();
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void reloadAmmo(){
		Bullet.configs.get(this.player).reload();
		MainApplication.playSound(MainApplication.RELOAD_SOUND, false);
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		if (this.score < 0) this.score = 0;
		
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
					System.out.println("GAME OVER");
					System.exit(0);
				}
				i--;
				if (obj instanceof Enemy && Math.random() > 0.85){ // 85%
					this.drops.add(new Drop(gc, obj.getX(), obj.getY()));
				}
			}
		}
		
		// Render explosion
		if (this.explosion != null){
			this.explosion.render();
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
			this.player.move(0, -playerSpeed, true);
		} else if (this.keys.getOrDefault(KeyCode.A, false)){
			this.player.move(-playerSpeed, 0, true);
		} else if (this.keys.getOrDefault(KeyCode.S, false)){
			this.player.move(0, playerSpeed, true);
		} else if (this.keys.getOrDefault(KeyCode.D, false)){
			this.player.move(playerSpeed, 0, true);
		}
		
		if (this.keys.getOrDefault(KeyCode.Q, false) && player.getHP() < 90){
			// heal
			MainApplication.playSound(MainApplication.HEAL_SOUND, false);
			long diff = System.currentTimeMillis()-this.lastHeal;
			if (diff > 30000){
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
		
		gc.setLineWidth(3);
		gc.setGlobalAlpha(0.7);
		gc.setStroke(Color.BLACK);
		
		// HP bar
		gc.setFill(Color.GREEN);
		gc.fillRect(20, 60, 200*this.player.getHP()/100.0, 30);
		gc.strokeRect(20, 60, 200, 30);
		
		// Explosion bar
		gc.setFill(Color.CYAN);
		gc.fillRect(20, 100, 200*Math.min(1, (System.currentTimeMillis()-this.lastExplosion)/15000.0), 20);
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
		
		gc.setGlobalAlpha(1);
		
		// Score
		gc.setFill(Color.BLACK);
		gc.setFont(FONT_45);
		gc.fillText(Integer.toString(this.score), 20, 40);
		
		// Time
		gc.setFill(Color.BLACK);
		gc.setFont(FONT_30);
		long diff = System.currentTimeMillis()-this.startTime;
		gc.fillText(String.format("%2d:%02d", diff/60000, diff/1000%60), 20, MainApplication.HEIGHT-20);
	}
}
