package GUI;

import DatabaseConnection.DBConnection;
import DatabaseConnection.NameIDQueryResult;
import FTElemente.VerbindungsTyp;
import Integrator.DatabaseReading;
import Integrator.Matching.Match;
import Integrator.Matching.StringMatcher;
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
import java.util.ResourceBundle;

/**
 * Controller für das Portmapping beim Zusammenführen einer funktionalen Komponente und eines Fehlermodells
 */
public class PortMatchingController extends AnchorPane implements Initializable {
    @FXML
    private Label selectedInputLabel;

    @FXML
    private Label selectedOutputLabel;

    @FXML
    private ListView<NameIDQueryResult> inportListView;

    @FXML
    private Button matchPortsButton;

    @FXML
    private ListView<NameIDQueryResult> outportListView;

    @FXML
    private Button backButton;

    @FXML
    private ListView<NameIDQueryResult> outputListView;

    @FXML
    private ListView<NameIDQueryResult> inputListView;

    private CombineFmAndLogicalComponentsController combineFmAndLogicalComponentsController;

    private NameIDQueryResult fm;

    private NameIDQueryResult component;

    private HashMap<NameIDQueryResult,ObservableList<NameIDQueryResult>> putListsCorrespondingToPortName = new HashMap<>();

    private ObservableList<NameIDQueryResult> listOfMovedInputElement;

    private NameIDQueryResult movedInputElement;

    private ObservableList<NameIDQueryResult> listOfMovedOutputElement;

    private NameIDQueryResult movedOutputElement;

    private NameIDQueryResult selectedInportBeforeMoving;

    private NameIDQueryResult selectedOutportBeforeMoving;

    void setComponentData(final NameIDQueryResult c){
        this.component = c;
    }

    void setCFT(final NameIDQueryResult c){
        this.fm = c;
    }

    void setParentController(final CombineFmAndLogicalComponentsController controller){
        this.combineFmAndLogicalComponentsController = controller;
    }

    void start(){
        DatabaseReading readDB = new DatabaseReading();
        List<NameIDQueryResult> inportList = readDB.getInportsOfLogicalComponent(component.getId());
        List<NameIDQueryResult> inputList = readDB.getInputsOfCFT(fm.getId());
        List<NameIDQueryResult> outportList = readDB.getOutportsOfLogicalComponent(component.getId());
        List<NameIDQueryResult> outputList = readDB.getOutputsOfCFT(fm.getId());

        inportList.forEach(e -> {
            ObservableList<NameIDQueryResult> observableList = FXCollections.observableArrayList();
            putListsCorrespondingToPortName.put(e,observableList);
        });

        outportList.forEach(e -> {
            ObservableList<NameIDQueryResult> observableList = FXCollections.observableArrayList();
            putListsCorrespondingToPortName.put(e,observableList);
        });

        ObservableList<NameIDQueryResult> inportObsList = FXCollections.observableList(inportList);
        ObservableList<NameIDQueryResult> outportObsList = FXCollections.observableList(outportList);
        outportListView.setItems(outportObsList);
        inportListView.setItems(inportObsList);


        StringMatcher stringMatcher = new StringMatcher();
        List<Match> inMatchesList = stringMatcher.matchStrings(inputList,inportList);
        List<Match> outMatchesList = stringMatcher.matchStrings(outputList,outportList);

        /*
        PortMatchingWithTypes portMatchingWithTypes = new PortMatchingWithTypes();
        List<Match> inMatchesList = portMatchingWithTypes.matchPorts(inputList,inportList);
        List<Match> outMatchesList = portMatchingWithTypes.matchPorts(outputList,outportList);
        */

        inMatchesList.forEach(match -> {
            putListsCorrespondingToPortName.get(match.getBB()).add(match.getAA());

        });

        outMatchesList.forEach(match -> {
            putListsCorrespondingToPortName.get(match.getBB()).add(match.getAA());

        });

    }

