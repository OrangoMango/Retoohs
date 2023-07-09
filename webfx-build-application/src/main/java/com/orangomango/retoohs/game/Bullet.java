package com.orangomango.retoohs.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.geometry.Point2D;
import javafx.scene.media.AudioClip;

import java.util.*;

import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.Json;
import dev.webfx.platform.util.keyobject.ReadOnlyIndexedArray;
import dev.webfx.platform.resource.Resource;

import com.orangomango.retoohs.ui.GameScreen;
import com.orangomango.retoohs.MainApplication;

public class Bullet extends GameObject{
	public static class ShooterConfig{
		private int ammo, defaultAmmo;
		private int ammoAmount, defaultAmount;
		private boolean shoot = true;
		private int cooldown;
		private String name;
		
		public ShooterConfig(String name, int a, int n, int c){
			this.ammo = a;
			this.defaultAmmo = a;
			this.ammoAmount = n;
			this.defaultAmount = n;
			this.cooldown = c;
			this.name = name;
		}
		
		public boolean shoot(){
			if (!this.shoot) return false;
			this.shoot = false;
			MainApplication.schedule(() -> this.shoot = true, this.cooldown);
			this.ammo--;
			if (this.ammo < 0){
				this.ammo = 0;
				MainApplication.playSound(MainApplication.NOAMMO_SOUND, false);
				return false;
			}
			MainApplication.playSound(gunSounds.get(this.name), false);
			return true;
		}
		
		public void reload(){
			this.ammoAmount--;
			if (this.ammoAmount >= 0){
				this.ammo = this.defaultAmmo;
			} else {
				this.ammoAmount = 0;
			}
		}
		
		public int getAmmo(){
			return this.ammo;
		}
		
		public int getAmmoAmount(){
			return this.ammoAmount;
		}
		
		public int getDefaultAmmo(){
			return this.defaultAmmo;
		}
		
		public int getDefaultAmount(){
			return this.defaultAmount;
		}
	}
	
	private GameObject shooter;
	private double angle;
	private boolean exists = true;
	private boolean explode;
	private int dmg;
	private JsonObject config;
	private Point2D startPos;
	private static final Image IMAGE = MainApplication.loadImage("bullet.png");

	private static JsonObject bulletConfig;
	public static Map<GameObject, ShooterConfig> configs = new HashMap<>();
	public static Map<String, AudioClip> gunSounds = new HashMap<>();
	public static Map<String, Image> gunImages = new HashMap<>();
	
	static {
		bulletConfig = Json.parseObjectSilently(Resource.getText(Resource.toUrl("/files/bulletConfig.json", Bullet.class)));
		// Load images
		ReadOnlyIndexedArray iterator = bulletConfig.keys();
		for (int i = 0; i < iterator.size(); i++){
			String key = iterator.getString(i);
			gunImages.put(key, MainApplication.loadImage(getBulletConfig(key).getString("imageName")));
		}
	}
	
	public Bullet(GraphicsContext gc, GameObject shooter, JsonObject config, double x, double y, double angle, int dmg){
		super(gc, x, y, 9, 9);
		this.shooter = shooter;
		this.config = config;
		this.angle = angle;
		this.dmg = dmg;
		this.startPos = new Point2D(this.x, this.y);
	}
	
	public static String getRandomGun(int rarity){
		List<String> guns = new ArrayList<>();
		ReadOnlyIndexedArray iterator = bulletConfig.keys();
                for (int i = 0; i < iterator.size(); i++){
			String key = iterator.getString(i);
			int r = getBulletConfig(key).getInteger("rarity");
			if (r == rarity){
				guns.add(key);
			}
		}
		Random random = new Random();
		return guns.get(random.nextInt(guns.size()));
	}
	
	public static void loadGunSounds(){
		ReadOnlyIndexedArray iterator = bulletConfig.keys();
		for (int i = 0; i < iterator.size(); i++){
			String key = iterator.getString(i);
			gunSounds.put(key, new AudioClip(Resource.toUrl("/audio/"+getBulletConfig(key).getString("audioName"), Bullet.class)));
		}
	}
	
	public static JsonObject getBulletConfig(String name){
		return bulletConfig.getObject(name);
	}
	
	public static void applyConfiguration(String name, List<Bullet> bullets, GraphicsContext gc, double x, double y, double angle, GameObject shooter){
		JsonObject config = getBulletConfig(name);
		ShooterConfig conf = configs.getOrDefault(shooter, null);
		if (conf == null){
			conf = new ShooterConfig(name, config.getInteger("ammo"), config.getInteger("ammoAmount"), config.getInteger("cooldown"));
			configs.put(shooter, conf);
		}
		if (bullets != null){
			boolean ok = conf.shoot();
			if (ok){
				if (config.getBoolean("screenShake")) GameScreen.getInstance().screenShake();
				for (int j = 0; j < config.getArray("angles").size(); j++){
					int o = config.getArray("angles").getInteger(j);
					for (int i = 0; i < config.getObject("timing").getInteger("amount"); i++){
						MainApplication.schedule(() -> {
							Bullet b = new Bullet(gc, shooter, config, x, y, angle+Math.toRadians(o), config.getInteger("damage"));
	        					if (config.getBoolean("explode")) b.setExplode();
	        					bullets.add(b);
						}, config.getObject("timing").getInteger("time"));
					}
				}
			}
		}
	}
	
	public void setExplode(){
		this.explode = true;
	}
	
	public boolean exists(){
		return this.exists;
	}
	
	private void explode(){
		if (this.explode){
			GameScreen.getInstance().getExplosions().add(new Explosion(this.gc, this.x, this.y, this.dmg));
			this.exists = false;
		}
	}
	
	@Override
	public void render(){
		double speed = this.config.getInteger("speed");
		gc.drawImage(IMAGE, 1+(this.explode ? 12+2 : 0), 1, 12, 12, this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		boolean m = move(speed*Math.cos(this.angle), speed*Math.sin(this.angle), false);
		double distance = this.startPos.distance(new Point2D(this.x, this.y));
		int increment = (int)(distance/15)*this.config.getObject("distanceDamage").getInteger("increment");
		int min = this.config.getObject("distanceDamage").getInteger("min");
		int max = this.config.getObject("distanceDamage").getInteger("max");
		increment += min;
		if (Math.abs(increment) > Math.abs(max)){
			increment = max;
		}
		for (int i = 0; i < GameScreen.getInstance().getGameObjects().size(); i++){
			GameObject obj = GameScreen.getInstance().getGameObjects().get(i);
			if (obj != this.shooter && obj.collided(this) && !obj.isInvulnerable()){
				obj.damage(this.dmg+increment);
				this.exists = this.config.getBoolean("goPast");
				explode();
			}
		}
		if (distance > 300){
			explode();
		}
		if (distance > this.config.getDouble("maxDistance") || !m){
			this.exists = false;
		}
	}
}
