package EA;

import Exceptions.EAProjectOpenFailedException;
import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;
import Reader.IExecutor;
import Reader.Reader;
import org.sparx.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse um die Verbindung zu einem EA Repository aufzubauen und die benötigten Daten auszulesen.
 */
public class SecondEAReader implements Reader {
    /**
     * Repository Objekt der Java API von EA
     */
    private Repository repo;
    /**
     * Pfad zu dem Repository
     */
    private String dataPath;
    /**
     * zugehöriger Executor, der die Daten speichern und verarbeiten soll.
     */
    private IExecutor<EAElement,EAVerbindung> executor;
    /**
     * Liste aller Elemente von Typ LogicalComponent aus dem EA Repository
     */
    private List<Element> selectedcomponents = new ArrayList<>();
    /**
     * Liste aller Elemente von Typ CFT aus dem EA Repository
     */
    private List<Element> selectedcfts = new ArrayList<>();
    /**
     * Liste aller Elemente von Typ FT aus dem EA Repository
     */
    private List<Element> selectedfts = new ArrayList<>();
    /**
     * Liste aller Elemente von Typ FailureType aus dem EA Repository
     */
    //FailureType Elemente auslesen, weil die Suche nach einem Package mit einem FailureTypeSystem drin nicht erfolgreich enden muss,
    //abhängig von der Benennung des Package, das ist der einzige Weg wirklich das FailureTypeSystem zu finden, wenn auch komplizierter
    private List<Element> failureTypeElements = new ArrayList<>();

    /**
     * Konstruktor von SecondEAReader
     * Ein Repository und eine Executor Objekt werden erzeugt
     * @param path Pfad zum Repository
     */
    public SecondEAReader (final String path){
        this.dataPath = path;
        repo = new org.sparx.Repository();

        executor = new EAExecutor();

    }


    @Override
    public void readData(){

        Boolean a = repo.OpenFile(dataPath);

        if(!a){
            repo.CloseFile();
            throw new EAProjectOpenFailedException();
        }

        findStartingElements();

        parseFailureTypeSystem(failureTypeElements);

        //Für jeden Elementtyp wird das zugehörige Wurzelelement erstellt und das Auslesen gestartet
        selectedcomponents.stream().forEach((component) -> {
            executor.addElement(new EAElement(component.GetElementID(), ElementTyp.LogicalComponent,"",component.GetName()));
            System.out.println(component.GetName());
            parseLogicalComponent(component);
        });

        selectedcfts.stream().forEach((cft) -> {
            executor.addElement(new EAElement(cft.GetElementID(), ElementTyp.CFT,"",cft.GetName()));
            System.out.println(cft.GetName());
            parseCFT(cft);
        });

        selectedfts.stream().forEach((ft) -> {
            executor.addElement(new EAElement(ft.GetElementID(),ElementTyp.FaultTree,"",ft.GetName()));
            System.out.println(ft.GetName());
            parseFT(ft);
        });

        //File schließen
        repo.CloseFile();

        //Verarbeitung der ausgelesenen Daten starten
        executor.execute();
    }

