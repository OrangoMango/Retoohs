package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;
import javafx.scene.text.TextAlignment;
import javafx.scene.media.MediaPlayer;

import dev.webfx.platform.resource.Resource;

import com.orangomango.retoohs.MainApplication;

public class CreditsScreen{
	private Timeline loop;
	private double scroll;
	private String credits;
	private MediaPlayer mediaPlayer;
	
	public CreditsScreen(){
		this.mediaPlayer = MainApplication.playSound(MainApplication.MENU_BACKGROUND_MUSIC, true);
	}
	
	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
				this.loop.stop();
				if (this.mediaPlayer != null) this.mediaPlayer.stop();
				HomeScreen hs = new HomeScreen();
				MainApplication.stage.getScene().setRoot(hs.getLayout());
			}
		});

		this.credits = Resource.getText(Resource.toUrl("/files/credits.txt", CreditsScreen.class));
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.YELLOW);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.BLACK);
		gc.save();
		gc.setFont(GameScreen.FONT_30);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.translate(0, -this.scroll);
		gc.fillText(this.credits, MainApplication.WIDTH/2, 125);
		gc.restore();
		this.scroll++;
		if (this.scroll > MainApplication.HEIGHT){
			this.scroll = 0;
		}
	}
}
