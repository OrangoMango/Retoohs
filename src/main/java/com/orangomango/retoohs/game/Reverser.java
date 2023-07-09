package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.ui.GameScreen;

public class Reverser{
	private GraphicsContext gc;
	private Image image = MainApplication.loadImage("warning.png");
	private volatile boolean blink;
	private long lastTime;
	private boolean startAllowed = true;
	private boolean makeSound = false;
	
	public Reverser(GraphicsContext gc){
		this.gc = gc;
		this.lastTime = System.currentTimeMillis();
		MainApplication.schedulePeriodic(() -> {
			if (!GameScreen.getInstance().isPaused()){
				this.blink = !this.blink;
				if (this.blink && this.makeSound){
					MainApplication.playSound(MainApplication.WARNING_SOUND, false);
				}
			}
		}, 250);
	}
	
	public void start(){
		this.startAllowed = false;
		this.blink = false;
		this.makeSound = false;
		GameScreen.getInstance().playsPlayer = false;
		GameScreen.getInstance().getPlayer().heal(100);
		GameScreen.getInstance().tempstop(gc.getCanvas());
	}
	
	public void allowStart(){
		this.startAllowed = true;
		this.lastTime = System.currentTimeMillis();
	}
	
	public void render(){
		if (this.blink && this.makeSound){
			gc.drawImage(this.image, MainApplication.WIDTH-100, MainApplication.HEIGHT-100, 64, 64);
		}
		long diff = System.currentTimeMillis()-this.lastTime-GameScreen.getInstance().getPausedTime();
		if (diff > 25000 && this.startAllowed){
			this.makeSound = true;
		}
		if (diff > 30000 && this.startAllowed && GameScreen.getInstance().getCurrentBoss() == null){
			start();
		}
	}
}
