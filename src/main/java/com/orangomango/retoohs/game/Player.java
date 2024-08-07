package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.effect.Glow;

import java.util.*;

import com.orangomango.retoohs.AssetLoader;
import com.orangomango.retoohs.ui.GameScreen;

public class Player extends GameObject implements GunObject{
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
	private double pointAngle;
	private volatile Point2D frameIndex = currentState.getStartPoint();
	private static Image IMAGE = AssetLoader.getInstance().getImage("player.png");
	private static final Image ARROW_IMAGE = AssetLoader.getInstance().getImage("arrow.png");
	
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
	
	@Override
	public void setGun(String name){
		Bullet.configs.remove(this);
		Bullet.applyConfiguration(name, null, null, 0, 0, 0, this);
		this.currentGun = name;
	}
	
	public void stopAnimation(){
		this.animation.stop();
	}
	
	@Override
	public String getCurrentGun(){
		return this.currentGun;
	}
	
	public void pointGun(double a){
		this.pointAngle = Math.toDegrees(a);
	}
	
	@Override
	public boolean move(double x, double y, boolean collision){
		boolean output = super.move(x, y, collision);
		if (Math.abs(x) > Math.abs(y)){
			if (x > 0){
				setState(State.MOVING_RIGHT);
			} else if (x < 0){
				setState(State.MOVING_LEFT);
			}
		} else if (Math.abs(x) < Math.abs(y)){
			if (y > 0){
				setState(State.MOVING_DOWN);
			} else if (y < 0){
				setState(State.MOVING_UP);
			}
		}
		return output;
	}
	
	@Override
	public void render(){
		gc.save();
		if (!this.damage){
			gc.setEffect(new Glow(0.9));
		}
		gc.drawImage(IMAGE, 1+(32+2)*this.frameIndex.getX(), 1+(32+2)*this.frameIndex.getY(), 32, 32, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		for (int i = 0; i < this.bullets.size(); i++){
			Bullet b = this.bullets.get(i);
			b.render();
			if (!b.exists()){
				this.bullets.remove(b);
				i--;
			}
		}
		
		Image gunImage = Bullet.gunImages.get(this.currentGun);
		renderGun(gunImage, this.pointAngle);
		
		if (GameScreen.getInstance().playsPlayer){
			gc.drawImage(ARROW_IMAGE, this.x-10, this.y-this.h/2-25, 20, 20);
		}
		
		gc.restore();
	}
}
