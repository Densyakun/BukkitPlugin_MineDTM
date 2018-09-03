package io.github.densyakun.bukkit.minedtm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Sound;


public class Track implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static Sound DEFAULT_SOUND;

	static {
		if ((DEFAULT_SOUND = Sound.valueOf("BLOCK_NOTE_HARP")) != null)
			DEFAULT_SOUND = Sound.valueOf("NOTE_PIANO");
	}

	List<Note> notes = new ArrayList<Note>();
	Sound sound;
	float volume = 1;
	float pitch = 0;

	public Track() {
		this.sound = DEFAULT_SOUND;
	}

	public Track(Sound sound) {
		this.sound = sound;
	}

	public Track(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public Sound getSound() {
		return sound;
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

	public void clearChord(int time) {
		for (int a = 0; a < notes.size(); a++)
			if (time == notes.get(a).tick)
				notes.remove(a);
	}

	public List<Note> getChord(int time) {
		List<Note> a = new ArrayList<Note>();
		Iterator<Note> i = notes.iterator();
		while (i.hasNext()) {
			Note n = i.next();
			if (time == n.getTick())
				a.add(n);
		}

		return a;
	}
}
