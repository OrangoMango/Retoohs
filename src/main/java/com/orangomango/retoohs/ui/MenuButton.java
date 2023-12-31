package com.orangomango.retoohs.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;

import com.orangomango.retoohs.MainApplication;

public class MenuButton{
	private GraphicsContext gc;
	private Runnable onClick;
	private Image image;
	private double x, y, w, h;
	
	public MenuButton(GraphicsContext gc, double x, double y, double w, double h, Image image, Runnable r){
		this.gc = gc;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.image = image;
		this.onClick = r;
	}
	
	public boolean click(double x, double y){
		x /= MainApplication.SCALE;
		y /= MainApplication.SCALE;
		Rectangle2D rect = new Rectangle2D(this.x, this.y, this.w, this.h);
		if (rect.contains(x, y)){
			this.onClick.run();
			return true;
		}
		return false;
	}
	
	public void render(){
		gc.drawImage(this.image, this.x, this.y, this.w, this.h);
	}
}
