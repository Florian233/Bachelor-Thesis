package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für das Auswahlmenü zur Integration
 */
public class IntegrationMenuController extends AnchorPane implements Initializable {

    @FXML
    private Button combineCFTsButton;

    @FXML
    private Button backButton;

    @FXML
    private Button combineFmAndLogicalComponentButton;

    @FXML
    private Button mapPortsToFailureTypesButton;

    private Main application;

    void setApp(final Main app){this.application = app;}

    @FXML
    void combineFmAndLogicalComponent(ActionEvent event)throws IOException {
        application.gotoCombineFmAndLogicalComponents();
    }

    @FXML
    void mapPortsToFailureTypes(ActionEvent event) {
        application.gotoMatchPortsToFailureTypes();
    }

    @FXML
    void combineCFTs(ActionEvent event) {
        application.gotoCombineCFTs();
    }

    @FXML
    void backToMainMenu(ActionEvent event) {
        application.gotoMainMenu();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
