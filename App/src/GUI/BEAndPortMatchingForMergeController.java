package GUI;

import DatabaseConnection.DBConnection;
import DatabaseConnection.NameIDQueryResult;
import Integrator.CFTMerging.CFT;
import Integrator.CFTMerging.CFTMerge;
import Integrator.DatabaseReading;
import Integrator.Matching.Match;
import Integrator.Matching.PortMatchingWithTypes;
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
import java.util.*;

/**
 * Klasse für das Matching der Basic Events, Fehlermodi und Instanzen beim Zusammenführen zweier Fehlermodelle
 */
public class BEAndPortMatchingForMergeController extends AnchorPane implements Initializable {

    private Stage dialog;

    private MergeCFTsController parentController;

    private CFT fm1;

    private CFT fm2;

    private Map<Integer,Integer> portmappingMapFailureModeToPort = new HashMap<>();

    private Map<Integer,List<NameIDQueryResult>> portmappingMapPortToFailureModesFM2 = new HashMap<>();

    private StringMatcher stringMatcher = new StringMatcher();

    private PortMatchingWithTypes portMatcher = new PortMatchingWithTypes();

    private List<Match> matchList = new ArrayList<>();

    @FXML
    private ListView<NameIDQueryResult> beFM1ListView;

    @FXML
    private ListView<NameIDQueryResult> fmInstance1ListView;

    @FXML
    private Button inputMatchButton;

    @FXML
    private ListView<NameIDQueryResult> beFM2ListView;

    @FXML
    private Button mergeFMButton;

    @FXML
    private ListView<NameIDQueryResult> inputFM1ListView;

    @FXML
    private Button instanceMatchButton;

    @FXML
    private ListView<NameIDQueryResult> inputFM2ListView;

    @FXML
    private Button beMatchButton;

    @FXML
    private Button backButton;

    @FXML
    private ListView<NameIDQueryResult> fmInstance2ListView;

    @FXML
    private ListView<NameIDQueryResult> outputFM2ListView;

    @FXML
    private ListView<NameIDQueryResult> outputFM1ListView;

    @FXML
    private Button outputMatchButton;

    private List<Match> instanceMatches = new ArrayList<>();

    private NameIDQueryResult noMatch = new NameIDQueryResult("Keiner",-1);

    private DBConnection db = DBConnection.getInstance();

