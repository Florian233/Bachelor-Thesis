package Essarel;

import FTElemente.ElementTyp;
import FTElemente.VerbindungsTyp;
import Reader.IExecutor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser für eine Realisierung (Datei .cft) einer Komponente aus Essarel
 */
public class EssarelCFTParser {

    /**
     * Liste mit allen connection Knoten
     */
    private NodeList nListConnections;
    /**
     * Liste mit allen gate Knoten
     */
    private NodeList nListGates;
    /**
     * Liste mit allen basicEvent Knoten
     */
    private NodeList nListEvents;
    /**
     * Liste mit allen proxies Knoten
     */
    private NodeList nListInstances;
    /**
     * Liste mit allen cftInportProxies Knoten
     */
    private NodeList nListInput;
    /**
     * Liste mit allen cftOutportProxies Knoten
     */
    private NodeList nListOutput;
    /**
     * Executor, dem alle Elemente und Verbindungen, die der Datenbank hingefügt werden sollen, übergeben werden
     */
    private IExecutor<EssarelElement,EssarelVerbindung> executor;
    /**
     * zugeöriges ComponentData Objekt zu der Realisierung der Komponente, die hier verarbeitet wird
     */
    private EssarelComponentData data;
    /**
     * Pfad der als Eingabe für das Programm dient
     */
    private String dataPath;
    /**
     * HashMap, die die Elemente aus der XML Datei auf die daraus resultierenden EssarelElemente mappt, damit bei erstellen der Verbindungen die richtigen Start und Ziel EssarelElemente ermittelt werden können
     */
    private Map<Element,EssarelElement> elements = new HashMap<>();
    /**
     * Instanz von EssarelComponentDataContainer aus dem die hier benötigten EssarelComponentData Objekt von Proxies geholt werden
     */
    private EssarelComponentDataContainer componentDataStorage = EssarelComponentDataContainer.getInstance();

    protected EssarelCFTParser(final Element root, final IExecutor executor,final EssarelComponentData data,final String dataPath){
        this.executor = executor;
        this.data = data;
        this.dataPath = dataPath;
        nListConnections = root.getElementsByTagName("cftConnections");
        nListGates = root.getElementsByTagName("gates");
        nListEvents = root.getElementsByTagName("basicEvents");
        nListInstances = root.getElementsByTagName("proxies");
        nListInput = root.getElementsByTagName("cftInportProxies");
        nListOutput = root.getElementsByTagName("cftOutportProxies");
    }


    /**
     * EssarelElemente für Component und Input, Outputs erstellen + Verbindungen zwischen diesen gemäß dem Datenbankschema
     */
    public void parseCFT(){
        System.out.println("parsen von komponente: "+data.getName());
        EssarelElement component = data.getComponent();

        //Name zu den ports suchen, die vorher in der Reihenfolge wie in der Specification.interface Datei in ComponentData Objekt abgelegt wurden, daher stimmen die Indices überein
        //Dann Element erzeugen und Verbindung von Component zu dem Element herstellen
        for(int i = 0;i<nListInput.getLength();i++){
            String findIndex = ((Element)((Element)nListInput.item(i)).getElementsByTagName("port").item(0)).getAttribute("href");
            int index = findIndex.lastIndexOf(".");
            int portIndex = Integer.parseInt(findIndex.substring(index+1));
            EssarelElement input = new EssarelElement((Element) nListInput.item(i),ElementTyp.Input,data.getInputName(portIndex));
            executor.addElement(input);
            elements.put((Element) nListInput.item(i),input);
            executor.addVerbindung(new EssarelVerbindung(component,input,"Input Of", VerbindungsTyp.InputOf));
        }

        for(int i = 0;i<nListOutput.getLength();i++){
            String findIndex = ((Element)((Element)nListOutput.item(i)).getElementsByTagName("port").item(0)).getAttribute("href");
            int index = findIndex.lastIndexOf(".");
            int portIndex = Integer.parseInt(findIndex.substring(index+1));
            EssarelElement output = new EssarelElement((Element) nListOutput.item(i),ElementTyp.Output,data.getOutputName(portIndex));
            executor.addElement(output);
            elements.put((Element) nListOutput.item(i),output);
            executor.addVerbindung(new EssarelVerbindung(component,output,"Output Of", VerbindungsTyp.OutputOf));
        }

        parseBasicElements();

        parseInstances();

        //Muss als letztes stehen, weil dazu sich schon alle benötigten Elemente in der HashMap befinden müssen
        parseConnections();

    }

