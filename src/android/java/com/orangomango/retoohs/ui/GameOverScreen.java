package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.MouseButton;

import com.orangomango.retoohs.MainApplication;

public class GameOverScreen{
	private String mediaPlayer;
	private long time;
	private int score;
	private int bossesKilled;
	private boolean clickAllowed;
	
	public GameOverScreen(long time, int s, int b){
		this.time = time;
		this.score = s;
		this.bossesKilled = b;
		MainApplication.audioPlayed = false;
		this.mediaPlayer = MainApplication.GAMEOVER_BACKGROUND_MUSIC;
		MainApplication.playSound(this.mediaPlayer, true);
		MainApplication.schedule(() -> this.clickAllowed = true, 2000);
	}

	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY && this.clickAllowed){
				MainApplication.removeMediaPlayer(this.mediaPlayer);
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
		gc.save();
		gc.scale(MainApplication.SCALE, MainApplication.SCALE);
		gc.setFill(Color.BLACK);
		gc.setFont(GameScreen.FONT_45);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText("GAME OVER\n\nYou survived "+(this.time/1000)+" seconds\nwith a score of "+this.score+"\nand killed "+this.bossesKilled+" bosses\n\nClick the screen to exit", 500, 125);
		gc.restore();
	}
}