    @FXML
    void matchBE(ActionEvent event) {
        if(beFM1ListView.getSelectionModel().getSelectedItem() != null && beFM2ListView.getSelectionModel().getSelectedItem() != null){
            NameIDQueryResult be1 = beFM1ListView.getSelectionModel().getSelectedItem();
            NameIDQueryResult be2 = beFM2ListView.getSelectionModel().getSelectedItem();

            if(be1.getId() != -1 && be2.getId() != -1){
                matchList.add(new Match(be1.getId(),be1,be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = beFM1ListView.getItems();
                obsList.remove(be1);
                beFM1ListView.setItems(obsList);
                ObservableList<NameIDQueryResult> obsList2 = beFM2ListView.getItems();
                obsList2.remove(be2);
                beFM2ListView.setItems(obsList2);
            }else if(be2.getId() == -1 && be1.getId() != -1){
                matchList.add(new Match(be1.getId(),be1));
                ObservableList<NameIDQueryResult> obsList = beFM1ListView.getItems();
                obsList.remove(be1);
                beFM1ListView.setItems(obsList);
            }else if(be1.getId() == -1 && be2.getId() != -1){
                matchList.add(new Match(be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = beFM2ListView.getItems();
                obsList.remove(be2);
                beFM2ListView.setItems(obsList);
            }
            beFM1ListView.getSelectionModel().select(0);
            NameIDQueryResult selecItem = beFM1ListView.getSelectionModel().getSelectedItem();
            if(selecItem != null && selecItem.getId() != -1){
                Match m = stringMatcher.matchSingleString(beFM1ListView.getSelectionModel().getSelectedItem(),beFM2ListView.getItems());
                if(m.getB() != -1){beFM2ListView.getSelectionModel().select(m.getBB());}
                else{beFM2ListView.getSelectionModel().select(noMatch);}
            }
        }
    }

    @FXML
    void matchInput(ActionEvent event) {
        if(inputFM1ListView.getSelectionModel().getSelectedItem()  != null && inputFM2ListView.getSelectionModel().getSelectedItem() != null){

            NameIDQueryResult be1 = inputFM1ListView.getSelectionModel().getSelectedItem();
            NameIDQueryResult be2 = inputFM2ListView.getSelectionModel().getSelectedItem();

            if(be1.getId() != -1 && be2.getId() != -1){
                matchList.add(new Match(be1.getId(),be1,be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = inputFM1ListView.getItems();
                obsList.remove(be1);
                inputFM1ListView.setItems(obsList);
                ObservableList<NameIDQueryResult> obsList2 = inputFM2ListView.getItems();
                obsList2.remove(be2);
                inputFM2ListView.setItems(obsList2);
            }else if(be2.getId() == -1 && be1.getId() != -1){
                matchList.add(new Match(be1.getId(),be1));
                ObservableList<NameIDQueryResult> obsList = inputFM1ListView.getItems();
                obsList.remove(be1);
                inputFM1ListView.setItems(obsList);
            }else if(be1.getId() == -1 && be2.getId() != -1){
                matchList.add(new Match(be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = inputFM2ListView.getItems();
                obsList.remove(be2);
                inputFM2ListView.setItems(obsList);
            }

            removeFailureModeFromAllMatchingLists(be1);
            removeFailureModeFromAllMatchingLists(be2);

            inputFM1ListView.getSelectionModel().select(0);
            NameIDQueryResult selecItem = inputFM1ListView.getSelectionModel().getSelectedItem();
            if(selecItem != null && selecItem.getId() != -1){
                NameIDQueryResult selecteditem = inputFM1ListView.getSelectionModel().getSelectedItem();
                Match m = portMatcher.matchPort(selecteditem,portmappingMapPortToFailureModesFM2.get(portmappingMapFailureModeToPort.get(selecteditem.getId())));
                if(m.getB() != -1){inputFM2ListView.getSelectionModel().select(m.getBB());}
                else{inputFM2ListView.getSelectionModel().select(noMatch);}
            }
        }
    }

    @FXML
    void matchInstance(ActionEvent event) {
        if(fmInstance1ListView.getSelectionModel().getSelectedItem()  != null && fmInstance2ListView.getSelectionModel().getSelectedItem() != null){

            NameIDQueryResult be1 = fmInstance1ListView.getSelectionModel().getSelectedItem();
            NameIDQueryResult be2 = fmInstance2ListView.getSelectionModel().getSelectedItem();

            if(be1.getId() != -1 && be2.getId() != -1){
                matchList.add(new Match(be1.getId(),be1,be2.getId(),be2));
                instanceMatches.add(new Match(be1.getId(),be1,be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = fmInstance1ListView.getItems();
                obsList.remove(be1);
                fmInstance1ListView.setItems(obsList);
                ObservableList<NameIDQueryResult> obsList2 = fmInstance2ListView.getItems();
                obsList2.remove(be2);
                fmInstance2ListView.setItems(obsList2);
            }else if(be2.getId() == -1 && be1.getId() != -1){
                matchList.add(new Match(be1.getId(),be1));
                instanceMatches.add(new Match(be1.getId(),be1));
                ObservableList<NameIDQueryResult> obsList = fmInstance1ListView.getItems();
                obsList.remove(be1);
                fmInstance1ListView.setItems(obsList);
            }else if(be1.getId() == -1 && be2.getId() != -1){
                matchList.add(new Match(be2.getId(),be2));
                instanceMatches.add(new Match(be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = fmInstance2ListView.getItems();
                obsList.remove(be2);
                fmInstance2ListView.setItems(obsList);
            }

            fmInstance1ListView.getSelectionModel().select(0);
            NameIDQueryResult selecItem = fmInstance1ListView.getSelectionModel().getSelectedItem();
            if(selecItem != null && selecItem.getId() != -1){
                List<NameIDQueryResult> matchableInstances = getListOfMatchableInstances(fmInstance2ListView.getItems(),fmInstance1ListView.getSelectionModel().getSelectedItem());
                Match m = stringMatcher.matchSingleString(fmInstance1ListView.getSelectionModel().getSelectedItem(),matchableInstances);
                if(m.getB() != -1){fmInstance2ListView.getSelectionModel().select(m.getBB());}
                else{fmInstance2ListView.getSelectionModel().select(noMatch);}
            }
        }
    }

    @FXML
    void matchOutput(ActionEvent event) {
        if(outputFM1ListView.getSelectionModel().getSelectedItem()  != null && outputFM2ListView.getSelectionModel().getSelectedItem() != null){

            NameIDQueryResult be1 = outputFM1ListView.getSelectionModel().getSelectedItem();
            NameIDQueryResult be2 = outputFM2ListView.getSelectionModel().getSelectedItem();

            if(be1.getId() != -1 && be2.getId() != -1){
                matchList.add(new Match(be1.getId(),be1,be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = outputFM1ListView.getItems();
                obsList.remove(be1);
                outputFM1ListView.setItems(obsList);
                ObservableList<NameIDQueryResult> obsList2 = outputFM2ListView.getItems();
                obsList2.remove(be2);
                outputFM2ListView.setItems(obsList2);
            }else if(be2.getId() == -1 && be1.getId() != -1){
                matchList.add(new Match(be1.getId(),be1));
                ObservableList<NameIDQueryResult> obsList = outputFM1ListView.getItems();
                obsList.remove(be1);
                outputFM1ListView.setItems(obsList);
            }else if(be1.getId() == -1 && be2.getId() != -1){
                matchList.add(new Match(be2.getId(),be2));
                ObservableList<NameIDQueryResult> obsList = outputFM2ListView.getItems();
                obsList.remove(be2);
                outputFM2ListView.setItems(obsList);
            }


            removeFailureModeFromAllMatchingLists(be1);
            removeFailureModeFromAllMatchingLists(be2);

            outputFM1ListView.getSelectionModel().select(0);
            NameIDQueryResult selecItem = outputFM1ListView.getSelectionModel().getSelectedItem();
            if( selecItem != null && selecItem.getId() != -1){
                NameIDQueryResult selecteditem = outputFM1ListView.getSelectionModel().getSelectedItem();
                Match m = portMatcher.matchPort(selecteditem,portmappingMapPortToFailureModesFM2.get(portmappingMapFailureModeToPort.get(selecteditem.getId())));
                if(m.getB() == -1){outputFM2ListView.getSelectionModel().select(noMatch);}
                else{outputFM2ListView.getSelectionModel().select(m.getBB());}
            }
        }
    }

    @FXML
    void backToMergeCFTMenu(ActionEvent event) {
        parentController.closeWindow(false);
    }

    @FXML
    void mergeFM(ActionEvent event) {
        List<NameIDQueryResult> notMatchedElements = new ArrayList<>();
        notMatchedElements.addAll(beFM1ListView.getItems());
        notMatchedElements.addAll(beFM2ListView.getItems());
        List<NameIDQueryResult> notMatchedinstances = (fmInstance1ListView.getItems());
        notMatchedinstances.addAll(fmInstance2ListView.getItems());
        notMatchedElements.addAll(inputFM1ListView.getItems());
        notMatchedElements.addAll(inputFM2ListView.getItems());
        notMatchedElements.addAll(outputFM1ListView.getItems());
        notMatchedElements.addAll(outputFM2ListView.getItems());

        notMatchedElements.stream().forEach(element -> {
            matchList.add(new Match(element.getId(),element));
        });

        DatabaseReading readDB = new DatabaseReading();
        notMatchedinstances.forEach(instance -> {
            matchList.add(new Match(instance.getId(),instance));
            readDB.getInputsOfCFT(instance.getId()).forEach(input -> {
                matchList.add(new Match(input.getId(),input));
            });
            readDB.getOutputsOfCFT(instance.getId()).forEach(output ->{
                matchList.add(new Match(output.getId(),output));
            });
        });

        /*
        //Port matching sollte genau so funktionieren, sonst hat der Benutzer eine falsche Eingabe getätigt
        instanceMatches.stream().forEach(match -> {
            int id1 = match.getA();
            int id2 = match.getB();
            DatabaseReading readDB = new DatabaseReading();
            if(id2 == 0){
                List<NameIDQueryResult> ports = readDB.getInputsOfCFT(id1);
                ports.addAll(readDB.getOutputsOfCFT(id1));

                ports.forEach(port -> {
                    matchList.add(new Match(port.getId(),port));
                });
            }else{
                List<NameIDQueryResult> inputs1 = readDB.getInputsOfCFT(id1);
                List<NameIDQueryResult> inputs2 = readDB.getInputsOfCFT(id2);
                List<NameIDQueryResult> outputs1 = readDB.getOutputsOfCFT(id1);
                List<NameIDQueryResult> outputs2 = readDB.getOutputsOfCFT(id2);

                if(inputs1.size() == inputs2.size() && outputs1.size() == outputs2.size()) {
                    matchList.addAll(stringMatcher.matchEverythingOnce(inputs1, inputs2));
                    matchList.addAll(stringMatcher.matchEverythingOnce(outputs1, outputs2));
                }else{//FEHLER, zurück zum vorherigen Fenster
                    parentController.closeWindow(false);
                }
            }
        });

        new CFTMerge(fm1,fm2,matchList).startMerging();*/


        if(instanceMatches.size() != 0) {
            InstanceMatchingController instanceMatchingController = null;
            try {
                instanceMatchingController = (InstanceMatchingController) createInstanceMatchingView();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (instanceMatchingController != null) {
                instanceMatchingController.setParentController(this);
                instanceMatchingController.setFMs(fm1, fm2);
                instanceMatchingController.setInstanceMatches(instanceMatches);
                instanceMatchingController.setMatchList(matchList);
                instanceMatchingController.start();
                dialog.show();
            }
        }else{
            new CFTMerge(fm1,fm2,matchList).startMerging();
        }
    }

    void setParentController(final MergeCFTsController c) {
        this.parentController = c;
    }

    void start() {
        stringMatcher.setMatchingThreshold(5);

        ObservableList<NameIDQueryResult> be1 = FXCollections.observableList(fm1.getBasicEventsList());
        be1.add(noMatch);
        beFM1ListView.setItems(be1);

        ObservableList<NameIDQueryResult> be2 = FXCollections.observableList(fm2.getBasicEventsList());
        be2.add(noMatch);
        beFM2ListView.setItems(be2);

        ObservableList<NameIDQueryResult> instances1 = FXCollections.observableArrayList(fm1.getInstancesList());
        instances1.add(noMatch);
        fmInstance1ListView.setItems(instances1);

        ObservableList<NameIDQueryResult> instances2 = FXCollections.observableList(fm2.getInstancesList());
        instances2.add(noMatch);
        fmInstance2ListView.setItems(instances2);

        ObservableList<NameIDQueryResult> inputs1 = FXCollections.observableList(fm1.getInputList());
        inputs1.add(noMatch);
        inputFM1ListView.setItems(inputs1);

        ObservableList<NameIDQueryResult> inputs2 = FXCollections.observableList(fm2.getInputList());
        inputs2.add(noMatch);
        inputFM2ListView.setItems(inputs2);

        ObservableList<NameIDQueryResult> outputs1 = FXCollections.observableList(fm1.getOutputList());
        outputs1.add(noMatch);
        outputFM1ListView.setItems(outputs1);

        ObservableList<NameIDQueryResult> outputs2 = FXCollections.observableList(fm2.getOutputList());
        outputs2.add(noMatch);
        outputFM2ListView.setItems(outputs2);

        inputs1.forEach(i -> {
            if(i.getId() != -1)portmappingMapFailureModeToPort.put(i.getId(),DatabaseReading.getPortmappingOfFailureMode(i.getId()).getId());
        });
        inputs2.forEach(i -> {
            if(i.getId() != -1) {
                NameIDQueryResult port = DatabaseReading.getPortmappingOfFailureMode(i.getId());
                portmappingMapFailureModeToPort.put(i.getId(), port.getId());
                if (portmappingMapPortToFailureModesFM2.containsKey(port.getId())) {
                    List<NameIDQueryResult> temp = portmappingMapPortToFailureModesFM2.get(port.getId());
                    temp.add(i);
                    portmappingMapPortToFailureModesFM2.put(port.getId(), temp);
                } else {
                    List<NameIDQueryResult> temp = new ArrayList<>();
                    temp.add(i);
                    portmappingMapPortToFailureModesFM2.put(port.getId(), temp);
                }
            }
        });
        outputs1.forEach(i -> {
            if(i.getId() != -1)portmappingMapFailureModeToPort.put(i.getId(),DatabaseReading.getPortmappingOfFailureMode(i.getId()).getId());
        });
        outputs2.forEach(i -> {
            if(i.getId() != -1) {
                NameIDQueryResult port = DatabaseReading.getPortmappingOfFailureMode(i.getId());
                portmappingMapFailureModeToPort.put(i.getId(), port.getId());
                if (portmappingMapPortToFailureModesFM2.containsKey(port.getId())) {
                    List<NameIDQueryResult> temp = portmappingMapPortToFailureModesFM2.get(port.getId());
                    temp.add(i);
                    portmappingMapPortToFailureModesFM2.put(port.getId(), temp);
                } else {
                    List<NameIDQueryResult> temp = new ArrayList<>();
                    temp.add(i);
                    portmappingMapPortToFailureModesFM2.put(port.getId(), temp);
                }
            }
        });
    }

    void setFMs(final CFT fm1, final CFT fm2) {
        this.fm1 = fm1;
        this.fm2 = fm2;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        beFM1ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        beFM2ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        inputFM1ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        inputFM2ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        fmInstance1ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        fmInstance2ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        outputFM1ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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

        outputFM2ListView.setCellFactory(new Callback<ListView<NameIDQueryResult>, ListCell<NameIDQueryResult>>() {

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
    }

    private Initializable createInstanceMatchingView() throws IOException {
        String fxml = "InstanceMatching.fxml";
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(Main.stage);
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = (AnchorPane) loader.load(in);
        }
        Scene scene = new Scene(page, 800, 600);
        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.setTitle("Ports der CFT Instanzen zusammenführen");
        return (Initializable) loader.getController();
    }

    public void closeWindow(final boolean success) {
        if (success) {
            parentController.closeWindow(true);
        }
        dialog.close();
        dialog = null;
    }

    @FXML
    void be1Clicked(MouseEvent event) {
        if(beFM1ListView.getSelectionModel().getSelectedItem() != null && beFM1ListView.getSelectionModel().getSelectedItem().getId() != -1){
            Match selectMatch = stringMatcher.matchSingleString(beFM1ListView.getSelectionModel().getSelectedItem(),beFM2ListView.getItems());
            if(selectMatch.getB() == -1){
                beFM2ListView.getSelectionModel().select(noMatch);
            }else {
                beFM2ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }
    }

    @FXML
    void be2Clicked(MouseEvent event) {
        /*
        if(beFM2ListView.getSelectionModel().getSelectedItem() != null && beFM2ListView.getSelectionModel().getSelectedItem().getId() != 0){
            Match selectMatch = stringMatcher.matchSingleString(beFM2ListView.getSelectionModel().getSelectedItem(),beFM1ListView.getItems());
            if(selectMatch.getB() == 0){
                beFM1ListView.getSelectionModel().select(noMatch);
            }else {
                beFM1ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }*/
    }

    @FXML
    void input1Clicked(MouseEvent event) {
        if(inputFM1ListView.getSelectionModel().getSelectedItem() != null && inputFM1ListView.getSelectionModel().getSelectedItem().getId() != -1){
            NameIDQueryResult selecteditem = inputFM1ListView.getSelectionModel().getSelectedItem();
            Integer a = portmappingMapFailureModeToPort.get(selecteditem.getId());
            List<NameIDQueryResult> b = portmappingMapPortToFailureModesFM2.get(a);
            b.forEach(x -> System.out.println(x.getName()));
            Match selectMatch = portMatcher.matchPort(selecteditem,b);
            System.out.println("MATCH: "+selectMatch.getBB().getName());
            if(selectMatch.getB() == -1){
                inputFM2ListView.getSelectionModel().select(noMatch);
            }else {
                inputFM2ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }
    }

    @FXML
    void input2Clicked(MouseEvent event) {
        /*
        if(inputFM2ListView.getSelectionModel().getSelectedItem() != null && inputFM2ListView.getSelectionModel().getSelectedItem().getId() != 0){
            Match selectMatch = stringMatcher.matchSingleString(inputFM2ListView.getSelectionModel().getSelectedItem(),inputFM1ListView.getItems());
            if(selectMatch.getB() == 0){
                inputFM1ListView.getSelectionModel().select(noMatch);
            }else {
                inputFM1ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }*/
    }

    @FXML
    void instance1Clicked(MouseEvent event) {
        if(fmInstance1ListView.getSelectionModel().getSelectedItem() != null && fmInstance1ListView.getSelectionModel().getSelectedItem().getId() != -1){
            List<NameIDQueryResult> matchableInstances = getListOfMatchableInstances(fmInstance2ListView.getItems(),fmInstance1ListView.getSelectionModel().getSelectedItem());
            Match selectMatch = stringMatcher.matchSingleString(fmInstance1ListView.getSelectionModel().getSelectedItem(),matchableInstances);
            if(selectMatch.getB() == -1){
                fmInstance2ListView.getSelectionModel().select(noMatch);
            }else {
                fmInstance2ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }
    }

    @FXML
    void instance2Clicked(MouseEvent event) {
        /*
        if(fmInstance2ListView.getSelectionModel().getSelectedItem() != null && fmInstance2ListView.getSelectionModel().getSelectedItem().getId() != 0){
            Match selectMatch = stringMatcher.matchSingleString(fmInstance2ListView.getSelectionModel().getSelectedItem(),fmInstance1ListView.getItems());
            if(selectMatch.getB() == 0){
                fmInstance1ListView.getSelectionModel().select(noMatch);
            }else {
                fmInstance1ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }*/
    }

    @FXML
    void output1Clicked(MouseEvent event) {
        if(outputFM1ListView.getSelectionModel().getSelectedItem() != null && outputFM1ListView.getSelectionModel().getSelectedItem().getId() != -1){
            NameIDQueryResult selecteditem = outputFM1ListView.getSelectionModel().getSelectedItem();
            Match selectMatch = portMatcher.matchPort(selecteditem,portmappingMapPortToFailureModesFM2.get(portmappingMapFailureModeToPort.get(selecteditem.getId())));
            if(selectMatch.getB() == -1){
                outputFM2ListView.getSelectionModel().select(noMatch);
            }else {
                outputFM2ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }
    }

    @FXML
    void output2Clicked(MouseEvent event) {
        /*
        if(outputFM2ListView.getSelectionModel().getSelectedItem() != null && outputFM2ListView.getSelectionModel().getSelectedItem().getId() != 0){
            Match selectMatch = stringMatcher.matchSingleString(outputFM2ListView.getSelectionModel().getSelectedItem(),outputFM1ListView.getItems());
            if(selectMatch.getB() == 0){
                outputFM1ListView.getSelectionModel().select(noMatch);
            }else {
                outputFM1ListView.getSelectionModel().select(selectMatch.getBB());
            }
        }*/
    }

    private List<NameIDQueryResult> getListOfMatchableInstances(final ObservableList<NameIDQueryResult> list, final NameIDQueryResult instance){
        List<NameIDQueryResult> result = new ArrayList<>();

        int id = DatabaseReading.getIdOfRealization(instance.getId());

        list.forEach(inst -> {
            int idInst = DatabaseReading.getIdOfRealization(inst.getId());
            if(id==idInst){
                result.add(inst);
            }
        });

        return result;
    }

    private void removeFailureModeFromAllMatchingLists(final NameIDQueryResult failuremode){
        portmappingMapPortToFailureModesFM2.entrySet().forEach(entry -> {
            Integer key = entry.getKey();
            List<NameIDQueryResult> values = entry.getValue();
            values.remove(failuremode);
            portmappingMapPortToFailureModesFM2.put(key,values);
        });
    }

    //TODO Liste mit allen schon gematchten ids, beim erstellen von neuem match prüfen ob schon gemachtet, wenn ja nix tuen
}
