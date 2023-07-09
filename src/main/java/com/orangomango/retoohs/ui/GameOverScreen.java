package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.MouseButton;
import javafx.scene.media.MediaPlayer;

import com.orangomango.retoohs.MainApplication;

public class GameOverScreen{
	private MediaPlayer mediaPlayer;
	private long time;
	private int score;
	private int bossesKilled;
	
	public GameOverScreen(long time, int s, int b){
		this.time = time;
		this.score = s;
		this.bossesKilled = b;
		MainApplication.schedule(() -> this.mediaPlayer = MainApplication.playSound(MainApplication.GAMEOVER_BACKGROUND_MUSIC, true), 500);
	}

	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
				if (this.mediaPlayer != null) this.mediaPlayer.stop();
				HomeScreen hs = new HomeScreen();
				MainApplication.stage.getScene().setRoot(hs.getLayout());
			}
		});
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.ORANGE);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.BLACK);
		gc.setFont(GameScreen.FONT_45);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText("GAME OVER\n\nYou survived "+(this.time/1000)+" seconds\nwith a score of "+this.score+"\nand killed "+this.bossesKilled+" bosses\n\nClick the screen to exit", MainApplication.WIDTH/2, 125);
	}
}
