package com.naples.facialrecognition;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;

public class IdentifyPersonController {

    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    ImageView imageView;

    HttpClient client;
    String currentPhoto;

    public IdentifyPersonController()   {
        client = HttpClient.newHttpClient();
    }

    @FXML
    protected void gotoMainMenuScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main-menu-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    protected void selectPhoto()    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if(selectedFile != null)   {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                imageView.setImage(image);
                currentPhoto = selectedFile.getAbsolutePath();
            }
            catch(FileNotFoundException e) {
                System.err.println("File not found");
            }
        }
    }

    @FXML
    protected void identifyPerson() {
        if(currentPhoto == null) return;

        String input = "{\"image\": \"" + currentPhoto.replace("\\", "/") + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/identify-person/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body().replace("\\", "\\\\");

            Gson gson = new Gson();
            TypeToken<Map<String, String>> mapType = new TypeToken<>(){};
            Map<String, String> jsonMap = gson.fromJson(body, mapType);

            if(jsonMap.get("Error").equals("None")) {
                Image image = new Image(new FileInputStream(jsonMap.get("photoPath")));
                imageView.setImage(image);
            }
            else {
                createPopupError(jsonMap.get("Error"));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPopupError(String error) {
        Stage newStage = new Stage();
        VBox comp = new VBox();
        comp.setAlignment(Pos.CENTER);
        Label errorField = new Label(error);
        comp.getChildren().add(errorField);

        Scene stageScene = new Scene(comp, 300, 100);
        newStage.setScene(stageScene);
        newStage.show();
    }
}


