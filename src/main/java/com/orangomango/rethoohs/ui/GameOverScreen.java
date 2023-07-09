package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;

import com.orangomango.retoohs.MainApplication;

public class GameOverScreen{
	private MediaPlayer mediaPlayer;
	
	public GameOverScreen(){
		MainApplication.schedule(() -> this.mediaPlayer = MainApplication.playSound(MainApplication.GAMEOVER_BACKGROUND_MUSIC, true), 500);
	}

	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (this.mediaPlayer != null) this.mediaPlayer.stop();
			HomeScreen hs = new HomeScreen();
			MainApplication.stage.getScene().setRoot(hs.getLayout());
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
