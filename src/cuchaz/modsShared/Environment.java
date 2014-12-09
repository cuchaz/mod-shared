/*******************************************************************************
 * Copyright (c) 2013 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jeff Martin - initial API and implementation
 ******************************************************************************/
package cuchaz.modsShared;

import net.minecraft.entity.Entity;


public class Environment
{
	private static Boolean m_isObfuscated;
	
	static
	{
		m_isObfuscated = null;
	}
	
	public static boolean isObfuscated( )
	{
		if( m_isObfuscated == null )
		{
			// attempt to detect whether or not the environment is obfuscated
			try
			{
				// check for a well-known method name
				Entity.class.getDeclaredMethod( "onUpdate" );
				m_isObfuscated = false;
			}
			catch( NoSuchMethodException ex )
			{
				m_isObfuscated = true;
			}
			catch( SecurityException ex )
			{
				// this is a problem...
				throw new Error( "Unable to reflect on Minecraft classes!", ex );
			}
		}
		
		return m_isObfuscated;
	}
	
	public static String getRuntimeName( String name, String id )
	{
		return isObfuscated() ? id : name;
	}
}
