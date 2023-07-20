package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.geometry.Rectangle2D;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.ui.GameScreen;

public class Drop{
	private GraphicsContext gc;
	private double x, y;
	private long startTime;
	private boolean exists = true;
	private int rarity;
	private Image image;
	private int localPausedTime;
	
	private static final double SIZE = 20;
	private static final int MAXTIME = 15000;
	private static final Image IMAGE_COMMON = MainApplication.assetLoader.getImage("drop_0.png");
	private static final Image IMAGE_EPIC = MainApplication.assetLoader.getImage("drop_1.png");
	private static final Image IMAGE_LEGENDARY = MainApplication.assetLoader.getImage("drop_2.png");
	
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
		if (this.rarity >= Bullet.getBulletConfig(GameScreen.getInstance().getPlayer().getCurrentGun()).getInteger("rarity")){
			GameScreen.getInstance().targetDrop = this;
		}
		resetPausedTime();
	}
	
	public boolean exists(){
		return this.exists;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}

	public void resetPausedTime(){
		this.localPausedTime = GameScreen.getInstance().getPausedTime();
	}
	
	public void render(){
		gc.drawImage(this.image, this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Player player = GameScreen.getInstance().getPlayer();
		long diff = System.currentTimeMillis()-this.startTime-(GameScreen.getInstance().getPausedTime()-this.localPausedTime);
		Rectangle2D thisRect = new Rectangle2D(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		GunObject collision = null;
		for (int i = 0; i < GameScreen.getInstance().getGameObjects().size(); i++){
			GameObject obj = GameScreen.getInstance().getGameObjects().get(i);
			Rectangle2D otherRect = new Rectangle2D(obj.getX()-obj.getWidth()/2, obj.getY()-obj.getHeight()/2, obj.getWidth(), obj.getHeight());
			if (obj instanceof GunObject && thisRect.intersects(otherRect)){
				collision = (GunObject)obj;
				break;
			}
		}
		int currentRarity = collision == null ? -1 : Bullet.getBulletConfig(collision.getCurrentGun()).getInteger("rarity");
		if (collision != null && (collision instanceof Player || (!GameScreen.getInstance().playsPlayer && collision instanceof Enemy))){
			if (this.rarity >= currentRarity || GameScreen.getInstance().getKeys().getOrDefault(KeyCode.E, false) || !GameScreen.getInstance().playsPlayer){
				String gunName = Bullet.getRandomGun(this.rarity);
				MainApplication.playSound(MainApplication.DROP_SOUND, false);
				collision.setGun(gunName);
				GameScreen.getInstance().getFloatingTexts().add(new FloatingText(this.gc, gunName.replace("_", " "), this.x-5, this.y));
				this.exists = false;
				GameScreen.getInstance().applyTutorial(t -> {
					if (t.getIndex() == 1){
						t.next();
					}
				});
				if (GameScreen.getInstance().targetDrop == this){
					GameScreen.getInstance().targetDrop = null;
				}
				if (collision instanceof Enemy){
					((Enemy)collision).setShooter();
				}
			}
		} else if (diff > MAXTIME){
			this.exists = false;
		}
		gc.setFill(Color.CYAN);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1.2);
		gc.fillRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE*(1-(double)diff/MAXTIME), 7);
		gc.strokeRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE, 7);
		if (collision != null && this.rarity < currentRarity && GameScreen.getInstance().playsPlayer){
			gc.setFill(Color.BLACK);
			gc.setFont(GameScreen.FONT_15);
			gc.fillText("Press E to confirm", this.x, this.y-SIZE/2-3);
		}
	}
}
