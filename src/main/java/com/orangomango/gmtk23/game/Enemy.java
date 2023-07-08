package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.effect.Glow;

import java.util.*;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.ui.GameScreen;

public class Enemy extends GameObject{
	public static final double SPEED = 3;
	private static final int SIZE = 32;
	private static final Image IMAGE = MainApplication.loadImage("enemy.png");
	private static final Image ARROW_IMAGE = MainApplication.loadImage("arrow.png");

	private GameObject target;
	private volatile double alpha = 0.5;
	private boolean shooter, attack = true;
	private int type;
	private int dmg;
	private List<Bullet> bullets = new ArrayList<>();
	
	public Enemy(GraphicsContext gc, double x, double y, GameObject target, int type){
		super(gc, x, y, SIZE+6*type, SIZE+6*type);
		this.type = type;
		this.target = target;
		this.invulnerable = true;
		this.shooter = Math.random() > 0.7;
		if (this.shooter) Bullet.applyConfiguration("enemy_gun", null, null, 0, 0, 0, this);
		this.hp = 10+10*this.type;
		this.dmg = 10+5*this.type;
		new Thread(() -> {
			try {
				while (this.alpha < 1){
					this.alpha += 0.05;
					Thread.sleep(80);
				}
				this.invulnerable = false;
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}
		}).start();
		startAnimation(5, 150);
	}
	
	@Override
	public void render(){
		gc.save();
		gc.setGlobalAlpha(this.alpha);
		if (!this.damage){
			gc.setEffect(new Glow(0.9));
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
		
		gc.drawImage(IMAGE, 1+(SIZE+2)*this.frameIndex, 1+(SIZE+2)*direction, SIZE, SIZE, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		
		double distance = Math.sqrt(Math.pow(this.target.getX()-this.x, 2)+Math.pow(this.target.getY()-this.y, 2));
		if (distance > (this.shooter ? 220 : 45)){
			if (GameScreen.getInstance().selectedEnemy != this) move(SPEED*Math.cos(angle), SPEED*Math.sin(angle), true);
		} else if (this.attack && !this.invulnerable){
			if (this.shooter){
				Bullet.applyConfiguration("enemy_gun", this.bullets, this.gc, this.x, this.y, angle, this);
			} else {
				this.target.damage(this.dmg);
			}
			this.attack = false;
			MainApplication.schedule(() -> this.attack = true, this.shooter ? 500 : 250);
		}
		
		// Target
		if (GameScreen.getInstance().selectedEnemy == this && !GameScreen.getInstance().playsPlayer){
			gc.drawImage(ARROW_IMAGE, this.x-10, this.y-this.h/2-25, 20, 20);
		}
		
		Bullet.ShooterConfig conf = Bullet.configs.getOrDefault(this, null);
		if (conf != null){
			Image gunImage = Bullet.gunImages.get("enemy_gun");
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
