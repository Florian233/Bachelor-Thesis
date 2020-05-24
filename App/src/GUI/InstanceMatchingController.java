package GUI;

import DatabaseConnection.NameIDQueryResult;
import FTElemente.ElementTyp;
import Integrator.CFTMerging.CFT;
import Integrator.CFTMerging.CFTMerge;
import Integrator.DatabaseReading;
import Integrator.Matching.Match;
import Integrator.Matching.PortMatchingWithTypes;
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
 * Controllerklasse für das Matching der Ports von gleichen Instanzen beim Zusammenfügen zweier Fehlermodelle
 */
public class InstanceMatchingController extends AnchorPane implements Initializable {

    private BEAndPortMatchingForMergeController parentController;

    private Map<Integer,List<NameIDQueryResult>> outputMap = new HashMap<>();

    private Map<Integer,List<NameIDQueryResult>> inputMap = new HashMap<>();

    @FXML
    private Button mergeButton;

    @FXML
    private ListView<NameIDQueryResult> inputFM2ListView;

    @FXML
    private Button backButton;

    @FXML
    private ListView<Match> instanceListView;

    @FXML
    private ListView<NameIDQueryResult> inputFm1ListView;

    @FXML
    private Label errorLabel;

    private List<Match> matchList;

    private CFT fm1;

    private CFT fm2;

    @FXML
    private Label portInstance2Label;

    @FXML
    private Label portinstance1Label;

    @FXML
    private Button matchButton;

    private List<Match> instanceMatches;

    private PortMatchingWithTypes portMatching = new PortMatchingWithTypes();

