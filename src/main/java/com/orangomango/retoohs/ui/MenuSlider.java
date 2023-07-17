package com.orangomango.retoohs.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.geometry.Rectangle2D;

import java.util.function.Consumer;

public class MenuSlider{
	private GraphicsContext gc;
	private double x, y, w, h;
	private Image icon;
	private double progress = 1;
	private Consumer<Double> onProgress;

	public MenuSlider(GraphicsContext gc, double x, double y, double w, double h, Image icon, Consumer<Double> onProgress){
		this.gc = gc;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.icon = icon;
		this.onProgress = onProgress;
	}

	public void setProgress(double v){
		this.progress = v;
	}

	public void setProgress(double x, double y){
		Rectangle2D rect = new Rectangle2D(this.x, this.y, this.w, this.h);
		if (rect.contains(x, y)){
			this.progress = (x-this.x)/this.w;
			this.onProgress.accept(this.progress);
		}
	}

	public void render(){
		gc.drawImage(this.icon, this.x-this.icon.getWidth()-5, this.y-(this.icon.getHeight()-this.h)/2, this.icon.getWidth(), this.icon.getHeight());
		gc.setFill(Color.BLACK);
		gc.fillRect(this.x, this.y, this.w*this.progress, this.h);
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(3);
		gc.strokeRect(this.x, this.y, this.w, this.h);
	}
}