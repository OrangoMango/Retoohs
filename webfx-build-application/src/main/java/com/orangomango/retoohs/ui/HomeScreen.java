package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
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
		
		Image playButtonImage = MainApplication.loadImage("warning.png"); //placeholder
		MenuButton playButton = new MenuButton(gc, 425, 250, 150, 50, playButtonImage, () -> {
			this.loop.stop();
			if (this.mediaPlayer != null) this.mediaPlayer.stop();
			GameScreen gs = new GameScreen();
			MainApplication.stage.getScene().setRoot(gs.getLayout());
		});
		this.buttons.add(playButton);
		Image creditsButtonImage = MainApplication.loadImage("warning.png"); //placeholder
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
		gc.setFill(Color.LIME);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		for (MenuButton mb : this.buttons){
			mb.render();
		}
	}
}
