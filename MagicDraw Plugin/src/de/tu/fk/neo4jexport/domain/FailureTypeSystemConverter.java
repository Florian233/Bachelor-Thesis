package de.tu.fk.neo4jexport.domain;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;

import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseNodeLabels;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseRelationshipLabels;
import de.tu.fk.neo4jexport.ModelElements.Connection;
import de.tu.fk.neo4jexport.ModelElements.Node;
import de.tu.fk.neo4jexport.utility.ModelReadingHelper;

public class FailureTypeSystemConverter {

	private ModelCreator modelCreator = ModelCreator.getInstance();
	
	private List<Property> elements = new ArrayList<Property>();
	
	private List<Connector> connections = new ArrayList<Connector>();
	
	public void convertFTS(final Element fts){
		
		for(Element e:fts.getOwnedElement()){
			if(e.getHumanType().equals("FailureType")){
				elements.add((Property) e);
			}else if(e.getHumanType().equals("Generalization")){
				connections.add((Connector) e);
				
			}
		}
		
		for(Property p:elements){
			modelCreator.addNode(new Node(p.getID(),p.getName(),DatabaseNodeLabels.FailureType));
		}
		
		for(Connector c:connections){
			Element x = ModelReadingHelper.getConnectionSource(c);
			Element y = ModelReadingHelper.getConnectionTarget(c);
			
			Node xNode = modelCreator.getNodeByID(x.getID());
			Node yNode = modelCreator.getNodeByID(y.getID());
			
			modelCreator.addConnection(new Connection(xNode,yNode,c.getID(),c.getName(),DatabaseRelationshipLabels.SuperFailureType));
		}
	}
	
}
