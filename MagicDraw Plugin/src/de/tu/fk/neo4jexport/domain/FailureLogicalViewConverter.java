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

public class FailureLogicalViewConverter {
	
	private ModelCreator modelCreator = ModelCreator.getInstance();
	private Component root;
	private Node rootNode = null;
	
	//Listen für die einzelnen "Bauteile" aus MagicDraw

	//List<Component> instances = new ArrayList<Component>();

	List<Property> gates = new ArrayList<Property>();
	List<Property> ports = new ArrayList<Property>();
	
	List<Connector> portMappingsList = new ArrayList<Connector>();
	
	//Aus dem Diagram
	List<ConnectorView> connections = new ArrayList<ConnectorView>();
	List<PartView> instancesOfDiagram = new ArrayList<PartView>();
	
	//Map um Connection zu den richtigen duplizierten Ports zu ziehen
	private Map<String,Node> portMap = new HashMap<String,Node>();
	
	private boolean startInstanceConvertion = false;
	
	public Node convertFailureModel(final Component c, final boolean startInstanceConvertion){
		this.startInstanceConvertion = startInstanceConvertion;
		return convertFailureModel(c);
	}
	
	public Node convertFailureModel(final Component c){
		root = c;
		
		//JOptionPane.showMessageDialog(null,"Auslesen von "+c.getHumanName());
		
		//Elements auslesen - Basiselemente und Portmapping
		for(Element e:root.getOwnedElement()){
			switch(e.getHumanType()){
				case "Or":gates.add((Property)e);
					break;
				case "Xor":gates.add((Property)e);
					break;
				case "Not":gates.add((Property)e);
					break;
				case "And":gates.add((Property)e);
					break;
				case "M/N":gates.add((Property)e);
					break;
				case "CFTComponentInstance":
					Component comp = ModelReadingHelper.getComponent(e);
					if(modelCreator.isConverted(comp.getID()) == null && startInstanceConvertion){
						new FailureLogicalViewConverter().convertFailureModel(comp,true);
					}
					break;
				case "SacOutport"://ports.add((Port)e); 
					break;
				case "SacInport"://ports.add((Port)e);
					break;
				case "BasicEvent":gates.add((Property)e);
					break;
				case "FailureInport"://ports.add((Port)e);
					break;
				case "FailureOutport"://ports.add((Port)e);
					break;
				case "InportMapping":portMappingsList.add((Connector)e);
					break;
				case "OutportMapping":portMappingsList.add((Connector)e);
					break;
				case "InputFailureMode":ports.add((Property)e);
					break;
				case "OutputFailureMode":ports.add((Property)e);
					break;
			}
		}
		if(!ports.isEmpty()){//Knoten nur erstellen, wenn auch eine Verbindung zu irgendetwas existiert
			//Knoten für diesen CFT erstellen
			rootNode = new Node(c.getID(),c.getName(),DatabaseNodeLabels.CFT);
			modelCreator.addNode(rootNode);
		}
		
		//Diagram auslesen - Instanzen und Fehlerweiterleitung wird aus Diagram verwendet
		DiagramPresentationElement diagram = ModelReadingHelper.getDiagramPresentationElement(c);
		for(PresentationElement e:diagram.getPresentationElements()){
			switch(e.getHumanType()){
				case "InportMapping"://connections.add((ConnectorView) e);
					break;
				case "OutportMapping"://connections.add((ConnectorView) e);
					break;
				case "OutgoingFtConnection":connections.add((ConnectorView) e);
					break;
				case "CFTComponentInstance":instancesOfDiagram.add((PartView) e);
					break;
			}
		}
		
		convertBasicElements();
		
		convertInstances();
		
		convertConnections();
		
		//nur für testzwecke
		//for(Component comp:instances){
			//new LogicalViewConverter().convertFailureModel(comp);
		//}
		
		return rootNode;
	}


	private void convertConnections() {
		for(ConnectorView cv:connections){
			PresentationElement x = cv.getClient();
			PresentationElement y = cv.getSupplier();
			
			Node xNode = getCorrespondingNode(x);
			Node yNode = getCorrespondingNode(y);
			//Fehlerweiterleitung übernehmen
			switch(cv.getHumanType()){
				case "InportMapping"://modelCreator.addConnection(new Connection(xNode,yNode,modelCreator.getInternID(),cv.getName(),DatabaseRelationshipLabels.PortMapping));
					break;
				case "OutportMapping"://modelCreator.addConnection(new Connection(xNode,yNode,modelCreator.getInternID(),cv.getName(),DatabaseRelationshipLabels.PortMapping));
					break;
				case "OutgoingFtConnection":modelCreator.addConnection(new Connection(xNode,yNode,modelCreator.getInternID(),cv.getName(),DatabaseRelationshipLabels.FailurePropagation));
					break;
			}
		}
		
		//Portmapping übernehmen
		for(Connector con:portMappingsList){
			Element start = ModelReadingHelper.getConnectionSource(con);
			Element end = ModelReadingHelper.getConnectionTarget(con);
			//Dazu wird SAC Ref aufgelöst und verbindung zu richtigem Port der funktionalen Komponente gezogen
			if(start.getHumanType().equals("SacInport")||start.getHumanType().equals("SacOutport")){
				
				String resolvedSacRefElementID = ModelReadingHelper.resolveItsSacReference((Port) start).getID();
				Node startNode = modelCreator.getNodeByID(resolvedSacRefElementID);
				Node endNode = modelCreator.getNodeByID(end.getID());
				
				modelCreator.addConnection(new Connection(startNode,endNode,con.getID(),con.getName(),DatabaseRelationshipLabels.PortMapping));
				
			}else if(end.getHumanType().equals("SacInport")||end.getHumanType().equals("SacOutport")){
				
				String resolvedSacRefElementID = ModelReadingHelper.resolveItsSacReference((Port) end).getID();
				Node endNode = modelCreator.getNodeByID(resolvedSacRefElementID);
				Node startNode = modelCreator.getNodeByID(start.getID());
				
				modelCreator.addConnection(new Connection(startNode,endNode,con.getID(),con.getName(),DatabaseRelationshipLabels.PortMapping));
			
			}
			
			
		}
	}

