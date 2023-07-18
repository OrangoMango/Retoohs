package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;

import java.util.Random;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.ui.GameScreen;

public class BonusPoint{
	private GraphicsContext gc;
	private double x, y;
	private long startTime;
	private volatile int extraY;
	private boolean forward = true;
	private Image image = MainApplication.assetLoader.getImage("bonusPoint.png");
	private int localPausedTime;
	private double radius = 15;

	private static final double SIZE = 20;
	private static final int MAXTIME = 30000;
	
	public BonusPoint(GraphicsContext gc, double x, double y){
		this.gc = gc;
		this.x = x;
		this.y = y;
		resetPausedTime();
		MainApplication.schedulePeriodic(() -> {
			if (!GameScreen.getInstance().isPaused()){
				this.extraY += this.forward ? 1 : -1;
				if (this.extraY == 0 || this.extraY == 10){
					this.forward = !this.forward;
				}
			}
			this.radius += 2;
			if (this.radius > 55) this.radius = 15;
		}, 50);
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public void relocate(){
		if (GameScreen.getInstance().targetPoint == this){
			GameScreen.getInstance().targetPoint = null;
		}
		Random random = new Random();
		this.x = random.nextInt(800)+100;
		this.y = random.nextInt(400)+100;
		this.startTime = System.currentTimeMillis();
	}

	public void resetPausedTime(){
		this.localPausedTime = GameScreen.getInstance().getPausedTime();
	}
	
	public void render(){
		gc.save();
		gc.translate(0, this.extraY);
		gc.drawImage(this.image, this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Player player = GameScreen.getInstance().getPlayer();
		Rectangle2D thisRect = new Rectangle2D(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Rectangle2D playerRect = new Rectangle2D(player.getX()-player.getWidth()/2, player.getY()-player.getHeight()/2, player.getWidth(), player.getHeight());
		long diff = System.currentTimeMillis()-this.startTime-(GameScreen.getInstance().getPausedTime()-this.localPausedTime);
		if (thisRect.intersects(playerRect)){
			GameScreen.getInstance().score += 50*(GameScreen.getInstance().playsPlayer ? 1 : -1);
			player.heal(10);
			MainApplication.playSound(MainApplication.SCORE_SOUND, false);
			GameScreen.getInstance().applyTutorial(t -> {
				if (t.getIndex() == 0){
					t.trigger();
				}
			});
			relocate();
		} else if (diff > MAXTIME){
			GameScreen.getInstance().score -= 150*(GameScreen.getInstance().playsPlayer ? 1 : -1);
			MainApplication.playSound(MainApplication.SCORELOST_SOUND, false);
			relocate();
		}
		gc.setFill(Color.YELLOW);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1.2);
		gc.fillRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE*(1-(double)diff/MAXTIME), 7);
		gc.strokeRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE, 7);
		gc.restore();

		if (diff > MAXTIME-7500 && GameScreen.getInstance().playsPlayer){
			gc.setStroke(Color.ORANGE);
			gc.strokeOval(this.x-this.radius, this.y-this.radius+7, this.radius*2, this.radius*2);
		}
	}
}
