package com.orangomango.retoohs.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

public class UIBar{
	private GraphicsContext gc;
	private double x, y, w, h;
	private Image image;
	private double progress;
	private double offX, offY;
	private Color color;
	private double pw, ph;

	public UIBar(GraphicsContext gc, double x, double y, double w, double h, Image image, Color color, double ox, double oy, double pw, double ph){
		this.gc = gc;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.image = image;
		this.offX = ox;
		this.offY = oy;
		this.color = color;
		this.pw = pw;
		this.ph = ph;
	}

	public void setProgress(double p){
		this.progress = p;
	}

	public void render(){
		gc.setFill(this.color);
		gc.fillRect(this.x+this.offX, this.y+this.offY, this.pw*this.progress, this.ph);
		gc.drawImage(this.image, this.x, this.y, this.w, this.h);
	}
}
