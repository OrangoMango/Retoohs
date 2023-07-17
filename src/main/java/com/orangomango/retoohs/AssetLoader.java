package com.orangomango.retoohs;

import javafx.scene.image.Image;

import java.util.*;

public class AssetLoader{
	private Map<String, Image> images = new HashMap<>();

	public void loadImages(){
		String[] images = new String[]{"arrow.png", "background.png", "bonusPoint.png", "bossbar.png", "boss_smash.png", "bullet.png", "button_credits.jpg",
										"button_home.jpg", "button_play.jpg", "button_resume.jpg", "drop_0.png", "drop_1.png", "drop_2.png", "enemy.png", "exbar.png",
										"ground.png", "ground_stone_0.png", "ground_stone_1.png", "hpbar.png", "icon.png", "logo.png", "normal_gun.png", "player.png",
										"restorebar.png", "reverse.png", "warning.png", "icon_sfx.png", "icon_music.png"};
		for (String name : images){
			this.images.put(name, loadImage(name));
		}
	}

	public Image getImage(String name){
		return this.images.get(name);
	}

	public static Image loadImage(String name){
		return new Image(AssetLoader.class.getResourceAsStream("/images/"+name));
	}
}