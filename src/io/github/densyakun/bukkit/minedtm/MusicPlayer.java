package io.github.densyakun.bukkit.minedtm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MusicPlayer implements Runnable {
	Song song;
	Location location;
	List<Player> listeners = new ArrayList<Player>();
	float speed = 1;
	boolean playing = false;
	boolean loop = false;
	int oldtime = -1;
	int time = 0;
	float volume = 1;
	float pitch = 0;
	private int length = 0;

	public MusicPlayer(Song song) {
		this.song = song;
		Iterator<Track> i = song.tracks.iterator();
		while (i.hasNext()) {
			Iterator<Note> j = i.next().notes.iterator();
			while (j.hasNext()) {
				Note note = j.next();
				if (length < note.tick)
					length = note.tick;
			}
		}
	}

	public Song getSong() {
		return song;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public void addListener(Player listener) {
		listeners.add(listener);
	}

	public void setListeners(List<Player> listeners) {
		this.listeners = listeners;
	}

	public List<Player> getListeners() {
		return listeners;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getSpeed() {
		return speed;
	}

	public void play() {
		if (!playing) {
			playing = true;
			new Thread(this).start();
		}
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public boolean isLoop() {
		return loop;
	}

	public void pause() {
		playing = false;
	}

	public void stop() {
		playing = false;
		oldtime = -1;
		time = 0;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setTime(int time) {
		if (loop) {
			if (length <= this.time) {
				oldtime = -1;
				this.time = time - length;

				return;
			} else if (0 > this.time) {
				oldtime = length;
				this.time = length - time;

				return;
			}
		} else if (0 > this.time) {
			oldtime = length;
			this.time = length - this.time - time;

			stop();

			return;
		} else if (length <= this.time) {
			oldtime = -1;
			this.time = time - time;

			stop();

			return;
		}

		oldtime = this.time;
		this.time = time;
	}

	public int getTime() {
		return time;
	}

	public void setVolume(float volume) {
		this.volume = volume;
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

	public int getLength() {
		return length;
	}

	@Override
	public void run() {
		while (playing) {
			Iterator<Track> tracks = song.tracks.iterator();
			while (tracks.hasNext()) {

				Track track = tracks.next();
				Sound sound = track.sound;
				if (sound == null)
					sound = Track.DEFAULT_SOUND;
				Iterator<Note> notes = track.notes.iterator();
				while (notes.hasNext()) {

					Note note = notes.next();
					boolean a = false;
					if (0 > speed) {
						if (oldtime > note.tick && note.tick >= time)
							a = true;
					} else if (oldtime < note.tick && note.tick <= time)
						a = true;

					if (a) {
						float notepitch = 0.5f + (pitch + track.pitch + note.pitch / 16f);
						if (0 < listeners.size()) {
							for (int b = 0; b < listeners.size(); b++) {
								Player listener = listeners.get(b);
								listener.playSound(location == null ? listener.getLocation() : location, sound,
										volume * track.volume * note.volume, notepitch);
							}
						} else if (location != null)
							location.getWorld().playSound(location, sound, volume * track.volume * note.volume,
									notepitch);
					}
				}
			}

			setTime((int) Math.ceil(time + (song.bpm / 240) * Note.MEASURE * speed / 20));

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
