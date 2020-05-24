package de.tu.fk.neo4jexport.utility;

import com.nomagic.magicdraw.openapi.uml.SessionManager;

public abstract class SessionHelper
{
	public static void createSession(String name)
	{
		SessionManager.getInstance().createSession(name);
	}

	public static boolean createSessionIfNecessary(String name)
	{
		boolean b = SessionManager.getInstance().isSessionCreated();

		if (!b)
		{
			SessionManager.getInstance().createSession(name);
			return true;
		}
		else
			return false;
	}

	public static void closeSession()
	{
		boolean b = SessionManager.getInstance().isSessionCreated();
		if (b)
			SessionManager.getInstance().closeSession();
	}
}
