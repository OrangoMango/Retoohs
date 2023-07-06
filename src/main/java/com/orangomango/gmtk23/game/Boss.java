package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

import com.orangomango.gmtk23.ui.GameScreen;
import com.orangomango.gmtk23.MainApplication;

public class Boss extends GameObject{
	private static final double SPEED = 1;
	private boolean attack = true;
	private long lastSuper;

	public Boss(GraphicsContext gc, double x, double y){
		super(gc, x, y, 128, 128);
		this.hp = 1000;
	}
	
	private void makeSuper(){
		Random random = new Random();
		int n = 0;
		MainApplication.playSound(MainApplication.BOSSSUPER_SOUND, false);
		switch (n){
			case 0:
				for (int i = 0; i < 5; i++){
					Enemy e = new Enemy(this.gc, random.nextInt(MainApplication.WIDTH-200)+100, random.nextInt(MainApplication.HEIGHT-200)+100, GameScreen.getInstance().getPlayer(), 0);
					GameScreen.getInstance().getGameObjects().add(e);
				}
				break;
		}
	}
	
	@Override
	public void render(){
		gc.setFill(Color.BLACK);
		gc.fillOval(this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		Player player = GameScreen.getInstance().getPlayer();
		double angle = Math.atan2(player.getY()-this.y, player.getX()-this.x);
		double distance = Math.sqrt(Math.pow(player.getX()-this.x, 2)+Math.pow(player.getY()-this.y, 2));
		if (distance < this.w/2+60){
			if (this.attack){
				player.damage(25);
				this.attack = false;
				MainApplication.schedule(() -> this.attack = true, 1000);
			}
		} else {
			move(SPEED*Math.cos(angle), SPEED*Math.sin(angle), false);
		}
		
		if (System.currentTimeMillis()-this.lastSuper > 20000){
			this.lastSuper = System.currentTimeMillis();
			makeSuper();
		}
	}
}
