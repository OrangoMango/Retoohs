package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Rectangle2D;

import com.orangomango.gmtk23.ui.GameScreen;
import com.orangomango.gmtk23.MainApplication;

public abstract class GameObject{
	protected double x, y, w, h;
	protected GraphicsContext gc;
	protected int hp = 100;
	private boolean damage = true;
	protected boolean invulnerable;
	protected volatile int frameIndex;
	
	public GameObject(GraphicsContext gc, double x, double y, double w, double h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.gc = gc;
	}
	
	protected void startAnimation(int frames, int time){
		Thread animation = new Thread(() -> {
			while (GameScreen.getInstance().isGameRunning()){
				try {
					this.frameIndex++;
					if (this.frameIndex == frames){
						this.frameIndex = 0;
					}
					Thread.sleep(time);
				} catch (InterruptedException ex){
					ex.printStackTrace();
				}
			}
		});
		animation.setDaemon(true);
		animation.start();
	}
	
	public void damage(int dmg){
		if (!damage || this.invulnerable) return;
		this.damage = false;
		this.hp -= dmg;
		if (this instanceof Player){
			MainApplication.playSound(MainApplication.DAMAGE_SOUND, false);
		} else {
			GameScreen.getInstance().score += dmg;
			GameScreen.getInstance().getFloatingTexts().add(new FloatingText(this.gc, Integer.toString(dmg), this.x, this.y));
		}
		MainApplication.schedule(() -> this.damage = true, 50);
	}
	
	public void heal(int heal){
		this.hp += heal;
		if (this.hp > 100) this.hp = 100;
	}
	
	public int getHP(){
		return this.hp;
	}
	
	public boolean isInvulnerable(){
		return this.invulnerable;
	}
	
	public boolean collided(GameObject other){
		Rectangle2D thisObject = new Rectangle2D(this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		Rectangle2D otherObject = new Rectangle2D(other.x-other.w/2, other.y-other.h/2, other.w, other.h);
		return thisObject.intersects(otherObject);
	}
	
	public boolean move(double x, double y, boolean collision){
		this.x += x;
		if (collision){
			for (int i = 0; i < GameScreen.getInstance().getGameObjects().size(); i++){
				GameObject obj = GameScreen.getInstance().getGameObjects().get(i);
				if (obj != this && collided(obj)){
					this.x -= x;
					break;
				}
			}
		}
		this.y += y;
		if (collision){
			for (int i = 0; i < GameScreen.getInstance().getGameObjects().size(); i++){
				GameObject obj = GameScreen.getInstance().getGameObjects().get(i);
				if (obj != this && collided(obj)){
					this.y -= y;
					break;
				}
			}
		}
		if (this.x-this.w < 0 || this.y-this.h < 0 || this.x+this.w > MainApplication.WIDTH || this.y+this.h > MainApplication.HEIGHT){
			this.x -= x;
			this.y -= y;
			return false;
		} else {
			return true;
		}
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public double getWidth(){
		return this.w;
	}
	
	public double getHeight(){
		return this.h;
	}
	
	public abstract void render();
}
