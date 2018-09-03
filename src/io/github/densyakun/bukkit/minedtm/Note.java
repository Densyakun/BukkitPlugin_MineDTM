package io.github.densyakun.bukkit.minedtm;

import java.io.Serializable;

public class Note implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final int MEASURE = 1920;

	int tick;
	float volume = 1;
	float pitch = 0;

	public Note(int tick) {
		this.tick = tick;
	}

	public Note(int tick, float volume, float pitch) {
		this.tick = tick;
		this.volume = 0 > volume ? 0 : volume;
		this.pitch = pitch;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public int getTick() {
		return tick;
	}

	public void setVolume(float volume) {
		this.volume = 0 > volume ? 0 : volume;
	}

	public float getVolume() {
		return volume;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getPitch() {
		return pitch;
	}
}
