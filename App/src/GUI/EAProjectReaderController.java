package GUI;

import DatabaseConnection.DBConnection;
import EA.SecondEAReader;
import Exceptions.DatabaseNotAccessibleException;
import Exceptions.EAProjectOpenFailedException;
import Integrator.DatabaseReading;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller für die GUI zum Auslesen eines EA Projekts
 */
public class EAProjectReaderController extends AnchorPane implements Initializable {
    @FXML
    private Label errorMessageLabel;

    @FXML
    private Button backButton;

    @FXML
    private ListView<String> functComponentsListView;

    @FXML
    private Button fileChooserButton;

    @FXML
    private TextField pathTextField;

    @FXML
    private Button readEAProjectButton;

    private boolean check;

    private Main application;

    void setApp(final Main app){this.application = app;}

    @FXML
    void readEAProject(ActionEvent event) {
        if(check){
            errorMessageLabel.setText("Auslesen der Daten! Bitte warten!");
            try {
                new SecondEAReader(pathTextField.getText()).readData();
                errorMessageLabel.setText("Auslesen der Daten abgeschlossen.");
            }catch(EAProjectOpenFailedException e){
                errorMessageLabel.setText("Bitte überprüfen sie den Pfad!");
            }
            List<String> componentsWithoutFmList = (new DatabaseReading()).getComponentsWithoutFailureModel();
            ObservableList<String> obsList = FXCollections.observableList(componentsWithoutFmList);
            functComponentsListView.setItems(obsList);
        }else{
            errorMessageLabel.setText("EA Projekt kann nicht ausgelesen werden, da die Datenbank nicht erreichbar ist!");
        }
    }

    @FXML
    void backToMainMenu(ActionEvent event) {
        application.gotoMainMenu();
    }

    @FXML
    void chooseFile(ActionEvent event) {
        pathTextField.setText(application.chooseFile());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            DBConnection.getInstance().testDB();
            check = true;
        }catch(DatabaseNotAccessibleException e){
            errorMessageLabel.setText("Bitte zuerst die Datenbankverbindung überprüfen!");
            check = false;
        }
    }
}
