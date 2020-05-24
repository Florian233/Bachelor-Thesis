package Essarel;

import Exceptions.ESSaRelProjectOpenFailedException;
import FTElemente.ElementTyp;
import Reader.IExecutor;
import Reader.Reader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse um von repository.xml an die Speicherhierarchie von Essarel auszulesen, für jede Komponente relevante Daten auszulesen
 * und für jede CFT Realisierung ein Objekt von EssarelCFTParser zu erstellen
 */
public class EssarelReader implements Reader {

    /**
     * Systempfad zu dem Ordner in dem die repository.xml für das EssarelRepository
     */
    private String dataPath;
    /**
     * DocBuilder für XML Parser
     */
    private DocumentBuilder dBuilder = null;
    /**
     * Executor für die Ausgelesenen Elemente und Verbindungen
     */
    private IExecutor<EssarelElement,EssarelVerbindung> ex;
    /**
     * Liste in der alle parser gespeichert werden, um dann zum schluss gestartet zu werden.
     */
    private ArrayList<EssarelCFTParser> cftparser = new ArrayList<>();
    /**
     * Instanz von EssarelComponentDataContainer in dem die hier erstellten EssarelComponentData gespeichert werden
     */
    private EssarelComponentDataContainer componentDataStorage = EssarelComponentDataContainer.getInstance();

    /**
     * Liste der Namen aller Komponenten ohne Realisierung zum späteren Ausgeben
     */
    private List<String> componentsWithoutRealizationList = new ArrayList<>();

