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
		if ((new Rectangle2D(x, y, 100, 100)).contains(evt.getX(), evt.getY())){
			angle = Math.atan2(evt.getY()-(y+50), evt.getX()-(x+50));
			extraX = evt.getX()-x-50;
			extraY = evt.getY()-y-50;
			insidePress = true;
		} else if (insidePress){
			angle = Math.atan2(evt.getY()-(y+50), evt.getX()-(x+50));
			extraX = 50*Math.cos(angle);
			extraY = 50*Math.sin(angle);
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
		gc.setStroke(Color.web("#FF7500"));
		gc.setLineWidth(4);
		gc.strokeOval(x, y, 100, 100);
		gc.setFill(Color.web("#FF7500"));
		gc.fillOval(x+30+extraX, y+30+extraY, 40, 40);
	}
}