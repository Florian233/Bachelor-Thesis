package GUI;

import DatabaseConnection.DBConnection;
import Essarel.EssarelReader;
import Exceptions.DatabaseNotAccessibleException;
import Exceptions.ESSaRelProjectOpenFailedException;
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
 * Controller für die GUI zum Auslesen eines ESSaRel Projekts
 */
public class ESSaRelProjectReaderController extends AnchorPane implements Initializable {

    @FXML
    private Label errorMessageLabel;

    @FXML
    private TextField pathTextField;

    @FXML
    private Button backButton;

    @FXML
    private Button readESSaRelProjectButton;

    @FXML
    private Button fileChooserButton;

    @FXML
    private ListView<String> displayResultListView;

    private Main application;

    private boolean check;

    void setApp(final Main app){this.application = app;}

    @FXML
    void readESSaRelProject(ActionEvent event) {
        if(check){
            try {
                errorMessageLabel.setText("Auslesen der Daten! Bitte warten!");
                List<String> list = new EssarelReader(pathTextField.getText()).readDataWithReturntype();
                errorMessageLabel.setText("Auslesen der Daten abgeschlossen.");
                ObservableList<String> obsList = FXCollections.observableList(list);
                displayResultListView.setItems(obsList);
            }catch(ESSaRelProjectOpenFailedException e){
                errorMessageLabel.setText("Bitte überprüfen sie den Pfad!");
            }
        }else{
            errorMessageLabel.setText("ESSaRel Projekt kann nicht ausgelesen werden, da die Datenbank nicht erreichbar ist!");
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
