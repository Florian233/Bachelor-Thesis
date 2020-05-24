package de.tu.fk.neo4jexport.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.ConnectorView;
import com.nomagic.magicdraw.uml.symbols.shapes.PartView;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;

import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseNodeLabels;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseRelationshipLabels;
import de.tu.fk.neo4jexport.ModelElements.Connection;
import de.tu.fk.neo4jexport.ModelElements.Node;
import de.tu.fk.neo4jexport.utility.ModelReadingHelper;

public class FunctionalViewConverter {

	
	private ModelCreator modelCreator = ModelCreator.getInstance();
	private Component root;
	private Node rootNode;
	
	//Listen für die einzelnen "Bauteile" aus MagicDraw
	List<Connector> connectors = new ArrayList<Connector>();
	//Liste in die jede Realisierung einer Instanz genau einmal eingefügt wird
	List<Component> instances = new ArrayList<Component>();
	List<Port> ports = new ArrayList<Port>();
	//List<String> convertedSubcomponents = new ArrayList<String>();
	
	//Listen für das Auslesen des Diagrams
	List<ConnectorView> connections = new ArrayList<ConnectorView>();
	List<PartView> instancesOfDiagramList = new ArrayList<PartView>();
	
	//Map um Connection zu den richtigen Ports zu ziehen - für Ports aus den Instanzen!
	private Map<String,Node> portMap = new HashMap<String,Node>();
	
	//Map um schon ausgelese Instanzen zu speichern für InstanceOf Verbindung
	private Map<String,List<Node>> instancesMap = new HashMap<String,List<Node>>();
	
	
	public FunctionalViewConverter(){};
	
	public Node convertfunctionalComponent(final Component c){
		this.root = c;
		
		
		//JOptionPane.showMessageDialog(null,"Auslesen von "+c.getHumanName());
		//Elemente in der Component auslesen
		for(Element e:root.getOwnedElement()){
			switch(e.getHumanType()){
				case "ComponentInstance":
					if(!instances.contains(ModelReadingHelper.getComponent(e))){
						instances.add(ModelReadingHelper.getComponent(e));
					}
					break;
				case "Connection":connectors.add((Connector) e);
					break;
				case "Inport":ports.add((Port) e);
					break;
				case "Outport":ports.add((Port)e);
					break;			
			}
		}
		
		DiagramPresentationElement diagram = ModelReadingHelper.getDiagramPresentationElement(c);
		for(PresentationElement e:diagram.getPresentationElements()){
			switch(e.getHumanType()){
				case "Connection":connections.add((ConnectorView) e);
					break;
				case "ComponentInstance":instancesOfDiagramList.add((PartView) e);
					break;
			}
		}
		
		if(!ports.isEmpty()){//Knoten wird nur erstellt, wenn die Komponente auch Ports hat, ansonsten ist sie sowieso unbrauchbar
			//Wurzel Knoten erstellen
			rootNode = new Node(root.getID(),root.getName(),DatabaseNodeLabels.LogicalComponent);
			modelCreator.addNode(rootNode);
		}
		
		//Zugehörige Nodes zu den Ports erzeugen
		convertPorts();
		
		//Ports aus den Instanzen holen, zugehörige Node erstellen und in die Map einfügen
		//Mit den Components der Instanzen weitermachen
		convertInstances();
		
		//Connections übernehmen
		convertConnections();
		
		//Subkomponenten umwandeln
		convertSubComponents();
		
		
		//FailureView auslesen
		Component fv = ModelReadingHelper.getFailureView(root);

		Node failureView = null;
		//Wenn fv null ist, dann hat die Komponente wohl kein Fehlermodell
		if(fv != null) failureView = new FailureLogicalViewConverter().convertFailureModel(fv);
		
		//Verbindung zwischen LogicalComponent und CFT - wenn allerdings kein Knoten zurückgegeben wird, hat das Fehlermodell keine Failure Modes und kann deshalb ignoriert werden
		if(failureView != null && rootNode != null)modelCreator.addConnection(new Connection(rootNode,failureView,modelCreator.getInternID(),"FailureModelOf",DatabaseRelationshipLabels.FailureModelOf));
						
		return rootNode;
	}
	
	
	//Methode startet die Konvertierung der Subkomponenten, vorher wird im modelCreator gesprüft, ob die Subkomponente schon konvertiert wurde
	private void convertSubComponents() {
		for(Component c:instances){
			//if(!convertedSubcomponents.contains(c.getID())){
				if(c == null)continue;
				Node n = modelCreator.isConverted(c.getID());
				if( n == null)n = new FunctionalViewConverter().convertfunctionalComponent(c);
				if(instancesMap.containsKey(c.getID())){
					for(Node node:instancesMap.get(c.getID())){
						modelCreator.addConnection(new Connection(n,node,modelCreator.getInternID(),"Instance Of",DatabaseRelationshipLabels.InstanceOf));
					}
				}
				//convertedSubcomponents.add(c.getID());
			//}
		}	
	}

