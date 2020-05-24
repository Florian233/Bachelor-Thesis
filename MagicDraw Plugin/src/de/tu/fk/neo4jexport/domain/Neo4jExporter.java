package de.tu.fk.neo4jexport.domain;

import java.util.List;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.ConnectorView;
import com.nomagic.magicdraw.uml.symbols.shapes.PartView;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.profiles.FailurePropagationModelProfile;
import de.tu.fk.neo4jexport.DatabaseConnection.DBConnection;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseNodeLabels;
import de.tu.fk.neo4jexport.DatabaseConnection.DatabaseRelationshipLabels;
import de.tu.fk.neo4jexport.DatabaseConnection.NotAbleToConnectToDatabaseException;
import de.tu.fk.neo4jexport.utility.ModelReadingHelper;
import de.tu.fk.neo4jexport.utility.Stereotypes;

public class Neo4jExporter 
{
	public static void exportComponent(Component component)
	{
		//JOptionPane.showMessageDialog(null, gatherSomeInfo(component));
		
		testDatabaseConnection();
		new FailureTypeSystemConverter().convertFTS(ModelReadingHelper.getFailureTypeSystem());
		new FunctionalViewConverter().convertfunctionalComponent(component);
		ModelCreator.getInstance().startCreation();
		DBConnection.getDBConnectionInstance().closeConnection();
		JOptionPane.showMessageDialog(null, "Überführen des Modells in die Datenbank abgeschlossen.");
	
	}
	
	public static void exportCft(Component cft)
	{
		//JOptionPane.showMessageDialog(null, gatherSomeInfo(cft));
		testDatabaseConnection();
		new FailureTypeSystemConverter().convertFTS(ModelReadingHelper.getFailureTypeSystem());
		new FailureLogicalViewConverter().convertFailureModel(cft,true);
		ModelCreator.getInstance().startCreation();
		JOptionPane.showMessageDialog(null, "Überführen des Modells in die Datenbank abgeschlossen.");
	}
	
	private static void testDatabaseConnection(){
		try{
			DBConnection.getDBConnectionInstance().connectToDatabase();
		}catch(NotAbleToConnectToDatabaseException e){
			JOptionPane.showMessageDialog(null, "Datenbankverbindung kann nicht aufgebaut werden.");
			showDatabaseDialog();
		}
	}
	
	private static void showDatabaseDialog(){
		String url = JOptionPane.showInputDialog("Database Url");
		String username = JOptionPane.showInputDialog("Database username");
		String pw = JOptionPane.showInputDialog("Database password");
		
		DBConnection db = DBConnection.getDBConnectionInstance();
		db.setUrl(url);
		db.setPasswd(pw);
		db.setUsername(username);
		
		testDatabaseConnection();
	}
	
