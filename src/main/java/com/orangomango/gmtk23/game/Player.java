package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

import com.orangomango.gmtk23.MainApplication;

public class Player extends GameObject{
	private List<Bullet> bullets = new ArrayList<>();
	private String currentGun = "normal_gun";
	
	public Player(GraphicsContext gc, double x, double y){
		super(gc, x, y, 32, 32);
	}
	
	public void shoot(double angle, boolean ex){
		if (ex){
			Bullet b = new Bullet(this.gc, this, Bullet.getBulletConfig("normal_gun"), this.x, this.y, angle, 10);
			b.setExplode();
			this.bullets.add(b);
		} else {
			Bullet.applyConfiguration(this.currentGun, this.bullets, this.gc, this.x, this.y, angle, this);
		}
	}
	
	public void setGun(String name){
		Bullet.configs.remove(this);
		Bullet.applyConfiguration(name, null, null, 0, 0, 0, this);
		this.currentGun = name;
	}
	
	@Override
	public void render(){
		gc.setFill(Color.RED);
		gc.fillOval(this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		for (int i = 0; i < this.bullets.size(); i++){
			Bullet b = this.bullets.get(i);
			b.render();
			if (!b.exists()){
				this.bullets.remove(b);
				i--;
			}
		}
	}
}
