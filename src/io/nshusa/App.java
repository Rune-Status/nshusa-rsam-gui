package io.nshusa;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nshusa.controller.StoreController;
import io.nshusa.meta.ArchiveMeta;
import io.nshusa.meta.StoreMeta;
import io.nshusa.rsam.util.HashUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class App extends Application {

	private static Stage stage;

	private static Scene scene;

	@Override
	public void init() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				if (!Files.exists(AppData.RESOURCE_PATH)) {
					AppData.RESOURCE_PATH.toFile().mkdirs();
				}

				if (!Files.exists(AppData.RESOURCE_PATH.resolve("stores.json"))) {

					List<StoreMeta> meta = Arrays.asList(new StoreMeta(0, "archive"),
							new StoreMeta(1, "model"),
							new StoreMeta(2, "animation"),
							new StoreMeta(3, "music"),
							new StoreMeta(4, "map"));

					Gson gson = new GsonBuilder().setPrettyPrinting().create();

					try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(AppData.RESOURCE_PATH.toFile(), "stores.json")))) {
						writer.write(gson.toJson(meta));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.RESOURCE_PATH.toFile(), "stores.json")))) {
					Gson gson = new Gson();

					List<StoreMeta> meta = Arrays.asList(gson.fromJson(reader, StoreMeta[].class));

					meta.stream().forEach(it -> AppData.storeNames.put(it.getId(), it.getName()));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {

				if (!Files.exists(AppData.RESOURCE_PATH)) {
					AppData.RESOURCE_PATH.toFile().mkdirs();
				}

				if (!Files.exists(AppData.RESOURCE_PATH.resolve("archives.json"))) {

					List<ArchiveMeta> meta = Arrays.asList(
							new ArchiveMeta(0, "empty", "empty", false),
							new ArchiveMeta(1, "title screen", "title.jag", true),
							new ArchiveMeta(2, "config", "config.jag", false),
							new ArchiveMeta(3,  "interface", "interface.jag", false),
							new ArchiveMeta(4, "2d graphics", "media.jag", true),
							new ArchiveMeta(5, "update list","versionlist.jag", false),
							new ArchiveMeta(6, "texture", "texture.jag", true),
							new ArchiveMeta(7, "chat system","wordenc.jag", false),
							new ArchiveMeta(8, "sound effects","sound.jag", false));

					try(BufferedWriter writer = new BufferedWriter(new FileWriter(AppData.RESOURCE_PATH.resolve("archives.json").toFile()))) {
						Gson gson = new GsonBuilder().setPrettyPrinting().create();

						writer.write(gson.toJson(meta));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.RESOURCE_PATH.toFile(), "archives.json")))) {
					Gson gson = new Gson();

					List<ArchiveMeta> meta = Arrays.asList(gson.fromJson(reader, ArchiveMeta[].class));

					meta.stream().forEach(it -> AppData.archiveMetas.put(it.getId(), it));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {

				if (!Files.exists(AppData.RESOURCE_PATH)) {
					AppData.RESOURCE_PATH.toFile().mkdirs();
				}

				if (!Files.exists(AppData.RESOURCE_PATH.resolve("hash_names.txt"))) {
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.RESOURCE_PATH.resolve("hash_names.txt").toFile()))) {
						writer.println("blackmark.dat");
						writer.println("param.dat");
						writer.println("param.idx");
						writer.println("mes.dat");
						writer.println("mes.idx");
						writer.println("mesanim.dat");
						writer.println("mesanim.idx");
						writer.println("backbase1.dat");
						writer.println("backbase2.dat");
						writer.println("backhmid1.dat");
						writer.println("backhmid2.dat");
						writer.println("backleft1.dat");
						writer.println("backleft2.dat");
						writer.println("backright1.dat");
						writer.println("backright2.dat");
						writer.println("backtop1.dat");
						writer.println("backvmid1.dat");
						writer.println("backvmid2.dat");
						writer.println("backvmid3.dat");
						writer.println("chatback.dat");
						writer.println("invback.dat");
						writer.println("redstone1.dat");
						writer.println("redstone2.dat");
						writer.println("redstone3.dat");
						writer.println("2.dat");
						writer.println("combaticons2.dat");
						writer.println("obj.idx");
						writer.println("button_brown_big.dat");
						writer.println("keys.dat");
						writer.println("index.dat");
						writer.println("sideicons2.dat");
						writer.println("number_button.dat");
						writer.println("6.dat");
						writer.println("0.dat");
						writer.println("button_red.dat");
						writer.println("map_crc");
						writer.println("optionicons3.dat");
						writer.println("hiticons.dat");
						writer.println("optionicons.dat");
						writer.println("steelborder2.dat");
						writer.println("data");
						writer.println("leftarrow_small.dat");
						writer.println("4.dat");
						writer.println("npc.dat");
						writer.println("cross.dat");
						writer.println("steelborder.dat");
						writer.println("48.dat");
						writer.println("rightarrow_small.dat");
						writer.println("model_index");
						writer.println("b12_full.dat");
						writer.println("tradebacking.dat");
						writer.println("startgame.dat");
						writer.println("50.dat");
						writer.println("mod_icons.dat");
						writer.println("magicoff.dat");
						writer.println("emotesoff.dat");
						writer.println("miscgraphics.dat");
						writer.println("44.dat");
						writer.println("26.dat");
						writer.println("tex_red.dat");
						writer.println("magicon.dat");
						writer.println("headicons_pk.dat");
						writer.println("combaticons.dat");
						writer.println("17.dat");
						writer.println("22.dat");
						writer.println("16.dat");
						writer.println("varbit.idx");
						writer.println("21.dat");
						writer.println("32.dat");
						writer.println("15.dat");
						writer.println("33.dat");
						writer.println("anim_index");
						writer.println("14.dat");
						writer.println("anim_crc");
						writer.println("40.dat");
						writer.println("34.dat");
						writer.println("29.dat");
						writer.println("hitmarks.dat");
						writer.println("model_version");
						writer.println("27.dat");
						writer.println("45.dat");
						writer.println("20.dat");
						writer.println("prayeroff.dat");
						writer.println("1451391714");
						writer.println("rightarrow.dat");
						writer.println("flo.idx");
						writer.println("tldlist.txt");
						writer.println("fragmentsenc.txt");
						writer.println("mapback.dat");
						writer.println("chest.dat");
						writer.println("fragmentscenc.txt");
						writer.println("46.dat");
						writer.println("mapscene.dat");
						writer.println("spotanim.dat");
						writer.println("loc.dat");
						writer.println("seq.dat");
						writer.println("combatboxes.dat");
						writer.println("overlay_duel.dat");
						writer.println("emoteicons.dat");
						writer.println("lunaron.dat");
						writer.println("28.dat");
						writer.println("headicons_prayer.dat");
						writer.println("varp.idx");
						writer.println("runes.dat");
						writer.println("prayeron.dat");
						writer.println("combaticons3.dat");
						writer.println("headicons_hint.dat");
						writer.println("npc.idx");
						writer.println("1.dat");
						writer.println("5.dat");
						writer.println("overlay_multiway.dat");
						writer.println("idk.idx");
						writer.println("7.dat");
						writer.println("titlescroll.dat");
						writer.println("9.dat");
						writer.println("lunaroff.dat");
						writer.println("titlebox.dat");
						writer.println("staticons.dat");
						writer.println("midi_index");
						writer.println("anim_version");
						writer.println("midi_version");
						writer.println("47.dat");
						writer.println("map_version");
						writer.println("staticons2.dat");
						writer.println("mapfunction.dat");
						writer.println("39.dat");
						writer.println("title.dat");
						writer.println("screenframe.dat");
						writer.println("obj.dat");
						writer.println("magicoff2.dat");
						writer.println("35.dat");
						writer.println("mapmarker.dat");
						writer.println("optionicons2.dat");
						writer.println("letter.dat");
						writer.println("domainenc.txt");
						writer.println("3.dat");
						writer.println("10.dat");
						writer.println("compass.dat");
						writer.println("sounds.dat");
						writer.println("map_index");
						writer.println("18.dat");
						writer.println("23.dat");
						writer.println("24.dat");
						writer.println("tex_brown.dat");
						writer.println("25.dat");
						writer.println("sworddecor.dat");
						writer.println("19.dat");
						writer.println("30.dat");
						writer.println("titlebutton.dat");
						writer.println("mapdots.dat");
						writer.println("13.dat");
						writer.println("31.dat");
						writer.println("spotanim.idx");
						writer.println("11.dat");
						writer.println("41.dat");
						writer.println("36.dat");
						writer.println("p12_full.dat");
						writer.println("prayerglow.dat");
						writer.println("38.dat");
						writer.println("43.dat");
						writer.println("q8_full.dat");
						writer.println("miscgraphics3.dat");
						writer.println("flo.dat");
						writer.println("coins.dat");
						writer.println("leftarrow.dat");
						writer.println("p11_full.dat");
						writer.println("logo.dat");
						writer.println("varbit.dat");
						writer.println("scrollbar.dat");
						writer.println("49.dat");
						writer.println("varp.dat");
						writer.println("attack.dat");
						writer.println("clanchat.dat");
						writer.println("sideicons.dat");
						writer.println("midi_crc");
						writer.println("wornicons.dat");
						writer.println("key.dat");
						writer.println("loc.idx");
						writer.println("seq.idx");
						writer.println("8.dat");
						writer.println("42.dat");
						writer.println("magicon2.dat");
						writer.println("37.dat");
						writer.println("idk.dat");
						writer.println("12.dat");
						writer.println("miscgraphics2.dat");
						writer.println("model_crc");
						writer.println("badenc.txt");
						writer.println("button_brown.dat");
						writer.println("mapedge.dat");
						writer.println("pen.dat");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					List<String> lines = Files.readAllLines(AppData.RESOURCE_PATH.resolve("hash_names.txt"));

					lines.stream().forEach(it -> AppData.commonHashNames.put(HashUtils.nameToHash(it), it));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}


		}).start();

		AppData.load();

	}

	@Override
	public void start(Stage stage) {
		App.stage = stage;
		try {
			FXMLLoader loader = new FXMLLoader(App.class.getResource("/StoreUI.fxml"));
			
			Parent root = (Parent)loader.load();
			
			StoreController controller = (StoreController)loader.getController();
			
			controller.setStage(stage);
			
			Scene scene = new Scene(root);
			App.scene = scene;
			scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
			stage.setScene(scene);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app_icon_128.png")));
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setResizable(false);
			stage.centerOnScreen();
			stage.setTitle("RS2 Asset Manager");
			stage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static Stage getStage() {
		return stage;
	}
	
	public static void setStage(Stage stage) {
		App.stage = stage;
	}

	public static Scene getScene() {
		return scene;
	}

}
