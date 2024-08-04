package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;
import javafx.scene.text.TextAlignment;
import javafx.scene.media.MediaPlayer;

import java.io.*;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.AssetLoader;

public class CreditsScreen{
	private Timeline loop;
	private double scroll;
	private String credits;
	private MediaPlayer mediaPlayer;
	private boolean clickAllowed;
	private Image background = AssetLoader.getInstance().getImage("background.png");
	
	public CreditsScreen(){
		this.mediaPlayer = MainApplication.playMusic("menu_background.wav");
		MainApplication.schedule(() -> this.clickAllowed = true, 1500);
	}
	
	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY && this.clickAllowed){
				this.loop.stop();
				if (this.mediaPlayer != null) this.mediaPlayer.stop();
				HomeScreen hs = new HomeScreen();
				MainApplication.stage.getScene().setRoot(hs.getLayout());
			}
		});
		
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(MainApplication.class.getResourceAsStream("/files/credits.txt")));
			reader.lines().forEach(line -> builder.append(line).append("\n"));
			reader.close();
			this.credits = builder.toString();
		} catch (IOException ex){
			ex.printStackTrace();
		}
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.drawImage(this.background, 0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.save();
		gc.scale(MainApplication.SCALE, MainApplication.SCALE);
		gc.setFill(Color.WHITE);
		gc.save();
		gc.setFont(GameScreen.FONT_30);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.translate(0, -this.scroll);
		gc.fillText(this.credits, 500, 125);
		gc.restore();
		this.scroll++;
		if (this.scroll > 600){
			this.scroll = 0;
		}
		gc.restore();
	}
}