    @FXML
    void pickInstance(MouseEvent event) {
        if(instanceListView.getSelectionModel().getSelectedItem() != null) {
            Match m = instanceListView.getSelectionModel().getSelectedItem();
            ObservableList<NameIDQueryResult> fm1List = FXCollections.observableArrayList();
            ObservableList<NameIDQueryResult> fm2List = FXCollections.observableArrayList();
            fm1List.addAll(inputMap.get(m.getA()));
            fm1List.addAll(outputMap.get(m.getA()));
            fm2List.addAll(inputMap.get(m.getB()));
            fm2List.addAll(outputMap.get(m.getB()));
            inputFm1ListView.setItems(fm1List);
            inputFM2ListView.setItems(fm2List);
            inputFm1ListView.getSelectionModel().select(0);
            if(inputFm1ListView.getSelectionModel().getSelectedItem() != null){
                Match mm = null;
                if(inputFm1ListView.getSelectionModel().getSelectedItem().getType().equals(ElementTyp.OutputInstance)){
                    mm = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),outputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
                }else{
                    mm = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),inputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
                }
                inputFM2ListView.getSelectionModel().select(mm.getBB());
                NameIDQueryResult selected1 = inputFm1ListView.getSelectionModel().getSelectedItem();
                NameIDQueryResult selected2 = inputFM2ListView.getSelectionModel().getSelectedItem();
                portinstance1Label.setText(selected1.getName()+" ("+selected1.getType()+")");
                portInstance2Label.setText(selected2.getName()+" ("+selected2.getType()+")");
            }

        }
    }

    @FXML
    void pickPortInstance1(MouseEvent event) {
        if(inputFm1ListView.getSelectionModel().getSelectedItem() != null) {
            Match m = null;
            if(inputFm1ListView.getSelectionModel().getSelectedItem().getType().equals(ElementTyp.OutputInstance)){
                m = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),outputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
            }else{
                m = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),inputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
            }
            inputFM2ListView.getSelectionModel().select(m.getBB());
            NameIDQueryResult selected1 = inputFm1ListView.getSelectionModel().getSelectedItem();
            NameIDQueryResult selected2 = inputFM2ListView.getSelectionModel().getSelectedItem();
            portinstance1Label.setText(selected1.getName()+" ("+selected1.getType()+")");
            portInstance2Label.setText(selected2.getName()+" ("+selected2.getType()+")");
        }
    }

    @FXML
    void pickPortInstance2(MouseEvent event) {
        if(inputFM2ListView.getSelectionModel().getSelectedItem() != null) {
            NameIDQueryResult selected2 = inputFM2ListView.getSelectionModel().getSelectedItem();
            portInstance2Label.setText(selected2.getName()+" ("+selected2.getType()+")");
        }
    }

    @FXML
    void matchPorts(ActionEvent event) {
        if(inputFm1ListView.getSelectionModel().getSelectedItem() != null && inputFM2ListView.getSelectionModel().getSelectedItem() != null){
            if(inputFm1ListView.getSelectionModel().getSelectedItem().getType().equals(inputFM2ListView.getSelectionModel().getSelectedItem().getType())){
                NameIDQueryResult selected1 = inputFm1ListView.getSelectionModel().getSelectedItem();
                NameIDQueryResult selected2 = inputFM2ListView.getSelectionModel().getSelectedItem();
                Match selectedInput = instanceListView.getSelectionModel().getSelectedItem();
                List<NameIDQueryResult> inputs1 = inputMap.get(selectedInput.getA());
                List<NameIDQueryResult> inputs2 = inputMap.get(selectedInput.getB());
                List<NameIDQueryResult> outputs1 = outputMap.get(selectedInput.getA());
                List<NameIDQueryResult> outputs2 = outputMap.get(selectedInput.getB());
                if(selected1.getType().equals(ElementTyp.InputInstance) && selected2.getType().equals(ElementTyp.InputInstance)){
                    inputs1.remove(selected1);
                    inputs2.remove(selected2);
                    //inputMap.put(selectedInput.getA(),inputs1);
                    //inputMap.put(selectedInput.getB(),inputs2);
                    matchList.add(new Match(selected1.getId(),selected1,selected2.getId(),selected2));
                    inputFm1ListView.getItems().remove(selected1);
                    inputFM2ListView.getItems().remove(selected2);
                }else if(selected1.getType().equals(ElementTyp.OutputInstance) && selected2.getType().equals(ElementTyp.OutputInstance)){
                    outputs1.remove(selected1);
                    outputs2.remove(selected2);
                    //outputMap.put(selectedInput.getA(),outputs1);
                    //outputMap.put(selectedInput.getB(),outputs2);
                    matchList.add(new Match(selected1.getId(),selected1,selected2.getId(),selected2));
                    inputFm1ListView.getItems().remove(selected1);
                    inputFM2ListView.getItems().remove(selected2);

                }


                if(inputFm1ListView.getItems().isEmpty() && inputFM2ListView.getItems().isEmpty()){
                    ObservableList<Match> currentList = instanceListView.getItems();
                    currentList.remove(instanceListView.getSelectionModel().getSelectedItem());
                    instanceListView.setItems(currentList);
                    instanceListView.getSelectionModel().select(0);
                    Match mmm = instanceListView.getSelectionModel().getSelectedItem();
                    if(mmm != null){
                        ObservableList<NameIDQueryResult> fm1List = FXCollections.observableArrayList();
                        ObservableList<NameIDQueryResult> fm2List = FXCollections.observableArrayList();
                        fm1List.addAll(inputMap.get(mmm.getA()));
                        fm1List.addAll(outputMap.get(mmm.getA()));
                        fm2List.addAll(inputMap.get(mmm.getB()));
                        fm2List.addAll(outputMap.get(mmm.getB()));
                        inputFm1ListView.setItems(fm1List);
                        inputFM2ListView.setItems(fm2List);
                    }
                }


                inputFm1ListView.getSelectionModel().select(0);
                if(inputFm1ListView.getSelectionModel().getSelectedItem() != null){
                    Match mm = null;
                    if(inputFm1ListView.getSelectionModel().getSelectedItem().getType().equals(ElementTyp.OutputInstance)){
                        mm = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),outputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
                    }else{
                        mm = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),inputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
                    }
                    inputFM2ListView.getSelectionModel().select(mm.getBB());
                    NameIDQueryResult selected11 = inputFm1ListView.getSelectionModel().getSelectedItem();
                    NameIDQueryResult selected22 = inputFM2ListView.getSelectionModel().getSelectedItem();
                    portinstance1Label.setText(selected11.getName()+" ("+selected11.getType()+")");
                    portInstance2Label.setText(selected22.getName()+" ("+selected22.getType()+")");
                }
            }
        }
    }


    void setFMs(final CFT fm1, final CFT fm2){
        this.fm1 = fm1;
        this.fm2 = fm2;
    }

    void setMatchList(final List<Match> matches){
        this.matchList = matches;
    }

    void start(){
        ObservableList<Match> obsList = FXCollections.observableList(instanceMatches);
        instanceListView.setItems(obsList);
        DatabaseReading readDB = new DatabaseReading();

        instanceMatches.forEach(inst -> {
            int id1 = inst.getA();
            int id2 = inst.getB();
            List<NameIDQueryResult> inputs1 = readDB.getInputsOfCFT(id1);
            List<NameIDQueryResult> inputs2 = readDB.getInputsOfCFT(id2);
            List<NameIDQueryResult> outputs1 = readDB.getOutputsOfCFT(id1);
            List<NameIDQueryResult> outputs2 = readDB.getOutputsOfCFT(id2);
            inputMap.put(id1,inputs1);
            inputMap.put(id2,inputs2);
            outputMap.put(id1,outputs1);
            outputMap.put(id2,outputs2);
        });

        instanceListView.getSelectionModel().select(0);
        Match m = instanceListView.getSelectionModel().getSelectedItem();
        ObservableList<NameIDQueryResult> fm1ports = FXCollections.observableArrayList();
        ObservableList<NameIDQueryResult> fm2ports = FXCollections.observableArrayList();
        fm1ports.addAll(inputMap.get(m.getA()));
        fm2ports.addAll(inputMap.get(m.getB()));
        fm1ports.addAll(outputMap.get(m.getA()));
        fm2ports.addAll(outputMap.get(m.getA()));

        inputFm1ListView.setItems(fm1ports);
        inputFM2ListView.setItems(fm2ports);

        instanceListView.getSelectionModel().select(0);
        Match mmm = instanceListView.getSelectionModel().getSelectedItem();
        if(mmm != null){
            ObservableList<NameIDQueryResult> fm1List = FXCollections.observableArrayList();
            ObservableList<NameIDQueryResult> fm2List = FXCollections.observableArrayList();
            fm1List.addAll(inputMap.get(mmm.getA()));
            fm1List.addAll(outputMap.get(mmm.getA()));
            fm2List.addAll(inputMap.get(mmm.getB()));
            fm2List.addAll(outputMap.get(mmm.getB()));
            inputFm1ListView.setItems(fm1List);
            inputFM2ListView.setItems(fm2List);
        }

        inputFm1ListView.getSelectionModel().select(0);
        if(inputFm1ListView.getSelectionModel().getSelectedItem() != null){
            Match mm = null;
            if(inputFm1ListView.getSelectionModel().getSelectedItem().getType().equals(ElementTyp.OutputInstance)){
                mm = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),outputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
            }else{
                mm = portMatching.matchPort(inputFm1ListView.getSelectionModel().getSelectedItem(),inputMap.get(instanceListView.getSelectionModel().getSelectedItem().getB()));
            }
            inputFM2ListView.getSelectionModel().select(mm.getBB());
            NameIDQueryResult selected11 = inputFm1ListView.getSelectionModel().getSelectedItem();
            NameIDQueryResult selected22 = inputFM2ListView.getSelectionModel().getSelectedItem();
            portinstance1Label.setText(selected11.getName()+" ("+selected11.getType()+")");
            portInstance2Label.setText(selected22.getName()+" ("+selected22.getType()+")");
        }
    }


    @FXML
    void backToBEPortMenu(ActionEvent event) {
        parentController.closeWindow(false);
    }

    void setParentController(final BEAndPortMatchingForMergeController c){this.parentController = c;}

    @FXML
    void mergeFMs(ActionEvent event) {
        final boolean[] merge = {false};
        inputMap.values().forEach(e -> {
            if(!e.isEmpty()) merge[0] =true;
        });
        outputMap.values().forEach(e -> {
            if(!e.isEmpty()) merge[0] =true;
        });
        if (merge[0]){
            errorLabel.setText("Wenn nicht alle Ports aufeinander abgebildet wurden, können die Instanzen nicht gleich sein! Merge nicht möglich!");
        }else {
            CFTMerge cftMerge = new CFTMerge(fm1, fm2, matchList);
            if (cftMerge.startMerging()) parentController.closeWindow(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instanceListView.setCellFactory(new Callback<ListView<Match>, ListCell<Match>>() {

            @Override
            public ListCell<Match> call(ListView<Match> param) {
                ListCell<Match> cell = new ListCell<Match>() {

                    @Override
                    protected void updateItem(Match item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getAA().getName()+" zu "+item.getAA().getName());
                        } else {
                            setText("");
                        }
                    }
                };
                return cell;
            }
        });
        inputFm1ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

            @Override
            public ListCell<NameIDQueryResult> call(ListView<NameIDQueryResult> param) {
                ListCell<NameIDQueryResult> cell = new ListCell<NameIDQueryResult>() {

                    @Override
                    protected void updateItem(NameIDQueryResult item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName()+"("+item.getType()+")");
                        } else {
                            setText("");
                        }
                    }
                };
                return cell;
            }
        });
        inputFM2ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

            @Override
            public ListCell<NameIDQueryResult> call(ListView<NameIDQueryResult> param) {
                ListCell<NameIDQueryResult> cell = new ListCell<NameIDQueryResult>() {

                    @Override
                    protected void updateItem(NameIDQueryResult item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName()+"("+item.getType()+")");
                        } else {
                            setText("");
                        }
                    }
                };
                return cell;
            }
        });
    }

    public void setInstanceMatches(List<Match> instanceMatches) {
        this.instanceMatches = instanceMatches;
    }
}
