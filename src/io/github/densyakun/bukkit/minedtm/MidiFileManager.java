package io.github.densyakun.bukkit.minedtm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import org.bukkit.entity.Player;

public class MidiFileManager {

	public static Song importSong(Main main, String name, Player player) throws InvalidMidiDataException, IOException {
		Song song = null;
		FileInputStream in = new FileInputStream(new File(main.getDataFolder(), name));
		Sequence sequence = MidiSystem.getSequence(in);
		in.close();
		song = new Song(main.sd.getNewSongName(name), player.getUniqueId(), Song.default_bpm);
		List<Track> tracks = new ArrayList<Track>();
		for (javax.sound.midi.Track t : sequence.getTracks()) {
			Track track = new Track();
			List<Note> notes = new ArrayList<Note>();
			for (int a = 0; a < t.size(); a++) {
				MidiEvent e = t.get(a);
				MidiMessage msg = e.getMessage();
				if (msg instanceof ShortMessage && ((ShortMessage) msg).getCommand() == ShortMessage.NOTE_ON)
					notes.add(new Note((int) e.getTick(), (float) ((ShortMessage) msg).getData2() / 127,
							((ShortMessage) msg).getData1()));
			}
			track.setNotes(notes);
			tracks.add(track);
		}
		song.setTracks(tracks);
		return song;
	}
}
