package io.nshusa.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import io.nshusa.rsam.IndexedFileSystem;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.binary.sprite.Sprite;
import io.nshusa.rsam.util.HashUtils;
import io.nshusa.util.Dialogue;
import io.nshusa.util.StringUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.nshusa.rsam.binary.Archive.ArchiveEntry;

public class ImageArchiveController implements Initializable {

	private Stage stage;

	@FXML
	private TreeView<String> treeView;

	@FXML
	private ImageView imageView;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Text progressText;

	public IndexedFileSystem cache;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		treeView.setRoot(new TreeItem<>("Root"));

		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

			if (newSelection == null) {
				return;
			}

			if (newSelection.isLeaf()) {

				ImageView imageView = (ImageView) newSelection.getGraphic();

				if (imageView == null) {
					return;
				}

				ContextMenu contextMenu = new ContextMenu();

				MenuItem exportMI = new MenuItem("Export");
				exportMI.setOnAction(e -> dump());

				contextMenu.getItems().add(exportMI);

				treeView.setContextMenu(contextMenu);

				this.imageView.setImage(imageView.getImage());

			} else {

				ContextMenu contextMenu = new ContextMenu();

				MenuItem exportMI = new MenuItem("Export");
				exportMI.setOnAction(e -> dump());

				contextMenu.getItems().add(exportMI);

				treeView.setContextMenu(contextMenu);

			}

		});
	}

	public void initImageArchive(String name, Archive archive) {
		
		Optional<TreeItem<String>> result = treeView.getRoot().getChildren().stream().filter(it -> it.getValue().equalsIgnoreCase(name)).findFirst();
		
		if (result.isPresent()) {
			return;
		}
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				TreeItem<String> root = new TreeItem<>(name);

				Platform.runLater(() ->	treeView.getRoot().getChildren().add(root));

				for (int i = 0; i < archive.getEntries().size(); i++) {
					ArchiveEntry entry = archive.getEntries().get(i);

					int indexHash = HashUtils.nameToHash("index.dat");

					if (entry.getHash() == indexHash) {
						continue;
					}

					TreeItem<String> parent = new TreeItem<String>(StringUtils.getCommonName(entry));

					final List<BufferedImage> bImages = new ArrayList<>();

					if (entry.getHash() == HashUtils.nameToHash("title.dat")) {
						decodeTitle(archive, entry, bImages);
					} else if (entry.getHash() == HashUtils.nameToHash("runes.dat")) {
						decodeRunes(archive, entry, bImages);
					} else {
						decodeNormal(archive, entry, bImages);
					}

					for (int frame = 0; frame < bImages.size(); frame++) {
						BufferedImage bImage = bImages.get(frame);

						Image image = SwingFXUtils.toFXImage(bImage, null);

						parent.getChildren().add(new TreeItem<String>(Integer.toString(frame), new ImageView(image)));
					}

					double progress = ((double) (i + 2) / archive.getEntries().size()) * 100;

					updateMessage(String.format("%.2f%s", progress, "%"));
					updateProgress((i + 1), archive.getEntries().size() - 1);

					root.getChildren().add(parent);

				}
				return true;
			}

		});
	}

	private void decodeNormal(Archive archive, ArchiveEntry entry, List<BufferedImage> bImages) {
		for(int i = 0;; i++) {
			try {
				bImages.add(Sprite.decode(archive, entry.getHash(), i).toBufferedImage());
			} catch (Exception ex) {
				break;
			}
		}
	}

	private void decodeRunes(Archive archive, ArchiveEntry entry, List<BufferedImage> bImages) {
		for (int i = 0; i < 16; i++) {
			try {
				bImages.add(Sprite.decode(archive, entry.getHash(), i).toBufferedImage());
			} catch (IOException e) {
				break;
			}
		}

	}

	private void decodeTitle(Archive archive, ArchiveEntry entry, List<BufferedImage> bImages) {
			try {
				ByteBuffer dataBuf = archive.readFile("title.dat");

				try(InputStream is = new ByteArrayInputStream(dataBuf.array())) {
					bImages.add(ImageIO.read(is));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@FXML
	private void unpack() {
		File archiveFile = Dialogue.fileChooser().showOpenDialog(stage);

		if (archiveFile == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {

					Archive archive = Archive.decode(ByteBuffer.wrap(Files.readAllBytes(archiveFile.toPath())));

					for (int i = 0; i < archive.getEntries().size(); i++) {
						ArchiveEntry entry = archive.getEntries().get(i);

						int indexHash = HashUtils.nameToHash("index.dat");

						if (entry.getHash() == indexHash) {
							continue;
						}

						TreeItem<String> parent = new TreeItem<String>(StringUtils.getCommonName(entry));

						List<BufferedImage> bImages = new ArrayList<>();

						if (entry.getHash() == HashUtils.nameToHash("title.dat")) {
							decodeTitle(archive, entry, bImages);
						} else {
							decodeNormal(archive, entry, bImages);
						}

						for (int frame = 0; frame < bImages.size(); frame++) {
							BufferedImage bImage = bImages.get(frame);

							Image image = SwingFXUtils.toFXImage(bImage, null);

							parent.getChildren().add(new TreeItem<String>(i + "_" + frame, new ImageView(image)));
						}

						double progress = ((double) (i + 1) / archive.getEntries().size()) * 100;

						updateMessage(String.format("%.2f%s", progress, "%"));
						updateProgress((i + 1), archive.getEntries().size() - 1);

						Platform.runLater(() -> treeView.getRoot().getChildren().add(parent));

					}

				} catch (Exception ex) {
					ex.printStackTrace();
					return false;
				}
				return true;
			}

		});

	}

	@FXML
	private void dump() {
		TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();

		if (selected == null) {
			return;
		}

		final File dir = Dialogue.directoryChooser().showDialog(stage);

		if (dir == null) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				if (selected.isLeaf()) {

					ImageView imageView = (ImageView) selected.getGraphic();

					try {
						ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png",
								new File(dir, selected.getValue() + ".png"));

						updateProgress(1, 1);
						updateMessage("100%");

						Platform.runLater(() -> {
							Dialogue.openDirectory("Would you like to view this image?", dir);
						});

					} catch (IOException e) {
						e.printStackTrace();
					}

				} else if (selected.getParent().getValue().equalsIgnoreCase("Root")) {

					System.out.println("detected main image archive");
					
					File mainFolder = new File(dir, selected.getValue());
					
					if (!mainFolder.exists()) {
						mainFolder.mkdirs();
					}
					
					final int size = selected.getChildren().size();
					
					for (int i = 0; i < size; i++) {
						
						TreeItem<String> archiveTI = selected.getChildren().get(i);
						
						String name = archiveTI.getValue().contains(".") ? archiveTI.getValue().substring(0, archiveTI.getValue().lastIndexOf(".")) : archiveTI.getValue();

						File subFolder = new File(mainFolder, name);

						if (!subFolder.exists()) {
							subFolder.mkdirs();
						}
						
						try {
							for (int frame = 0; frame < archiveTI.getChildren().size(); frame++) {
								TreeItem<String> child = archiveTI.getChildren().get(frame);

								ImageView imageView = (ImageView) child.getGraphic();

								ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png",
										new File(subFolder, child.getValue() + ".png"));

							}							
						} catch (IOException e) {
							e.printStackTrace();
						}	
						
						double progress = ((double)(i + 1) / selected.getChildren().size()) * 100;
						
						updateProgress((i + 1), selected.getChildren().size());
						updateMessage(String.format("%.2f%s", progress, "%"));
						
					}
					
					Platform.runLater(() -> {
						Dialogue.openDirectory("Would you like to view these images?", mainFolder);
					});

				} else {

					File mainFolder = new File(dir, selected.getParent().getValue());

					if (!mainFolder.exists()) {
						mainFolder.mkdirs();
					}
					
					String name = selected.getValue().contains(".") ? selected.getValue().substring(0, selected.getValue().lastIndexOf(".")) : selected.getValue();

					File subFolder = new File(mainFolder, name);

					if (!subFolder.exists()) {
						subFolder.mkdirs();
					}
					try {
						for (int i = 0; i < selected.getChildren().size(); i++) {
							TreeItem<String> child = selected.getChildren().get(i);

							ImageView imageView = (ImageView) child.getGraphic();

							ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png",
									new File(subFolder, child.getValue() + ".png"));

							
							double progress = ((double)(i + 1) / selected.getChildren().size()) * 100;
							
							updateProgress((i + 1), selected.getChildren().size());
							updateMessage(String.format("%.2f%s", progress, "%"));

						}
						
						Platform.runLater(() -> {
							Dialogue.openDirectory("Would you like to view this image?", subFolder);
						});

					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				return true;
			}

		});

	}

	@FXML
	private void clear() {
		treeView.getRoot().getChildren().clear();
		imageView.setImage(null);
	}

	private double xOffset, yOffset;

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

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
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
