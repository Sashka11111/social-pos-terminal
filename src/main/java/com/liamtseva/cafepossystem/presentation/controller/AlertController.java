package com.liamtseva.cafepossystem.presentation.controller;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AlertController {

  @FXML
  private Label messageLabel;
  @FXML
  private Button closeButton;

  private Stage stage;
  @FXML
  void initialize() {
    closeButton.setOnAction(event -> closeStageWithAnimation());
  }
  @FXML
  private void handleOkAction() {
    closeStageWithAnimation();
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setMessage(String message) {
    messageLabel.setText(message);
  }

  public static void showAlert( String message) {
    try {
      FXMLLoader loader = new FXMLLoader(AlertController.class.getResource("/view/alert.fxml"));
      AnchorPane root = loader.load();
      AlertController controller = loader.getController();
      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.initStyle(StageStyle.UNDECORATED);
      Scene scene = new Scene(root);
      stage.setScene(scene);
      controller.setStage(stage);
      controller.setMessage(message);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
      fadeIn.setFromValue(0.0);
      fadeIn.setToValue(1.0);
      fadeIn.play();

      stage.showAndWait();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void closeStageWithAnimation() {
    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> stage.close());
    fadeOut.play();
  }
}