	private void convertInstances() {
		for(PartView p:instancesOfDiagram){
			//Knoten für die Instanz erstellen und im modelCreator eintragen zur späteren Verbindung zur Realisierung
			Node instanceRoot = new Node(p.getID(),p.getName(),DatabaseNodeLabels.CFTInstance);
			modelCreator.addNode(instanceRoot);
			String id = ((Property)p.getElement()).getType().getID();
			modelCreator.addFailureViewInstanceToMap(instanceRoot, id);
			//Failure Modes der Instanz auslesen 
			for(PresentationElement pe:p.getPresentationElements()){
				switch(pe.getHumanType()){
					case "FailureInport":
						Node n = new Node(pe.getID(),pe.getName(),DatabaseNodeLabels.InputInstance);
						portMap.put(pe.getID(), n);
						modelCreator.addNode(n);
						modelCreator.addConnection(new Connection(instanceRoot,n,modelCreator.getInternID(),"Input Of",DatabaseRelationshipLabels.InputOf));
						break;
					case "FailureOutport":
						Node node = new Node(pe.getID(),pe.getName(),DatabaseNodeLabels.OutputInstance);
						portMap.put(pe.getID(), node);
						modelCreator.addNode(node);
						modelCreator.addConnection(new Connection(instanceRoot,node,modelCreator.getInternID(),"Output Of",DatabaseRelationshipLabels.OutputOf));
						break;
				}
			}
		}
		
	}

	private void convertBasicElements() {
		for(Property e:gates){
			switch(e.getHumanType()){
				case "Or":
					modelCreator.addNode(new Node(e.getID(),e.getName(),DatabaseNodeLabels.ORGate));
					break;
				case "Xor":
					modelCreator.addNode(new Node(e.getID(),e.getName(),DatabaseNodeLabels.XORGate));
					break;
				case "Not":
					modelCreator.addNode(new Node(e.getID(),e.getName(),DatabaseNodeLabels.NotGate));
					break;
				case "And":
					modelCreator.addNode(new Node(e.getID(),e.getName(),DatabaseNodeLabels.ANDGate));
					break;
				case "M/N":
					modelCreator.addNode(new Node(e.getID(),e.getName(),DatabaseNodeLabels.MoonGate));
					break;
				case "BasicEvent":
					modelCreator.addNode(new Node(e.getID(),e.getName(),DatabaseNodeLabels.BasicEvent));
					break;
			}
		}
		
		for(Property p:ports){
			Node n = null;
			//Port port;
			switch(p.getHumanType()){
				case "InputFailureMode":
					n = new Node(p.getID(),p.getName(),DatabaseNodeLabels.Input);
					modelCreator.addNode(n);
					modelCreator.addConnection(new Connection(rootNode,n,modelCreator.getInternID(),"Input Of",DatabaseRelationshipLabels.InputOf));
					break;
				case "OutputFailureMode":
					n = new Node(p.getID(),p.getName(),DatabaseNodeLabels.Output);
					modelCreator.addNode(n);
					modelCreator.addConnection(new Connection(rootNode,n,modelCreator.getInternID(),"Output Of",DatabaseRelationshipLabels.OutputOf));
					break;
				case "SacOutport":
					//port = ModelReadingHelper.resolveItsSacReference(p);
					//n = modelCreator.getNodeByID(port.getID());
					//modelCreator.
					break;
				case "SacInport":
					//port = ModelReadingHelper.resolveItsSacReference(p);
					//n = modelCreator.getNodeByID(port.getID());
					break;
			}
			//FailureType auslesen
			Property prop = ModelReadingHelper.getFailureTypeOfFailureMode(p);
			if(prop != null && n != null ){
				Node ft = modelCreator.getNodeByID(prop.getID());
				modelCreator.addConnection(new Connection(n,ft,modelCreator.getInternID(),"Failure Type Of",DatabaseRelationshipLabels.FailureTypeOf));
			}
		}
		
	}
		
	private Node getCorrespondingNode(final PresentationElement e){
		if(portMap.containsKey(e.getID())){
			return portMap.get(e.getID());
		}
		return modelCreator.getNodeByID(e.getElement().getID());
	}

}
