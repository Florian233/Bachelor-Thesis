package GUI;

import DatabaseConnection.NameIDQueryResult;
import Integrator.DatabaseReading;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controllerklasse für die GUI zum Zusammenführen zweier Fehlermodelle.
 * Dient zur Auswahl der zusammenzufügenden Komponenten.
 */
public class CombineCFTsController extends AnchorPane implements Initializable {

    private Main application;

    private Stage dialog;

    @FXML
    private Button backButton;

    @FXML
    private Button mergeFMsButton;

    @FXML
    private ListView<NameIDQueryResult> componentsListView;

    @FXML
    private ListView<NameIDQueryResult> failureModelListView;

    private Map<NameIDQueryResult,ObservableList<NameIDQueryResult>> fmLogicalComponentMap = new HashMap<>();

    private ObservableList<NameIDQueryResult> logicalComponentsObsList = FXCollections.observableArrayList();


    @FXML
    void componentListViewClicked(MouseEvent event) {
        if(componentsListView.getSelectionModel().getSelectedItem() != null){
            failureModelListView.setItems(fmLogicalComponentMap.get(componentsListView.getSelectionModel().getSelectedItem()));
        }
    }

    @FXML
    void backToIntegrationMenu(ActionEvent event) {
        application.gotoIntegrationMenu();
    }

    @FXML
    void mergeFMs(ActionEvent event) {
        if(componentsListView.getSelectionModel().getSelectedItem() != null) {
            MergeCFTsController mergeCFTsController = null;
            try {
                mergeCFTsController = (MergeCFTsController) createCFTMergingView();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mergeCFTsController != null) {
                mergeCFTsController.setParentController(this);
                List<NameIDQueryResult> fmList = fmLogicalComponentMap.get(componentsListView.getSelectionModel().getSelectedItem());
                mergeCFTsController.setFDMs(fmList.get(0),fmList.get(1));
                mergeCFTsController.setHostServices(application.getHostServices());
                dialog.show();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        componentsListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        failureModelListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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
        List<NameIDQueryResult> logicalComponentWith2FMList = readDB.getLogicalComponentsWith2FailureModels();
        logicalComponentsObsList.addAll(logicalComponentWith2FMList);
        componentsListView.setItems(logicalComponentsObsList);

        logicalComponentWith2FMList.stream().forEach(component -> {
            List<NameIDQueryResult> failureModelsList = readDB.getCFTsOfLogicalComponent(component.getId());
            ObservableList<NameIDQueryResult> obsList = FXCollections.observableList(failureModelsList);
            fmLogicalComponentMap.put(component,obsList);
        });

        componentsListView.getSelectionModel().select(0);
        if(componentsListView.getSelectionModel().getSelectedItem() != null){
            failureModelListView.setItems(fmLogicalComponentMap.get(componentsListView.getSelectionModel().getSelectedItem()));
        }


    }

    public void setApp(Main app) {this.application = app;}

    private Initializable createCFTMergingView() throws IOException {
        String fxml = "MergeCFTs.fxml";
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(Main.stage);
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Main.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in); //Auch wenn IntelliJ sagt der Cast ist redundant, das muss bleiben!
        } finally {
            in.close();
        }
        Scene scene = new Scene(page, 800, 600);
        dialog.setScene(scene);
        dialog.sizeToScene();
        return (Initializable)loader.getController();

    }

    public void closeWindow(final boolean success){
        if(success){
            logicalComponentsObsList.remove(componentsListView.getSelectionModel().getSelectedItem());
            componentsListView.setItems(logicalComponentsObsList);
            if(componentsListView.getSelectionModel().getSelectedItem() != null){
                failureModelListView.setItems(fmLogicalComponentMap.get(componentsListView.getSelectionModel().getSelectedItem()));
            }
        }
        dialog.close();
        dialog = null;
    }
}
