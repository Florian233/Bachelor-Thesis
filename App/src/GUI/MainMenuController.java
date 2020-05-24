package GUI;

import DatabaseConnection.DBConnection;
import Exceptions.DatabaseNotAccessibleException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für das Hauptmenü. Auswahl zwischen EA, ESSaRel auslesen , Datenbank und Integration
 */
public class MainMenuController extends AnchorPane implements Initializable {


    @FXML
    private Button integrationButton;

    @FXML
    private Button clearDatabaseButton;

    @FXML
    private Label databaseAdressOutput;

    @FXML
    private Label clearDatabaseOutput;

    @FXML
    private Button DatabaseButton;

    @FXML
    private Button readESSaRelProjectButton;

    @FXML
    private Label databaseUsernameOutput;

    @FXML
    private Label batabasePasswortOutput;

    @FXML
    private Button readEAProjectButton;

    private Main application;

    private DBConnection db = DBConnection.getInstance();

    void setApp(final Main app){this.application = app;}


    @FXML
    void setDatabase(ActionEvent event) {
        application.gotoSetDatabaseConnection();
    }

    @FXML
    void readEAProject(ActionEvent event) {
        application.gotoEAProjectReader();
    }

    @FXML
    void readESSaRelProject(ActionEvent event) {
        application.gotoESSaRelProjectReader();
    }

    @FXML
    void integrateModels(ActionEvent event) {
        application.gotoIntegrationMenu();
    }

    @FXML
    void clearDatabase(ActionEvent event) {
        try {
            db.testDB();
            db.clearDB();
            clearDatabaseOutput.setText("Die Datenbank wurde geleert!");
        }catch(DatabaseNotAccessibleException e){
            clearDatabaseOutput.setText("Datenbank nicht erreichbar.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseAdressOutput.setText(db.getUrl());
        databaseUsernameOutput.setText(db.getUsername());
        batabasePasswortOutput.setText(db.getPassword());
        try{
            db.testDB();
            clearDatabaseOutput.setText("Datenbank erreichbar!");
        }catch(DatabaseNotAccessibleException e){
            clearDatabaseOutput.setText("Bitte die Datenbankverbindung überprüfen!");
        }
    }
}
