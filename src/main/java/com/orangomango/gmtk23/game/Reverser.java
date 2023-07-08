package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.ui.GameScreen;

public class Reverser{
	private GraphicsContext gc;
	private Image image = MainApplication.loadImage("warning.png");
	private volatile boolean blink;
	private long lastTime;
	private boolean startAllowed = true;
	
	public Reverser(GraphicsContext gc){
		this.gc = gc;
		this.lastTime = System.currentTimeMillis();
		MainApplication.schedulePeriodic(() -> {
			if (!GameScreen.getInstance().isPaused() && !this.startAllowed){
				this.blink = !this.blink;
				if (this.blink) MainApplication.playSound(MainApplication.WARNING_SOUND, false);
			}
		}, 500);
	}
	
	public void start(){
		this.startAllowed = false;
		GameScreen.getInstance().playsPlayer = false;
		GameScreen.getInstance().getPlayer().heal(100);
		GameScreen.getInstance().tempstop(gc.getCanvas());
	}
	
	public void allowStart(){
		this.startAllowed = true;
	}
	
	public void render(){
		if (this.blink){
			gc.drawImage(this.image, MainApplication.WIDTH-100, MainApplication.HEIGHT-100, 64, 64);
		}
		long diff = System.currentTimeMillis()-this.lastTime-GameScreen.getInstance().getPausedTime();
		if (diff > 20000 && this.startAllowed){
			this.lastTime = System.currentTimeMillis();
			start();
		}
	}
}
