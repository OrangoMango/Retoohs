package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.geometry.Rectangle2D;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.ui.GameScreen;

public class Drop{
	private GraphicsContext gc;
	private double x, y;
	private long startTime;
	private boolean exists = true;
	private int rarity;
	private Image image;
	
	private static final double SIZE = 20;
	private static final int MAXTIME = 15000;
	private static final Image IMAGE_COMMON = MainApplication.loadImage("drop_0.png");
	private static final Image IMAGE_EPIC = MainApplication.loadImage("drop_1.png");
	private static final Image IMAGE_LEGENDARY = MainApplication.loadImage("drop_2.png");
	
	public Drop(GraphicsContext gc, double x, double y){
		this.gc = gc;
		this.x = x;
		this.y = y;
		this.startTime = System.currentTimeMillis();
		double n = Math.random();
		if (n <= 0.5){
			this.rarity = 0; // Common
			this.image = IMAGE_COMMON;
		} else if (n <= 0.9){
			this.rarity = 1; // Epic
			this.image = IMAGE_EPIC;
		} else {
			this.rarity = 2; // Legendary
			this.image = IMAGE_LEGENDARY;
		}
	}
	
	public boolean exists(){
		return this.exists;
	}
	
	public void render(){
		gc.drawImage(this.image, this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Player player = GameScreen.getInstance().getPlayer();
		Rectangle2D thisRect = new Rectangle2D(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Rectangle2D playerRect = new Rectangle2D(player.getX()-player.getWidth()/2, player.getY()-player.getHeight()/2, player.getWidth(), player.getHeight());
		long diff = System.currentTimeMillis()-this.startTime-GameScreen.getInstance().getPausedTime();
		int playerRarity = Bullet.getBulletConfig(player.getCurrentGun()).getInt("rarity");
		if (thisRect.intersects(playerRect)){
			if (this.rarity >= playerRarity || GameScreen.getInstance().getKeys().getOrDefault(KeyCode.E, false)){		
				String gunName = Bullet.getRandomGun(this.rarity);
				System.out.println(gunName);
				MainApplication.playSound(MainApplication.DROP_SOUND, false);
				player.setGun(gunName);
				this.exists = false;
			}
		} else if (diff > MAXTIME){
			this.exists = false;
		}
		gc.setFill(Color.CYAN);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1.2);
		gc.fillRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE*(1-(double)diff/MAXTIME), 7);
		gc.strokeRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE, 7);
		if (thisRect.intersects(playerRect) && this.rarity < playerRarity){
			gc.setFill(Color.BLACK);
			gc.setFont(GameScreen.FONT_15);
			gc.fillText("Press E to confirm", this.x, this.y-SIZE/2-3);
		}
	}
}
