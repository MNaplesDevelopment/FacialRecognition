package com.naples.facialrecognition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateDatabaseController {

    @FXML
    ImageView imageView;
    @FXML
    TextField nameTextField;
    @FXML
    TextField employeeTextField;

    private Parent root;
    private Stage stage;
    private Scene scene;

    ArrayList<String> photoPaths = new ArrayList<>();
    HttpClient client;
    int displayedPhoto = -1;

    public CreateDatabaseController()  {
        client = HttpClient.newHttpClient();
    }

    @FXML
    protected void addPhoto()   {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        //File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        List<File> files = fileChooser.showOpenMultipleDialog(imageView.getScene().getWindow());

        for(int i = 0; i < files.size(); i++) {
            File selectedFile = files.get(i);
            if (selectedFile != null) {
                try {
                    Image image = new Image(new FileInputStream(selectedFile));
                    imageView.setImage(image);
                    String currentPhotoPath = selectedFile.getAbsolutePath();
                    photoPaths.add(currentPhotoPath);
                    displayedPhoto = photoPaths.size() - 1;
                } catch (FileNotFoundException e) {
                    System.err.println("File not found");
                }
            }
        }
    }

    @FXML
    protected void submitEntry()   {
        String name = nameTextField.getText();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"images\": [");
        for(String s: photoPaths)   {
            sb.append("\"").append(s.replace("\\", "/")).append("\",\n");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("],\n\"name\": \"").append(name).append("\",\n");
        sb.append("\"employeeID\": ").append(employeeTextField.getText()).append("\n}");
        String input = sb.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/encode-image/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        clearFields();
    }

    @FXML
    protected void nextPhoto()  {
        if(displayedPhoto < photoPaths.size() - 1)  {
            displayedPhoto++;
            updateDisplayedPhoto();
        }
    }

    @FXML
    protected void previousPhoto()  {
        if(displayedPhoto > 0)  {
            displayedPhoto--;
            updateDisplayedPhoto();
        }
    }

    @FXML
    protected void gotoMainMenuScene(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("main-menu-view.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void updateDisplayedPhoto() {
        Image image = new Image(photoPaths.get(displayedPhoto));
        imageView.setImage(image);
    }

    private void clearFields()  {
        displayedPhoto = -1;
        photoPaths.clear();
        imageView.setImage(null);
        nameTextField.clear();
        employeeTextField.clear();
    }
}


//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//
//            String output;
//            System.out.println("Output from Server .... \n");
//            while ((output = br.readLine()) != null) {
//                System.out.println(output);
//            }
