package io.nshusa.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import io.nshusa.AppData;
import io.nshusa.model.ArchiveEntryWrapper;
import io.nshusa.model.ArchiveWrapper;
import io.nshusa.rsam.FileStore;
import io.nshusa.rsam.IndexedFileSystem;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.util.CompressionUtil;
import io.nshusa.rsam.util.HashUtils;
import io.nshusa.util.Dialogue;
import io.nshusa.util.StringUtils;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import io.nshusa.rsam.binary.Archive.ArchiveEntry;

public final class ArchiveController implements Initializable {

	@FXML
	private ListView<ArchiveWrapper> listView;

	final ObservableList<ArchiveEntryWrapper> data = FXCollections.observableArrayList();

	final ObservableList<ArchiveWrapper> indexes = FXCollections.observableArrayList();

	@FXML
	private TableView<ArchiveEntryWrapper> tableView;

	@FXML
	private TableColumn<ArchiveEntryWrapper, Integer> hashCol;

	@FXML
	private TableColumn<ArchiveEntryWrapper, String> nameCol, extCol, sizeCol;

	@FXML
	private TableColumn<ArchiveEntryWrapper, ImageView> iconCol;

	@FXML
	private TextField fileTf, indexTf;

	@FXML
	private Text progressText;

	@FXML
	private ProgressBar progressBar;

	private double xOffset, yOffset;

	private Stage stage;
	
	public IndexedFileSystem cache;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		ContextMenu listViewCM = new ContextMenu();

		MenuItem renameMI = new MenuItem("Rename");
		renameMI.setGraphic(new ImageView(AppData.renameIcon16));
		renameMI.setOnAction(e -> renameArchive());

		MenuItem removeMI = new MenuItem("Remove");
		removeMI.setGraphic(new ImageView(AppData.deleteIcon));
		removeMI.setOnAction(e -> removeArchive());

		MenuItem exportMI = new MenuItem("Export");
		exportMI.setGraphic(new ImageView(AppData.saveIcon16));
		exportMI.setOnAction(e -> dumpArchive());

		listViewCM.getItems().addAll(Arrays.asList(renameMI, removeMI, exportMI));

		listView.setContextMenu(listViewCM);

		ContextMenu tableCM = new ContextMenu();

		MenuItem addMI = new MenuItem("Add");
		addMI.setGraphic(new ImageView(AppData.addIcon));
		addMI.setOnAction(e -> addEntry());

		MenuItem renameMI2 = new MenuItem("Rename");
		renameMI2.setGraphic(new ImageView(AppData.renameIcon16));
		renameMI2.setOnAction(e -> renameEntry());

		MenuItem identifyMI = new MenuItem("Identify");
		identifyMI.setGraphic(new ImageView(AppData.identify16Icon));
		identifyMI.setOnAction(e -> identifyHash());

		MenuItem removeMI2 = new MenuItem("Remove");
		removeMI2.setGraphic(new ImageView(AppData.deleteIcon));
		removeMI2.setOnAction(e -> removeEntry());

		MenuItem replaceMI2 = new MenuItem("Replace");
		replaceMI2.setGraphic(new ImageView(AppData.replace16Icon));
		replaceMI2.setOnAction(e -> replaceEntry());

		MenuItem exportMI2 = new MenuItem("Export");
		exportMI2.setGraphic(new ImageView(AppData.saveIcon16));
		exportMI2.setOnAction(e -> dumpEntry());

		MenuItem clearMI = new MenuItem("Clear");
		clearMI.setGraphic(new ImageView(AppData.clearIcon16));
		clearMI.setOnAction(e -> clearArchive());

		tableCM.getItems().addAll(Arrays.asList(addMI, renameMI2, identifyMI, removeMI2, replaceMI2, exportMI2, clearMI));

		tableView.setContextMenu(tableCM);

		hashCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		extCol.setCellValueFactory(cellData -> cellData.getValue().getExtensionProperty());
		sizeCol.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
		iconCol.setCellValueFactory(new PropertyValueFactory<ArchiveEntryWrapper, ImageView>("image"));

		listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

			data.clear();

			if (newSelection == null || cache == null) {
				return;
			}
			