    /**
     * Es werden alle ElementIDs von LogicalComponent, CFT, FT und FailureType Elementen aus dem Repository mittels einer SQL Abfrage ermittelt.
     * Mittel der Repository Funktion GetElementByID werden die zugehörigen Elemente des Repository ermittelt und den  jeweiligen Listen hinzugefügt.
     */
    private void findStartingElements() {

        //Direkte SQL Abfragen für jeden benötigten Typ
        String componentsXML = repo.SQLQuery("SELECT Object_ID FROM t_object WHERE Stereotype = 'IESELogicalComponent'");
        String cftXML = repo.SQLQuery("SELECT Object_ID FROM t_object WHERE Stereotype = 'CFT'");
        String ftXML = repo.SQLQuery("SELECT Object_ID FROM t_object WHERE Stereotype = 'FT'");
        String failureTypeXML = repo.SQLQuery("SELECT Object_ID FROM t_object WHERE Stereotype = 'FailureType'");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        //componentsXML bearbeiten
        Document componentDoc = null;
        InputSource inputComponentsXML = new InputSource(new StringReader(componentsXML));
        try {
            assert builder != null;
            componentDoc = builder.parse(inputComponentsXML);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert componentDoc != null;
        NodeList nListComponents = componentDoc.getElementsByTagName("Object_ID");

        for(int i = 0;i<nListComponents.getLength();i++){
            selectedcomponents.add(repo.GetElementByID(Integer.parseInt(nListComponents.item(i).getFirstChild().getNodeValue())));
        }

        //cftXML bearbeiten
        Document cftDoc = null;
        InputSource inputCFTXML = new InputSource(new StringReader(cftXML));
        try {
            cftDoc = builder.parse(inputCFTXML);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert cftDoc != null;
        NodeList nListCfts = cftDoc.getElementsByTagName("Object_ID");

        for(int i = 0;i<nListCfts.getLength();i++){
            int a = Integer.parseInt(nListCfts.item(i).getFirstChild().getNodeValue());
            selectedcfts.add(repo.GetElementByID(a));
        }

        //ftXML bearbeiten
        Document ftDoc = null;
        InputSource inputFTXML = new InputSource(new StringReader(ftXML));
        try {
            ftDoc = builder.parse(inputFTXML);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert ftDoc != null;
        NodeList nListFts = ftDoc.getElementsByTagName("Object_ID");

        for(int i = 0;i<nListFts.getLength();i++){
            selectedfts.add(repo.GetElementByID(Integer.parseInt(nListFts.item(i).getFirstChild().getNodeValue())));
        }

        //failureTypeXML bearbeiten
        Document failureTypeDoc = null;
        InputSource inputFailureTypeXML = new InputSource(new StringReader(failureTypeXML));
        try {
            failureTypeDoc = builder.parse(inputFailureTypeXML);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert failureTypeDoc != null;
        NodeList nListFailureTypes = failureTypeDoc.getElementsByTagName("Object_ID");

        for(int i = 0;i<nListFailureTypes.getLength();i++){
            failureTypeElements.add(repo.GetElementByID(Integer.parseInt(nListFailureTypes.item(i).getFirstChild().getNodeValue())));
        }
    }

    /**
     * Methode um aus allen FailureType Elementen das FailureTypeSystem in die Datenbank einzulesen
     * @param failureTypeElements Liste aller Elemente mit Stereotype FailureType im EA Repository
     */
    private void parseFailureTypeSystem(final List<Element> failureTypeElements) {

        failureTypeElements.forEach((failureTypeElement) -> {
            executor.addElement(new EAElement(failureTypeElement.GetElementID(),ElementTyp.FailureType,failureTypeElement.GetName()));
            failureTypeElement.GetConnectors().forEach((connector) -> {
                if(connector.GetClientID() == failureTypeElement.GetElementID()) {//Um doppelte Verbindungen auszuschließen, da EA die Verbindung bei beiden Elementen der Verbindung speichert
                    executor.addVerbindung(new EAVerbindung(connector.GetClientID(), connector.GetSupplierID(), connector.GetName(), "", VerbindungsTyp.SuperFailureType));
                }
            });
        });
    }

    /**
     * Benötigte Verbindungen und Elemente extrahieren.
     * @param cftRootElement Root Element(EA) des CFTs
     */
    private void parseCFT(final Element cftRootElement) {

        //Die Eingebetteten Elemente in das Root Element sind die Ein- und Ausgänge des CFTs, hier werden nur die Verbindungen zum RootElement hergestellt
        // Alle weiteren Elemte in dem CFT sind allerdings auch eingebettet, werden aber ignoriert, bzw nicht identifiziert
        cftRootElement.GetEmbeddedElements().forEach((subElement) -> {
            System.out.println("Name des eingebetteten Elements: "+subElement.GetName());
            System.out.println("Typ des eingebetteten Elements: "+subElement.GetStereotype());
            System.out.println("Id des eingebetteten Elements: "+subElement.GetElementID());

            int id = subElement.GetElementID();
            String name = subElement.GetName();

            switch(subElement.GetStereotype()){
                case "OutputFailureMode":
                    executor.addVerbindung(new EAVerbindung(cftRootElement.GetElementID(),subElement.GetElementID(),"Output Of","",VerbindungsTyp.OutputOf));
                    EAElement correspondingOutput = new EAElement(id,ElementTyp.Output,"",name);
                    setTypeOfPort(subElement.MiscData(0),correspondingOutput);
                    executor.addElement(correspondingOutput);
                    break;
                case "InputFailureMode":
                    executor.addVerbindung(new EAVerbindung(cftRootElement.GetElementID(),subElement.GetElementID(),"Input Of","",VerbindungsTyp.InputOf));
                    EAElement correspondingInput = new EAElement(id,ElementTyp.Input,"",name);
                    setTypeOfPort(subElement.MiscData(0),correspondingInput);
                    executor.addElement(correspondingInput);
                    break;
                default: System.out.println("CFTElement konnte nicht zugeordnet werden!");// Nicht weiter schlimm, zuordnung nicht notwendig -> Elemente hier erstmal ignorieren
            }
        });


        //Das Diagramm enthält alle Elemente und Verbindungen
        Collection<Diagram> diagrams = cftRootElement.GetDiagrams();
        diagrams.forEach((diagram) -> {
            //Verbindungen werden in einer extra Methode ausgelesen
            parseLinks(diagram.GetDiagramLinks());

            //Alle Elemente auslesen und in Executor einfügen
            diagram.GetDiagramObjects().forEach((object) -> {
                System.out.println("ElementID dieses Objekts: " + object.GetElementID());
                System.out.println("Name des Elements: " + repo.GetElementByID(object.GetElementID()).GetName());
                System.out.println("Typ des Elements: " + repo.GetElementByID(object.GetElementID()).GetStereotype());
                System.out.println("ID des Elements: " + repo.GetElementByID(object.GetElementID()).GetElementID());

                int id = object.GetElementID();
                Element elementToParse = repo.GetElementByID(id);
                switch(elementToParse.GetStereotype()){
                    case "FTAND":
                        executor.addElement(new EAElement(id, ElementTyp.ANDGate,elementToParse.GetName()));
                        break;
                    case "FTBasicEvent":
                        executor.addElement(new EAElement(id, ElementTyp.BasicEvent,elementToParse.GetName()));
                        break;
                    case "FTM/N":
                        String m = "M:"+getMofMooNGate(elementToParse);
                        executor.addElement( new EAElement(id, ElementTyp.MoonGate,m,elementToParse.GetName()));
                        break;
                    case "FTNOT":
                        executor.addElement(new EAElement(id, ElementTyp.NotGate,elementToParse.GetName()));
                        break;
                    case "FTOR":
                        executor.addElement(new EAElement(id, ElementTyp.ORGate,elementToParse.GetName()));
                        break;
                    case "FTXOR":
                        executor.addElement(new EAElement(id, ElementTyp.XORGate,elementToParse.GetName()));
                        break;
                    case "InputFailureMode":
                        //EAElement correspondingInput = new EAElement(id,ElementTyp.Input,"",elementToParse.GetName());
                        //setTypeOfPort(elementToParse.MiscData(0),correspondingInput);
                        //executor.addElement(correspondingInput);
                        break;
                    case "OutputFailureMode":
                        //EAElement correspondingOutput = new EAElement(id,ElementTyp.Output,"",elementToParse.GetName());
                        //setTypeOfPort(elementToParse.MiscData(0),correspondingOutput);
                        //executor.addElement(correspondingOutput);
                        break;
                    case "CFT":
                        break;
                    case "IESELogicalOutport":
                        break;
                    case "IESELogicalInport":
                        break;
                    case "IESELogicalComponent":
                        break;
                    case "IESELogicalComponentInstance":
                        break;
                    case "CFTInstance":
                        //Verbindung von Instanz zu Realisierung
                        executor.addVerbindung(new EAVerbindung(id,repo.GetElementByID(id).GetClassfierID(),"Instance Of","",VerbindungsTyp.InstanceOf));
                        //Bei einer Instanz werden zusätzlich die Verbindungen zu dessen Input und Output hergestellt, diesen sind wieder die eingebetteten Elemente
                        executor.addElement(new EAElement(id,ElementTyp.CFTInstance,repo.GetElementByID(id).GetName()));
                        repo.GetElementByID(id).GetEmbeddedElements().forEach((e) -> {
                            System.out.println("ID von Embedded: "+e.GetElementID());
                            System.out.println("Typ: "+e.GetStereotype());
                            System.out.println("Name von Embedded: "+e.GetName());

                            switch(e.GetStereotype()){
                                case "OutputFailureMode":
                                    executor.addVerbindung(new EAVerbindung(id,e.GetElementID(),"Output Of","",VerbindungsTyp.OutputOf));
                                    EAElement correspondingOutput = new EAElement(e.GetElementID(),ElementTyp.OutputInstance,"",e.GetName());
                                    setTypeOfPort(elementToParse.MiscData(0),correspondingOutput);
                                    executor.addElement(correspondingOutput);
                                    break;
                                case "InputFailureMode":
                                    executor.addVerbindung(new EAVerbindung(id,e.GetElementID(),"Input Of","",VerbindungsTyp.InputOf));
                                    EAElement correspondingInput = new EAElement(e.GetElementID(),ElementTyp.InputInstance,"",e.GetName());
                                    setTypeOfPort(elementToParse.MiscData(0),correspondingInput);
                                    executor.addElement(correspondingInput);
                                    break;
                                default: System.out.println("Elemente konnte nicht zugeordnet werden!");
                            }
                        });
                        break;
                    default:System.out.println("Element nicht identifiziert");
                }
            });
        });
    }

    /**
     * Auslesen der IESELogicalComponent und Elemente und Verbindungen in Executor einfügen
     * @param logicalComponentRootElement Root Element der IESELogicalComponent
     */
    private void parseLogicalComponent(final Element logicalComponentRootElement) {

        //Eingebettete Elemente sind wieder Inport und Outports, Umfassende LogicalComponent wird aufgebaut
        logicalComponentRootElement.GetEmbeddedElements().forEach((subElement) -> {
            System.out.println("Embedded Name: "+subElement.GetName());
            System.out.println("Embedded ID: "+ subElement.GetElementID());
            System.out.println("Embedded Typ: "+subElement.GetStereotype());
            switch(subElement.GetStereotype()){
                case "IESELogicalOutport":
                    EAElement correspondingOutportEAElement = new EAElement(subElement.GetElementID(),ElementTyp.Outport,"",subElement.GetName());
                    setTypeOfPort(subElement.MiscData(0),correspondingOutportEAElement);
                    executor.addElement(correspondingOutportEAElement);
                    executor.addVerbindung(new EAVerbindung(logicalComponentRootElement.GetElementID(),subElement.GetElementID(),"Outport Of","",VerbindungsTyp.OutportOf));
                    break;
                case "IESELogicalInport":
                    EAElement correspondingInportEAElement = new EAElement(subElement.GetElementID(),ElementTyp.Inport,"",subElement.GetName());
                    setTypeOfPort(subElement.MiscData(0),correspondingInportEAElement);
                    executor.addElement(correspondingInportEAElement);
                    executor.addVerbindung(new EAVerbindung(logicalComponentRootElement.GetElementID(),subElement.GetElementID(),"Inport Of","",VerbindungsTyp.InportOf));
                    break;
                default:System.out.println("Port nicht zugeordnet");
            }
        });


        logicalComponentRootElement.GetDiagrams().forEach((diagram) -> {

            //Verbindungen werden in extra Methode ausgelesen
            parseLinks(diagram.GetDiagramLinks());

            Collection<DiagramObject> objects = diagram.GetDiagramObjects();

            //Alle Elemente auslesen
            objects.forEach((object) -> {
                int elementid = object.GetElementID();
                /*
                System.out.println("ElementID dieses Objekts: "+object.GetElementID());
                System.out.println("Name des Elements: "+repo.GetElementByID(object.GetElementID()).GetName());
                System.out.println("Name des Elements: "+repo.GetElementByID(object.GetElementID()).GetStereotype());
                System.out.println("ID des Elements: "+repo.GetElementByID(object.GetElementID()).GetElementID());
                */
                System.out.println(repo.GetElementByID(elementid).GetStereotype());

                switch(repo.GetElementByID(elementid).GetStereotype()){
                    case "IESELogicalComponentInstance"://Nur diese wichtig, weil diese Ebene nur aus Verbindungen und Instanzen von IESELogicalComponent besteht
                        System.out.println("Name von Instanz: "+repo.GetElementByID(elementid).GetName());
                        parseLogicalComponentInstance(repo.GetElementByID(elementid));
                        break;
                    case "IESELogicalOutport":
                        break;
                    case "IESELogicalInport":
                        break;
                    case "IESELogicalComponent":
                        System.out.println("Das sollte nicht sein! Der Name ist: "+repo.GetElementByID(elementid).GetName());
                        break;
                    case "IESELogicalOutportInstance":
                        System.out.println(elementid);
                        System.out.println(repo.GetElementByID(elementid).GetName());
                        break;
                    case "IESELogicalInportInstance":
                        System.out.println(elementid);
                        System.out.println(repo.GetElementByID(elementid).GetName());
                        break;
                    default:System.out.println("Element konnte nicht zugeordnet werden");
                }
            });
        });
    }

    /**
     * Auslesen der IESELogicalComponentInstance und einfügen in den Executor
     * @param instanceRootElement Root Element der Instanz
     */
    private void parseLogicalComponentInstance(final Element instanceRootElement) {

        //Erzeugen des Instanz Elements
        executor.addElement(new EAElement(instanceRootElement.GetElementID(),ElementTyp.LogicalComponentInstance,"",instanceRootElement.GetName()));
        //Verbindung zu der LogicalComponent Klasse ziehen von der Instanziiert wird
        executor.addVerbindung(new EAVerbindung(instanceRootElement.GetElementID(),instanceRootElement.GetClassfierID(),"Instance Of","",VerbindungsTyp.InstanceOf));
        //In- und Outports auslesen, erzeugen und Verbindung zu Instanz Element ziehen
        instanceRootElement.GetEmbeddedElements().forEach((subElement) -> {
            System.out.println("Port von Instanz: "+subElement.GetName());
            System.out.println("ID des Ports: "+subElement.GetElementID());
            switch(subElement.GetStereotype()){
                case "IESELogicalOutportInstance":
                    EAElement correspondingOutportEAElement = new EAElement(subElement.GetElementID(),ElementTyp.OutportInstance,"",subElement.GetName());
                    setTypeOfPort(subElement.MiscData(0),correspondingOutportEAElement);
                    executor.addElement(correspondingOutportEAElement);
                    executor.addVerbindung(new EAVerbindung(instanceRootElement.GetElementID(),subElement.GetElementID(),"Outport Of","",VerbindungsTyp.OutportOf));
                    break;
                case "IESELogicalInportInstance":
                    EAElement correspondingInportEAElement = new EAElement(subElement.GetElementID(),ElementTyp.InportInstance,"",subElement.GetName());
                    setTypeOfPort(subElement.MiscData(0),correspondingInportEAElement);
                    executor.addElement(correspondingInportEAElement);
                    executor.addVerbindung(new EAVerbindung(instanceRootElement.GetElementID(),subElement.GetElementID(),"Inport Of","",VerbindungsTyp.InportOf));
                    break;
                default:System.out.println("Port nicht zugeordnet");
            }
        });
    }

    /**
     * Links aus dem EA Modell in Executor überführen
     * @param diagramLinks Liste von Verbindungen aus dem EA Modell
     */
    private void parseLinks(final Collection<DiagramLink> diagramLinks) {

        diagramLinks.forEach((link) -> {

            Connector connector = repo.GetConnectorByID(link.GetConnectorID());

            System.out.println(connector.GetStereotype());
            System.out.println("Verbindung von :"+connector.GetClientID()+" nach "+connector.GetSupplierID());
            System.out.println("Verbindung von :"+repo.GetElementByID(connector.GetClientID()).GetName()+" nach "+repo.GetElementByID(connector.GetSupplierID()).GetName());

            switch(connector.GetStereotype()){
                case "Logical Information Flow"://Fluss zwischen LogicalComponents
                    executor.addVerbindung(new EAVerbindung(connector.GetClientID(),connector.GetSupplierID(),connector.GetName(),"", VerbindungsTyp.InformationFlow));
                    break;
                case "PortFailureModeTrace"://Nur CFT
                    executor.addVerbindung(new EAVerbindung(connector.GetClientID(),connector.GetSupplierID(),connector.GetName(),"",VerbindungsTyp.PortMapping));
                    break;
                case "FailurePropagation"://Nur CFT und FT
                    executor.addVerbindung(new EAVerbindung(connector.GetClientID(),connector.GetSupplierID(),connector.GetName(),"",VerbindungsTyp.FailurePropagation));
                    break;
                case "ComponentFailureModelTrace"://Verbindung zwischen LogicalComponent und dessen Fault Model
                    executor.addVerbindung(new EAVerbindung(connector.GetSupplierID(),connector.GetClientID(),connector.GetName(),"",VerbindungsTyp.FailureModelOf));
                    break;
                default:System.out.println("Typ konnte nicht zugeordnet werden");
            }
        });
    }

    /**
     * FT auslesen und in Executor überführen
     * @param ftRootElement Root Element(EA) des FT
     */
    private void parseFT(final Element ftRootElement){
        Collection<Diagram> diagrams = ftRootElement.GetDiagrams();
        diagrams.forEach((diagram) -> {
            parseLinks(diagram.GetDiagramLinks());

            diagram.GetDiagramObjects().forEach((object) -> {
                System.out.println("ElementID dieses Objekts: " + object.GetElementID());
                System.out.println("Name des Elements: " + repo.GetElementByID(object.GetElementID()).GetName());
                System.out.println("Typ des Elements: " + repo.GetElementByID(object.GetElementID()).GetStereotype());

                int id = object.GetElementID();
                Element element = repo.GetElementByID(id);
                switch(element.GetStereotype()){
                    case "FTAND":
                        executor.addElement(new EAElement(id, ElementTyp.ANDGate,element.GetName()));
                        break;
                    case "FTBasicEvent":
                        executor.addElement(new EAElement(id, ElementTyp.BasicEvent,element.GetName()));
                        break;
                    case "FTM/N":
                        String m = "M:"+getMofMooNGate(element);
                        executor.addElement( new EAElement(id, ElementTyp.MoonGate,m,element.GetName()));
                        break;
                    case "FTNOT":
                        executor.addElement(new EAElement(id, ElementTyp.NotGate,element.GetName()));
                        break;
                    case "FTOR":
                        executor.addElement(new EAElement(id, ElementTyp.ORGate,element.GetName()));
                        break;
                    case "FTXOR":
                        executor.addElement(new EAElement(id, ElementTyp.XORGate,element.GetName()));
                        break;
                    case "InputFailureMode":
                        break;
                    case "OutputFailureMode"://Extra Verbindung zu FT Element ziehen
                        EAElement correspondingEAElement = new EAElement(id,ElementTyp.Output,"",element.GetName());
                        setTypeOfPort(element.MiscData(0),correspondingEAElement);
                        executor.addElement(correspondingEAElement);
                        executor.addVerbindung(new EAVerbindung(ftRootElement.GetElementID(),id,"Output Of","",VerbindungsTyp.OutputOf));
                        break;
                    case "CFT":
                        break;
                    case "IESELogicalOutport":
                        break;
                    case "IESELogicalInport":
                        break;
                    case "IESELogicalComponent":
                        break;
                    case "IESELogicalComponentInstance":
                        break;
                    case "CFTInstance":
                        break;
                    case "FT":
                        break;
                    default:System.out.println("Element nicht identifiziert");
                }
            });
        });
    }


    /**
     * Methode um MiscData(0) von einem Element in den Typ zu übersetzen, ist es ein Standardtyp wird es als Eigenschaft dem zugehörigen EAElement
     * hinzugefügt, ist es ein Element aus dem EARepository wird eine Verbindung zwischen den Elementen gezogen. Liefert für Packages einen Fehler!
     * @param typeString String der mittels MiscData(0) ausgelesen wurde.
     */
    private void setTypeOfPort(final String typeString,final EAElement eaElement){
        if(typeString.contains("EABOOL")){eaElement.setProperty("Type:'Boolean'");}
        else if (typeString.contains("EAINT")){eaElement.setProperty("Type:'Integer'");}
        else if (typeString.equals("")){eaElement.setProperty("Type:'Null'");}
        else if (typeString.contains("EASTRING")){eaElement.setProperty("Type:'String'");}
        else if (typeString.contains("EAREAL")){eaElement.setProperty("Type:'Real'");}
        else if (typeString.contains("EAUNAT")){eaElement.setProperty("Type:'Unlimited Natural'");}
        else{executor.addVerbindung(new EAVerbindung(eaElement.getEAID(),repo.GetElementByGuid(typeString).GetElementID(),"Failure Type Of","",VerbindungsTyp.FailureTypeOf));}
    }

    /**
     * Methode um den Parameter M eines MooN Gates auszulesen
     * @param e EA Element des MooN Gates
     * @return String mit dem M Wert
     */
    private String getMofMooNGate(final Element e){
        final String[] m = new String[1];
        e.GetTaggedValues().forEach(property -> {
            if(property.GetName().equals("m")) m[0] =  property.GetValue();
        });

        return m[0];
    }
}
