package com.orangomango.retoohs.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Rectangle2D;

public class JoyStick{
	private GraphicsContext gc;
	private double x;
	private double y;
	private double extraX, extraY;
	private double angle;
	private boolean insidePress;
	
	public JoyStick(GraphicsContext gc, double px, double py){
		this.gc = gc;
		this.x = px;
		this.y = py;
	}
	
	public void onMousePressed(MouseEvent evt){
		if ((new Rectangle2D(x, y, 150, 150)).contains(evt.getX(), evt.getY())){
			angle = Math.atan2(evt.getY()-(y+75), evt.getX()-(x+75));
			extraX = evt.getX()-x-75;
			extraY = evt.getY()-y-75;
			insidePress = true;
		} else if (insidePress){
			angle = Math.atan2(evt.getY()-(y+75), evt.getX()-(x+75));
			extraX = 75*Math.cos(angle);
			extraY = 75*Math.sin(angle);
		}
	}
	
	public void onMouseReleased(){
		this.extraX = 0;
		this.extraY = 0;
		this.angle = 0;
		this.insidePress = false;
	}
	
	public boolean isUsed(){
		return this.insidePress;
	}
	
	public double getAngle(){
		return this.angle;
	}
	
	public void render(){
		gc.save();
		gc.setGlobalAlpha(0.7);
		gc.setStroke(Color.web("#FF7500"));
		gc.setLineWidth(4);
		gc.strokeOval(x, y, 150, 150);
		gc.setFill(Color.web("#FF7500"));
		gc.fillOval(x+50+extraX, y+50+extraY, 50, 50);
		gc.restore();
	}
}