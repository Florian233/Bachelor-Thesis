package de.tu.fk.neo4jexport.utility;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import de.fhg.iese.magicdraw.modeling.util.UtilStereotypesHelper;
import de.fhg.iese.magicdraw.modeling.util.profiles.FailurePropagationModelProfile;
import de.fhg.iese.magicdraw.modeling.util.profiles.FaultTreeProfile;
import de.fhg.iese.magicdraw.modeling.util.profiles.MagicDrawDefaultProfile;
import de.fhg.iese.magicdraw.modeling.util.profiles.SafetyAspectComponentProfile;

public class Stereotypes
{
	public final Stereotype outportST = SafetyAspectComponentProfile.Stereotypes.Outport.getStereotype();
	public final Stereotype inportST = SafetyAspectComponentProfile.Stereotypes.Inport.getStereotype();
	public final Stereotype connectionST = SafetyAspectComponentProfile.Stereotypes.Connection.getStereotype();
	public final Stereotype failurePortMappingST = SafetyAspectComponentProfile.Stereotypes.FailurePortMapping.getStereotype();

	public final Stereotype inputEventST = FailurePropagationModelProfile.Stereotypes.InputFailureMode.getStereotype();
	public final Stereotype inportMappingST = FailurePropagationModelProfile.Stereotypes.InportMapping.getStereotype();
	public final Stereotype outputEventST = FailurePropagationModelProfile.Stereotypes.OutputFailureMode.getStereotype();
	public final Stereotype outportMappingST = FailurePropagationModelProfile.Stereotypes.OutportMapping.getStereotype();

	public final Stereotype cftCompST = FaultTreeProfile.Stereotypes.CFTComponent.getStereotype();
	public final Stereotype cftCompInstanceST = FaultTreeProfile.Stereotypes.CFTComponentInstance.getStereotype();
	public final Stereotype cftOutgoingConnectionST = FaultTreeProfile.Stereotypes.OutgoingFtConnection.getStereotype();
	public final Stereotype functionalOutportST = FailurePropagationModelProfile.Stereotypes.SacOutport.getStereotype();
	public final Stereotype functionalInportST = FailurePropagationModelProfile.Stereotypes.SacInport.getStereotype();
	public final Stereotype failureInportST = FailurePropagationModelProfile.Stereotypes.FailureInport.getStereotype();
	public final Stereotype failureOutportST = FailurePropagationModelProfile.Stereotypes.FailureOutport.getStereotype();
	public final Stereotype cftOrGateST = FaultTreeProfile.Stereotypes.Or.getStereotype();
	public final Stereotype hyperlinkOwnerST = MagicDrawDefaultProfile.Stereotypes.HyperlinkOwner.getStereotype();

	public final Stereotype compST;
	public final Stereotype compInstST;

	public Stereotypes(Project project)
	{
		compST = getCompST(project);
		compInstST = getCompInstST(project);
	}

	public static Stereotype getCompST(Project project)
	{
		return SafetyAspectComponentProfile.Stereotypes.Component.getStereotype(project);
	}

	private static Stereotype getCompInstST(Project project)
	{
		return SafetyAspectComponentProfile.Stereotypes.ComponentInstance.getStereotype(project);
	}

	public boolean check(Property p, Stereotype s)
	{
		boolean b = UtilStereotypesHelper.hasStereotype(p, s);

		if (!b)
		{
			String typeName;
			if (s == functionalOutportST)
				typeName = "SacOutport";
			else if (s == functionalInportST)
				typeName = "SacInport";
			else if (s == failureOutportST)
				typeName = "FailureOutport";
			else if (s == failureInportST)
				typeName = "FailureInport";
			else if (s == outputEventST)
				typeName = "OutputFailureMode";
			else if (s == inputEventST)
				typeName = "InputFailureMode";
			else
				typeName = null;

			if (typeName != null && typeName.equals(p.getType().getName()))
			{
				boolean wasSessionCreated = SessionHelper.createSessionIfNecessary("Add missing stereotype");
				UtilStereotypesHelper.addStereotype(p, s);
				if (wasSessionCreated)
					SessionHelper.closeSession();

				System.err.println("Enforced missing stereotype '" + s.getName() + "'");
				b = true;
			}
		}
		return b;
	}
}
