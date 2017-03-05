package com.softgate.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import com.softgate.App;
import com.softgate.AppData;
import com.softgate.fs.Cache;
import com.softgate.fs.FileStore;
import com.softgate.model.FileStoreEntryWrapper;
import com.softgate.util.Dialogue;
import com.softgate.util.FileUtils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Duration;

public final class Controller implements Initializable {

	@FXML
	private ListView<String> listView;

	final ObservableList<FileStoreEntryWrapper> data = FXCollections.observableArrayList();

	final ObservableList<String> indexes = FXCollections.observableArrayList();

	@FXML
	private TableView<FileStoreEntryWrapper> tableView;

	@FXML
	private TableColumn<FileStoreEntryWrapper, Integer> idCol;

	@FXML
	private TableColumn<FileStoreEntryWrapper, String> nameCol, extCol, sizeCol;

	@FXML
	private TableColumn<FileStoreEntryWrapper, ImageView> iconCol;

	@FXML
	private TextField fileTf, indexTf;

	@FXML
	private Text progressText;

	@FXML
	private ProgressBar progressBar;

	private double xOffset, yOffset;

	public static final Image indexIcon = new Image(App.class.getResourceAsStream("/images/index_icon.png"));

	public static final Image datIcon = new Image(App.class.getResourceAsStream("/images/dat_icon.png"));

	public static final Image idxIcon = new Image(App.class.getResourceAsStream("/images/idx_icon.png"));

	public static final Image textIcon = new Image(App.class.getResourceAsStream("/images/text_icon.png"));

	public static final Image midiIcon = new Image(App.class.getResourceAsStream("/images/midi_icon.png"));

	public static final Image pngIcon = new Image(App.class.getResourceAsStream("/images/dat_icon.png"));

	public static final Image fileIcon = new Image(App.class.getResourceAsStream("/images/file_icon.png"));

	private Cache cache;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		extCol.setCellValueFactory(cellData -> cellData.getValue().getExtensionProperty());
		sizeCol.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
		iconCol.setCellValueFactory(new PropertyValueFactory<FileStoreEntryWrapper, ImageView>("image"));

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

		FilteredList<FileStoreEntryWrapper> filteredData = new FilteredList<>(data, p -> true);

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

		SortedList<FileStoreEntryWrapper> sortedData = new SortedList<>(filteredData);

		sortedData.comparatorProperty().bind(tableView.comparatorProperty());

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
				ImageView imageView = new ImageView(indexIcon);
				setGraphic(imageView);
				setText(item);
			}
		}
	}

	@FXML
	private void loadFS() {

		clearProgram();

		final File selectedDirectory = Dialogue.chooseDirectory();

		if (selectedDirectory == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				cache = Cache.init(selectedDirectory.toPath(), false);

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
				name = Integer.toString(i);
			}

			indexes.add(name);
		}
	}

	private void populateTable(int storeId) {		
		if (cache == null) {
			return;
		}

		if (storeId < 0) {
			return;
		}

		FileStore store = cache.getStore(storeId);

		data.clear();

		for (int i = 0; i < store.getFileCount(); i++) {

			byte[] fileData = store.readFile(i);

			if (fileData == null) {
				fileData = new byte[0];
			}

			boolean gzipped = FileUtils.isCompressed(fileData);
			
			if (storeId == 0) {
					
				String name = AppData.archiveNames.get(i);
				
				if (name == null) {
					name = Integer.toString(i);
				}				
				
				data.add(new FileStoreEntryWrapper(i, name, fileData.length));
				
			} else {
				data.add(new FileStoreEntryWrapper(i, gzipped ? i + ".gz" : i + ".dat",
						fileData.length));
			}

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
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.storeResourcePath.toFile()))) {
						for (Entry<Integer, String> set : AppData.storeNames.entrySet()) {
							writer.println(set.getKey() + ":" + set.getValue());
						}
					}
					
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
			
			AppData.archiveNames.put(selectedIndex, name);
			
			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					try(PrintWriter writer = new PrintWriter(new FileWriter(AppData.archiveResourcePath.toFile()))) {
						for (Entry<Integer, String> set : AppData.archiveNames.entrySet()) {
							writer.println(set.getKey() + ":" + set.getValue());
						}
					}
					
					FileStore store = cache.getStore(selectedIndex);
					
					final byte[] fileData = store.readFile(selectedEntry);
					
					double progress = 100.00;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress(1, 1);
					
					Platform.runLater(() -> {
						data.set(selectedEntry, new FileStoreEntryWrapper(selectedEntry, name, fileData == null ? 0 : fileData.length));
					});
					
					return true;
				}
				
			});			
			
		}
	}
	
	@FXML
	private void addEntry() {

		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1 || cache == null) {
			return;
		}

		final List<File> files = Dialogue.chooseFiles();

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

					byte[] data = FileUtils.readFile(file);

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

		final File selectedFile = Dialogue.chooseFile();

		if (selectedFile == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				final FileStore store = cache.getStore(selectedIndex);

				final byte[] data = FileUtils.readFile(selectedFile);

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

		final int selectedEntry = tableView.getSelectionModel().getSelectedIndex();

		if (selectedEntry == -1) {
			return;
		}

		final File selectedDirectory = Dialogue.chooseDirectory();

		if (selectedDirectory == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				final FileStore store = cache.getStore(selectedIndex);

				byte[] data = store.readFile(selectedEntry);

				if (data == null) {
					return false;
				}

				final boolean gzipped = FileUtils.isCompressed(data);

				try (FileOutputStream fos = new FileOutputStream(new File(selectedDirectory, selectedIndex > 0
						? gzipped ? selectedEntry + ".gz" : selectedEntry + ".dat" : selectedEntry + ".jag"))) {
					fos.write(data);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				final double progress = 100.00;

				updateMessage(String.format("%.2f%s", progress, "%"));
				updateProgress(1, 1);

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

		final File selectedDirectory = Dialogue.chooseDirectory();

		if (selectedDirectory == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				final FileStore store = cache.getStore(selectedIndex);

				final int storeCount = store.getFileCount();

				for (int i = 0; i < storeCount; i++) {
					byte[] data = store.readFile(i);

					if (data == null) {
						continue;
					}

					boolean gzipped = FileUtils.isCompressed(data);

					try (FileOutputStream fos = new FileOutputStream(new File(selectedDirectory,
							selectedIndex > 0 ? gzipped ? i + ".gz" : i + ".dat" : i + ".jag"))) {
						fos.write(data);
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
	private void handleMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	@FXML
	private void handleMouseDragged(MouseEvent event) {
		App.getStage().setX(event.getScreenX() - xOffset);
		App.getStage().setY(event.getScreenY() - yOffset);
	}

	@FXML
	private void clearProgram() {
		indexes.clear();
		data.clear();
	}

	@FXML
	private void minimizeProgram() {

		if (App.getStage() == null) {
			return;
		}

		App.getStage().setIconified(true);
	}

	@FXML
	private void closeProgram() {
		Platform.exit();
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

}
