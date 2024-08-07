package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.AssetLoader;
import com.orangomango.retoohs.ui.GameScreen;

public class Reverser{
	private GraphicsContext gc;
	private Image image = AssetLoader.getInstance().getImage("warning.png");
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
				if (this.blink && this.makeSound && GameScreen.getInstance().getCurrentBoss() == null){
					MainApplication.playSound("warning.wav", false);
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
		GameScreen.getInstance().tempstop(this.gc.getCanvas());
		GameScreen.getInstance().applyTutorial(t -> {
			if (t.getIndex() == 4){
				t.next();
			}
		});
	}

	public void tutorialModify(){
		allowStart();
		this.lastTime = System.currentTimeMillis()-GameScreen.getInstance().getPausedTime()-15000;
	}
	
	public void allowStart(){
		this.startAllowed = true;
		this.lastTime = System.currentTimeMillis();
	}
	
	public void render(){
		if (this.blink && this.makeSound){
			gc.drawImage(this.image, 900, 50, 64, 64);
		}
		long diff = System.currentTimeMillis()-this.lastTime-GameScreen.getInstance().getPausedTime();
		if (diff > 15000 && this.startAllowed){
			this.makeSound = true;
		}
		if (diff > 20000 && this.startAllowed && GameScreen.getInstance().getCurrentBoss() == null){
			start();
		}
	}
}
