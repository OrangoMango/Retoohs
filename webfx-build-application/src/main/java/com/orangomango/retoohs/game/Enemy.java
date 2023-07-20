package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
//import javafx.scene.effect.Glow;

import dev.webfx.platform.scheduler.Scheduler;

import java.util.*;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.ui.GameScreen;

public class Enemy extends GameObject implements GunObject{
	public static final double SPEED = 3;
	private static final int SIZE = 32;
	private static final Image IMAGE = MainApplication.assetLoader.getImage("enemy.png");
	private static final Image ARROW_IMAGE = MainApplication.assetLoader.getImage("arrow.png");

	private GameObject target;
	private volatile double alpha = 0.5;
	private boolean shooter, attack = true;
	private int type;
	private int dmg;
	private int overrideDirection = -1;
	private List<Bullet> bullets = new ArrayList<>();
	private String currentGun = "enemy_gun";
	
	public Enemy(GraphicsContext gc, double x, double y, GameObject target, int type){
		super(gc, x, y, SIZE+6*type, SIZE+6*type);
		this.type = type;
		this.target = target;
		this.invulnerable = true;
		this.shooter = Math.random() > 0.7;
		if (this.shooter) Bullet.applyConfiguration(this.currentGun, null, null, 0, 0, 0, this);
		this.hp = 10+5*this.type;
		this.dmg = 10+5*this.type;
		Scheduler.schedulePeriodic(80, scheduled -> {
			if (!MainApplication.threadsRunning || this.alpha >= 1){
				this.invulnerable = false;
				scheduled.cancel();
			} else {
				this.alpha += 0.05;
			}
		});
		startAnimation(5, 150);
	}

	public void setShooter(){
		this.shooter = true;
	}

	@Override
	public String getCurrentGun(){
		return this.currentGun;
	}

	@Override
	public void setGun(String name){
		Bullet.configs.remove(this);
		Bullet.applyConfiguration(name, null, null, 0, 0, 0, this);
		this.currentGun = name;
	}
	
	@Override
	public boolean move(double x, double y, boolean collision){
		boolean output = super.move(x, y, collision);
		if (Math.abs(x) > Math.abs(y)){
			if (x > 0){
				this.overrideDirection = 2;
			} else if (x < 0){
				this.overrideDirection = 3;
			}
		} else if (Math.abs(x) < Math.abs(y)){
			if (y > 0){
				this.overrideDirection = 0;
			} else if (y < 0){
				this.overrideDirection = 1;
			}
		} else {
			this.overrideDirection = -1;
		}
		return output;
	}
	
	@Override
	public void render(){
		gc.save();
		gc.setGlobalAlpha(this.alpha);
		if (!this.damage){
			//gc.setEffect(new Glow(0.9));
		}
		double angle = Math.atan2(this.target.getY()-this.y, this.target.getX()-this.x);

		/*
		 * Set direction:
		 * 0 down
		 * 1 up
		 * 2 right
		 * 3 left
		 */
		int direction;
		if ((angle >= 0 && angle < Math.PI/3) || (angle <= 0 && angle > -Math.PI/3)) direction = 2;
		else if (angle <= -Math.PI/3 && angle > -2*Math.PI/3) direction = 1;
		else if (angle >= Math.PI/3 && angle < 2*Math.PI/3) direction = 0;
		else direction = 3;
		
		if (this.overrideDirection >= 0){
			direction = this.overrideDirection;
		}
		
		gc.drawImage(IMAGE, 1+(SIZE+2)*this.frameIndex, 1+(SIZE+2)*direction, SIZE, SIZE, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		
		double distance = Math.sqrt(Math.pow(this.target.getX()-this.x, 2)+Math.pow(this.target.getY()-this.y, 2));
		if (distance > (this.shooter ? 220 : 45)){
			if (GameScreen.getInstance().selectedEnemy != this) move(SPEED*Math.cos(angle), SPEED*Math.sin(angle), true);
		} else if (this.attack && !this.invulnerable){
			if (this.shooter){
				Bullet.applyConfiguration(this.currentGun, this.bullets, this.gc, this.x, this.y, angle, this);
			} else {
				this.target.damage(this.dmg);
			}
			this.attack = false;
			MainApplication.schedule(() -> this.attack = true, this.shooter ? Bullet.getBulletConfig(this.currentGun).getInt("cooldown") : 250);
		}
		
		// Target
		if (GameScreen.getInstance().selectedEnemy == this && !GameScreen.getInstance().playsPlayer){
			gc.drawImage(ARROW_IMAGE, this.x-10, this.y-this.h/2-25, 20, 20);
		}
		
		Bullet.ShooterConfig conf = Bullet.configs.getOrDefault(this, null);
		if (conf != null){
			Image gunImage = Bullet.gunImages.get(this.currentGun);
			renderGun(gunImage, Math.toDegrees(angle));
		}
		
		// Render the bullets
		for (int i = 0; i < this.bullets.size(); i++){
			Bullet b = this.bullets.get(i);
			b.render();
			if (!b.exists()){
				this.bullets.remove(b);
				i--;
			}
		}
		gc.restore();
	}
}
