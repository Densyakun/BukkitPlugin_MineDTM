package io.github.densyakun.bukkit.minedtm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class SongDatabase implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	List<Song> songs = new ArrayList<Song>();

	public void setSong(Song song) {
		for (int a = 0; a < songs.size(); a++) {
			Song s = songs.get(a);
			if (s.name.equalsIgnoreCase(song.name)) {
				songs.set(a, song);

				return;
			}
		}

		songs.add(song);
	}

	public Song getSong(String name) {
		Iterator<Song> i = songs.iterator();
		while (i.hasNext()) {
			Song s = i.next();
			if (s.name.equalsIgnoreCase(name))
				return s;
		}

		return null;
	}

	public List<Song> getSongs() {
		return songs;
	}

	public List<Song> getSongs(UUID creator) {
		List<Song> songsc = new ArrayList<Song>();
		Iterator<Song> i = songs.iterator();
		while (i.hasNext()) {
			Song s = i.next();
			if (s.creator.equals(creator))
				songsc.add(s);
		}

		return songsc;
	}

	public void removeSong(String name) {
		for (int a = 0; a < songs.size(); a++) {
			if (songs.get(a).name.equalsIgnoreCase(name)) {
				songs.remove(a);

				return;
			}
		}
	}

	public String getNewSongName(String name) {
		for (int a = 0; getSong(name) != null; a++)
			name += a;

		return name;
	}
}