	private void convertConnections(){
		for(ConnectorView cv:connections){
			PresentationElement x = cv.getClient();
			PresentationElement y = cv.getSupplier();
			
			Node xNode = getCorrespondingNode(x);
			Node yNode = getCorrespondingNode(y);
			
			modelCreator.addConnection(new Connection(xNode,yNode,modelCreator.getInternID(),cv.getName(),DatabaseRelationshipLabels.InformationFlow));
		}
	}
	
	private void convertInstances(){
		//Knoten für die Instanz erstellen und in einer Map(Realisierung->Instanz) speichern um später die InstanceOf Verbindung zu ziehen
		for(PartView p:instancesOfDiagramList){
			Node instanceRoot = new Node(p.getID(),p.getName(),DatabaseNodeLabels.LogicalComponentInstance);
			modelCreator.addNode(instanceRoot);
			String id = ((Property)p.getElement()).getType().getID();
			if(instancesMap.containsKey(id)){
				List<Node> temp = instancesMap.get(id);
				temp.add(instanceRoot);
				instancesMap.put(id, temp);
			}else{
				List<Node> temp = new ArrayList<Node>();
				temp.add(instanceRoot);
				instancesMap.put(id, temp);
			}

			//Ports der Instanz auslesen
			for(PresentationElement pe:p.getPresentationElements()){
				switch(pe.getHumanType()){
					case "Inport":
						Node n = new Node(pe.getID(),pe.getName(),DatabaseNodeLabels.InportInstance);
						portMap.put(pe.getID(), n);
						modelCreator.addNode(n);
						modelCreator.addConnection(new Connection(instanceRoot,n,modelCreator.getInternID(),"Inport Of",DatabaseRelationshipLabels.InportOf));
						break;
					case "Outport":
						Node node = new Node(pe.getID(),pe.getName(),DatabaseNodeLabels.OutportInstance);
						portMap.put(pe.getID(), node);
						modelCreator.addNode(node);
						modelCreator.addConnection(new Connection(instanceRoot,node,modelCreator.getInternID(),"Outport Of",DatabaseRelationshipLabels.OutportOf));
						break;
				}
			}
		}	
	}
	
	private void convertPorts(){
		for(Port p:ports){		
			Node n;
			if(p.getHumanType().equals("Inport")){
				n = new Node(p.getID(),p.getName(),DatabaseNodeLabels.Inport);
				modelCreator.addNode(n);
				modelCreator.addConnection(new Connection(rootNode,n,modelCreator.getInternID(),"Inport Of",DatabaseRelationshipLabels.InportOf));
			}else{
				n = new Node(p.getID(),p.getName(),DatabaseNodeLabels.Outport);
				modelCreator.addNode(n);
				modelCreator.addConnection(new Connection(rootNode,n,modelCreator.getInternID(),"Outport Of",DatabaseRelationshipLabels.OutportOf));
				
			}
			
			//FailureType auslesen
			Property prop = ModelReadingHelper.getFailureTypeOfPort(p);
			if(prop != null){//Wenn Fehlertyp existent, Verbindung zu diesem einfügen
				Node ft = modelCreator.getNodeByID(prop.getID());
				modelCreator.addConnection(new Connection(n,ft,modelCreator.getInternID(),"Failure Type Of",DatabaseRelationshipLabels.FailureTypeOf));
			}
		}
	}

	/*
	 * Methode prüft, ob der gesuchte Port in dieser Klasse als Element des Diagramms ausgelesen wurde mit der portMap, die alle Ports, der Instanzen speichert.
	 * Notwendig um die die Verbindung zwischen den Elementen, die aus dem Diagramm ausgelesen wurden, und den Elementen die mittels GetOwnedElement ermittelt wurden hinzugrigen
	 */
	private Node getCorrespondingNode(final PresentationElement e){
		if(portMap.containsKey(e.getID())){
			return portMap.get(e.getID());
		}
		return modelCreator.getNodeByID(e.getElement().getID());
	}
	
}
