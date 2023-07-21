package com.orangomango.retoohs.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.MouseButton;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.TextInputDialog;

import java.util.*;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.Leaderboard;

public class GameOverScreen{
	private MediaPlayer mediaPlayer;
	private long time;
	private int score;
	private int bossesKilled;
	private boolean clickAllowed;
	private MenuButton submit;
	private Leaderboard lb;
	private List<Map.Entry<String, Integer>> output;

	public GameOverScreen(long time, int s, int b){
		this.time = time;
		this.score = s;
		this.bossesKilled = b;
		MainApplication.audioPlayed = false;
		this.mediaPlayer = MainApplication.playSound(MainApplication.GAMEOVER_BACKGROUND_MUSIC, true);
		MainApplication.schedule(() -> this.clickAllowed = true, 2000);
	}

	public StackPane getLayout(){
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(MainApplication.WIDTH, MainApplication.HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		canvas.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY){
				boolean ok = this.submit.click(e.getX(), e.getY());
				if (this.clickAllowed && !ok){
					if (this.mediaPlayer != null) this.mediaPlayer.stop();
					HomeScreen hs = new HomeScreen();
					MainApplication.stage.getScene().setRoot(hs.getLayout());
				}
			}
		});

		this.lb = new Leaderboard("https://mangogamesid.000webhostapp.com/games/retoohs/leaderboard.php");
		updateLead();

		this.submit = new MenuButton(gc, 800, 500, 120, 30, MainApplication.assetLoader.getImage("warning.png"), () -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Submit score");
			dialog.setHeaderText("Enter username");
			dialog.showAndWait().ifPresent(v -> {
				if (!v.equals("") && this.output != null){
					boolean allow = true;
					for (Map.Entry<String, Integer> entry : this.output){
						if (entry.getKey().equals(v)){
							allow = this.score > entry.getValue();
						}
					}
					if (allow){
						MainApplication.schedule(() -> {
							this.lb.addEntry(v, this.score);
							updateLead();
						}, 500);
					}
				}
			});
		});
		
		Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/MainApplication.FPS), e -> update(gc)));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
		
		pane.getChildren().add(canvas);
		return pane;
	}

	private void updateLead(){
		MainApplication.schedule(() -> {
			this.lb.load();
			this.output = this.lb.getEntries();
			this.output.sort((v1, v2) -> -Integer.compare(v1.getValue(), v2.getValue()));
		}, 500);
	}
	
	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.setFill(Color.ORANGE);
		gc.fillRect(0, 0, MainApplication.WIDTH, MainApplication.HEIGHT);
		gc.save();
		gc.scale(MainApplication.SCALE, MainApplication.SCALE);
		gc.setFill(Color.BLACK);
		gc.setFont(GameScreen.FONT_30);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText("GAME OVER\n\nYou survived "+(this.time/1000)+" seconds\nwith a score of "+this.score+"\nand killed "+this.bossesKilled+" bosses\n\nClick the screen to exit", 300, 155);
		this.submit.render();
		
		gc.setFill(Color.BLUE);
		if (this.output != null){
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for (Map.Entry<String, Integer> entry : this.output){
				if (i == 10) break;
				builder.append(++i).append(". ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
			}
			gc.fillText(builder.toString(), 750, 125);
		} else {
			gc.fillText("Loading leaderboard...", 750, 125);
		}

		gc.restore();
	}
}
