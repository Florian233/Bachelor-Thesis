package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Main Application.
 */
public class Main extends Application {

    static Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 390.0;
    private final double MINIMUM_WINDOW_HEIGHT = 500.0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(Main.class, (java.lang.String[])null);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Integrating Safety Models using a Graph Database");
        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
        gotoSetDatabaseConnection();
        primaryStage.show();
    }

    public void gotoSetDatabaseConnection(){
        SetDatabaseConnectionController setDatabaseConnectionController = null;
        try {
            setDatabaseConnectionController = (SetDatabaseConnectionController) replaceSceneContent("SetDatabaseConnection.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (setDatabaseConnectionController != null) {
            setDatabaseConnectionController.setApp(this);
        }
    }

    public  void gotoEAProjectReader(){
        EAProjectReaderController eaProjectReaderController = null;
        try {
            eaProjectReaderController = (EAProjectReaderController) replaceSceneContent("EAProjectReader.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (eaProjectReaderController != null) {
            eaProjectReaderController.setApp(this);
        }
    }

    public void gotoMainMenu() {
        MainMenuController mainMenuController = null;
        try {
            mainMenuController = (MainMenuController) replaceSceneContent("MainMenu.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mainMenuController != null) {
            mainMenuController.setApp(this);
        }
    }

    public void gotoIntegrationMenu() {
        IntegrationMenuController integrationMenuController = null;
        try {
            integrationMenuController = (IntegrationMenuController) replaceSceneContent("IntegrationMenu.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (integrationMenuController != null) {
            integrationMenuController.setApp(this);
        }
    }

    public void gotoESSaRelProjectReader() {
        ESSaRelProjectReaderController esSaRelProjectReaderController = null;
        try {
            esSaRelProjectReaderController = (ESSaRelProjectReaderController) replaceSceneContent("ESSaRelProjectReader.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (esSaRelProjectReaderController != null) {
            esSaRelProjectReaderController.setApp(this);
        }
    }

    public void gotoCombineFmAndLogicalComponents() {
        CombineFmAndLogicalComponentsController combineFmAndLogicalComponentsController = null;
        try {
            combineFmAndLogicalComponentsController = (CombineFmAndLogicalComponentsController) replaceSceneContent("CombineFmAndLogicalComponents.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (combineFmAndLogicalComponentsController != null) {
            combineFmAndLogicalComponentsController.setApp(this);
        }
    }

    public void gotoMatchPortsToFailureTypes() {
        MatchPortsToFailureTypesController matchPortsToFailureTypesController = null;
        try {
            matchPortsToFailureTypesController = (MatchPortsToFailureTypesController) replaceSceneContent("MatchPortsToFailureTypes.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (matchPortsToFailureTypesController != null) {
            matchPortsToFailureTypesController.setApp(this);
        }
    }

    public void gotoCombineCFTs() {
        CombineCFTsController combineCFTsController = null;
        try {
            combineCFTsController = (CombineCFTsController) replaceSceneContent("CombineCFTs.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (combineCFTsController != null) {
            combineCFTsController.setApp(this);
        }
    }

    public String chooseFile(){
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        return file.getAbsolutePath();
    }

    private Initializable replaceSceneContent(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Main.class.getResource(fxml));
        AnchorPane page;
        /*try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        }*/
        try (InputStream in = Main.class.getResourceAsStream(fxml)) {
            page = (AnchorPane) loader.load(in);
        }
        Scene scene = new Scene(page, 800, 600);
        stage.setScene(scene);
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

}
