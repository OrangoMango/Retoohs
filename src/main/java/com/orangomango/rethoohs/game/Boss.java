package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.MainApplication;

public class Boss extends GameObject{
	private static final double SPEED = 1;
	public static final int HEALTH = 1000;
	private boolean attack = true;
	private double speed;
	private long lastSuper;
	private Double fixedAngle;

	public Boss(GraphicsContext gc, double x, double y){
		super(gc, x, y, 128, 128);
		this.speed = SPEED;
		this.hp = HEALTH;
		this.lastSuper = System.currentTimeMillis();
	}
	
	private void makeSuper(){
		Random random = new Random();
		int n = random.nextInt(2);
		MainApplication.playSound(MainApplication.BOSSSUPER_SOUND, false);
		this.hp += 50;
		switch (n){
			case 0:
				for (int i = 0; i < 5; i++){
					Enemy e = new Enemy(this.gc, random.nextInt(MainApplication.WIDTH-200)+100, random.nextInt(MainApplication.HEIGHT-200)+100, GameScreen.getInstance().getPlayer(), 0);
					GameScreen.getInstance().getGameObjects().add(e);
				}
				break;
			case 1:
				this.speed *= 8;
				Player player = GameScreen.getInstance().getPlayer();
				this.fixedAngle = Math.atan2(player.getY()-this.y, player.getX()-this.x);
				MainApplication.schedule(() -> {
					this.speed = SPEED;
					this.fixedAngle = null;
				}, 2000);
				break;
		}
	}
	
	@Override
	public void render(){
		gc.setFill(Color.BLACK);
		gc.fillOval(this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		Player player = GameScreen.getInstance().getPlayer();
		double angle = this.fixedAngle != null ? this.fixedAngle : Math.atan2(player.getY()-this.y, player.getX()-this.x);
		double distance = Math.sqrt(Math.pow(player.getX()-this.x, 2)+Math.pow(player.getY()-this.y, 2));
		if (distance < this.w/2+40){
			if (this.attack){
				player.damage(25);
				this.attack = false;
				MainApplication.schedule(() -> this.attack = true, 1000);
			}
			if (this.speed != SPEED){
				move(this.speed*Math.cos(angle), this.speed*Math.sin(angle), false);
			}
		} else {
			move(this.speed*Math.cos(angle), this.speed*Math.sin(angle), false);
		}
		
		if (System.currentTimeMillis()-this.lastSuper > 7000){ // 20s
			this.lastSuper = System.currentTimeMillis();
			makeSuper();
		}
	}
}