	private static String gatherSomeInfo(Component c)
	{
		StringBuilder sb = new StringBuilder("Anfang "+c.getHumanName()+" owned elements:\n");
		
		Project project = Application.getInstance().getProject();
		
		Object o = ModelReadingHelper.getDiagramPresentationElement(c);
		
		if (o instanceof DiagramPresentationElement)
		{
			
			DiagramPresentationElement cftDiagram = (DiagramPresentationElement) o;
			for (PresentationElement pe : cftDiagram.getPresentationElements()){
				//sb.append("- " + pe.getHumanName() +"  Und: "+pe.getClassType().getName()+ "  Und Typ:"+pe.getHumanType()+"  Und id:"+pe.getID()+"\n");
				//sb.append("- "+pe.getElement().getID()+"\n");
				if(pe.getHumanType().equals("OutgoingFtConnection")||pe.getHumanType().equals("InportMapping")||pe.getHumanType().equals("OutportMapping")){
					ConnectorView con = (ConnectorView) pe;
					PresentationElement x = con.getClient();
					PresentationElement y = con.getSupplier();
					//sb.append("Name+ID start: "+x.getHumanName()+"  "+x.getHumanType()+"  "+x.getID()+"\n");
					//sb.append("Name+ID ziel: "+y.getHumanName()+"  "+y.getHumanType()+"  "+y.getID()+"\n");
				}else if(pe.getHumanType().equals("ComponentInstance")){
					PartView pv = (PartView) pe;
					//sb.append(((Property)pv.getElement()).getType().getID()+"  "+pv.getEncapsulatedClassifierProvider().getID()+"\n");
					//for(PresentationElement pp:pv.getPresentationElements()){
						//sb.append("++ " + pp.getHumanName() +"  Und: "+pp.getClassType().getName()+ "  Und Typ:"+pp.getHumanType()+" und id"+pp.getID()+"\n");
					//}
				}else if(pe.getHumanType().equals("CFTComponentInstance")){
					PartView pv = (PartView) pe;
					//sb.append(((Property)pv.getElement()).getType().getID()+"  "+pv.getEncapsulatedClassifierProvider().getID()+"\n");
					for(PresentationElement pp:pv.getPresentationElements()){
						//sb.append("++ " + pp.getHumanName() +"  Und: "+pp.getClassType().getName()+ "  Und Typ:"+pp.getHumanType()+" und id"+pp.getID()+"\n");
					}
				}

			}
		}
	
		//Object o = UtilStereotypesHelper.getStereotypePropertyFirst(c, Stereotypes.getCompST(project),"itsFailureView");
		
		//Component fv = (Component) o;
		
		//sb.append("FailureView: "+fv.getHumanName()+"\n");
		sb.append("Normale Elemente:\n");
		for (Element e : c.getOwnedElement()){
			sb.append("- " + e.getHumanName() +"  Und: "+e.getClass().getName()+ "  Und Typ:"+e.getHumanType()+"  Und id:"+e.getID()+"\n");
			//if(e.getHumanType().equals("SacInport")){
				//Object o = UtilStereotypesHelper.getStereotypePropertyFirst(e, SafetyAspectComponentProfile.Stereotypes.Inport.getStereotype(project),"itsSacReference");
				//Port p = (Port)o;
				//sb.append("++ "+p.getHumanName()+"  "+p.getHumanType()+"\n");
				//sb.append(o.getClass().getName()+"\n");
			//}
			//sb.append("+ "+e.getID()+"\n");
			if(e.getHumanType().equals("FailureInport")){
				//sb.append("PORT ID:"+e.getID()+"\n");
				if(ModelReadingHelper.getFailureTypeOfPort((Port) e)!=null){
					//sb.append(ModelReadingHelper.getFailureTypeOfPort((Port)e).getName()+"\n");
					Property a = ModelReadingHelper.getFailureTypeOfPort((Port)e);
					//sb.append(a.getOwner());
				}
			}else if(e.getHumanType().equals("Outport")||e.getHumanType().equals("Inport")){
				//sb.append("PORT ID:"+e.getID()+"\n");
			}
			if(e.getHumanType().equals("ComponentInstance")){
				//sb.append("Instance:"+e.getID()+"   "+((Property) e).getType().getID()+"\n");
				//sb.append(gatherSomeInfo(getComponentClass((Property) e)));
			}else if(e.getHumanType().equals("OutgoingFtConnection")||e.getHumanType().equals("InportMapping")||e.getHumanType().equals("OutportMapping")){
				Connector con = (Connector) e;
				Element x = ModelHelper.getClientElement(con);
				Element y = ModelHelper.getSupplierElement(con);
				//sb.append("Name+ID start: "+x.getHumanName()+"  "+x.getHumanType()+"  "+x.getID()+"\n");
				//sb.append("Name+ID ziel: "+y.getHumanName()+"  "+y.getHumanType()+"  "+y.getID()+"\n");
			}else if(e.getHumanType().equals("CFTComponentInstance")){
				sb.append("Instance:"+e.getID()+"   "+((Property) e).getType().getID()+"\n");
			}else if(e.getHumanType().equals("SacInport")||e.getHumanType().equals("SacOutport")){
				//sb.append("Aufgelöste Sac Ref: "+ModelReadingHelper.resolveItsSacReference((Port)e).getID());
			}else if(e.getHumanType().equals("InputFailureMode")||e.getHumanType().equals("OutputFailureMode")){
				//Element ele = ModelReadingHelper.resolveItsFailurePortReference((Property)e);
				//sb.append(ele.getID()+"  "+ele.getHumanType()+"\n");
				if(ModelReadingHelper.getFailureTypeOfFailureMode((Property) e)!=null){
					//sb.append("FailureType: "+ModelReadingHelper.getFailureTypeOfFailureMode((Property) e).getName()+"\n");
			
				}
			
			}
		}
		sb.append("Ende "+c.getHumanName()+"\n");
		return sb.toString();
	}
	
	private static Component getComponentClass(final Property compInst){
		Component compClass = (Component) compInst.getType();
		return compClass;
	}
	
}
