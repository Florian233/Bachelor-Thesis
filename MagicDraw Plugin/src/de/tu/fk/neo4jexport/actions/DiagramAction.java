package de.tu.fk.neo4jexport.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.actions.DefaultDiagramAction;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.profiles.SafetyAspectComponentProfile;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

public class DiagramAction extends DefaultDiagramAction
{
    private static final long serialVersionUID = -4173621706241374823L;

    public DiagramAction(String text)
    {
        super(text, text, null, null);
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        Component component = findCurrentComponent();
        if (component != null)
            JOptionPane.showMessageDialog(null, "clicked on diagram");
    }

    private Component findCurrentComponent()
    {
        // called when clicking on a menu item (from within context-menu) in a diagram
        Project project = Application.getInstance().getProject();
        Stereotype compST = SafetyAspectComponentProfile.Stereotypes.Component.getStereotype(project);
        Stereotype compInstST = SafetyAspectComponentProfile.Stereotypes.ComponentInstance.getStereotype(project);

        Element element;

        if (this.getSelected() != null && !this.getSelected().isEmpty())
        {
            PresentationElement firstElement = this.getSelected().get(0);
            element = firstElement.getElement();
        }
        else
        {
            Element diagramImpl = this.getDiagram().getActualElement();
            element = diagramImpl.getOwner();
        }

        if (element != null)
        {
            Component component = null;
            if (element instanceof Property && UtilStereotypesHelper.hasStereotype(element, compInstST))
            {
                Property compInst = (Property) element;
                Type type = compInst.getType();
                if (type instanceof Component && UtilStereotypesHelper.hasStereotype(type, compST))
                    component = (Component) type;
            }
            else if (element instanceof Component && UtilStereotypesHelper.hasStereotype(element, compST))
                component = (Component) element;

            return component;
        }
        return null;
    }

    @Override
    public void updateState()
    {
    	/*
        Component component = findCurrentComponent();
        Component cft = CftFinder.findCftFor(component);

        setEnabled(action.isEnabledFor(cft));
        setName(action.getNameFor(cft));
        */
    }
}
