package GUI;

import DatabaseConnection.DBConnection;
import DatabaseConnection.NameIDQueryResult;
import Integrator.CFTMerging.CFT;
import Integrator.CFTMerging.CFTRekonstruktion;
import Integrator.DatabaseWriting;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für das Auswahlmenü welches Fehlermodell bestehen bleiben soll , Fehlermodell 1 oder 2 oder kombiniertes Fehlermodell.
 * Während des Zusammenführens von zwei Fehlermodellen.
 */
public class MergeCFTsController extends AnchorPane implements Initializable {

    private CombineCFTsController parentController;

    private NameIDQueryResult fm1;

    private NameIDQueryResult fm2;

    private Stage dialog;

    private HostServices hostServices;

    @FXML
    private Label cft2Label;

    @FXML
    private Button backButton;

    @FXML
    private Button copyButton;

    @FXML
    private Hyperlink datebaseLink;

    @FXML
    private TextArea queryTextArea;

    @FXML
    private Label cft1Label;

    @FXML
    private Button selectFM2Button;

    @FXML
    private Button selectFM1Button;

    @FXML
    private Button selectCombinedFMButton;

    private CFT cft1;

    private CFT cft2;

    @FXML
    void selectFM1(ActionEvent event) {
        new DatabaseWriting().deleteCFT(cft2);
        parentController.closeWindow(true);
    }

    @FXML
    void selectFM2(ActionEvent event) {
        new DatabaseWriting().deleteCFT(cft1);
        parentController.closeWindow(true);
    }

    @FXML
    void selectCombinedFM(ActionEvent event) {
        BEAndPortMatchingForMergeController controller = null;
        try {
            controller = (BEAndPortMatchingForMergeController) createBEandPortMatchingView();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (controller != null) {
            controller.setParentController(this);
            controller.setFMs(cft1,cft2);
            controller.start();
            dialog.show();
        }
    }

    @FXML
    void backToCombineCFTsMenu(ActionEvent event) {
        parentController.closeWindow(false);
    }

    @FXML
    void copyText(ActionEvent event) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(queryTextArea.getText());
        clipboard.setContent(content);
    }

    @FXML
    void cft1LabelClicked(MouseEvent event) {
        queryTextArea.setText(cft1.getCypherQuery());
    }

    @FXML
    void cft2LabelClicked(MouseEvent event) {
        queryTextArea.setText(cft2.getCypherQuery());
    }

    void setFDMs(final NameIDQueryResult fm1, final NameIDQueryResult fm2){
        this.fm1 = fm1;
        this.fm2 = fm2;
        cft1Label.setText(fm1.getName());
        cft2Label.setText(fm2.getName());

        cft1 = new CFTRekonstruktion(fm1.getId()).readCFT();
        cft1.printCFT();
        cft2 = new CFTRekonstruktion(fm2.getId()).readCFT();
        cft2.printCFT();
    }

    void setParentController(final CombineCFTsController combineCFTsController){this.parentController = combineCFTsController;}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        datebaseLink.setText(DBConnection.getInstance().getUrl());
    }

    private Initializable createBEandPortMatchingView() throws IOException {
        String fxml = "BEAndPortMatchingForMerge.fxml";
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
        dialog.setTitle("Ports und Basic Events zusammenführen");
        return (Initializable)loader.getController();
    }

    public void closeWindow(final boolean success){
        if(success){
            parentController.closeWindow(true);
        }
        dialog.close();
        dialog = null;
    }

    @FXML
    void linkClicked(ActionEvent event) {
        hostServices.showDocument(datebaseLink.getText());
    }

    void setHostServices(final HostServices hostServices){
        this.hostServices = hostServices;
    }

}
