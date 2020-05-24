package GUI;

import DatabaseConnection.DBConnection;
import DatabaseConnection.NameIDQueryResult;
import FTElemente.VerbindungsTyp;
import Integrator.DatabaseReading;
import Integrator.Matching.Match;
import Integrator.Matching.TypeMatcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller für die GUI zum Verbinden von Ports mit den zugehörigen Fehlertypen
 */
public class MatchPortsToFailureTypesController extends AnchorPane implements Initializable {

    private Map<NameIDQueryResult,ObservableList<NameIDQueryResult>> fmComponentToPortMap = new HashMap<>();

    private Map<NameIDQueryResult,NameIDQueryResult> matchesMap = new HashMap<>();

    private NameIDQueryResult selectedType;

    private NameIDQueryResult selectedPort;

    private Main application;

    @FXML
    private Button mappingButton;

    @FXML
    private ListView<NameIDQueryResult> logicalComponentsListView;

    @FXML
    private ListView<NameIDQueryResult> failureTypeListView;

    @FXML
    private Button backButton;

    @FXML
    private Label typeLabel;

    @FXML
    private Label portLabel;

    @FXML
    private ListView<NameIDQueryResult> portListView;

    private NameIDQueryResult noType = new NameIDQueryResult("Es konnte kein Typ ermittel werden",-1);

    @FXML
    void mapPortToFailureType(ActionEvent event) {
        if(portListView.getSelectionModel().getSelectedItem() != null && failureTypeListView.getSelectionModel().getSelectedItem() != null && failureTypeListView.getSelectionModel().getSelectedItem() != noType) {
            int idport = portListView.getSelectionModel().getSelectedItem().getId();
            int idfailureType = failureTypeListView.getSelectionModel().getSelectedItem().getId();
            DBConnection.getInstance().createRelationship(idport, idfailureType, VerbindungsTyp.FailureTypeOf);
            //Element aus der Liste löschen
            ObservableList<NameIDQueryResult> list = fmComponentToPortMap.get(logicalComponentsListView.getSelectionModel().getSelectedItem());
            list.remove(portListView.getSelectionModel().getSelectedItem());
            if(!list.isEmpty()) {
                portListView.setItems(list);
                portListView.getSelectionModel().select(0);
                if (portListView.getSelectionModel().getSelectedItem() != null) {
                    failureTypeListView.getSelectionModel().select(matchesMap.get(portListView.getSelectionModel().getSelectedItem()));
                    portLabel.setText(portListView.getSelectionModel().getSelectedItem().getName());
                    if(failureTypeListView.getSelectionModel().getSelectedItem() != null)typeLabel.setText(failureTypeListView.getSelectionModel().getSelectedItem().getName());
                } else {
                    portLabel.setText("");
                    typeLabel.setText("");
                }
            }else{
                ObservableList<NameIDQueryResult> logicalComponentsFMList = logicalComponentsListView.getItems();
                logicalComponentsFMList.remove(logicalComponentsListView.getSelectionModel().getSelectedItem());
                logicalComponentsListView.setItems(logicalComponentsFMList);
                logicalComponentsListView.getSelectionModel().select(0);
                if(logicalComponentsListView.getSelectionModel().getSelectedItem() != null){
                    NameIDQueryResult selecteditem = logicalComponentsListView.getSelectionModel().getSelectedItem();
                    ObservableList<NameIDQueryResult> portsObsList = fmComponentToPortMap.get(selecteditem);
                    portListView.setItems(portsObsList);
                    portListView.getSelectionModel().select(0);
                    if(portListView.getSelectionModel().getSelectedItem() != null){
                        NameIDQueryResult selected = portListView.getSelectionModel().getSelectedItem();
                        failureTypeListView.getSelectionModel().select(matchesMap.get(selected));
                    }
                }
            }
        }
    }

    @FXML
    void backToIntegrationMenu(ActionEvent event) {
        application.gotoIntegrationMenu();
    }

    @FXML
    void portClicked(MouseEvent event) {
        if(portListView.getSelectionModel().getSelectedItem()!=null) {
            portLabel.setText(portListView.getSelectionModel().getSelectedItem().getName());
            failureTypeListView.getSelectionModel().select(matchesMap.get(portListView.getSelectionModel().getSelectedItem()));
            if(failureTypeListView.getSelectionModel().getSelectedItem() != null)typeLabel.setText(failureTypeListView.getSelectionModel().getSelectedItem().getName());
        }
    }