    public EssarelReader(final String path){

        this.dataPath = path.replace("\\repository.xml","");

        ex = new EssarelExecutor();

        /*
      Factory für XML Parser
     */
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readData(){
        File inputFile = new File(dataPath+"\\repository.xml");

        ArrayList<String> coreComponents;

        if(!inputFile.exists()){throw new ESSaRelProjectOpenFailedException();}
        coreComponents = parseRepository(inputFile);

        ArrayList<String> components = parseCoreComponents(coreComponents);
        ArrayList<EssarelComponentData> cfts = parseComponents(components);
        parseCFTs(cfts);

        //Alle parser starten
        cftparser.stream().forEach(EssarelCFTParser::parseCFT);

        ex.execute();
    }

    /**
     * Methode analog zu readData allerdings mit Rückgabewert
     * @return Liste der Namen aller Komponenten ohne Realisierung für die GUI
     */
    public List<String> readDataWithReturntype() {

        File inputFile = new File(dataPath+"\\repository.xml");

        ArrayList<String> coreComponents;

        if(!inputFile.exists()){throw new ESSaRelProjectOpenFailedException();}
        //ermitteln der coreComponents
        coreComponents = parseRepository(inputFile);
        //ermitteln der components
        ArrayList<String> components = parseCoreComponents(coreComponents);
        //auslesen der component.xml
        ArrayList<EssarelComponentData> cfts = parseComponents(components);
        //auslesen der spezifikation und erstellen des wurzelknoten elements
        parseCFTs(cfts);

        //Alle parser starten
        cftparser.stream().forEach(EssarelCFTParser::parseCFT);

        ex.execute();

        return componentsWithoutRealizationList;
    }


    /**
     * Methode liest die Namen der In und Outputs aus der Sepzifikation aus, sofern vorhanden, und fügt sie in der gleichen Reihenfolge der zugehörigen ComponentData hinzu.
     * Danach wird sofern eine Realisierung existiert dafür ein EssarelCFTParser Objekt erstellt.
     * @param cfts Liste mit allen ComponentData Objekten zu den Components aus Essarel
     */
    private void parseCFTs(final ArrayList<EssarelComponentData> cfts) {

        cfts.stream().forEach((cft) -> {

            //Die beiden Docs für den XMLParser
            Document realizationDoc = null;
            Document specificationDoc = null;

            //Namen der Inputs und Outputs auslesen, sofern vorhanden, und der zugehörigen EssarelComponentData hinzufügen, auf Reihenfolge achten, Reihenfolge muss übereinstimmen mit der Reihenfolge der NodeList
            if(cft.getSpecificationPath() != null) {

                File specificationFile = new File(cft.getSpecificationPath());
                try {
                    specificationDoc = dBuilder.parse(specificationFile);
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                }
                specificationDoc.getDocumentElement().normalize();

                NodeList nListInputs = specificationDoc.getElementsByTagName("inports");
                NodeList nListOutputs = specificationDoc.getElementsByTagName("outports");

                ArrayList<String> inputNamen = new ArrayList<>();
                ArrayList<String> outputNamen = new ArrayList<>();
                for (int i = 0; i < nListInputs.getLength(); i++) {
                    inputNamen.add(((Element) nListInputs.item(i)).getAttribute("name"));
                }
                for (int i = 0; i < nListOutputs.getLength(); i++) {
                    outputNamen.add(((Element) nListOutputs.item(i)).getAttribute("name"));
                }

                cft.setInputNamen(inputNamen);
                cft.setOutputNamen(outputNamen);
            }

            //CFTParser für Realisierung erstellen, sofern Realisierung vorhanden
            if(cft.getRealizationPath() != null) {
                File realizationFile = new File(cft.getRealizationPath());
                try {
                    realizationDoc = dBuilder.parse(realizationFile);
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                }
                realizationDoc.getDocumentElement().normalize();
                System.out.println("Root element: " + realizationDoc.getDocumentElement().getNodeName());


                //Einen neuen Parser für die Component erzeugen
                 cftparser.add(new EssarelCFTParser(realizationDoc.getDocumentElement(), ex, cft,dataPath));
            }

            //Für den Fall, dass keine Realisierung existieren sollte, wird stattdessen das Interface genommen um Verbindungen zu Proxies zu ziehen
            if(realizationDoc != null){
                String cftname = realizationDoc.getDocumentElement().getAttribute("name");
                cft.setCftName(cftname);
                cft.addEssarelComponent(new EssarelElement(realizationDoc.getDocumentElement(), ElementTyp.CFT, cftname));
            }else if(specificationDoc != null){
                componentsWithoutRealizationList.add(cft.getName());
                String cftname = specificationDoc.getDocumentElement().getAttribute("name");
                cft.setCftName(cftname);
                cft.addEssarelComponent(new EssarelElement(specificationDoc.getDocumentElement(), ElementTyp.CFT, cftname));
                //Da es keine notwendigkeit gibt nur zum Erstellen der In und Outports ohne Realisierung einen weiteren CFTParser zu starten, werden diese hier erstellt
                //TODO : Keine Inputs und Outputs für CFTs ohne Realisierung
            }else{componentsWithoutRealizationList.add(cft.getName());}
            //Element hier schon dem Executor hinzufügen, weil es einen Parser nur bei existierender Realisierung gibt
            ex.addElement(cft.getComponent());


        });
    }

    /**
     * Methode liest aus jeder component.xml Spezifikation und Realisierung für CFTs aus und erstellt sofern min. eines vorhanden ein EssarelComponentData Objekt dafür.
     * @param components Liste mit allen Dateipfaden zu den component.xml Dateien
     * @return Liste mit je einem EssarelComponentData Objekt pro component, sofern Spezifikation oder Realisierung vorhanden sind
     */
    private ArrayList<EssarelComponentData> parseComponents(final ArrayList<String> components) {
        ArrayList<EssarelComponentData> cfts = new ArrayList<>();

        components.stream().forEach((componentPath) -> {
            File file = new File(componentPath.replace("%20"," ")); // %20 wieder leerzeichen

            Document doc = null;
            try {
                doc = dBuilder.parse(file);
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            //Pfad zu Spezifizierung auslesen, Spezifizierung von CFT endet ist vom xsi:type de.essarel.specification:Interface
            NodeList nListSpec = doc.getElementsByTagName("specificationViews");
            String specificationPath = null;
            for(int i = 0;i<nListSpec.getLength();i++) {
                Element specificationView = (Element) nListSpec.item(i);
                if(specificationView.getAttribute("xsi:type").equals("de.essarel.specification:Interface"))
                specificationPath = componentPath.replace("component.xml", "") + specificationView.getAttribute("href").replace("#/", "");
                specificationPath = specificationPath != null ? specificationPath.replace("%20", " ") : null; //Leerzeichen richtig setzen
            }

            //realisierung auslesen und ComponentData Objekt erzeugen, beide Pfade in dem Objekt speichern
            String realizationPath = null;
            NodeList nListRealization = doc.getElementsByTagName("realizationViews");
            for(int i = 0;i<nListRealization.getLength();i++) {

                Element realizationView = (Element) nListRealization.item(i);

                //Die Realisierung wird nur bearbeitet wenn es sich um eine CFT handelt, alles weitere wird nicht beachtet
                if (realizationView.getAttribute("xsi:type").equals("de.essarel.realization:Cft")) {//Nur CFTs auslesen
                    System.out.println(realizationView.getAttribute("href").replace("#/", ""));
                    realizationPath = componentPath.replace("component.xml", "") + realizationView.getAttribute("href").replace("#/", "");
                    realizationPath = realizationPath.replace("%20"," ");
                }
            }
            //ComponentData nur erstellen, wenn mindestens eines von beiden existiert
            if(specificationPath != null || realizationPath != null){
                EssarelComponentData componentData = new EssarelComponentData(doc.getDocumentElement().getAttribute("name"), realizationPath, specificationPath);
                cfts.add(componentData);
                componentDataStorage.addComponentData(componentPath, componentData);
            }
        });
        return cfts;
    }

    /**
     * Methode um aus den corecomponent.xml Dateien die Pfade zu den zugehörigen component.xml Dateien auszulesen
     * @param coreComponents Liste von allen Pfaden relativ zu dem Ordner von repository.xml zu den corecomponent.xml Dateien
     * @return Liste von allen Dateipfaden zu den component.xml Dateien
     */
    private ArrayList<String> parseCoreComponents(final ArrayList<String> coreComponents) {

        ArrayList<String> components = new ArrayList<>();

        coreComponents.stream().forEach((component) -> {
            File file = new File(dataPath + "\\" + component.replace("%20"," ")); // %20 entspricht leerzeichen
            Document doc = null;
            try {
                doc = dBuilder.parse(file);
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("components");

            for (int i = 0; i < nList.getLength(); i++) {
                Element e = (Element) nList.item(i);
                System.out.println(e.getAttribute("href").replace("#/", ""));
                components.add(dataPath+"\\"+component.replace("corecomponent.xml","")+e.getAttribute("href").replace("#/", ""));
            }
        });

        return components;
    }


    /**
     * Methode um die repository.xml Datei auszlesen und die Pfade zu allen corecomponent.xml Dateien zurückzugeben
     * @param inputFile File Objekt von repository.xml
     * @return Liste von allen Pfaden relativ zu dem Ordner von repository.xml zu den corecomponent.xml Dateien
     */
    private ArrayList<String> parseRepository(final File inputFile){

        ArrayList<String> componentPath = new ArrayList<>();

        Document doc = null;
        try {
            doc = dBuilder.parse(inputFile);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        doc.getDocumentElement().normalize();
        System.out.println("Root element :"+ doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("coreComponents");

        for( int i = 0; i < nList.getLength();i++){
            Element e = (Element) nList.item(i);
            System.out.println(e.getAttribute("href"));
            componentPath.add(e.getAttribute("href").replace("#/",""));
        }
        return componentPath;
    }
}
