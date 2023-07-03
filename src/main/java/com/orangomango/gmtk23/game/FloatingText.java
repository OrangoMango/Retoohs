package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import com.orangomango.gmtk23.ui.GameScreen;

public class FloatingText{
	private String text;
	private GraphicsContext gc;
	private double x, y;
	private double deltaY;

	public FloatingText(GraphicsContext gc, String text, double x, double y){
		this.gc = gc;
		this.text = text;
		this.x = x;
		this.y = y;
	}
	
	public double getDeltaY(){
		return this.deltaY;
	}
	
	public void render(){
		gc.setFill(Color.BLUE);
		gc.setFont(GameScreen.FONT_30);
		gc.fillText(this.text, this.x, this.y);
		final double speed = 3;
		this.y -= speed;
		this.deltaY += speed;
	}
}
