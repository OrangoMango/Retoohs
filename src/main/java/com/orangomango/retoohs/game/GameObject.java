package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.MainApplication;

public abstract class GameObject{
	protected double x, y, w, h;
	protected GraphicsContext gc;
	protected int hp = 100;
	protected boolean damage = true;
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
		MainApplication.schedulePeriodic(() -> {
			if (!GameScreen.getInstance().isPaused()){
				this.frameIndex++;
				if (this.frameIndex == frames){
					this.frameIndex = 0;
				}
			}
		}, time);
	}
	
	protected void renderGun(Image gunImage, double angle){
		gc.save();
		final double gw = 17;
		final double gh = 17*(Math.abs(angle) > 90 ? -1 : 1);
		gc.translate(this.x, this.y+2);
		gc.rotate(angle);
		gc.setGlobalAlpha(0.75);
		gc.drawImage(gunImage, -gw/2, -gh/2, gw, gh);
		gc.restore();
	}
	
	public void damage(int dmg){
		if (!damage || this.invulnerable) return;
		this.damage = false;
		this.hp -= dmg;
		if (this instanceof Player){
			MainApplication.playSound(MainApplication.DAMAGE_SOUND, false);
			GameScreen.getInstance().screenShake();
			if (!GameScreen.getInstance().playsPlayer){
				GameScreen.getInstance().score += dmg;
				GameScreen.getInstance().getFloatingTexts().add(new FloatingText(this.gc, Integer.toString(dmg), this.x, this.y));
			}
		} else {
			if (this instanceof Boss) MainApplication.playSound(MainApplication.BOSSHIT_SOUND, false);
			if (GameScreen.getInstance().playsPlayer) GameScreen.getInstance().score += dmg;
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
		if (other instanceof Boss) return false;
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
	
	public void setInvulnerable(boolean v){
		this.invulnerable = v;
	}
	
	public abstract void render();
}