    @FXML
    void failureTypeClicked(MouseEvent event) {
        if(failureTypeListView.getSelectionModel().getSelectedItem() != null) {
            typeLabel.setText(failureTypeListView.getSelectionModel().getSelectedItem().getName());
        }
    }

    @FXML
    void componentFmListViewClicked(MouseEvent event) {
        if(logicalComponentsListView.getSelectionModel().getSelectedItem() != null){
            portListView.setItems(fmComponentToPortMap.get(logicalComponentsListView.getSelectionModel().getSelectedItem()));
            portListView.getSelectionModel().select(0);
            if(portListView.getSelectionModel().getSelectedItem() != null){
                failureTypeListView.getSelectionModel().select(matchesMap.get(portListView.getSelectionModel().getSelectedItem()));
                portLabel.setText(portListView.getSelectionModel().getSelectedItem().getName());
                typeLabel.setText(failureTypeListView.getSelectionModel().getSelectedItem().getName());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        portListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        failureTypeListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        logicalComponentsListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

            @Override
            public ListCell<NameIDQueryResult> call(ListView<NameIDQueryResult> param) {
                ListCell<NameIDQueryResult> cell3 = new ListCell<NameIDQueryResult>() {

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
                return cell3;
            }
        });

        DatabaseReading read = new DatabaseReading();

        List<NameIDQueryResult> cAFm = read.getAllFmAndComponents();
        ObservableList<NameIDQueryResult> componentsAndFMs = FXCollections.observableArrayList(cAFm);

        List<NameIDQueryResult> failuretypes = read.getFailureTypes();
        ObservableList<NameIDQueryResult> failureTypesObsList = FXCollections.observableArrayList(failuretypes);
        failureTypesObsList.add(noType);
        failureTypeListView.setItems(failureTypesObsList);

        //StringMatcher stringMatcher = new StringMatcher();
        TypeMatcher typeMatcher = new TypeMatcher();
        cAFm.stream().forEach(e -> {
            int a = -1;
            if (e != null) {
                a = e.getId();
            }
            List<NameIDQueryResult> ports = read.getPortsOfFmOrComponentWithoutFailureType(a);
            if(ports.isEmpty()){//leere Listen brauchen nicht betrachtet werden
                componentsAndFMs.remove(e);
            }else {
                ObservableList<NameIDQueryResult> portsObsList = FXCollections.observableList(ports);
                fmComponentToPortMap.put(e, portsObsList);
                //Übereinstimmungen berechnen
                //List<Match> matchList = stringMatcher.matchStrings(ports,failuretyes);
                List<Match> matchList = typeMatcher.matchPortToType(ports, failuretypes);
                matchList.stream().forEach(match -> {
                    if(match.getB() != -1) {
                        matchesMap.put(match.getAA(), match.getBB());
                        matchesMap.put(match.getBB(), match.getAA());
                    }else{
                        matchesMap.put(match.getAA(),noType);
                    }
                });
            }

        });

        logicalComponentsListView.setItems(componentsAndFMs);

        logicalComponentsListView.getSelectionModel().select(0);
        if(logicalComponentsListView.getSelectionModel().getSelectedItem() != null){
            NameIDQueryResult selecteditem = logicalComponentsListView.getSelectionModel().getSelectedItem();
            ObservableList<NameIDQueryResult> portsObsList = fmComponentToPortMap.get(selecteditem);
            portListView.setItems(portsObsList);
            portListView.getSelectionModel().select(0);
            if(portListView.getSelectionModel().getSelectedItem() != null){
                NameIDQueryResult selected = portListView.getSelectionModel().getSelectedItem();
                failureTypeListView.getSelectionModel().select(matchesMap.get(selected));
                portLabel.setText(portListView.getSelectionModel().getSelectedItem().getName());
                typeLabel.setText(failureTypeListView.getSelectionModel().getSelectedItem().getName());
            }
        }
    }

    public void setApp(Main app) {
        this.application = app;
    }
}
