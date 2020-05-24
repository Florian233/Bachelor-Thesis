 package de.tu.fk.neo4jexport.utility;

import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.profiles.FailurePropagationModelProfile;

//Klasse für ein paar Hilfsmethoden um Referenzen im Modell aufzulösen
public class ModelReadingHelper {
	
	public static Element getConnectionSource(final Connector c){
		return 	ModelHelper.getClientElement(c);
	}
	
	public static Element getConnectionTarget(final Connector c){
		return ModelHelper.getSupplierElement(c);
	}
	
	//Liefert zu einer Instanz das Component Element der Realisierung
	public static Component getComponent(final Property p){
		return (Component) p.getType();
	}
	
	public static Component getComponent(final Element e){
		return getComponent((Property)e);
	}
	
	public static Port resolveItsSacReference(final Port p){
        final Stereotype functionalOutportST = FailurePropagationModelProfile.Stereotypes.SacOutport.getStereotype();
        final Stereotype functionalInportST = FailurePropagationModelProfile.Stereotypes.SacInport.getStereotype();
        
        boolean isOutport = UtilStereotypesHelper.hasStereotype(p, functionalOutportST);
        boolean isInport = UtilStereotypesHelper.hasStereotype(p, functionalInportST);
        
        Port o = null;
        
        if(isOutport){
        	o =(Port) StereotypesHelper.getStereotypePropertyFirst(p, functionalOutportST, "itsSacReference");
        }else if(isInport){
        	o =(Port) StereotypesHelper.getStereotypePropertyFirst(p, functionalInportST, "itsSacReference");
        }
        
        return o;
        
	}
	
	public static Port resolveItsFailurePortReference(final Property p){
		
		final Stereotype failureOutputST = FailurePropagationModelProfile.Stereotypes.OutputFailureMode.getStereotype();
        final Stereotype failureInputST = FailurePropagationModelProfile.Stereotypes.InputFailureMode.getStereotype();
        
        boolean isOutport = UtilStereotypesHelper.hasStereotype(p, failureOutputST);
        boolean isInport = UtilStereotypesHelper.hasStereotype(p, failureInputST);
        
        Port o = null;
        
        if(isOutport){
        	o =(Port) StereotypesHelper.getStereotypePropertyFirst(p, failureOutputST, "itsFailurePort");
        }else if(isInport){
        	o =(Port) StereotypesHelper.getStereotypePropertyFirst(p, failureInputST, "itsFailurePort");
        }
        
        return o;
	}
	
	//Liefert die Fehlersicht einer funktionalen Componente zurück
	public static Component getFailureView(final Component c){
		Project project = Application.getInstance().getProject();
		
		Object o = UtilStereotypesHelper.getStereotypePropertyFirst(c, Stereotypes.getCompST(project),"itsFailureView");
		
		return (Component) o;
	}
	
	//Liefert zu einer Component das zugehörige DiagramPresentationElement um das Diagramm auszulesen
	public static DiagramPresentationElement getDiagramPresentationElement(final Component c){
		Project project = Application.getInstance().getProject();

		Object o = null;

		o = c.getOwnedDiagram().iterator().next();
		
		DiagramPresentationElement diagramPresEle = null;
		
		if (o instanceof Diagram)
		{
			Diagram dia = (Diagram) o;
			diagramPresEle = project.getDiagram(dia);
		}
		if(diagramPresEle != null)diagramPresEle.open();
		return diagramPresEle;
	}
	
	//Fehlertyp eines Ports auslesen
	public static Property getFailureTypeOfPort(final Port p){
		final Stereotype failureInportST = FailurePropagationModelProfile.Stereotypes.FailureInport.getStereotype();
        final Stereotype failureOutportST = FailurePropagationModelProfile.Stereotypes.FailureOutport.getStereotype();
        
        boolean isOutport = UtilStereotypesHelper.hasStereotype(p, failureOutportST);
        boolean isInport = UtilStereotypesHelper.hasStereotype(p, failureInportST);
        
        Stereotype stereotype = isOutport ? failureOutportST:failureInportST;
        Property failureType = (Property) StereotypesHelper.getStereotypePropertyFirst(p, stereotype, "failureType");
        return failureType;
	}
	
	public static Property getFailureTypeOfFailureMode(final Property p){
		
		Port e = resolveItsFailurePortReference(p);
		return getFailureTypeOfPort(e);
	}
	
	//Liefert das Fehlertypen-System des aktuellen Projekts
	public static Element getFailureTypeSystem(){
		Project project = Application.getInstance().getProject();
		
		Stereotype stereotype = StereotypesHelper.getStereotype(project,"FailureTypeSystem");
		List<Element> list = StereotypesHelper.getExtendedElements(stereotype);

		return list.get(0);

	}

 }
