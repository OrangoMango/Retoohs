package com.orangomango.gmtk23.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.media.AudioClip;

import java.io.*;
import java.util.*;
import org.json.JSONObject;

import com.orangomango.gmtk23.ui.GameScreen;
import com.orangomango.gmtk23.MainApplication;

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
	private JSONObject config;
	private Point2D startPos;

	private static JSONObject bulletConfig;
	public static Map<GameObject, ShooterConfig> configs = new HashMap<>();
	public static Map<String, AudioClip> gunSounds = new HashMap<>();
	
	static {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Bullet.class.getResourceAsStream("/bulletConfig.json")));
			StringBuilder builder = new StringBuilder();
			reader.lines().forEach(builder::append);
			reader.close();
			bulletConfig = new JSONObject(builder.toString());
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}
	
	public Bullet(GraphicsContext gc, GameObject shooter, JSONObject config, double x, double y, double angle, int dmg){
		super(gc, x, y, 15, 15);
		this.shooter = shooter;
		this.config = config;
		this.angle = angle;
		this.dmg = dmg;
		this.startPos = new Point2D(this.x, this.y);
	}
	
	public static String getRandomGun(int rarity){
		List<String> guns = new ArrayList<>();
		Iterator<String> iterator = bulletConfig.keys();
		while (iterator.hasNext()){
			String key = iterator.next();
			int r = getBulletConfig(key).getInt("rarity");
			if (r == rarity){
				guns.add(key);
			}
		}
		Random random = new Random();
		return guns.get(random.nextInt(guns.size()));
	}
	
	public static void loadGunSounds(){
		Iterator<String> iterator = bulletConfig.keys();
		while (iterator.hasNext()){
			String key = iterator.next();
			gunSounds.put(key, new AudioClip(Bullet.class.getResource("/audio/"+getBulletConfig(key).getString("audioName")).toExternalForm()));
		}
	}
	
	public static JSONObject getBulletConfig(String name){
		return bulletConfig.getJSONObject(name);
	}
	
	public static void applyConfiguration(String name, List<Bullet> bullets, GraphicsContext gc, double x, double y, double angle, GameObject shooter){
		JSONObject config = getBulletConfig(name);
		ShooterConfig conf = configs.getOrDefault(shooter, null);
		if (conf == null){
			conf = new ShooterConfig(name, config.getInt("ammo"), config.getInt("ammoAmount"), config.getInt("cooldown"));
			configs.put(shooter, conf);
		}
		if (bullets != null){
			boolean ok = conf.shoot();
			if (ok){
				Thread gen = new Thread(() -> {
					try {
						for (Object o : config.getJSONArray("angles")){
							for (int i = 0; i < config.getJSONObject("timing").getInt("amount"); i++){
								Bullet b = new Bullet(gc, shooter, config, x, y, angle+Math.toRadians((int)o), config.getInt("damage"));
								if (config.getBoolean("explode")) b.setExplode();
								bullets.add(b);
								Thread.sleep(config.getJSONObject("timing").getInt("time"));
							}
						}
					} catch (InterruptedException ex){
						ex.printStackTrace();
					}
				});
				gen.start();
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
			GameScreen.getInstance().explosion = new Explosion(this.gc, this.x, this.y, this.dmg);
			this.exists = false;
		}
	}
	
	@Override
	public void render(){
		double speed = this.config.getInt("speed");
		gc.setFill(this.explode ? Color.CYAN : Color.GRAY);
		gc.fillOval(this.x-this.w/2, this.y-this.h/2, this.w, this.h);
		boolean m = move(speed*Math.cos(this.angle), speed*Math.sin(this.angle), false);
		double distance = this.startPos.distance(new Point2D(this.x, this.y));
		int increment = (int)(distance/15)*this.config.getJSONObject("distanceDamage").getInt("increment");
		int min = this.config.getJSONObject("distanceDamage").getInt("min");
		int max = this.config.getJSONObject("distanceDamage").getInt("max");
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