			if (newSelection.getId() < 0) {
				return;
			}

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					try {					

						FileStore store = cache.getStore(0);
						
						final Archive archive = Archive.decode(store.readFile(newSelection.getId()));
						
						final int entries = archive.getEntries().size();
						
						for (int i = 0; i < entries; i++) {
							final ArchiveEntry entry = archive.getEntries().get(i);
							
							Platform.runLater(() ->	data.add(new ArchiveEntryWrapper(entry)));
							
							double progress = ((double)(i + 1) / entries) * 100;
							
							updateMessage(String.format("%.2f%s", progress, "%"));
							updateProgress((i + 1), entries);
							
						}
	
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return true;
				}
				
			});

		});

		FilteredList<ArchiveWrapper> filteredIndexes = new FilteredList<>(indexes, p -> true);

		indexTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredIndexes.setPredicate(idx -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (idx.getName().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				} else if (idx.getName().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
				return false;
			});
		});

		listView.setItems(filteredIndexes);

		listView.setCellFactory(new Callback<ListView<ArchiveWrapper>, ListCell<ArchiveWrapper>>() {
			@Override
			public ListCell<ArchiveWrapper> call(ListView<ArchiveWrapper> list) {
				return new AttachmentListCell();
			}
		});

		FilteredList<ArchiveEntryWrapper> filteredData = new FilteredList<>(data, p -> true);

		fileTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(file -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (file.getName().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				} else if (Integer.toString(file.getHash()).contains(lowerCaseFilter)) {
					return true;
				} else if (file.getExtension().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
				return false;
			});
		});

		SortedList<ArchiveEntryWrapper> sortedData = new SortedList<>(filteredData);

		sortedData.comparatorProperty().bind(tableView.comparatorProperty());
		 
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);  

		tableView.setItems(sortedData);
	}

	private static class AttachmentListCell extends ListCell<ArchiveWrapper> {
		@Override
		public void updateItem(ArchiveWrapper item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				ImageView imageView = new ImageView(AppData.fileStoreIcon);
				setGraphic(imageView);
				setText(item.getName());
			}
		}
	}

	@FXML
	private void addArchive() {

	}
	
	public void initArchive(int id) {
		Optional<ArchiveWrapper> result = indexes.stream().filter(it -> it.getId() == id).findFirst();
		
		if (result.isPresent()) {
			return;
		}
		
		indexes.add(new ArchiveWrapper(id));
	}

	@FXML
	private void createArchive() {

		Optional<String> result = Dialogue.showInput("Enter the name of this archive.").showAndWait();

		if (!result.isPresent()) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				
				FileStore store = cache.getStore(0);
				
				Archive archive = new Archive(new ArchiveEntry[0]);

				try {
					byte[] encoded = archive.encode();
					
					store.writeFile(store.getFileCount(), ByteBuffer.wrap(encoded), encoded.length);

					Platform.runLater(() ->	indexes.add(new ArchiveWrapper(store.getFileCount())));
					
					updateMessage("100%");
					updateProgress(1, 1);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			
		});
		
	}

	@FXML
	private void clearArchive() {

		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}

		Dialogue.OptionMessage option = new Dialogue.OptionMessage("Are you sure you want to do this?",
				"All entries will be deleted.");

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
				try {
					
					FileStore store = cache.getStore(0);
					
					Archive archive = Archive.decode(store.readFile(wrapper.getId()));
					
					archive.getEntries().clear();

					byte[] encoded = archive.encode();
					
					store.writeFile(wrapper.getId(), ByteBuffer.wrap(encoded), encoded.length);

					Platform.runLater(data::clear);
					
					updateMessage("100%");
					updateProgress(1, 1);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			
		});

	}

	@FXML
	private void renameArchive() {

		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (selectedIndex == -1 || wrapper == null) {
			return;
		}

		Optional<String> result = Dialogue.showInput("Enter a new name for this archive.").showAndWait();

		if (!result.isPresent()) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
					Platform.runLater(() -> {
						indexes.set(selectedIndex, new ArchiveWrapper(wrapper.getId()));
					});
					
					updateMessage("100%");
					updateProgress(1, 1);

				return true;
			}
			
		});

	}

	@FXML
	private void renameEntry() {
		final int selectedIndex = tableView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		final ArchiveEntryWrapper entryWrapper = tableView.getSelectionModel().getSelectedItem();

		if (entryWrapper == null) {
			return;
		}

		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}

		final Optional<String> result = Dialogue.showInput("Enter a new name").showAndWait();

		if (!result.isPresent()) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					
					FileStore store = cache.getStore(0);
					
					Archive archive = Archive.decode(store.readFile(wrapper.getId()));

					ArchiveEntry entry = archive.getEntry(entryWrapper.getHash());

					String nName = result.get();

					if (nName == null) {
						return false;
					}

					int nHash = HashUtils.nameToHash(nName);

					int slot = archive.indexOf(entry.getHash());

					if (slot == -1) {
						return false;
					}

					AppData.commonHashNames.put(nHash, nName);

					Archive.ArchiveEntry nEntry = new Archive.ArchiveEntry(nHash, entry.getUncompressedSize(), entry.getCompresseedSize(),
							entry.getBuffer());

					archive.getEntries().set(slot, nEntry);
					
					Platform.runLater(() ->	data.set(selectedIndex, new ArchiveEntryWrapper(nEntry)));

					saveHashes();

					byte[] encoded = archive.encode();
					
					store.writeFile(wrapper.getId(), ByteBuffer.wrap(encoded), encoded.length);
					
					updateMessage("100%");
					updateProgress(1, 1);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			
		});

	}
	
	private synchronized void saveHashes() {
		try (PrintWriter writer = new PrintWriter(new FileWriter(AppData.RESOURCE_PATH.resolve("hash_names.txt").toFile()))) {
			for (Entry<Integer, String> set : AppData.commonHashNames.entrySet()) {
				writer.println(set.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void removeArchive() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}
		
		indexes.remove(selectedIndex);
	}

	@FXML
	private void addEntry() {
		
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		List<File> selectedFiles = Dialogue.fileChooser().showOpenMultipleDialog(stage);

		if (selectedFiles == null) {
			return;
		}

		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					
					FileStore store = cache.getStore(0);
					
					Archive archive = Archive.decode(store.readFile(wrapper.getId()));
					
					for (int i = 0; i < selectedFiles.size(); i++) {
						try {
							
							File file = selectedFiles.get(i);

							byte[] fileData = Files.readAllBytes(file.toPath());

							byte[] bzipped = CompressionUtil.bzip2(fileData);

							int hash = HashUtils.nameToHash(file.getName());

							int uncompressedSize = fileData.length;

							int compressedSize = bzipped.length;

							AppData.commonHashNames.put(hash, file.getName());

							ArchiveEntry entry = new ArchiveEntry(hash, uncompressedSize, compressedSize, ByteBuffer.wrap(bzipped));

							archive.getEntries().add(entry);
							
							Platform.runLater(() ->	data.add(new ArchiveEntryWrapper(entry)));
							
							double progress = ((i + 1) / selectedFiles.size()) * 100;
							
							updateMessage(Double.toString(progress) + "%");
							updateProgress((i + 1), selectedFiles.size());

						} catch (IOException e) {
							continue;
						}
					}

					saveHashes();

					byte[] encoded = archive.encode();

					store.writeFile(wrapper.getId(), ByteBuffer.wrap(encoded), encoded.length);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			
		});

	}

	@FXML
	private void removeEntry() {
		
		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}


		final int selectedIndex = tableView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		final ArchiveEntryWrapper entryWrapper = tableView.getSelectionModel().getSelectedItem();

		if (entryWrapper == null) {
			return;
		}

		Dialogue.OptionMessage option = new Dialogue.OptionMessage("Are you sure you want to do this?",
				"This file will be deleted permanently.");

		option.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		Optional<ButtonType> result = option.showAndWait();

		if (!result.isPresent()) {
			return;
		}
		
		ButtonType type = result.get();

		if (type != ButtonType.YES) {				
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					
					FileStore store = cache.getStore(0);
					
					Archive archive = Archive.decode(store.readFile(wrapper.getId()));

					System.out.println(entryWrapper.getHash());
					
					archive.remove(entryWrapper.getHash());
					
					System.out.println(archive.getEntries().size());

					byte[] encoded = archive.encode();					

					if (!archive.getEntries().isEmpty()) {
						store.writeFile(wrapper.getId(), ByteBuffer.wrap(encoded), encoded.length);
					}

					Platform.runLater(() ->	data.remove(selectedIndex));
					
					updateMessage("100%");
					updateProgress(1, 1);
					
	

				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			
		});

	}

	@FXML
	private void replaceEntry() {

		final int selectedIndex = tableView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		final ArchiveEntryWrapper entryWrapper = tableView.getSelectionModel().getSelectedItem();

		if (entryWrapper == null) {
			return;
		}

		File selectedFile = Dialogue.fileChooser().showOpenDialog(stage);

		if (selectedFile == null) {
			return;
		}

		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					
					FileStore store = cache.getStore(0);
					
					Archive archive = Archive.decode(store.readFile(wrapper.getId()));

					try {

						byte[] fileData = Files.readAllBytes(selectedFile.toPath());

						byte[] bzipped = CompressionUtil.bzip2(fileData);

						int hash = HashUtils.nameToHash(selectedFile.getName());

						int uncompressedSize = fileData.length;

						int compressedSize = bzipped.length;

						AppData.commonHashNames.put(hash, selectedFile.getName());

						ArchiveEntry entry = new ArchiveEntry(hash, uncompressedSize, compressedSize, ByteBuffer.wrap(bzipped));

						int slot = archive.indexOf(entryWrapper.getHash());

						if (slot == -1) {
							return false;
						}

						archive.getEntries().set(slot, entry);
						
						Platform.runLater(() -> {
							data.set(selectedIndex, new ArchiveEntryWrapper(entry));
						});

					} catch (IOException ex) {
						ex.printStackTrace();
					}

					saveHashes();

					byte[] encoded = archive.encode();
					
					store.writeFile(wrapper.getId(), ByteBuffer.wrap(encoded), encoded.length);
					
					updateMessage("100%");
					updateProgress(1, 1);

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			
		});

	}
	
	@FXML
	private void dumpArchive() {
		final ArchiveWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}
		
		final File dir = Dialogue.directoryChooser().showDialog(stage);

		if (dir == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					
					FileStore store = cache.getStore(0);
					
					Archive archive = Archive.decode(store.readFile(wrapper.getId()));
					
					int entries = archive.getEntries().size();
					
					for (int i = 0; i < entries; i++) {
						
						ArchiveEntry entry = archive.getEntries().get(i);
						
						ByteBuffer fileBuffer = archive.readFile(entry.getHash());

						try(FileChannel channel = new FileOutputStream(new File(dir, StringUtils.getCommonName(entry))).getChannel()) {
							channel.write(fileBuffer);
						}

						double progress = ((i + 1) / entries) * 100;
						
						updateMessage(String.format("%.2f%s", progress, "%"));
						updateProgress((i + 1), entries);
						
					}
					
					Platform.runLater(() ->	Dialogue.openDirectory("Would you like to view this file?", dir));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			
		});
	}

	@FXML
	private void dumpEntry() {
		final ArchiveWrapper fileWrapper = listView.getSelectionModel().getSelectedItem();

		if (fileWrapper == null) {
			return;
		}
		
		final List<ArchiveEntryWrapper> archiveWrappers = tableView.getSelectionModel().getSelectedItems();
		
		final File dir = Dialogue.directoryChooser().showDialog(stage);

		if (dir == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					
					for (int i = 0; i < archiveWrappers.size(); i++) {
						ArchiveEntryWrapper archiveWrapper = archiveWrappers.get(i);
						
						if (archiveWrapper == null) {
							continue;
						}
						
						FileStore store = cache.getStore(0);
												
						Archive archive = Archive.decode(store.readFile(fileWrapper.getId()));
						
						ByteBuffer fileBuffer = archive.readFile(archiveWrapper.getHash());

						try(FileChannel channel = new FileOutputStream(new File(dir, archiveWrapper.getName() + "." + archiveWrapper.getExtension())).getChannel()) {
							channel.write(fileBuffer);
						}
						
						double progress = ((double)(i + 1) / archiveWrappers.size()) * 100;
						
						updateMessage(String.format("%.2f%s", progress, "%"));
						updateProgress((i + 1), archiveWrappers.size());
						
					}
					
					Platform.runLater(() -> {
						Dialogue.openDirectory("Would you like to view this file?", dir);
					});
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			
		});

	}

	@FXML
	private void dumpIndex() {

	}

	@FXML
	private void clearIndex() {

	}

	@FXML
	private void identifyHash() {
		final ArchiveEntryWrapper wrapper = tableView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}

		Optional<String> result = Dialogue.showInput("Enter a name to try").showAndWait();

		if (result.isPresent()) {
			String name = result.get();

			int hash = HashUtils.nameToHash(name);

			if (hash == wrapper.getHash()) {
				Dialogue.showInfo("Information",
						String.format("Success! The string=%s matches the hash=%d", name, wrapper.getHash()));
			} else {
				Dialogue.showInfo("Information",
						String.format("The name=%s does not match the hash=%d", name, wrapper.getHash()));
			}
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
	
	public Stage getStage() {
		return stage;
	}
	
}
