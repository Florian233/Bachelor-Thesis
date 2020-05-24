package GUI;

import DatabaseConnection.DBConnection;
import Exceptions.DatabaseNotAccessibleException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controllerklasse für die GUI zum Setzen der Datenbankverbindung
 */
public class SetDatabaseConnectionController extends AnchorPane implements Initializable {
    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField adressTextField;

    @FXML
    private Label errorMessageLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button insertConnectionDataButton;

    @FXML
    private TextField usernameTextField;

    private Main application;

    private DBConnection db = DBConnection.getInstance();

    void setApp(final Main app){this.application = app;}

    @FXML
    void insertConnection(ActionEvent event) {
        db.setURL(adressTextField.getText());
        db.setPassword(passwordTextField.getText());
        db.setUsername(usernameTextField.getText());
        errorMessageLabel.setText("Setzen der Datenbankverbindung abgeschlossen. Verbinden zu der Datenbank!");
        try {
            db.connectToDatabase();
            application.gotoMainMenu();
        }catch(DatabaseNotAccessibleException e){
            errorMessageLabel.setText("Bitte die Daten überprüfen. Keine Verbindung möglich.");
        }
    }

    @FXML
    void backToMainMenu(ActionEvent event) {

        try {
            db.testDB();
            application.gotoMainMenu();
        }catch(DatabaseNotAccessibleException e){
            errorMessageLabel.setText("Bitte unbedingt zuerst die Datenbankverbindung setzen!");
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adressTextField.setText(db.getUrl());
        usernameTextField.setText(db.getUsername());
        passwordTextField.setText(db.getPassword());
    }
}
