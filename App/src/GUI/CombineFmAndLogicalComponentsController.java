package GUI;

import DatabaseConnection.NameIDQueryResult;
import Integrator.DatabaseReading;
import Integrator.Matching.Match;
import Integrator.Matching.StringMatcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controllerklasse für die GUI zum Zusammenfügen einer funktionalen Komponente und eines Fehlermodells.
 * Dient zur Auswahl der Komponenten.
 */
public class CombineFmAndLogicalComponentsController extends AnchorPane implements Initializable {

    private Map<NameIDQueryResult,List<NameIDQueryResult>> logicalComponentToFMMap = new HashMap<>();

    @FXML
    private ListView<NameIDQueryResult> cftListView;

    private ObservableList<NameIDQueryResult> fmObsList;

    @FXML
    private Button portMatchButton;

    @FXML
    private ListView<NameIDQueryResult> componentListView;

    @FXML
    private Label fmLabel;

    @FXML
    private Button backButton;

    @FXML
    private Label componentLabel;

    private StringMatcher stringMatcher = new StringMatcher();

    private Main application;

    private Stage dialog;

    void setApp(final Main app){this.application = app;}

    @FXML
    void backToIntegrationMenu(ActionEvent event) {
        application.gotoIntegrationMenu();
    }

    @FXML
    void matchPorts(ActionEvent event) {

        if(componentListView.getSelectionModel().getSelectedItem() != null && cftListView.getSelectionModel().getSelectedItem() != null) {
            PortMatchingController portMatchingController = null;
            try {
                portMatchingController = (PortMatchingController) createPortMatchingView();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (portMatchingController != null) {
                portMatchingController.setParentController(this);

                portMatchingController.setCFT(cftListView.getSelectionModel().getSelectedItem());
                portMatchingController.setComponentData(componentListView.getSelectionModel().getSelectedItem());
                portMatchingController.start();
                dialog.show();
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cftListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

            @Override
            public ListCell<NameIDQueryResult> call(ListView<NameIDQueryResult> param) {
                ListCell<NameIDQueryResult> cell = new ListCell<NameIDQueryResult>() {

                    @Override
                    protected void updateItem(NameIDQueryResult item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName());
                        } else {
                            setText("");
                        }
                    }
                };
                return cell;
            }
        });

        componentListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

            @Override
            public ListCell<NameIDQueryResult> call(ListView<NameIDQueryResult> param) {
                ListCell<NameIDQueryResult> cell2 = new ListCell<NameIDQueryResult>() {

                    @Override
                    protected void updateItem(NameIDQueryResult item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName());
                        } else {
                            setText("");
                        }
                    }
                };
                return cell2;
            }
        });

        DatabaseReading readDB = new DatabaseReading();
        List<NameIDQueryResult> logicalComponentsList = readDB.getAllComponents();
        ObservableList<NameIDQueryResult> logicalComponentsObsList = FXCollections.observableList(logicalComponentsList);
        componentListView.setItems(logicalComponentsObsList);

        List<NameIDQueryResult> fmWithoutFailureTraceList = readDB.getAllCFTsWithoutFailureModeTrace();
        fmObsList = FXCollections.observableList(fmWithoutFailureTraceList);
        cftListView.setItems(fmObsList);

        componentListView.getSelectionModel().select(0);
        if(componentListView.getSelectionModel().getSelectedItem() != null){
            Match m = stringMatcher.matchSingleString(componentListView.getSelectionModel().getSelectedItem(),logicalComponentsList);
            cftListView.getSelectionModel().select(m.getBB());
            componentLabel.setText(componentListView.getSelectionModel().getSelectedItem().getName());
            fmLabel.setText(cftListView.getSelectionModel().getSelectedItem().getName());
        }


    }

    private Initializable createPortMatchingView() throws IOException{
        String fxml = "PortMatching.fxml";
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(Main.stage);
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Main.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        }
        Scene scene = new Scene(page, 800, 600);
        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.setTitle("Ports der logischen Komponente auf Ports des  Fehlermodells abbilden");
        return (Initializable)loader.getController();
    }

    public void closeWindow(final boolean success){
        if(success){
            fmObsList.remove(cftListView.getSelectionModel().getSelectedItem());
            cftListView.setItems(fmObsList);
        }
        dialog.close();
        dialog = null;
    }

    @FXML
    void componentClicked(MouseEvent event) {
        if(componentListView.getSelectionModel().getSelectedItem()!= null){
            Match m = stringMatcher.matchSingleString(componentListView.getSelectionModel().getSelectedItem(),fmObsList);
            cftListView.getSelectionModel().select(m.getBB());
            componentLabel.setText(componentListView.getSelectionModel().getSelectedItem().getName());
            if(cftListView.getSelectionModel().getSelectedItem() != null)fmLabel.setText(cftListView.getSelectionModel().getSelectedItem().getName());
        }
    }

    @FXML
    void fmClicked(MouseEvent event) {
        if(cftListView.getSelectionModel().getSelectedItem() != null){
            fmLabel.setText(cftListView.getSelectionModel().getSelectedItem().getName());
        }
    }
}
