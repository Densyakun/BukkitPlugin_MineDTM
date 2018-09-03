package io.github.densyakun.bukkit.minedtm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Song implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final float max_bpm = 480;
	public static final int max_tracks = 8;

	public static int default_bpm = 120;

	String name;
	UUID creator;
	float bpm;
	List<Track> tracks;
	long update_date = -1;
	boolean copyable = true;

	public Song(String name, UUID creator, float bpm) {
		this.name = name.trim();
		this.creator = creator;
		this.bpm = bpm;
		tracks = new ArrayList<Track>();
		for (int a = 0; a < max_tracks; a++)
			tracks.add(new Track());
		update_date = new Date().getTime();
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public String getName() {
		return name;
	}

	public UUID getCreator() {
		return creator;
	}

	public void setTempo(float bpm) {
		this.bpm = 0 > bpm ? 0 : max_bpm < bpm ? max_bpm : bpm;
	}

	public float getTempo() {
		return bpm;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	public void clearTrack(int tracknum) {
		if (0 <= tracknum && tracknum < tracks.size())
			tracks.set(tracknum, new Track());
	}

	public List<Track> getTracks() {
		return tracks;
	}

	void setUpdate_date(long update_date) {
		this.update_date = update_date;
	}

	public long getUpdate_date() {
		return update_date;
	}

	public void setCopyable(boolean copyable) {
		this.copyable = copyable;
	}

	public boolean isCopyable() {
		return copyable;
	}

	public static Song copy(UUID creator, Song song) {
		if (song.isCopyable()) {
			String songname = song.name + "_copy";
			Player player = Bukkit.getPlayer(creator);
			if (player == null) {
				OfflinePlayer op = Bukkit.getOfflinePlayer(creator);
				if (op != null)
					songname += "_by_" + op.getName();
			} else
				songname += "_by_" + ChatColor.stripColor(player.getDisplayName());
			Song s = new Song(songname, creator, song.bpm);
			s.tracks = song.tracks;

			return s;
		}

		return null;
	}
}
