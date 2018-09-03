package io.github.densyakun.bukkit.minedtm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static final String param_is_not_enough = "パラメータが足りません";
	public static final String param_wrong_cmd = "パラメータが間違っています";

	File songs_file = new File(getDataFolder(), "songs.dat");
	String msg_prefix = ChatColor.GOLD + "[" + getName() + "]";
	int list_lines = 5;
	SongDatabase sd;
	Map<UUID, Song> editing_song = new HashMap<UUID, Song>();
	Map<UUID, Integer> editing_track = new HashMap<UUID, Integer>();

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(songs_file));
			sd = (SongDatabase) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			sd = new SongDatabase();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("dtm")) {
			if (sender instanceof Player) {
				if (args.length == 0) {
					sender.sendMessage(msg_prefix + ChatColor.GOLD + "パラメータを入力して下さい");
					sender.sendMessage(ChatColor.GREEN + "/dtm song");
				} else if (args[0].equalsIgnoreCase("song")) {
					if (args.length == 1) {
						sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
						sender.sendMessage(ChatColor.GREEN
								+ "/dtm song (new|edit|e|copy|delete|del|info|i|list|l|name|tempo|copyable|track|t|note|n|play|p)");
					} else if (args[1].equalsIgnoreCase("new")) {
						if (args.length == 2) {
							sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/dtm song new (name)");
						} else {
							Song song = new Song(sd.getNewSongName(args[2]), ((Player) sender).getUniqueId(),
									Song.default_bpm);
							sd.setSong(song);
							sender.sendMessage(msg_prefix + ChatColor.AQUA + "曲「" + ChatColor.GOLD + song.name
									+ ChatColor.AQUA + "」を作成しました");
							edit((Player) sender, song);
						}
					} else if (args[1].equalsIgnoreCase("edit") || args[1].equalsIgnoreCase("e")) {
						if (args.length == 2) {
							sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/dtm song open (song)");
						} else {
							Song song = sd.getSong(args[2]);
							if (song == null)
								sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲が見つかりません");
							else if (((Player) sender).getUniqueId().equals(song.creator))
								edit((Player) sender, song);
							else
								sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
						}
					} else if (args[1].equalsIgnoreCase("copy")) {
						Song song = editing_song.get(((Player) sender).getUniqueId());
						if (song == null) {
							sender.sendMessage(msg_prefix + ChatColor.RED + "曲を開いていません");
							sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit を実行して下さい");
						} else if (song.isCopyable()) {
							Song s = Song.copy(((Player) sender).getUniqueId(), song);
							s.name = sd.getNewSongName(s.name);
							sd.setSong(s);
							sender.sendMessage(msg_prefix + ChatColor.AQUA + "曲をコピーしました");
							sender.sendMessage(
									msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit " + s.name + " を実行して下さい");
						} else
							sender.sendMessage(msg_prefix + ChatColor.RED + "この曲はコピーできません");
					} else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("del")) {
						if (args.length == 2) {
							sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/dtm song delete (song)");
						} else {
							Song song = sd.getSong(args[2]);
							if (song == null)
								sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲が見つかりません");
							else if (((Player) sender).getUniqueId().equals(song.creator)) {
								edit((Player) sender, null);
								sd.removeSong(song.name);
								sender.sendMessage(msg_prefix + ChatColor.AQUA + "曲を削除しました: " + song.name);
							} else
								sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
						}
					} else if (args[1].equalsIgnoreCase("info") || args[1].equalsIgnoreCase("i")) {
						if (args.length == 2) {
							sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/dtm song info (song)");
						} else {
							Song song = sd.getSong(args[2]);
							if (song == null)
								sender.sendMessage(msg_prefix + ChatColor.RED + "曲が見つかりません");
							else {
								String creator = "---";
								Player p = getServer().getPlayer(song.creator);
								if (p != null)
									creator = ChatColor.stripColor(p.getDisplayName());
								OfflinePlayer op = getServer().getOfflinePlayer(song.creator);
								if (op != null)
									creator = op.getName();
								sender.sendMessage(msg_prefix + ChatColor.GREEN + "曲名: " + ChatColor.GOLD + song.name
										+ ChatColor.GREEN + " 作曲: " + ChatColor.GOLD + creator + ChatColor.GREEN
										+ " 更新日時: " + ChatColor.GOLD + new Date(song.update_date) + ChatColor.GREEN
										+ " テンポ: " + ChatColor.GOLD + song.bpm + "BPM" + ChatColor.GREEN + " 複製: "
										+ ChatColor.GOLD + (song.copyable ? "可能" : "不可") + ChatColor.GREEN);
							}
						}
					} else if (args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("l")) {
						int page = 0;
						if (args.length != 2) {
							try {
								page = Integer.valueOf(args[2]) - 1;
							} catch (NumberFormatException e) {
							}
						}
						sender.sendMessage(msg_prefix + ChatColor.GREEN + "楽曲一覧 ページ: " + (page + 1) + "/"
								+ ((int) Math.ceil(sd.songs.size() / list_lines) + 1));
						for (int a = page * list_lines; a < sd.songs.size() && a < (page + 1) * list_lines; a++) {
							Song song = sd.songs.get(a);
							String creator = "---";
							Player p = getServer().getPlayer(song.creator);
							if (p != null)
								creator = ChatColor.stripColor(p.getDisplayName());
							OfflinePlayer op = getServer().getOfflinePlayer(song.creator);
							if (op != null)
								creator = op.getName();
							sender.sendMessage(msg_prefix + ChatColor.GREEN + (a + 1) + ": 曲名: " + ChatColor.GOLD
									+ song.name + ChatColor.GREEN + " 作曲: " + ChatColor.GOLD + creator);
						}
					} else if (args[1].equalsIgnoreCase("name")) {
						Song song = editing_song.get(((Player) sender).getUniqueId());
						if (song == null) {
							sender.sendMessage(msg_prefix + ChatColor.RED + "曲を開いていません");
							sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit を実行して下さい");
						} else if (((Player) sender).getUniqueId().equals(song.creator)) {
							if (args.length == 2)
								sender.sendMessage(msg_prefix + ChatColor.GREEN + "曲名: " + song.name);
							else {
								song.setName(args[2]);
								sender.sendMessage(msg_prefix + ChatColor.AQUA + "曲名を変更しました: " + song.name);
							}
						} else
							sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
					} else if (args[1].equalsIgnoreCase("tempo")) {
						Song song = editing_song.get(((Player) sender).getUniqueId());
						if (song == null) {
							sender.sendMessage(msg_prefix + ChatColor.RED + "曲を開いていません");
							sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit を実行して下さい");
						} else if (((Player) sender).getUniqueId().equals(song.creator)) {
							if (args.length == 2)
								sender.sendMessage(msg_prefix + ChatColor.GREEN + "テンポ: " + song.bpm + "BPM");
							else {
								try {
									song.setTempo(Float.valueOf(args[2]));
									sender.sendMessage(msg_prefix + ChatColor.AQUA + "テンポを変更しました: " + song.bpm + "BPM");
								} catch (NumberFormatException e) {
									sender.sendMessage(msg_prefix + ChatColor.RED + "正しい数字を入力して下さい");
								}
							}
						} else
							sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
					} else if (args[1].equalsIgnoreCase("copyable")) {
						Song song = editing_song.get(((Player) sender).getUniqueId());
						if (song == null) {
							sender.sendMessage(msg_prefix + ChatColor.RED + "曲を開いていません");
							sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit を実行して下さい");
						} else if (((Player) sender).getUniqueId().equals(song.creator)) {
							if (args.length == 2)
								sender.sendMessage(
										msg_prefix + ChatColor.GREEN + "複製: " + (song.copyable ? "可能" : "不可"));
							else {
								try {
									song.setCopyable(Boolean.valueOf(args[2]));
									sender.sendMessage(msg_prefix + ChatColor.AQUA + "設定を変更しました。複製: "
											+ (song.copyable ? "可能" : "不可"));
								} catch (Exception e) {
									sender.sendMessage(
											msg_prefix + ChatColor.RED + "複製が可能な場合はtrueを、不可の場合はfalseを入力して下さい");
								}
							}
						} else
							sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
					} else if (args[1].equalsIgnoreCase("track") || args[1].equalsIgnoreCase("t")) {
						Song song = editing_song.get(((Player) sender).getUniqueId());
						if (song == null) {
							sender.sendMessage(msg_prefix + ChatColor.RED + "曲を開いていません");
							sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit を実行して下さい");
						} else if (((Player) sender).getUniqueId().equals(song.creator)) {
							if (args.length == 2) {
								sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
								sender.sendMessage(ChatColor.GREEN + "/dtm song track (edit|clear|sound|volume|pitch)");
							} else if (args[2].equalsIgnoreCase("edit")) {
								if (args.length == 3) {
									sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
									sender.sendMessage(ChatColor.GREEN + "/dtm song track edit (1-8)");
								} else {
									try {
										int tracknum = Integer.valueOf(args[3]) - 1;
										if (0 <= tracknum && tracknum < song.tracks.size()) {
											editing_track.put(((Player) sender).getUniqueId(), tracknum);
											sender.sendMessage(
													msg_prefix + ChatColor.AQUA + "トラック" + (tracknum + 1) + "を編集中です");
										} else
											sender.sendMessage(
													msg_prefix + ChatColor.RED + "正しくトラック番号を入力して下さい(1-8の数字)");
									} catch (NumberFormatException e) {
										sender.sendMessage(msg_prefix + ChatColor.RED + "トラック番号を入力して下さい(1-8)");
									}
								}
							} else if (args[2].equalsIgnoreCase("clear")) {
								Integer tracknum = editing_track.get(((Player) sender).getUniqueId());
								if (tracknum == null) {
									sender.sendMessage(msg_prefix + ChatColor.RED + "トラックを選択して下さい");
									sender.sendMessage(
											msg_prefix + ChatColor.GOLD + "トラックを選択するには、/dtm song track edit を実行して下さい");
								} else {
									song.clearTrack(tracknum);
									sender.sendMessage(
											msg_prefix + ChatColor.AQUA + "トラック" + (tracknum + 1) + "を初期化しました");
								}
							} else if (args[2].equalsIgnoreCase("sound")) {
								Integer tracknum = editing_track.get(((Player) sender).getUniqueId());
								if (tracknum == null) {
									sender.sendMessage(msg_prefix + ChatColor.RED + "トラックを選択して下さい");
									sender.sendMessage(
											msg_prefix + ChatColor.GOLD + "トラックを選択するには、/dtm song track edit を実行して下さい");
								} else {
									Track track = song.tracks.get(tracknum);
									if (track == null)
										sender.sendMessage(msg_prefix + ChatColor.RED + "トラックが不明です（曲が破損しています）");
									else {
										if (args.length == 3)
											sender.sendMessage(msg_prefix + ChatColor.GREEN + "楽器: " + track.sound);
										else {
											try {
												Sound sound = Sound.valueOf(args[3]);
												track.setSound(sound);
												sender.sendMessage(
														msg_prefix + ChatColor.AQUA + "楽器を変更しました: " + track.sound);
											} catch (IllegalArgumentException e) {
												sender.sendMessage(msg_prefix + ChatColor.RED + "正しく楽器を指定して下さい");
												String a = "[";
												Sound[] b = Sound.values();
												for (int c = 0; c < b.length; c++) {
													if (c != 0) {
														a += ", ";
													}
													a += b[c];
												}
												a += "]";
												sender.sendMessage(msg_prefix + ChatColor.GOLD + "使用可能な楽器: " + a);
											}
										}
									}
								}
							} else if (args[2].equalsIgnoreCase("volume")) {
								Integer tracknum = editing_track.get(((Player) sender).getUniqueId());
								if (tracknum == null) {
									sender.sendMessage(msg_prefix + ChatColor.RED + "トラックを選択して下さい");
									sender.sendMessage(
											msg_prefix + ChatColor.GOLD + "トラックを選択するには、/dtm song track edit を実行して下さい");
								} else {
									Track track = song.tracks.get(tracknum);
									if (track == null)
										sender.sendMessage(msg_prefix + ChatColor.RED + "トラックが不明です（曲が破損しています）");
									else {
										if (args.length == 3)
											sender.sendMessage(
													msg_prefix + ChatColor.GREEN + "音量: " + (int) (track.volume * 100));
										else {
											try {
												track.setVolume(Float.valueOf(args[3]) / 100);
												sender.sendMessage(msg_prefix + ChatColor.AQUA + "音量を変更しました: "
														+ (int) (track.volume * 100));
											} catch (NumberFormatException e) {
												sender.sendMessage(msg_prefix + ChatColor.RED + "正しく音量を指定して下さい(0-100)");
											}
										}
									}
								}
							} else if (args[2].equalsIgnoreCase("pitch")) {
								Integer tracknum = editing_track.get(((Player) sender).getUniqueId());
								if (tracknum == null) {
									sender.sendMessage(msg_prefix + ChatColor.RED + "トラックを選択して下さい");
									sender.sendMessage(
											msg_prefix + ChatColor.GOLD + "トラックを選択するには、/dtm song track edit を実行して下さい");
								} else {
									Track track = song.tracks.get(tracknum);
									if (track == null)
										sender.sendMessage(msg_prefix + ChatColor.RED + "トラックが不明です（曲が破損しています）");
									else {
										if (args.length == 3)
											sender.sendMessage(
													msg_prefix + ChatColor.GREEN + "Pitch: " + track.pitch * 12);
										else {
											try {
												float a = Float.parseFloat(args[3]);
												track.setPitch(a);
												sender.sendMessage(msg_prefix + ChatColor.AQUA + "ピッチを変更しました: "
														+ track.pitch * 12);
											} catch (NumberFormatException e) {
												sender.sendMessage(msg_prefix + ChatColor.RED + "正しくピッチ(数値)を指定して下さい");
											}
										}
									}
								}
							} else {
								sender.sendMessage(msg_prefix + ChatColor.GOLD + param_wrong_cmd);
								sender.sendMessage(ChatColor.GREEN + "/dtm song track (edit|clear|sound|volume|pitch)");
							}
						} else
							sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
					} else if (args[1].equalsIgnoreCase("note") || args[1].equalsIgnoreCase("n")) {
						Song song = editing_song.get(((Player) sender).getUniqueId());
						if (song == null) {
							sender.sendMessage(msg_prefix + ChatColor.RED + "曲を開いていません");
							sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲を開くには、/dtm song edit を実行して下さい");
						} else if (((Player) sender).getUniqueId().equals(song.creator)) {
							Integer tracknum = editing_track.get(((Player) sender).getUniqueId());
							if (tracknum == null) {
								sender.sendMessage(msg_prefix + ChatColor.RED + "トラックを選択して下さい");
								sender.sendMessage(
										msg_prefix + ChatColor.GOLD + "トラックを選択するには、/dtm song track edit を実行して下さい");
							} else {
								Track track = song.tracks.get(tracknum);
								if (track == null)
									sender.sendMessage(msg_prefix + ChatColor.RED + "トラックが不明です（曲が破損しています）");
								else {
									if (args.length == 2) {
										sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
										sender.sendMessage(ChatColor.GREEN + "/dtm song note (chord|c)");
									} else if (args[2].equalsIgnoreCase("chord") || args[2].equalsIgnoreCase("c")) {
										if (args.length == 3) {
											sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
											sender.sendMessage(
													ChatColor.GREEN + "/dtm song note chord|c (add|get|clear)");
										} else if (args[3].equalsIgnoreCase("add")) {
											if (args.length <= 5) {
												sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
												sender.sendMessage(ChatColor.GREEN
														+ "/dtm song note chord|c add (time) (pitch0) [pitch1...]");
											} else {
												try {
													Integer a = Integer.valueOf(args[4]);
													if (0 > a)
														sender.sendMessage(ChatColor.RED + "時間(整数の正数)を入力して下さい。一小節が"
																+ Note.MEASURE + "です");
													else {
														for (int b = 5; b < args.length; b++) {
															float c = Float.valueOf(args[b]);
															track.notes.add(new Note(a, 1, c));
														}
														sender.sendMessage(ChatColor.AQUA + "ノートを追加しました");
													}
												} catch (NumberFormatException e) {
													sender.sendMessage(ChatColor.RED + "時間(整数の正数)を入力して下さい。一小節が"
															+ Note.MEASURE + "です");
												}
											}
										} else if (args[3].equalsIgnoreCase("get")) {
											if (args.length == 4) {
												sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
												sender.sendMessage(
														ChatColor.GREEN + "/dtm song note chord|c get (time)");
											} else {
												try {
													Integer a = Integer.valueOf(args[4]);
													if (0 > a)
														sender.sendMessage(ChatColor.RED + "時間(整数の正数)を入力して下さい。一小節が"
																+ Note.MEASURE + "です");
													else {
														List<Note> b = track.getChord(a);
														String c = "[";
														for (int d = 0; d < b.size(); d++) {
															if (d != 0)
																c += ", ";
															c += b.get(d).pitch;
														}
														c += "]";
														sender.sendMessage(ChatColor.AQUA + "ノート: " + c);
													}
												} catch (NumberFormatException e) {
													sender.sendMessage(ChatColor.RED + "時間(整数の正数)を入力して下さい。一小節が"
															+ Note.MEASURE + "です");
												}
											}
										} else if (args[3].equalsIgnoreCase("clear")) {
											if (args.length == 4) {
												sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
												sender.sendMessage(
														ChatColor.GREEN + "/dtm song note chord|c clear (time)");
											} else {
												try {
													Integer a = Integer.valueOf(args[4]);
													if (0 > a)
														sender.sendMessage(ChatColor.RED + "時間(整数の正数)を入力して下さい。一小節が"
																+ Note.MEASURE + "です");
													else {
														track.clearChord(a);
														sender.sendMessage(ChatColor.AQUA + "ノートを削除しました");
													}
												} catch (NumberFormatException e) {
													sender.sendMessage(ChatColor.RED + "時間(整数の正数)を入力して下さい。一小節が"
															+ Note.MEASURE + "です");
												}
											}
										} else {
											sender.sendMessage(msg_prefix + ChatColor.GOLD + param_wrong_cmd);
											sender.sendMessage(
													ChatColor.GREEN + "/dtm song note chord|c (add|get|clear)");
										}
									} else {
										sender.sendMessage(msg_prefix + ChatColor.GOLD + param_wrong_cmd);
										sender.sendMessage(ChatColor.GREEN + "/dtm song note (chord|c)");
									}
								}
							}
						} else
							sender.sendMessage(msg_prefix + ChatColor.RED + "この曲の作者ではありません");
					} else if (args[1].equalsIgnoreCase("play") || args[1].equalsIgnoreCase("p")) {
						if (args.length == 2) {
							sender.sendMessage(msg_prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/dtm song play (song)");
						} else {
							Song song = sd.getSong(args[2]);
							if (song == null)
								sender.sendMessage(msg_prefix + ChatColor.GOLD + "曲が見つかりません");
							else {
								MusicPlayer mplayer = new MusicPlayer(song);
								mplayer.addListener((Player) sender);
								mplayer.play();
								sender.sendMessage(ChatColor.GOLD + song.name + ChatColor.AQUA + "を再生中");
							}
						}
					} else {
						sender.sendMessage(msg_prefix + ChatColor.GOLD + param_wrong_cmd);
						sender.sendMessage(ChatColor.GREEN
								+ "/dtm song (new|edit|e|copy|delete|del|info|i|list|l|name|tempo|copyable|track|t|note|n|play|p)");
					}
				} else {
					sender.sendMessage(msg_prefix + ChatColor.GOLD + param_wrong_cmd);
					sender.sendMessage(ChatColor.GREEN + "/dtm song");
				}
			}
		}
		return true;
	}

	@Override
	public void onDisable() {
		try {
			getDataFolder().mkdirs();
			songs_file.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(songs_file));
			oos.writeObject(sd);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setSongDatabase(SongDatabase sd) {
		this.sd = sd;
	}

	public SongDatabase getSongDatabase() {
		return sd;
	}

	public void edit(Player editor, Song song) {
		if (song == null) {
			editing_song.remove(editor.getUniqueId());
			editing_track.remove(editor.getUniqueId());
		} else {
			editing_song.put(editor.getUniqueId(), song);
			editing_track.remove(editor.getUniqueId());
			editor.sendMessage(
					msg_prefix + ChatColor.AQUA + "曲「" + ChatColor.GOLD + song.name + ChatColor.AQUA + "」を編集中です");
		}
	}
}
