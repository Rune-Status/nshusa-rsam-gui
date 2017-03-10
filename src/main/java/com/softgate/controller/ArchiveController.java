package com.softgate.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import com.softgate.AppData;
import com.softgate.fs.binary.Archive;
import com.softgate.fs.binary.Archive.ArchiveEntry;
import com.softgate.model.ArchiveEntryWrapper;
import com.softgate.model.FileWrapper;
import com.softgate.util.CompressionUtil;
import com.softgate.util.Dialogue;
import com.softgate.util.FileUtils;
import com.softgate.util.HashUtils;
import com.softgate.util.StringUtils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public final class ArchiveController implements Initializable {

	@FXML
	private ListView<FileWrapper> listView;

	final ObservableList<ArchiveEntryWrapper> data = FXCollections.observableArrayList();

	final ObservableList<FileWrapper> indexes = FXCollections.observableArrayList();

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		hashCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
		nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		extCol.setCellValueFactory(cellData -> cellData.getValue().getExtensionProperty());
		sizeCol.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());
		iconCol.setCellValueFactory(new PropertyValueFactory<ArchiveEntryWrapper, ImageView>("image"));

		listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

			data.clear();

			if (newSelection == null) {
				return;
			}

			final File selectedFile = newSelection.getFile();
			
			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					try {

						final byte[] fileData = Files.readAllBytes(selectedFile.toPath());
						
						final Archive archive = Archive.decode(fileData);
						
						final int entries = archive.getEntries().size();
						
						for (int i = 0; i < entries; i++) {
							final ArchiveEntry entry = archive.getEntries().get(i);
							
							Platform.runLater(() -> {
								data.add(new ArchiveEntryWrapper(entry));
							});
							
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

		FilteredList<FileWrapper> filteredIndexes = new FilteredList<>(indexes, p -> true);

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

		listView.setCellFactory(new Callback<ListView<FileWrapper>, ListCell<FileWrapper>>() {
			@Override
			public ListCell<FileWrapper> call(ListView<FileWrapper> list) {
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

	private static class AttachmentListCell extends ListCell<FileWrapper> {
		@Override
		public void updateItem(FileWrapper item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setGraphic(null);
				setText(null);
			} else {
				ImageView imageView = new ImageView(AppData.indexIcon);
				setGraphic(imageView);
				setText(item.getName());
			}
		}
	}

	@FXML
	private void addArchive() {

		final List<File> selectedFiles = Dialogue.fileChooser().showOpenMultipleDialog(stage);

		if (selectedFiles == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				for (int i = 0; i < selectedFiles.size(); i++) {

					File selectedFile = selectedFiles.get(i);

					byte[] fileData = FileUtils.readFile(selectedFile);

					try {

						Archive.decode(fileData);

						Platform.runLater(() -> {
							indexes.add(new FileWrapper(selectedFile));
						});

						double progress = (double) ((i + 1) / selectedFiles.size()) * 100;

						updateMessage(String.format("%.2f%s", progress, "%"));
						updateProgress((i + 1), selectedFiles.size());

					} catch (Exception ex) {
						continue;
					}

				}

				return true;
			}

		});

	}

	@FXML
	private void createArchive() {

		Optional<String> result = Dialogue.showInput("Enter the name of this archive.").showAndWait();

		if (!result.isPresent()) {
			return;
		}

		File dir = Dialogue.directoryChooser().showDialog(stage);

		if (dir == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				Archive archive = Archive.create();

				try {
					byte[] encoded = archive.encode();

					File file = new File(dir, result.get());

					try (FileOutputStream fos = new FileOutputStream(file)) {
						fos.write(encoded);
					}

					Platform.runLater(() -> {
						indexes.add(new FileWrapper(file));
					});
					
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

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

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
					Archive archive = Archive.create();

					archive.getEntries().clear();

					byte[] encoded = archive.encode();

					try (FileOutputStream fos = new FileOutputStream(
							new File(wrapper.getFile().getParentFile(), wrapper.getName()))) {
						fos.write(encoded);
					}

					Platform.runLater(() -> {
						data.clear();
					});
					
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

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

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
				File file = new File(wrapper.getFile().getParentFile(), result.get());

				if (wrapper.getFile().renameTo(file)) {
					Platform.runLater(() -> {
						indexes.set(selectedIndex, new FileWrapper(file));
					});
					
					updateMessage("100%");
					updateProgress(1, 1);
					
				}
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

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

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
					Archive archive = Archive.decode(Files.readAllBytes(wrapper.getFile().toPath()));

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

					ArchiveEntry nEntry = new ArchiveEntry(nHash, entry.getUncompressedSize(), entry.getCompresseedSize(),
							entry.getData());

					archive.getEntries().set(slot, nEntry);
					
					Platform.runLater(() -> {
						data.set(selectedIndex, new ArchiveEntryWrapper(nEntry));
					});

					saveHashes();

					byte[] encoded = archive.encode();

					try (FileOutputStream fos = new FileOutputStream(new File(wrapper.getFile().getParentFile(), wrapper.getName()))) {
						fos.write(encoded);
					}
					
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
		try (PrintWriter writer = new PrintWriter(new FileWriter(AppData.hashResourcePath.toFile()))) {
			for (Entry<Integer, String> set : AppData.commonHashNames.entrySet()) {
				writer.println(set.getKey() + ":" + set.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void removeArchive() {
		final int selectedIndex = listView.getSelectionModel().getSelectedIndex();

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (selectedIndex == -1 || wrapper == null) {
			return;
		}

		Dialogue.OptionMessage option = new Dialogue.OptionMessage("Are you sure you want to delete this archive?",
				"This archive will be deleted permanently.");

		option.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

		Optional<ButtonType> result = option.showAndWait();

		if (result.isPresent()) {

			if (result.get() == ButtonType.YES) {
				
				createTask(new Task<Boolean>() {

					@Override
					protected Boolean call() throws Exception {
						if (wrapper.getFile().delete()) {
							Platform.runLater(() -> {
								indexes.remove(selectedIndex);
							});
							
							updateMessage("100%");
							updateProgress(1, 1);
							
						}
						return true;
					}
					
				});

			}
			
		}
	}

	@FXML
	private void addEntry() {

		List<File> selectedFiles = Dialogue.fileChooser().showOpenMultipleDialog(stage);

		if (selectedFiles == null) {
			return;
		}

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					Archive archive = Archive.decode(Files.readAllBytes(wrapper.getFile().toPath()));
					
					for (int i = 0; i< selectedFiles.size(); i++) {
						try {
							
							File file = selectedFiles.get(i);

							byte[] fileData = Files.readAllBytes(file.toPath());

							byte[] bzipped = CompressionUtil.bzip2(fileData);

							int hash = HashUtils.nameToHash(file.getName());

							int uncompressedSize = fileData.length;

							int compressedSize = bzipped.length;

							AppData.commonHashNames.put(hash, file.getName());

							ArchiveEntry entry = new ArchiveEntry(hash, uncompressedSize, compressedSize, bzipped);

							archive.getEntries().add(entry);
							
							Platform.runLater(() -> {
								data.add(new ArchiveEntryWrapper(entry));
							});
							
							double progress = ((i + 1) / selectedFiles.size()) * 100;
							
							updateMessage(Double.toString(progress) + "%");
							updateProgress((i + 1), selectedFiles.size());

						} catch (IOException e) {
							continue;
						}
					}

					saveHashes();

					byte[] encoded = archive.encode();

					try (FileOutputStream fos = new FileOutputStream(new File(wrapper.getFile().getParentFile(), wrapper.getName()))) {
						fos.write(encoded);
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			
		});

	}

	@FXML
	private void removeEntry() {

		final int selectedIndex = tableView.getSelectionModel().getSelectedIndex();

		if (selectedIndex == -1) {
			return;
		}

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
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
					Archive archive = Archive.decode(Files.readAllBytes(wrapper.getFile().toPath()));

					archive.remove(entryWrapper.getHash());

					byte[] encoded = archive.encode();

					try (FileOutputStream fos = new FileOutputStream(
							new File(wrapper.getFile().getParentFile(), wrapper.getName()))) {
						fos.write(encoded);
					}

					Platform.runLater(() -> {
						data.remove(selectedIndex);
					});
					
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

		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

		if (wrapper == null) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					Archive archive = Archive.decode(Files.readAllBytes(wrapper.getFile().toPath()));

					try {

						byte[] fileData = Files.readAllBytes(selectedFile.toPath());

						byte[] bzipped = CompressionUtil.bzip2(fileData);

						int hash = HashUtils.nameToHash(selectedFile.getName());

						int uncompressedSize = fileData.length;

						int compressedSize = bzipped.length;

						AppData.commonHashNames.put(hash, selectedFile.getName());

						ArchiveEntry entry = new ArchiveEntry(hash, uncompressedSize, compressedSize, bzipped);

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

					try (FileOutputStream fos = new FileOutputStream(
							new File(wrapper.getFile().getParentFile(), wrapper.getName()))) {
						fos.write(encoded);
					}
					
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
		final FileWrapper wrapper = listView.getSelectionModel().getSelectedItem();

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
					Archive archive = Archive.decode(Files.readAllBytes(wrapper.getFile().toPath()));
					
					int entries = archive.getEntries().size();
					
					for (int i = 0; i < entries; i++) {
						
						ArchiveEntry entry = archive.getEntries().get(i);
						
						byte[] fileData = archive.readFile(entry.getHash());
						
						try (FileOutputStream fos = new FileOutputStream(
								new File(dir, StringUtils.getCommonName(entry)))) {
							fos.write(fileData);
						}
						
						double progress = ((i + 1) / entries) * 100;
						
						updateMessage(String.format("%.2f%s", progress, "%"));
						updateProgress((i + 1), entries);
						
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
	private void dumpEntry() {
		final FileWrapper fileWrapper = listView.getSelectionModel().getSelectedItem();

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
												
						Archive archive = Archive.decode(Files.readAllBytes(fileWrapper.getFile().toPath()));
						
						byte[] fileData = archive.readFile(archiveWrapper.getHash());
						
						try (FileOutputStream fos = new FileOutputStream(
								new File(dir, archiveWrapper.getName() + "." + archiveWrapper.getExtension()))) {
							fos.write(fileData);
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

}
