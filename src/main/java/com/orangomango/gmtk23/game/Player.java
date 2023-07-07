package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.*;

import com.orangomango.gmtk23.MainApplication;

public class Player extends GameObject{
	public static enum State{
		IDLE(new Point2D(2, 0), 2),
		MOVING_UP(new Point2D(0, 1), 2),
		MOVING_DOWN(new Point2D(0, 0), 2),
		MOVING_RIGHT(new Point2D(0, 2), 4),
		MOVING_LEFT(new Point2D(0, 3), 4);
		
		private Point2D startFrame;
		private int frames;
		
		private State(Point2D s, int frames){
			this.startFrame = s;
			this.frames = frames;
		}
		
		public Point2D getStartPoint(){
			return this.startFrame;
		}
		
		public Point2D next(Point2D point){
			Point2D p = new Point2D(point.getX()+1, point.getY());
			if (p.getX()-this.startFrame.getX() >= this.frames){
				p = this.startFrame;
			}
			return p;
		}
	}
	
	private List<Bullet> bullets = new ArrayList<>();
	private String currentGun = "normal_gun";
	private State currentState = State.IDLE;
	private Timeline animation;
	private volatile Point2D frameIndex = currentState.getStartPoint();
	private static Image IMAGE = MainApplication.loadImage("player.png");
	
	public Player(GraphicsContext gc, double x, double y){
		super(gc, x, y, 40, 40);
		this.animation = new Timeline(new KeyFrame(Duration.millis(200), e -> this.frameIndex = this.currentState.next(this.frameIndex)));
		this.animation.setCycleCount(Animation.INDEFINITE);
		this.animation.play();
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
	
	public void setState(State s){
		if (s == this.currentState) return;
		this.currentState = s;
		this.frameIndex = this.currentState.getStartPoint();
	}
	
	public void setGun(String name){
		Bullet.configs.remove(this);
		Bullet.applyConfiguration(name, null, null, 0, 0, 0, this);
		this.currentGun = name;
	}
	
	public void stopAnimation(){
		this.animation.stop();
	}
	
	public String getCurrentGun(){
		return this.currentGun;
	}
	
	@Override
	public void render(){
		gc.drawImage(IMAGE, 1+(32+2)*this.frameIndex.getX(), 1+(32+2)*this.frameIndex.getY(), 32, 32, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
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
