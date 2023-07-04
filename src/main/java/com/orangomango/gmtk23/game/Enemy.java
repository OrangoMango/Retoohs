package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.*;

import com.orangomango.gmtk23.MainApplication;

public class Enemy extends GameObject{
	private GameObject target;
	private static final double SPEED = 2;
	private static final Image IMAGE = MainApplication.loadImage("enemy.png");
	private volatile double alpha = 0.5;
	private boolean shooter, attack = true;
	private int type;
	private List<Bullet> bullets = new ArrayList<>();
	
	public Enemy(GraphicsContext gc, double x, double y, GameObject target, int type){
		super(gc, x, y, 32+12*type, 32+12*type);
		this.type = type;
		this.target = target;
		this.invulnerable = true;
		this.shooter = Math.random() > 0.7;
		this.hp = 10+10*this.type;
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
		
		gc.drawImage(IMAGE, 1+(this.w+2)*this.frameIndex, 1+(this.h+2)*direction, this.w, this.h, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		
		// Target
		double distance = Math.sqrt(Math.pow(this.target.getX()-this.x, 2)+Math.pow(this.target.getY()-this.y, 2));
		int dmg = 10+5*this.type;
		if (distance > (this.shooter ? 220 : 55)){
			move(SPEED*Math.cos(angle), SPEED*Math.sin(angle), true);
		} else if (this.attack && !this.invulnerable){
			if (this.shooter){
				Bullet.applyConfiguration("normal_gun", this.bullets, this.gc, this.x, this.y, angle, this);
			} else {
				this.target.damage(dmg);
			}
			this.attack = false;
			MainApplication.schedule(() -> this.attack = true, this.shooter ? 1000 : 500);
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
