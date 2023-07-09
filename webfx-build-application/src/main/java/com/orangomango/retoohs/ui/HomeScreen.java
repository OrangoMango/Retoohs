package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;
import javafx.scene.media.MediaPlayer;

import java.util.*;

import com.orangomango.retoohs.MainApplication;

public class HomeScreen{
	private MediaPlayer mediaPlayer;
	private Timeline loop;
	private List<MenuButton> buttons = new ArrayList<>();
	private Image background = MainApplication.loadImage("background.png");
	private Image logo = MainApplication.loadImage("logo.png");
	
	public HomeScreen(){
		this.mediaPlayer = MainApplication.playSound(MainApplication.MENU_BACKGROUND_MUSIC, true);
	}
	
	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
				for (MenuButton mb : this.buttons){
					mb.click(e.getX(), e.getY());
				}
			}
		});
		
		Image playButtonImage = MainApplication.loadImage("button_play.jpg"); //placeholder
		MenuButton playButton = new MenuButton(gc, 425, 300, 150, 50, playButtonImage, () -> {
			this.loop.stop();
			if (this.mediaPlayer != null) this.mediaPlayer.stop();
			GameScreen gs = new GameScreen();
			MainApplication.stage.getScene().setRoot(gs.getLayout());
		});
		this.buttons.add(playButton);
		Image creditsButtonImage = MainApplication.loadImage("button_credits.jpg"); //placeholder
		MenuButton creditsButton = new MenuButton(gc, 425, 400, 150, 50, creditsButtonImage, () -> {
			this.loop.stop();
			if (this.mediaPlayer != null) this.mediaPlayer.stop();
			CreditsScreen cs = new CreditsScreen();
			MainApplication.stage.getScene().setRoot(cs.getLayout());
		});
		this.buttons.add(creditsButton);
		
		this.loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		this.loop.setCycleCount(Animation.INDEFINITE);
		this.loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.drawImage(this.background, 0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.drawImage(this.logo, 350, 150, 300, 100);
		for (MenuButton mb : this.buttons){
			mb.render();
		}
	}
}
