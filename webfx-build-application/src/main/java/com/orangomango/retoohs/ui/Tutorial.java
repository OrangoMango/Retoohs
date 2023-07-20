package com.orangomango.retoohs.ui;

import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.util.*;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.json.Json;
import dev.webfx.platform.util.keyobject.ReadOnlyIndexedArray;
import dev.webfx.platform.resource.Resource;

import com.orangomango.retoohs.MainApplication;
import com.orangomango.retoohs.game.Enemy;

public class Tutorial{
	private JsonObject json;
	private List<ReadOnlyJsonObject> timeStamps = new ArrayList<>();
	private int index = -1;
	private String text;
	private String command;
	private GraphicsContext gc;

	public Tutorial(GraphicsContext gc){
		this.gc = gc;
		this.json = Json.parseObjectSilently(Resource.getText(Resource.toUrl("/files/tutorial.json", Tutorial.class)));
		for (int i = 0; i < this.json.getArray("timestamps").size(); i++){
			ReadOnlyJsonObject o = this.json.getArray("timestamps").getObject(i);
			this.timeStamps.add(o);
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
		ReadOnlyJsonObject obj = this.timeStamps.get(this.index);
		this.text = obj.getString("text");
		this.command = obj.getString("command");
		int time = obj.getInteger("time");
		MainApplication.schedule(() -> {
			GameScreen.getInstance().setPause(true);
			GameScreen.getInstance().pausedImage = MainApplication.assetLoader.getImage("background.png");
			if (!this.command.startsWith("finish")){
				trigger();
			}
		}, time);
	}
}
