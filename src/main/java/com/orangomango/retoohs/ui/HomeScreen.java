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
	private List<MenuSlider> sliders = new ArrayList<>();
	private Image background = MainApplication.assetLoader.getImage("background.png");
	private Image logo = MainApplication.assetLoader.getImage("logo.png");
	
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
				for (MenuSlider ms : this.sliders){
					ms.setProgress(e.getX(), e.getY());
				}
			}
		});

		canvas.setOnMouseDragged(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
				for (MenuSlider ms : this.sliders){
					ms.setProgress(e.getX(), e.getY());
				}
			}
		});
		
		this.buttons.add(new MenuButton(gc, 250, 300, 150, 50, MainApplication.assetLoader.getImage("button_play.jpg"), () -> {
			this.loop.stop();
			if (this.mediaPlayer != null) this.mediaPlayer.stop();
			GameScreen gs = new GameScreen();
			MainApplication.stage.getScene().setRoot(gs.getLayout());
		}));
		this.buttons.add(new MenuButton(gc, 250, 400, 150, 50, MainApplication.assetLoader.getImage("button_credits.jpg"), () -> {
			this.loop.stop();
			if (this.mediaPlayer != null) this.mediaPlayer.stop();
			CreditsScreen cs = new CreditsScreen();
			MainApplication.stage.getScene().setRoot(cs.getLayout());
		}));

		MenuSlider musicSlider = new MenuSlider(gc, 550, 315, 200, 30, MainApplication.assetLoader.getImage("icon_music.png"), v -> {
			MainApplication.musicVolume = v;
			if (this.mediaPlayer != null) this.mediaPlayer.setVolume(v);
		});
		musicSlider.setProgress(MainApplication.musicVolume);
		MenuSlider sfxSlider = new MenuSlider(gc, 550, 375, 200, 30, MainApplication.assetLoader.getImage("icon_sfx.png"), v -> MainApplication.sfxVolume = v);
		sfxSlider.setProgress(MainApplication.sfxVolume);
		this.sliders.add(musicSlider);
		this.sliders.add(sfxSlider);
		
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
		gc.drawImage(this.logo, 350, 120, 300, 100);
		for (MenuButton mb : this.buttons){
			mb.render();
		}
		for (MenuSlider ms : this.sliders){
			ms.render();
		}
		gc.restore();
	}
}
