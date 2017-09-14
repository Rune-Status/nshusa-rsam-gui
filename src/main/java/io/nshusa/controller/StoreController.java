package io.nshusa.controller;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.softgate.fs.FileStore;
import com.softgate.fs.IndexedFileSystem;
import com.softgate.fs.binary.Archive;

import io.nshusa.App;
import io.nshusa.AppData;
import io.nshusa.meta.StoreMeta;
import io.nshusa.model.ArchiveMeta;
import io.nshusa.model.StoreEntryWrapper;
import io.nshusa.util.Dialogue;
import io.nshusa.util.GZipUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;

public final class StoreController implements Initializable {

	@FXML
	private ListView<String> listView;

	final ObservableList<StoreEntryWrapper> data = FXCollections.observableArrayList();

	final ObservableList<String> indexes = FXCollections.observableArrayList();

	@FXML
	private TableView<StoreEntryWrapper> tableView;

	@FXML
	private TableColumn<StoreEntryWrapper, Integer> idCol;

	@FXML
	private TableColumn<StoreEntryWrapper, String> nameCol, extCol, sizeCol;

	@FXML
	private TableColumn<StoreEntryWrapper, ImageView> iconCol;

	@FXML
	private TextField fileTf, indexTf;

	@FXML
	private Text progressText;

	@FXML
	private ProgressBar progressBar;

	private double xOffset, yOffset;

	private IndexedFileSystem cache;

	private Stage stage;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		extCol.setCellValueFactory(cellData -> cellData.getValue().getExtensionProperty());
		sizeCol.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
		iconCol.setCellValueFactory(new PropertyValueFactory<StoreEntryWrapper, ImageView>("image"));

		listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {

			final int selectedRow = newSelection.intValue();

			if (selectedRow == -1) {
				return;
			}

			ContextMenu context = new ContextMenu();

			MenuItem repackMI = new MenuItem("Repack");
			repackMI.setOnAction(e -> addEntry());

			MenuItem renameMI = new MenuItem("Rename");
			renameMI.setOnAction(e -> renameStore());

			MenuItem exportMI = new MenuItem("Export");
			exportMI.setGraphic(new ImageView(AppData.saveIcon16));
			exportMI.setOnAction(e -> dumpEntry());

			context.getItems().addAll(repackMI, renameMI, exportMI);

			listView.setContextMenu(context);

		});
		
		tableView.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
			
			final int selectedRow = newSelection.intValue();
			
			final int selectedIndex = listView.getSelectionModel().getSelectedIndex();
			
			if (selectedIndex == -1 || selectedRow == -1) {
				return;
			}
			
			if (selectedIndex == 0) {

				ContextMenu context = new ContextMenu();

				MenuItem openMI = new MenuItem("Open");
				openMI.setOnAction(e -> openArchive());				
				context.getItems().add(openMI);
				
				ArchiveMeta meta = AppData.archiveMetas.get(selectedRow);
				
				if (meta != null) {
					if (meta.isImageArchive()) {
						MenuItem viewMI = new MenuItem("View");
						viewMI.setOnAction(e -> viewArchive());				
						context.getItems().add(viewMI);
					}
				}

				MenuItem renameMI = new MenuItem("Rename");
				renameMI.setOnAction(e -> renameArchive());				
				context.getItems().add(renameMI);

				MenuItem removeMI = new MenuItem("Remove");
				removeMI.setGraphic(new ImageView(AppData.deleteIcon));
				removeMI.setOnAction(e -> removeEntry());				
				context.getItems().add(removeMI);

				MenuItem replaceMI = new MenuItem("Replace");
				replaceMI.setOnAction(e -> replaceEntry());				
				context.getItems().add(replaceMI);

				MenuItem exportMI = new MenuItem("Export");
				exportMI.setGraphic(new ImageView(AppData.saveIcon16));
				exportMI.setOnAction(e -> dumpEntry());
				context.getItems().add(exportMI);

				MenuItem clearMI = new MenuItem("Clear");
				clearMI.setOnAction(e -> clearIndex());
				clearMI.setGraphic(new ImageView(AppData.clearIcon16));
				context.getItems().add(clearMI);

				tableView.setContextMenu(context);

			} else {
				ContextMenu context = new ContextMenu();

				MenuItem addMI = new MenuItem("Add");
				addMI.setGraphic(new ImageView(AppData.addIcon));
				addMI.setOnAction(e -> addEntry());

				MenuItem removeMI = new MenuItem("Remove");
				removeMI.setGraphic(new ImageView(AppData.deleteIcon));
				removeMI.setOnAction(e -> removeEntry());

				MenuItem replaceMI = new MenuItem("Replace");
				replaceMI.setOnAction(e -> replaceEntry());

				MenuItem exportMI = new MenuItem("Export");
				exportMI.setGraphic(new ImageView(AppData.saveIcon16));
				exportMI.setOnAction(e -> dumpEntry());

				MenuItem clearMI = new MenuItem("Clear");
				clearMI.setGraphic(new ImageView(AppData.clearIcon16));
				clearMI.setOnAction(e -> clearIndex());

				context.getItems().addAll(addMI, removeMI, replaceMI, exportMI, clearMI);

				tableView.setContextMenu(context);
			}
			
		});

		listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {

			data.clear();

			final int selectedIndex = newSelection.intValue();

			if (selectedIndex < 0) {
				return;
			}

			if (cache == null) {
				return;
			}
			
			populateTable(selectedIndex);

		});

		FilteredList<String> filteredIndexes = new FilteredList<>(indexes, p -> true);

		indexTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredIndexes.setPredicate(idx -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (idx.toLowerCase().contains(lowerCaseFilter)) {
					return true;
				} else if (idx.toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
				return false;
			});
		});

		listView.setItems(filteredIndexes);

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {
				return new AttachmentListCell();
			}
		});

		FilteredList<StoreEntryWrapper> filteredData = new FilteredList<>(data, p -> true);

		fileTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(file -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (file.getName().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				} else if (Integer.toString(file.getId()).contains(lowerCaseFilter)) {
					return true;
				} else if (file.getExtension().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
				return false;
			});
		});

		SortedList<StoreEntryWrapper> sortedData = new SortedList<>(filteredData);

		sortedData.comparatorProperty().bind(tableView.comparatorProperty());

		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableView.setItems(sortedData);

	}

	private static class AttachmentListCell extends ListCell<String> {
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				ImageView imageView = new ImageView(AppData.fileStoreIcon);
				setGraphic(imageView);
				setText(item);
			}
		}
	}

	@FXML
	private void loadFS() {

		clearProgram();

		final File selectedDirectory = Dialogue.directoryChooser().showDialog(stage);

		if (selectedDirectory == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				cache = IndexedFileSystem.init(selectedDirectory.toPath());

				Platform.runLater(() -> {
					populateIndex();
				});

				double progress = 100.00;

				updateMessage(String.format("%.2f%s", progress, "%"));
				updateProgress(1, 1);

				return true;
			}

		});

	}

	private void populateIndex() {

		indexes.clear();

		for (int i = 0; i < cache.getStoreCount(); i++) {

			String name = AppData.storeNames.get(i);

			if (name == null) {
				name = String.format("index %d (unknown)", i);
			}

			indexes.add(String.format("index %d (%s)", i, name));
		}
	}

	private void populateTable(int storeId) {
		if (cache == null) {
			return;
		}

		if (storeId < 0) {
			return;
		}

		data.clear();

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				FileStore store = cache.getStore(storeId);

				if (store == null) {
					return false;
				}

				final List<StoreEntryWrapper> storeWrappers = new ArrayList<>();

				int entries = store.getFileCount();

				for (int i = 0; i < entries; i++) {

					byte[] fileData = store.readFile(i);

					if (fileData == null) {
						fileData = new byte[0];
					}

					boolean gzipped = GZipUtils.isGZipped(fileData);

					if (storeId == 0) {
						
						ArchiveMeta meta = AppData.archiveMetas.get(i);

						String name = meta == null ? "unknown" : meta.getName();

						storeWrappers.add(new StoreEntryWrapper(i, name, fileData.length));
					} else {
						storeWrappers.add(new StoreEntryWrapper(i, gzipped ? i + ".gz" : fileData.length == 0 ? "empty" : Integer.toString(i) , fileData.length));
					}

					double progress = ((double) (i + 1) / entries) * 100;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress((i + 1), entries);

				}

				Platform.runLater(() -> {
					data.addAll(storeWrappers);
				});

				return true;
			}

		});

	}

	private ArchiveController archiveController;

	@FXML
	private void openArchive() {
		StoreEntryWrapper wrapper = tableView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}

		try {
			
			FileStore store = cache.getStore(0);
			
			// this is to check if the archive could be read before adding it to the archive viewer			
			@SuppressWarnings("unused")
			Archive archive = Archive.decode(store.readFile(wrapper.getId()));

			FXMLLoader loader = new FXMLLoader(App.class.getResource("/ArchiveUI.fxml"));

			Parent root = (Parent) loader.load();

			if (archiveController == null || !archiveController.getStage().isShowing()) {
				archiveController = (ArchiveController) loader.getController();

				archiveController.cache = cache;
				archiveController.initArchive(wrapper.getId());

				Stage stage = new Stage();

				archiveController.setStage(stage);
				stage.setTitle("Archive Editor");
				Scene scene = new Scene(root);
				scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
				stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app_icon_128.png")));
				stage.setScene(scene);
				stage.initStyle(StageStyle.TRANSPARENT);
				stage.setResizable(false);
				stage.centerOnScreen();
				stage.setTitle("Archive Editor");
				stage.show();
			} else {
				archiveController.initArchive(wrapper.getId());
			}
		} catch (IOException e) {
			e.printStackTrace();
			Dialogue.showWarning("Could not read archive.");
		}

	}
	
	private ImageArchiveController imageArchiveController;
	
	@FXML
	private void viewArchive() {
		StoreEntryWrapper wrapper = tableView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}

		try {
			
			FileStore store = cache.getStore(0);
	
			Archive archive = Archive.decode(store.readFile(wrapper.getId()));

			FXMLLoader loader = new FXMLLoader(App.class.getResource("/ImageArchiveUI.fxml"));

			Parent root = (Parent) loader.load();

			if (imageArchiveController == null || !imageArchiveController.getStage().isShowing()) {
				imageArchiveController = (ImageArchiveController) loader.getController();

				imageArchiveController.cache = cache;
				imageArchiveController.initImageArchive(wrapper.getName(), archive);

				Stage stage = new Stage();

				imageArchiveController.setStage(stage);
				Scene scene = new Scene(root);
				scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
				stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app_icon_128.png")));
				stage.setScene(scene);
				stage.initStyle(StageStyle.TRANSPARENT);
				stage.setResizable(false);
				stage.centerOnScreen();
				stage.setTitle("Image Archive Editor");
				stage.show();
			} else {
				imageArchiveController.initImageArchive(wrapper.getName(), archive);
			}
		} catch (IOException e) {
			e.printStackTrace();
			Dialogue.showWarning("Could not read archive.");
		}

	}

	@FXML
	private void createStore() {
		if (cache == null) {
			return;
		}

		final int nextIndex = cache.getStoreCount();

		try {
			cache.createStore(nextIndex);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		final FileStore store = cache.getStore(nextIndex);

		if (store == null) {
			return;
		}

		Optional<String> result = Dialogue.showInput("Name this store").showAndWait();

		if (result.isPresent()) {

			String name = result.get();

			if (name == null) {
				Dialogue.showWarning("Name cannot be null");
				return;
			} else if (name.isEmpty()) {
				Dialogue.showWarning("Name cannot be empty");
				return;
			} else if (name.length() >= 20) {
				Dialogue.showWarning("Name must be shorter than 20 characters");
				return;
			}

			indexes.add(name);

			AppData.storeNames.put(nextIndex, name);

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					saveStoreCookies();

					double progress = 100.00;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress(1, 1);

					return true;
				}

			});

		}

	}

	private synchronized void saveStoreCookies() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(AppData.resourcePath.toFile(), "stores.json")))) {
			List<StoreMeta> meta = new ArrayList<>();

			AppData.storeNames.entrySet().stream().forEach(it -> meta.add(new StoreMeta(it.getKey(), it.getValue())));

			Gson gson = new GsonBuilder().setPrettyPrinting().create();

			writer.write(gson.toJson(meta));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void renameStore() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		Optional<String> result = Dialogue.showInput("Enter a new name", "").showAndWait();

		if (result.isPresent()) {
			String name = result.get();

			if (name == null) {
				Dialogue.showWarning("Name cannot be null");
				return;
			} else if (name.isEmpty()) {
				Dialogue.showWarning("Name cannot be empty");
				return;
			} else if (name.length() >= 20) {
				Dialogue.showWarning("Name must be shorter than 20 characters");
				return;
			}

			AppData.storeNames.put(selectedIndex, name);

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					saveStoreCookies();

					double progress = 100.00;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress(1, 1);

					Platform.runLater(() -> {
						indexes.set(selectedIndex, name);
					});

					return true;
				}

			});

		}
	}

	@FXML
	private void renameArchive() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		final int selectedEntry = tableView.getSelectionModel().getSelectedIndex();

		if (selectedEntry == -1) {
			return;
		}

		if (cache == null) {
			return;
		}

		Optional<String> result = Dialogue.showInput("Enter a new name", "").showAndWait();

		if (result.isPresent()) {
			String name = result.get();

			if (name == null) {
				Dialogue.showWarning("Name cannot be null");
				return;
			} else if (name.isEmpty()) {
				Dialogue.showWarning("Name cannot be empty");
				return;
			} else if (name.length() >= 20) {
				Dialogue.showWarning("Name must be shorter than 20 characters");
				return;
			}
			
			ArchiveMeta meta = AppData.archiveMetas.get(selectedEntry);
			
			if (meta == null) {
				return;
			}

			AppData.archiveMetas.put(selectedEntry, new ArchiveMeta(selectedEntry, name, meta.isImageArchive()));

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					saveArchiveCookies();

					FileStore store = cache.getStore(selectedIndex);

					final byte[] fileData = store.readFile(selectedEntry);

					double progress = 100.00;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress(1, 1);

					Platform.runLater(() -> {
						data.set(selectedEntry,
								new StoreEntryWrapper(selectedEntry, name, fileData == null ? 0 : fileData.length));
					});

					return true;
				}

			});

		}
	}

	private synchronized void saveArchiveCookies() {
		try (PrintWriter writer = new PrintWriter(new FileWriter(AppData.archiveResourcePath.toFile()))) {
			for (Entry<Integer, ArchiveMeta> set : AppData.archiveMetas.entrySet()) {
				writer.println(set.getKey() + ":" + set.getValue().getName() + ":" + set.getValue().isImageArchive());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void addEntry() {

		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1 || cache == null) {
			return;
		}

		final List<File> files = Dialogue.fileChooser().showOpenMultipleDialog(stage);

		if (files == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				FileStore store = cache.getStore(selectedIndex);

				int fileCount = 0;
				for (File file : files) {

					int fileId = store.getFileCount();

					try {
						fileId = Integer.parseInt(file.getName().substring(0, file.getName().lastIndexOf(".")));
					} catch (Exception ex) {

					}

					byte[] data = Files.readAllBytes(file.toPath());

					store.writeFile(fileId, data, data.length);

					fileCount++;

					double progress = ((double) fileCount) / files.size() * 100;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress(fileCount, files.size());

				}

				Platform.runLater(() -> {
					populateTable(selectedIndex);
				});

				return true;
			}

		});

	}

	@FXML
	private void removeEntry() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1 || cache == null) {
			return;
		}

		final int selectedFile = tableView.getSelectionModel().getSelectedIndex();

		if (selectedFile == -1) {
			return;
		}

		Dialogue.OptionMessage option = new Dialogue.OptionMessage("Are you sure you want to do this?",
				"This file will be deleted permanently.");

		option.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		Optional<ButtonType> result = option.showAndWait();

		if (result.isPresent()) {

			ButtonType type = result.get();

			if (type != ButtonType.YES) {
				return;
			}

		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				FileStore store = cache.getStore(selectedIndex);

				store.writeFile(selectedFile, new byte[0], 0);

				double progress = 100.00;

				updateMessage(String.format("%.2f%s", progress, "%"));
				updateProgress(1, 1);

				Platform.runLater(() -> {
					populateTable(selectedIndex);
				});
				return true;
			}

		});

	}

	@FXML
	private void replaceEntry() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1 || cache == null) {
			return;
		}

		final int selectedEntry = tableView.getSelectionModel().getSelectedIndex();

		if (selectedEntry == -1) {
			return;
		}

		final File selectedFile = Dialogue.fileChooser().showOpenDialog(stage);

		if (selectedFile == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				final FileStore store = cache.getStore(selectedIndex);

				final byte[] data = Files.readAllBytes(selectedFile.toPath());

				store.writeFile(selectedEntry, data, data.length);

				final double progress = 100.00;

				updateMessage(String.format("%.2f%s", progress, "%"));
				updateProgress(1, 1);

				Platform.runLater(() -> {
					populateTable(selectedIndex);
				});
				return true;
			}

		});
	}

	@FXML
	private void dumpEntry() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1 || cache == null) {
			return;
		}

		final List<Integer> selectedIndexes = tableView.getSelectionModel().getSelectedIndices();

		final List<StoreEntryWrapper> selectedEntries = tableView.getSelectionModel().getSelectedItems();

		if (selectedEntries == null) {
			return;
		}

		final File selectedDirectory = Dialogue.directoryChooser().showDialog(stage);

		if (selectedDirectory == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				final FileStore store = cache.getStore(selectedIndex);

				for (int i = 0; i < selectedIndexes.size(); i++) {
					int selectedEntryIndex = selectedIndexes.get(i);

					StoreEntryWrapper storeWrapper = selectedEntries.get(i);

					byte[] fileData = store.readFile(selectedEntryIndex);

					if (fileData == null) {
						return false;
					}

					try (FileOutputStream fos = new FileOutputStream(
							new File(selectedDirectory, storeWrapper.getName() + "." + storeWrapper.getExtension()))) {
						fos.write(fileData);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					final double progress = ((double) (i + 1) / selectedIndexes.size()) * 100;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress((i + 1), selectedIndexes.size());

				}

				Platform.runLater(() -> {
					populateTable(selectedIndex);

					Dialogue.openDirectory("Would you like to view this file?", selectedDirectory);
				});
				return true;
			}

		});
	}

	@FXML
	private void dumpIndex() {

		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1 || cache == null) {
			return;
		}

		final File selectedDirectory = Dialogue.directoryChooser().showDialog(stage);

		if (selectedDirectory == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				final FileStore store = cache.getStore(selectedIndex);

				final int storeCount = store.getFileCount();

				for (int i = 0; i < storeCount; i++) {
					byte[] fileData = store.readFile(i);

					if (fileData == null) {
						continue;
					}

					StoreEntryWrapper wrapper = data.get(i);

					try (FileOutputStream fos = new FileOutputStream(
							new File(selectedDirectory, wrapper.getName() + "." + wrapper.getExtension()))) {
						fos.write(fileData);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					double progress = ((double) (i + 1) / storeCount) * 100;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress((i + 1), storeCount);

				}

				Platform.runLater(() -> {
					Dialogue.openDirectory("Would you like to open this directory?", selectedDirectory);
				});
				return true;
			}

		});

	}

	@FXML
	private void clearIndex() {
		if (cache == null) {
			return;
		}

		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		final FileStore store = cache.getStore(selectedIndex);

		Dialogue.OptionMessage option = new Dialogue.OptionMessage("Are you sure you want to do this?",
				"All files will be deleted permanently.");

		option.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		Optional<ButtonType> result = option.showAndWait();

		if (result.isPresent()) {

			ButtonType type = result.get();

			if (type != ButtonType.YES) {
				return;
			}

		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				for (int i = 0; i < store.getFileCount(); i++) {
					store.writeFile(i, new byte[0], 0);
				}

				Platform.runLater(() -> {
					populateTable(selectedIndex);
				});
				return true;
			}

		});

	}

	@FXML
	private void loadArchiveEditor() {
		try {
			FXMLLoader loader = new FXMLLoader(App.class.getResource("/ArchiveUI.fxml"));

			Parent root = (Parent) loader.load();

			ArchiveController controller = (ArchiveController) loader.getController();

			Stage stage = new Stage();

			controller.setStage(stage);
			stage.setTitle("Archive Editor");
			Scene scene = new Scene(root);
			scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app_icon_128.png")));
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setResizable(false);
			stage.centerOnScreen();
			stage.setTitle("Archive Editor");
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void loadImageArchiveEditor() {
		try {
			FXMLLoader loader = new FXMLLoader(App.class.getResource("/ImageArchiveUI.fxml"));

			Parent root = (Parent) loader.load();

			ImageArchiveController controller = (ImageArchiveController) loader.getController();

			Stage stage = new Stage();

			controller.setStage(stage);
			stage.setTitle("Image Archive Editor");
			Scene scene = new Scene(root);
			scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app_icon_128.png")));
			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setResizable(false);
			stage.centerOnScreen();
			stage.setTitle("Archive Editor");
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	@FXML
	private void handleMouseDragged(MouseEvent event) {
		stage.setX(event.getScreenX() - xOffset);
		stage.setY(event.getScreenY() - yOffset);
	}

	@FXML
	private void clearProgram() {
		indexes.clear();
		data.clear();
	}

	@FXML
	private void minimizeProgram() {
		if (stage == null) {
			return;
		}

		stage.setIconified(true);
	}

	@FXML
	private void closeProgram() {
		stage.close();
	}

	private void createTask(Task<?> task) {

		progressBar.setVisible(true);

		progressBar.progressProperty().unbind();
		progressBar.progressProperty().bind(task.progressProperty());

		progressText.textProperty().unbind();
		progressText.textProperty().bind(task.messageProperty());

		new Thread(task).start();

		task.setOnSucceeded(e -> {

			PauseTransition pause = new PauseTransition(Duration.seconds(1));

			pause.setOnFinished(event -> {
				progressBar.setVisible(false);
				progressText.textProperty().unbind();
				progressText.setText("");
			});

			pause.play();
		});

		task.setOnFailed(e -> {

			PauseTransition pause = new PauseTransition(Duration.seconds(1));

			pause.setOnFinished(event -> {
				progressBar.setVisible(false);
				progressText.textProperty().unbind();
				progressText.setText("");
			});

			pause.play();

		});
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

}
