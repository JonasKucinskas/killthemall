package com.javakaian.shooter.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.javakaian.states.GameOverState;
import com.javakaian.states.State.StateEnum;

public class GameOverInput extends InputAdapter {

	private GameOverState gameOver;

	public GameOverInput(GameOverState game) {
		this.gameOver = game;
	}

	@Override
	public boolean keyDown(int keycode) {

		switch (keycode) {

		case Keys.R:
			gameOver.restart();
			gameOver.getSc().setState(StateEnum.PlayState);
			break;

		default:
			break;
		}

		return true;
	}

}