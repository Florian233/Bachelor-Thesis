package de.tu.fk.neo4jexport.actions;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import de.fhg.iese.magicdraw.modeling.util.BasicPluginStartup;
import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.profiles.FaultTreeProfile.Stereotypes;
import de.fhg.iese.magicdraw.modeling.util.profiles.SafetyAspectComponentProfile;
import de.fhg.iese.magicdraw.spm.modeling.ProjectListener;

public class PluginStartup extends BasicPluginStartup
{
    @Override
    public void init()
    {
        super.init();

        BrowserContextAMConfigurator configurator = new BrowserContextAMConfigurator()
        {
            @Override
            public void configure(ActionsManager manager, Tree tree)
            {
                if (tree.getSelectedNodes() != null && tree.getSelectedNodes().length == 1)
                {
                    Node node = tree.getSelectedNode();
                    Object userObject = node.getUserObject();
                    if (userObject instanceof Component)
                    {
                        Component component = (Component) userObject;
                        boolean b = UtilStereotypesHelper.hasStereotype(component, SafetyAspectComponentProfile.Stereotypes.Component);
                        b |= UtilStereotypesHelper.hasStereotype(component, Stereotypes.CFTComponent);
                        
                        if (b)
                        {
                            MDActionsCategory category = new MDActionsCategory("neo4jex", "neo4jex");
                            category.addAction(new BrowserAction("<html>Export to <font color=red>Neo4J</font></html>"));

                            manager.addCategory(category);
                        }
                    }
                }
            }

            @Override
            public int getPriority()
            {
                return AMConfigurator.MEDIUM_PRIORITY;
            }
        };
        ActionsConfiguratorsManager.getInstance().addContainmentBrowserContextConfigurator(configurator);
    }

    @Override
    public boolean close()
    {
        return true;
    }

    @Override
    public boolean isSupported()
    {
        return true;
    }
}