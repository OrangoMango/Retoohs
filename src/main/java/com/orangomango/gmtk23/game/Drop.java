package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.ui.GameScreen;

public class Drop{
	private GraphicsContext gc;
	private double x, y;
	private long startTime;
	private boolean exists = true;
	private int rarity;
	private static final double SIZE = 20;
	private static final int MAXTIME = 15000;
	
	public Drop(GraphicsContext gc, double x, double y){
		this.gc = gc;
		this.x = x;
		this.y = y;
		this.startTime = System.currentTimeMillis();
		double n = Math.random();
		if (n <= 0.5){
			this.rarity = 0; // Common
		} else if (n <= 0.9){
			this.rarity = 1; // Epic
		} else {
			this.rarity = 2; // Legendary
		}
	}
	
	public boolean exists(){
		return this.exists;
	}
	
	public void render(){
		gc.setFill(this.rarity == 0 ? Color.LIME : (this.rarity == 1 ? Color.PURPLE : Color.YELLOW));
		gc.fillRect(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Player player = GameScreen.getInstance().getPlayer();
		Rectangle2D thisRect = new Rectangle2D(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Rectangle2D playerRect = new Rectangle2D(player.getX()-player.getWidth()/2, player.getY()-player.getHeight()/2, player.getWidth(), player.getHeight());
		long diff = System.currentTimeMillis()-this.startTime;
		if (thisRect.intersects(playerRect)){
			String gunName = Bullet.getRandomGun(this.rarity);
			System.out.println(gunName);
			player.setGun(gunName);
			this.exists = false;
		} else if (diff > MAXTIME){
			this.exists = false;
		}
		gc.setFill(Color.CYAN);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1.2);
		gc.fillRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE*(1-(double)diff/MAXTIME), 7);
		gc.strokeRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE, 7);
	}
}
