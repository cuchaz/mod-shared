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


public class RuntimeMapping
{
	private static Boolean m_isObfuscatedEnvironment;
	
	static
	{
		m_isObfuscatedEnvironment = null;
	}
	
	public static boolean isObfuscatedEnvironment( )
	{
		if( m_isObfuscatedEnvironment == null )
		{
			// attempt to detect whether or not the environment is obfuscated
			try
			{
				// check for a well-known method name
				Entity.class.getDeclaredMethod( "onUpdate" );
				m_isObfuscatedEnvironment = false;
			}
			catch( NoSuchMethodException ex )
			{
				m_isObfuscatedEnvironment = true;
			}
			catch( SecurityException ex )
			{
				// this is a problem...
				throw new Error( "Unable to reflect on Minecraft classes!", ex );
			}
		}
		
		return m_isObfuscatedEnvironment;
	}
	
	public static String getRuntimeName( String name, String id )
	{
		return isObfuscatedEnvironment() ? id : name;
	}
}
