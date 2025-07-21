package com.naples.facialrecognition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
import java.util.HashMap;
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
        String input = "{\"image\": \"" + currentPhoto.replace("\\", "/") + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/identify-person/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            HashMap<String, String> jsonMap = jsonToMap(response.body());
            Image image = new Image(new FileInputStream(jsonMap.get("photoPath")));
            imageView.setImage(image);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //Replace with a proper JSON parser like Gson
    private HashMap<String, String> jsonToMap(String jsonString)    {
        HashMap<String, String> map = new HashMap<>();
        int[] indexes = new int[4];
        int j = 0;
        for(int i = 0; i < jsonString.length(); i++)    {
            if(jsonString.charAt(i) == '"') {
                indexes[j] = i;
                j++;
            }
        }
        String field = jsonString.substring(indexes[0]+1, indexes[1]);
        String value = jsonString.substring(indexes[2]+1, indexes[3]);
        map.put(field, value);
        return map;
    }
}
