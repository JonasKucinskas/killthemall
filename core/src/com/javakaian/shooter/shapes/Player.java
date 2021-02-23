package com.javakaian.shooter.shapes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Player {

	private float size;
	private Vector2 position;
	private String name;
	private Set<Bullet> bulletSet;

	public Player() {
	}

	public Player(float x, float y, float size) {
		this.position = new Vector2(x, y);
		this.size = size;
		this.name = UUID.randomUUID().toString().replaceAll("-", "");

		bulletSet = new HashSet<Bullet>();
	}

	public void render(ShapeRenderer sr) {
		// TODO Auto-generated method stub
		sr.setColor(Color.GREEN);
		sr.rect(position.x, position.y, size, size);
		sr.setColor(Color.WHITE);

		bulletSet.stream().forEach(b -> b.render(sr));
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Vector2 getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public Set<Bullet> getBulletSet() {
		return bulletSet;
	}
}