    /**
     * Für jeden connection Knoten aus der XML Datei das zugehörige Start und Ziel EssarelElement(Element das auch dem Executor zum Eintragen in die DB übergeben wurde)
     * holen und EssarelVerbindung erstellen, diese dann dem Executor übergeben.
     */
    private void parseConnections() {
        for(int i = 0;i<nListConnections.getLength();i++){

            EssarelVerbindung connection =new EssarelVerbindung(
                    parseConnectionString(((Element)nListConnections.item(i)).getAttribute("source")),
                    parseConnectionString(((Element)nListConnections.item(i)).getAttribute("target")),
                    ((Element) nListConnections.item(i)).getAttribute("name"),
                    VerbindungsTyp.FailurePropagation);
            executor.addVerbindung(connection);
        }
    }

    /**
     * Methode, die für einen href String aus der XML Datei das zugehörige EssarelElement ermittelt
     * @param source href String aus connection Element(XML)
     * @return Zugehöriges EssarelElement zu dem source String
     */
    private EssarelElement parseConnectionString(final String source){
        //NodeList (nList) aus der das Element stammt und dessen Index dort ermitteln und mittels der hashmap elements das zugehörige EssarelElement ausgeben
        System.out.println(source);
        int startElement = 0;
        startElement = source.indexOf("@");
        int startIndex = 0;
        startIndex = source.indexOf(".");
        int endIndex = 0;
        endIndex = source.lastIndexOf("/@");
        String elementType;
        int index;
        System.out.println(startElement+ " "+startIndex + " " + endIndex);
        if(endIndex<startIndex){
            elementType = source.substring(startElement + 1, startIndex);
            index = Integer.parseInt(source.substring(startIndex + 1));
        }else {
            elementType = source.substring(startElement + 1, startIndex);
            index = Integer.parseInt(source.substring(startIndex + 1, endIndex));
        }
        System.out.println("Connection parsen: "+elementType + " " + index);

        Element result = null;

        switch(elementType){
            case "basicEvents":
                result = (Element) nListEvents.item(index);
                break;
            case "gates":
                result = (Element) nListGates.item(index);
                break;
            case "proxies":
                int portIndex = source.lastIndexOf(".");
                int proxiePortTypeIndex = source.lastIndexOf("@");
                int listIndex = Integer.parseInt(source.substring(portIndex+1));
                String portType = source.substring(proxiePortTypeIndex+1,portIndex);
                System.out.println("Proxies parsen: "+portType + " " + listIndex);
                if(portType.equals("proxyInportInstances")){
                    result = (Element) ((Element)nListInstances.item(index)).getElementsByTagName("proxyInportInstances").item(listIndex);
                }else if(portType.equals("proxyOutportInstances")){
                    result = (Element) ((Element)nListInstances.item(index)).getElementsByTagName("proxyOutportInstances").item(listIndex);
                }
                break;
            case "cftInportProxies":
                result = (Element) nListInput.item(index);
                break;
            case "cftOutportProxies":
                result = (Element) nListOutput.item(index);
                break;
            default:System.out.println("Elementtyp der Verbindung konnte nicht zugeordnet werden.");
        }
        System.out.println(elements.get(result).getTyp());
        return elements.get(result);
    }

    /**
     * Element zu allen BasicEvents und Gates erstellen.
     */
    private void parseBasicElements(){

        //BasicEvents erstellen
        for(int i = 0;i<nListEvents.getLength();i++){
            EssarelElement basicEvent = new EssarelElement((Element) nListEvents.item(i),ElementTyp.BasicEvent,((Element)nListEvents.item(i)).getAttribute("name"));
            executor.addElement(basicEvent);
            elements.put((Element) nListEvents.item(i),basicEvent);
        }

        //Gates erstellen
        for(int i = 0;i<nListGates.getLength();i++){

            ElementTyp typ = null;
            Element gateElement = (Element) nListGates.item(i);
            Element typElement = (Element) gateElement.getElementsByTagName("gateType").item(0);
            switch(typElement.getAttribute("xsi:type")){
                case "de.essarel.realization:Or":
                    typ = ElementTyp.ORGate;
                    break;
                case "de.essarel.realization:Moon":
                    typ = ElementTyp.MoonGate;
                    break;
                case "de.essarel.realization:And":
                    typ = ElementTyp.ANDGate;
                    break;
                case "de.essarel.realization:Nor":
                    typ = ElementTyp.NORGate;
                    break;
                default:System.out.println("Typ des Gates konnte nicht ermittelt werden.");
            }
            //Wenn typ ermittelt werden konnte Element erstellen
            if(typ != null) {

                EssarelElement gate;
                if(typ.equals(ElementTyp.MoonGate)){ // Moon Gatter: auslesen von M unklar, m auf 1 setzen
                    gate = new EssarelElement((Element) nListGates.item(i), typ,"M:1", ((Element) nListGates.item(i)).getAttribute("name"));
                }else {

                    gate = new EssarelElement((Element) nListGates.item(i), typ, ((Element) nListGates.item(i)).getAttribute("name"));
                }
                executor.addElement(gate);
                elements.put((Element) nListGates.item(i), gate);
            }
        }

    }

