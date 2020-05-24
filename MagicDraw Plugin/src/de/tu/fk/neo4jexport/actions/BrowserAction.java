package de.tu.fk.neo4jexport.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.profiles.SafetyAspectComponentProfile;
import de.fhg.iese.magicdraw.modeling.util.profiles.SafetyAspectComponentProfile.Stereotypes;
import de.tu.fk.neo4jexport.domain.Neo4jExporter;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

public class BrowserAction extends DefaultBrowserAction
{
    private static final long serialVersionUID = -2953326012170012936L;
    private final Stereotype cftStereotype;
    private final Stereotype compStereotype;
   
    public BrowserAction(String text)
    {
        super(text, text, null, null);
        
        Project project = Application.getInstance().getProject();
        cftStereotype = de.fhg.iese.magicdraw.modeling.util.profiles.FaultTreeProfile.Stereotypes.CFTComponent.getStereotype(project);
        compStereotype = SafetyAspectComponentProfile.Stereotypes.Component.getStereotype(project);
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        Component component = findSelectedComponent();
        
        if (isCft(component))
        	Neo4jExporter.exportCft(component);
        else if (isComponent(component))
        	Neo4jExporter.exportComponent(component);
    }

    private Component findSelectedComponent()
    {
        Tree tree = getTree();
        if (tree.getSelectedNodes().length == 1)
        {
            Node node = tree.getSelectedNode();
            Object userObject = node.getUserObject();
            
            if (userObject instanceof Component)
            	return (Component) userObject;
        }
        return null;
    }

    private boolean isCft(Component c)
    {
    	return c != null && (UtilStereotypesHelper.hasStereotype(c, cftStereotype));
    }
    
    private boolean isComponent(Component c)
    {
    	return c != null && (UtilStereotypesHelper.hasStereotype(c, compStereotype));
    }
    
    @Override
    public void updateState()
    {
        Component component = findSelectedComponent();
        setEnabled(isComponent(component) || isCft(component));
    }
}
