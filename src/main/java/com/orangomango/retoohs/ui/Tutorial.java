package com.orangomango.retoohs.ui;

import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.util.*;
import org.json.JSONObject;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.AssetLoader;
import com.orangomango.retoohs.game.Enemy;

public class Tutorial{
	private JSONObject json;
	private List<JSONObject> timeStamps = new ArrayList<>();
	private int index = -1;
	private String text;
	private String command;
	private GraphicsContext gc;

	public Tutorial(GraphicsContext gc){
		this.gc = gc;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Tutorial.class.getResourceAsStream("/files/tutorial.json")));
			StringBuilder builder = new StringBuilder();
			reader.lines().forEach(builder::append);
			reader.close();
			this.json = new JSONObject(builder.toString());
			for (Object o : this.json.getJSONArray("timestamps")){
				this.timeStamps.add((JSONObject)o);
			}
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public String getCurrentText(){
		return this.text;
	}

	public int getIndex(){
		return this.index;
	}

	public void trigger(){
		if (this.command.startsWith("finish")){
			next();
		} else if (this.command.startsWith("summon_zombie")){
			int n = Integer.parseInt(this.command.split("summon_zombie ")[1]);
			Random random = new Random();
			for (int i = 0; i < n; i++){
				Enemy e = new Enemy(this.gc, random.nextInt(800)+100, random.nextInt(400)+100, GameScreen.getInstance().getPlayer(), 0);
				GameScreen.getInstance().getGameObjects().add(e);
			}
		} else if (this.command.startsWith("stop_after")){
			int n = Integer.parseInt(this.command.split("stop_after ")[1]);
			GameScreen.getInstance().setOnResume(() -> MainApplication.schedule(this::next, n));
		} else if (this.command.equals("trigger_reverser")){
			GameScreen.getInstance().getReverser().tutorialModify();
			GameScreen.getInstance().startSpawner(this.gc);
		} else if (this.command.equals("end_tutorial")){
			GameScreen.getInstance().setOnResume(() -> {
				GameScreen.getInstance().quit();
				HomeScreen hs = new HomeScreen();
				MainApplication.stage.getScene().setRoot(hs.getLayout());
			});
		}
	}

	public void next(){
		this.index++;
		JSONObject obj = this.timeStamps.get(this.index);
		this.text = obj.getString("text");
		this.command = obj.getString("command");
		int time = obj.getInt("time");
		MainApplication.schedule(() -> {
			GameScreen.getInstance().setPause(true);
			GameScreen.getInstance().pausedImage = AssetLoader.getInstance().getImage("background.png");
			if (!this.command.startsWith("finish")){
				trigger();
			}
		}, time);
	}
}