    /**
     * Für jede Instanz ein EssarelElement erstellen, ebenso für dessen In und Outputs und die Verbindungen zwischen den In und Outputs und der Instanz
     */
    private void parseInstances(){

        for(int i = 0;i<nListInstances.getLength();i++){

            //Eingebettete Elemente auslesen
            NodeList output = ((Element) nListInstances.item(i)).getElementsByTagName("proxyOutportInstances");
            NodeList input = ((Element) nListInstances.item(i)).getElementsByTagName("proxyInportInstances");
            Element component = (Element) ((Element) nListInstances.item(i)).getElementsByTagName("component").item(0);

            //Zugehörige ComponentData holen mittels dem Pfad zum component.xml der Component
            EssarelComponentData instanceData = componentDataStorage.getComponentData(dataPath+"\\"+component.getAttribute("href").replace("../../../","").replace("#/",""));

            System.out.println("Parsen von Instanz: "+instanceData.getName());

            //Name der Component auslesen, wenn keiner vorhanden, dann wird der Name der Realisierung der Instanz genommen und Instanz angehängt
            String name = ((Element)nListInstances.item(i)).getAttribute("name");
            if(name.equals("")){
                name = instanceData.getCftName()+" Instance";
            }

            //EssarelElement für Instanz erzeugen
            EssarelElement instance = new EssarelElement(((Element) nListInstances.item(i)),ElementTyp.CFTInstance,name);
            //Instanz dem Executor übergeben und der Map hinzufügen
            executor.addElement(instance);
            elements.put((Element) nListInstances.item(i),instance);//wird eigentlich nicht benötigt, da alle verbindung zu und von dem Element hier schon gezogen wurden
            //Verbindung von Instanz zu Realisierung ziehen
            executor.addVerbindung(new EssarelVerbindung(instance,instanceData.getComponent(),"Instance Of",VerbindungsTyp.InstanceOf));

            //Outputs erzeugen und Verbindungen zu Instance ziehen
            for(int j = 0;j<output.getLength();j++){
                String findIndex = ((Element)((Element)output.item(j)).getElementsByTagName("port").item(0)).getAttribute("href");
                int index = findIndex.lastIndexOf(".");
                int portIndex = Integer.parseInt(findIndex.substring(index+1));
                System.out.println("Index von Port: "+portIndex);
                EssarelElement outputElement = new EssarelElement((Element) output.item(j),ElementTyp.OutputInstance,instanceData.getOutputName(portIndex));
                elements.put((Element) output.item(j),outputElement);
                executor.addElement(outputElement);
                executor.addVerbindung(new EssarelVerbindung(
                        instance,outputElement,"Output Of",VerbindungsTyp.OutputOf
                ));
            }

            //inputs erzeugen und Verbindungen zu Instance ziehen
            for(int k = 0;k<input.getLength();k++){
                String findIndex = ((Element)((Element)input.item(k)).getElementsByTagName("port").item(0)).getAttribute("href");
                int index = findIndex.lastIndexOf(".");
                int portIndex = Integer.parseInt(findIndex.substring(index+1));
                EssarelElement inputElement = new EssarelElement((Element) input.item(k),ElementTyp.InputInstance,instanceData.getInputName(portIndex));
                elements.put((Element) input.item(k),inputElement);
                executor.addElement(inputElement);
                executor.addVerbindung(new EssarelVerbindung(
                        instance,inputElement,"Input Of",VerbindungsTyp.InputOf
                ));
            }
        }
    }
}
