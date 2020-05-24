package de.tu.fk.neo4jexport.actions;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import de.fhg.iese.magicdraw.modeling.util.UtilStereoModel.UtilStereo;
import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.listeners.abstraction.AbstractDiagramConfigurator;
import de.fhg.iese.magicdraw.modeling.util.profiles.FaultTreeProfile.Stereotypes;
import de.fhg.iese.magicdraw.modeling.util.profiles.SafetyAspectComponentProfile;

/**
 * this class defines a section in the context menu where the navigation actions are placed
 *
 * @author velascom
 */
class Configurator extends AbstractDiagramConfigurator
{
	public Configurator(MDAction action)
	{
		super(action);
	}

	// required to include the action into a context menu
	@Override
	public void configure(ActionsManager manager, DiagramPresentationElement diagram, PresentationElement[] selected, PresentationElement requester)
	{
		Stereotype componentST = SafetyAspectComponentProfile.Stereotypes.Component.getStereotype();
		Stereotype componentInstST = SafetyAspectComponentProfile.Stereotypes.ComponentInstance.getStereotype();
		Stereotype cftST = Stereotypes.CFTComponent.getStereotype();
		Stereotype cftInstST =Stereotypes.CFTComponentInstance.getStereotype(); 

		boolean validSelection = false;
		
		if (requester != null)
		{
			Element element = requester.getActualElement();
			validSelection |= element != null && UtilStereotypesHelper.hasStereotype(element, componentInstST);
			validSelection |= element != null && UtilStereotypesHelper.hasStereotype(element, cftInstST);
		}
		else if (diagram != null)
		{
			Element diagramImpl = diagram.getActualElement();
			Element element = diagramImpl.getOwner();
			validSelection |= element != null && UtilStereotypesHelper.hasStereotype(element, componentST);
			validSelection |= element != null && UtilStereotypesHelper.hasStereotype(element, cftST);
		}

		if (validSelection)
		{
			ActionsCategory category = manager.getCategory("neo4jex");
			if (category == null)
			{
				category = new MDActionsCategory("neo4jex", "neo4jex");
				manager.addCategory(category);
			}
			category.addAction(action);
		}
	}
}
