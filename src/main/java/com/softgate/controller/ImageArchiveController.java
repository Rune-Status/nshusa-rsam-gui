package com.softgate.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.softgate.fs.binary.Archive;
import com.softgate.fs.binary.Archive.ArchiveEntry;
import com.softgate.fs.binary.ImageArchive;
import com.softgate.fs.binary.Sprite;
import com.softgate.util.Dialogue;
import com.softgate.util.HashUtils;
import com.softgate.util.StringUtils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

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
				
				this.imageView.setImage(imageView.getImage());
				
			}
			
		});
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
					
					Archive archive = Archive.decode(Files.readAllBytes(archiveFile.toPath()));
					
					for (int i = 0; i < archive.getEntries().size(); i++) {
						ArchiveEntry entry = archive.getEntries().get(i);
						
						int indexHash = HashUtils.nameToHash("index.dat");
						
						if (entry.getHash() == indexHash) {
							continue;
						}
						
						TreeItem<String> parent = new TreeItem<String>(StringUtils.getCommonName(entry));
						
						List<Sprite> sprites = ImageArchive.decode(ByteBuffer.wrap(archive.readFile(entry.getHash())), ByteBuffer.wrap(archive.readFile("index.dat")), true);
						
						for (int frame = 0; frame < sprites.size(); frame++) {
							Sprite sprite = sprites.get(frame);
							
							Image image = SwingFXUtils.toFXImage(sprite.toBufferedImage(), null);
							
							parent.getChildren().add(new TreeItem<String>(i + "_" + frame, new ImageView(image)));
						}

						double progress = ((double)(i + 1) / archive.getEntries().size()) * 100;
						
						updateMessage(String.format("%.2f%s", progress, "%"));
						updateProgress((i + 1), archive.getEntries().size());
						
						Platform.runLater(() -> {
							treeView.getRoot().getChildren().add(parent);
						});	
						
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
	private void dumpAll() {
		if (treeView.getRoot().getChildren().isEmpty()) {
			return;
		}
		
		final File dir = Dialogue.directoryChooser().showDialog(stage);
		
		if (dir == null) {
			return;
		}	
		
		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				
				final int size = treeView.getRoot().getChildren().size();
				
				for (int i = 0; i < size; i++) {
					
					TreeItem<String> archive = treeView.getRoot().getChildren().get(i);
					
					for (TreeItem<String> entry : archive.getChildren()) {
						
						ImageView imageView = (ImageView) entry.getGraphic();
						
						try {
						
							ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), "png", new File(dir, entry.getValue() + ".png"));
						} catch (IOException e) {					
							e.printStackTrace();
							continue;
						}
						
					}
					
					double progress = ((double)(i + 1) / size) * 100;
					
					updateProgress((i + 1), size);
					updateMessage(String.format("%.2f%s", progress, "%"));
					
				}
				
				Platform.runLater(() -> {
					Dialogue.openDirectory("Would you like to view these files?", dir);
				});

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