    @FXML
    void backToCombineFmAndLogicalComponents(ActionEvent event) {
        combineFmAndLogicalComponentsController.closeWindow(false);
    }

    @FXML
    void matchPorts(ActionEvent event) {
        DBConnection db = DBConnection.getInstance();
        db.createRelationship(component.getId(),fm.getId(),VerbindungsTyp.FailureModelOf);
        putListsCorrespondingToPortName.entrySet().forEach(entry -> {
            int idPort = entry.getKey().getId();
            entry.getValue().forEach(value -> db.createRelationship(value.getId(),idPort,VerbindungsTyp.PortMapping));
        });
        combineFmAndLogicalComponentsController.closeWindow(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inportListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        outportListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        inputListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        outputListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

            @Override
            public ListCell<NameIDQueryResult> call(ListView<NameIDQueryResult> param) {
                ListCell<NameIDQueryResult> cell4 = new ListCell<NameIDQueryResult>() {

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
                return cell4;
            }
        });
    }

    @FXML
    void inportClicked(MouseEvent event) {
        if(event.getClickCount()>1 && movedInputElement != null){
            ObservableList<NameIDQueryResult> addList = putListsCorrespondingToPortName.get(inportListView.getSelectionModel().getSelectedItem());
            addList.add(movedInputElement);
            listOfMovedInputElement.remove(movedInputElement);
            inportListView.getSelectionModel().select(selectedInportBeforeMoving);
            listOfMovedInputElement = null;
            movedInputElement = null;
            selectedInputLabel.setText("");
        }else{
            inputListView.setItems(putListsCorrespondingToPortName.get(inportListView.getSelectionModel().getSelectedItem()));
            selectedInportBeforeMoving = inportListView.getSelectionModel().getSelectedItem();
        }

    }

    @FXML
    void inputClicked(MouseEvent event) {
        if(event.getClickCount()>1){
            if(inputListView.getSelectionModel().getSelectedItem().equals(movedInputElement)){
                listOfMovedInputElement = null;
                movedInputElement = null;
                selectedInputLabel.setText("");
            }else{
                listOfMovedInputElement = putListsCorrespondingToPortName.get(selectedInportBeforeMoving);
                movedInputElement = inputListView.getSelectionModel().getSelectedItem();
                selectedInputLabel.setText(movedInputElement.getName());
            }

        }else{
            if(movedInputElement != null && !inputListView.getItems().isEmpty()) inputListView.getSelectionModel().select(movedInputElement);
        }
    }

    @FXML
    void outportClicked(MouseEvent event) {
        if(event.getClickCount()>1 && movedOutputElement != null){
            ObservableList<NameIDQueryResult> addList = putListsCorrespondingToPortName.get(outportListView.getSelectionModel().getSelectedItem());
            addList.add(movedOutputElement);
            listOfMovedOutputElement.remove(movedOutputElement);
            outportListView.getSelectionModel().select(selectedOutportBeforeMoving);
            listOfMovedOutputElement = null;
            movedOutputElement = null;
            selectedOutputLabel.setText("");
        }else{
            outputListView.setItems(putListsCorrespondingToPortName.get(outportListView.getSelectionModel().getSelectedItem()));
            selectedOutportBeforeMoving = outportListView.getSelectionModel().getSelectedItem();
        }
    }

    @FXML
    void outputClicked(MouseEvent event) {
        if(event.getClickCount()>1){
            if(outputListView.getSelectionModel().getSelectedItem().equals(movedOutputElement)){
                listOfMovedOutputElement = null;
                movedOutputElement = null;
                selectedOutputLabel.setText("");
            }else{
                listOfMovedOutputElement = putListsCorrespondingToPortName.get(selectedOutportBeforeMoving);
                movedOutputElement = outputListView.getSelectionModel().getSelectedItem();
                selectedOutputLabel.setText(movedOutputElement.getName());
            }

        }else{
            if(movedOutputElement != null && !outputListView.getItems().isEmpty()) outputListView.getSelectionModel().select(movedOutputElement);
        }
    }
}
