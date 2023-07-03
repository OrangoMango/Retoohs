package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

import java.util.Random;

import com.orangomango.gmtk23.MainApplication;
import com.orangomango.gmtk23.ui.GameScreen;

public class BonusPoint{
	private GraphicsContext gc;
	private double x, y;
	private long startTime;
	private static final double SIZE = 20;
	private static final int MAXTIME = 30000;
	
	public BonusPoint(GraphicsContext gc, double x, double y){
		this.gc = gc;
		this.x = x;
		this.y = y;
	}
	
	public void relocate(){
		Random random = new Random();
		this.x = random.nextInt(MainApplication.WIDTH-200)+100;
		this.y = random.nextInt(MainApplication.HEIGHT-200)+100;
		this.startTime = System.currentTimeMillis();
	}
	
	public void render(){
		gc.setFill(Color.YELLOW);
		gc.fillOval(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Player player = GameScreen.getInstance().getPlayer();
		Rectangle2D thisRect = new Rectangle2D(this.x-SIZE/2, this.y-SIZE/2, SIZE, SIZE);
		Rectangle2D playerRect = new Rectangle2D(player.getX()-player.getWidth()/2, player.getY()-player.getHeight()/2, player.getWidth(), player.getHeight());
		long diff = System.currentTimeMillis()-this.startTime;
		if (thisRect.intersects(playerRect)){
			GameScreen.getInstance().score += 50;
			player.heal(10);
			relocate();
		} else if (diff > MAXTIME){
			GameScreen.getInstance().score -= 150;
			relocate();
		}
		gc.setFill(Color.YELLOW);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1.2);
		gc.fillRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE*(1-(double)diff/MAXTIME), 7);
		gc.strokeRect(this.x-SIZE/2, this.y+SIZE/2+5, SIZE, 7);
	}
}
