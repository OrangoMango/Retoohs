package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;

import com.orangomango.gmtk23.ui.GameScreen;
import com.orangomango.gmtk23.MainApplication;

public class Explosion{
	private GraphicsContext gc;
	private double x, y;
	private double r = 30;
	private int dmg;
	private boolean exists = true;
	
	public Explosion(GraphicsContext gc, double x, double y, int dmg){
		this.gc = gc;
		this.x = x;
		this.y = y;
		this.dmg = dmg;
	}
	
	public boolean exists(){
		return this.exists;
	}
	
	public void render(){
		gc.setFill(Color.ORANGE);
		gc.fillOval(this.x-this.r, this.y-this.r, this.r*2, this.r*2);
		Point2D pos = new Point2D(this.x, this.y);
		for (GameObject obj : GameScreen.getInstance().getGameObjects()){
			if (pos.distance(new Point2D(obj.getX(), obj.getY())) < this.r){
				obj.damage(this.dmg);
			}
		}
		this.r += 5;
		if (this.r > 100){
			MainApplication.playSound(MainApplication.EXPLOSION_SOUND, false);
			this.exists = false;
		}
	}
}
