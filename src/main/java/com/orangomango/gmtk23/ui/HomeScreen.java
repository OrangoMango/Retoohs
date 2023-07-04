package com.orangomango.gmtk23.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;

import com.orangomango.gmtk23.MainApplication;

public class HomeScreen{
	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			GameScreen gs = new GameScreen();
			MainApplication.stage.getScene().setRoot(gs.getLayout());
		});
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void update(GraphicsContext gc){
		
	}
}
