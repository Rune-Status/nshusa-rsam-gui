package io.nshusa;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nshusa.controller.StoreController;
import io.nshusa.model.ArchiveMeta;
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

				if (!Files.exists(AppData.resourcePath)) {
					AppData.resourcePath.toFile().mkdirs();
				}

				if (!Files.exists(AppData.storeResourcePath)) {
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.storeResourcePath.toFile()))) {
						writer.println("0:archive");
						writer.println("1:model");
						writer.println("2:animation");
						writer.println("3:music");
						writer.println("4:map");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.resourcePath.toFile(), "stores.txt")))) {

					String line;

					while((line = reader.readLine()) != null) {

						String[] split = line.split(":");

						AppData.storeNames.put(Integer.parseInt(split[0]), split[1]);

					}

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

				if (!Files.exists(AppData.resourcePath)) {
					AppData.resourcePath.toFile().mkdirs();
				}

				if (!Files.exists(AppData.archiveResourcePath)) {

					List<ArchiveMeta> meta = Arrays.asList(
							new ArchiveMeta(0, "empty.jag", false),
							new ArchiveMeta(1, "title.jag", true),
							new ArchiveMeta(2, "config.jag", false),
							new ArchiveMeta(3, "interface.jag", false),
							new ArchiveMeta(4, "media.jag", true),
							new ArchiveMeta(5, "versionlist.jag", false),
							new ArchiveMeta(6, "texture.jag", true),
							new ArchiveMeta(7, "wordenc.jag", false),
							new ArchiveMeta(8, "sound.jag", false));

					try(BufferedWriter writer = new BufferedWriter(new FileWriter(AppData.archiveResourcePath.toFile()))) {
						Gson gson = new GsonBuilder().setPrettyPrinting().create();

						writer.write(gson.toJson(meta));
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.resourcePath.toFile(), "archives.json")))) {
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

				if (!Files.exists(AppData.resourcePath)) {
					AppData.resourcePath.toFile().mkdirs();
				}

				if (!Files.exists(AppData.hashResourcePath)) {
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.hashResourcePath.toFile()))) {
						writer.println("blackmark.dat:-1857300557");
						writer.println("param.dat:-1818025236");
						writer.println("param.idx:-1818006444");
						writer.println("mes.dat:1029250116");
						writer.println("mes.idx:1029268908");
						writer.println("mesanim.dat:182685561");
						writer.println("mesanim.idx:182704353");
						writer.println("backbase1.dat:125902192");
						writer.println("backbase2.dat:139748033");
						writer.println("backhmid1.dat:-1623648789");
						writer.println("backhmid2.dat:-1609802948");
						writer.println("backleft1.dat:1354546316");
						writer.println("backleft2.dat:1368392157");
						writer.println("backright1.dat:-1593819477");
						writer.println("backright2.dat:-1579973636");
						writer.println("backtop1.dat:-1102299012");
						writer.println("backvmid1.dat:1464846521");
						writer.println("backvmid2.dat:1478692362");
						writer.println("backvmid3.dat:1492538203");
						writer.println("chatback.dat:1766681864");
						writer.println("invback.dat:-1568083395");
						writer.println("redstone1.dat:-1392068576");
						writer.println("redstone2.dat:-1378222735");
						writer.println("redstone3.dat:-1364376894");
						writer.println("2.dat:252538893");
						writer.println("combaticons2.dat:-952192193");
						writer.println("obj.idx:-1667598946");
						writer.println("button_brown_big.dat:-90207845");
						writer.println("keys.dat:1986120039");
						writer.println("index.dat:-1929337337");
						writer.println("sideicons2.dat:-654418698");
						writer.println("number_button.dat:1165431679");
						writer.println("6.dat:307922257");
						writer.println("0.dat:224847211");
						writer.println("button_red.dat:-888498683");
						writer.println("map_crc:1915414053");
						writer.println("optionicons3.dat:1844915687");
						writer.println("hiticons.dat:1541859312");
						writer.println("optionicons.dat:-393264488");
						writer.println("steelborder2.dat:-716997548");
						writer.println("data:8297314");
						writer.println("leftarrow_small.dat:-1004178375");
						writer.println("4.dat:280230575");
						writer.println("npc.dat:1489108188");
						writer.println("cross.dat:529843337");
						writer.println("steelborder.dat:1043559214");
						writer.println("48.dat:47670775");
						writer.println("rightarrow_small.dat:523617556");
						writer.println("model_index:-706585152");
						writer.println("b12_full.dat:-1124181286");
						writer.println("tradebacking.dat:-1000916878");
						writer.println("startgame.dat:2004158547");
						writer.println("50.dat:781500348");
						writer.println("mod_icons.dat:449541346");
						writer.println("magicoff.dat:661178691");
						writer.println("emotesoff.dat:1006023899");
						writer.println("miscgraphics.dat:2081559868");
						writer.println("44.dat:-7712589");
						writer.println("26.dat:-1669213509");
						writer.println("tex_red.dat:-1811229622");
						writer.println("magicon.dat:-869490323");
						writer.println("headicons_pk.dat:2038060091");
						writer.println("combaticons.dat:53973365");
						writer.println("17.dat:1795003327");
						writer.println("22.dat:-1724596873");
						writer.println("16.dat:1781157486");
						writer.println("varbit.idx:-514850793");
						writer.println("21.dat:-1738442714");
						writer.println("32.dat:-880000572");
						writer.println("15.dat:1767311645");
						writer.println("33.dat:-866154731");
						writer.println("anim_index:715169772");
						writer.println("14.dat:1753465804");
						writer.println("anim_crc:-40228664");
						writer.println("40.dat:-63095953");
						writer.println("34.dat:-852308890");
						writer.println("29.dat:-1627675986");
						writer.println("hitmarks.dat:-1502153170");
						writer.println("model_version:252137566");
						writer.println("27.dat:-1655367668");
						writer.println("45.dat:6133252");
						writer.println("20.dat:-1752288555");
						writer.println("prayeroff.dat:305236077");
						writer.println("1451391714:1275835656");
						writer.println("rightarrow.dat:1442199444");
						writer.println("flo.idx:-1569242604");
						writer.println("tldlist.txt:-840867198");
						writer.println("fragmentsenc.txt:-573349193");
						writer.println("mapback.dat:1644583778");
						writer.println("chest.dat:-416634290");
						writer.println("fragmentscenc.txt:-1407234166");
						writer.println("46.dat:19979093");
						writer.println("mapscene.dat:839488367");
						writer.println("spotanim.dat:-955170442");
						writer.println("loc.dat:682978269");
						writer.println("seq.dat:886159288");
						writer.println("combatboxes.dat:-1868599050");
						writer.println("overlay_duel.dat:450862262");
						writer.println("emoteicons.dat:-659742015");
						writer.println("lunaron.dat:1041009790");
						writer.println("28.dat:-1641521827");
						writer.println("headicons_prayer.dat:-1337835461");
						writer.println("varp.idx:383757988");
						writer.println("runes.dat:-1668775416");
						writer.println("prayeron.dat:392041951");
						writer.println("combaticons3.dat:-938346352");
						writer.println("headicons_hint.dat:1018124075");
						writer.println("npc.idx:1489126980");
						writer.println("1.dat:238693052");
						writer.println("5.dat:294076416");
						writer.println("overlay_multiway.dat:2025126712");
						writer.println("idk.idx:150838643");
						writer.println("7.dat:321768098");
						writer.println("titlescroll.dat:-384541308");
						writer.println("9.dat:349459780");
						writer.println("lunaroff.dat:1237568592");
						writer.println("titlebox.dat:-1891508522");
						writer.println("staticons.dat:661681639");
						writer.println("midi_index:-1691482954");
						writer.println("anim_version:-797498902");
						writer.println("midi_version:-945480188");
						writer.println("47.dat:33824934");
						writer.println("map_version:-923525801");
						writer.println("staticons2.dat:1758274153");
						writer.println("mapfunction.dat:-1204854137");
						writer.println("39.dat:-783079685");
						writer.println("title.dat:-566502255");
						writer.println("screenframe.dat:1219084034");
						writer.println("obj.dat:-1667617738");
						writer.println("magicoff2.dat:1727594325");
						writer.println("35.dat:-838463049");
						writer.println("mapmarker.dat:1955804455");
						writer.println("optionicons2.dat:1831069846");
						writer.println("letter.dat:819035239");
						writer.println("domainenc.txt:1694783164");
						writer.println("3.dat:266384734");
						writer.println("10.dat:1698082440");
						writer.println("compass.dat:-427405255");
						writer.println("sounds.dat:232787039");
						writer.println("map_index:1987120305");
						writer.println("18.dat:1808849168");
						writer.println("23.dat:-1710751032");
						writer.println("24.dat:-1696905191");
						writer.println("tex_brown.dat:-351562801");
						writer.println("25.dat:-1683059350");
						writer.println("sworddecor.dat:-884827257");
						writer.println("19.dat:1822695009");
						writer.println("30.dat:-907692254");
						writer.println("titlebutton.dat:1955686745");
						writer.println("mapdots.dat:612871759");
						writer.println("13.dat:1739619963");
						writer.println("31.dat:-893846413");
						writer.println("spotanim.idx:-955151650");
						writer.println("11.dat:1711928281");
						writer.println("41.dat:-49250112");
						writer.println("36.dat:-824617208");
						writer.println("p12_full.dat:-227242592");
						writer.println("prayerglow.dat:1694123055");
						writer.println("38.dat:-796925526");
						writer.println("43.dat:-21558430");
						writer.println("q8_full.dat:204062206");
						writer.println("miscgraphics3.dat:-1809621253");
						writer.println("flo.dat:-1569261396");
						writer.println("coins.dat:-58065069");
						writer.println("leftarrow.dat:1922934081");
						writer.println("p11_full.dat:1654911043");
						writer.println("logo.dat:-1752651416");
						writer.println("varbit.dat:-514869585");
						writer.println("scrollbar.dat:-1571073093");
						writer.println("49.dat:61516616");
						writer.println("varp.dat:383739196");
						writer.println("attack.dat:-1131525781");
						writer.println("clanchat.dat:-886248759");
						writer.println("sideicons.dat:1889496696");
						writer.println("midi_crc:-1121254206");
						writer.println("wornicons.dat:1152574301");
						writer.println("key.dat:1150791544");
						writer.println("loc.idx:682997061");
						writer.println("seq.idx:886178080");
						writer.println("8.dat:335613939");
						writer.println("42.dat:-35404271");
						writer.println("magicon2.dat:-1448902313");
						writer.println("37.dat:-810771367");
						writer.println("idk.dat:150819851");
						writer.println("12.dat:1725774122");
						writer.println("miscgraphics2.dat:-1823467094");
						writer.println("model_crc:-1761598724");
						writer.println("badenc.txt:1648736955");
						writer.println("button_brown.dat:1451391714");
						writer.println("mapedge.dat:1362520410");
						writer.println("pen.dat:902321338");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try(BufferedReader reader = new BufferedReader(new FileReader(new File(AppData.resourcePath.toFile(), "hashes.txt")))) {

					String line;

					while((line = reader.readLine()) != null) {

						String[] split = line.split(":");

						AppData.commonHashNames.put(Integer.parseInt(split[1]), split[0]);

					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}


		}).start();

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
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon_128.png")));
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
