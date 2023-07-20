package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;

import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.MainApplication;

public class Boss extends GameObject{
	private static final double SPEED = 1;
	public static final int HEALTH = 850;
	private boolean attack = true;
	private double speed;
	private long lastSuper;
	private Double fixedAngle;
	private Image image = MainApplication.assetLoader.getImage("boss_smash.png");
	private int smashFrameIndex;
	private boolean smash;

	public Boss(GraphicsContext gc, double x, double y){
		super(gc, x, y, 128, 128);
		this.speed = SPEED;
		this.hp = HEALTH;
		this.lastSuper = System.currentTimeMillis();
		startAnimation(3, 250);
		startSmashAnimation(10, 150);
	}
	
	private void startSmashAnimation(int frames, int time){
		MainApplication.schedulePeriodic(() -> {
			if (!GameScreen.getInstance().isPaused()){
				this.smashFrameIndex++;
				if (this.smashFrameIndex == frames){
					this.smashFrameIndex = 0;
				}
			}
		}, time);
	}
	
	private void makeSuper(){
		Random random = new Random();
		int n = random.nextInt(2);
		MainApplication.playSound(MainApplication.BOSSSUPER_SOUND, false);
		this.hp += 50;
		if (this.hp > HEALTH) this.hp = HEALTH;
		switch (n){
			case 0:
				int amount = random.nextInt(2);
				this.smash = true;
				MainApplication.schedule(() -> this.smash = false, 1500);
				for (int i = 0; i < 3+amount; i++){
					Enemy e = new Enemy(this.gc, random.nextInt(800)+100, random.nextInt(400)+100, GameScreen.getInstance().getPlayer(), 0);
					GameScreen.getInstance().getGameObjects().add(e);
				}
				break;
			case 1:
				this.speed *= 8;
				Player player = GameScreen.getInstance().getPlayer();
				this.fixedAngle = Math.atan2(player.getY()-this.y, player.getX()-this.x);
				MainApplication.schedule(() -> {
					this.speed = SPEED;
					this.fixedAngle = null;
				}, 2000);
				break;
		}
	}
	
	@Override
	public void render(){
		Player player = GameScreen.getInstance().getPlayer();
		double angle = this.fixedAngle != null ? this.fixedAngle : Math.atan2(player.getY()-this.y, player.getX()-this.x);
		double distance = Math.sqrt(Math.pow(player.getX()-this.x, 2)+Math.pow(player.getY()-this.y, 2));
		gc.drawImage(this.image, 1+(128+2)*(this.smash ? this.smashFrameIndex : this.frameIndex), 1, 128, 128, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		if (distance < this.w/2+40){
			if (this.attack){
				player.damage(25);
				this.attack = false;
				MainApplication.schedule(() -> this.attack = true, 1000);
			}
			if (this.speed != SPEED){
				move(this.speed*Math.cos(angle), this.speed*Math.sin(angle), false);
			}
		} else {
			move(this.speed*Math.cos(angle), this.speed*Math.sin(angle), false);
		}
		
		if (System.currentTimeMillis()-this.lastSuper > 7000){ // 20s
			this.lastSuper = System.currentTimeMillis();
			makeSuper();
		}
	}
}
