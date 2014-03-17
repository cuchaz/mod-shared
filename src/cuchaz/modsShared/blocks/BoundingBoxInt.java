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
package cuchaz.modsShared.blocks;

import java.util.Set;

import net.minecraft.util.ChunkCoordinates;

public class BoundingBoxInt
{
	public int minX;
	public int maxX;
	public int minY;
	public int maxY;
	public int minZ;
	public int maxZ;
	
	public BoundingBoxInt( )
	{
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
		minZ = Integer.MAX_VALUE;
		maxZ = Integer.MIN_VALUE;
	}
	
	public BoundingBoxInt( Set<ChunkCoordinates> blocks )
	{
		this();
		for( ChunkCoordinates coords : blocks )
		{
			expandBoxToInclude( coords.posX, coords.posY, coords.posZ );
		}
	}
	
	public BoundingBoxInt( BoundingBoxInt other )
	{
		minX = other.minX;
		minY = other.minY;
		minZ = other.minZ;
		maxX = other.maxX;
		maxY = other.maxY;
		maxZ = other.maxZ;
	}

	public void expandBoxToInclude( int x, int y, int z )
	{
		minX = Math.min( minX, x );
		maxX = Math.max( maxX, x );
		
		minY = Math.min( minY, y );
		maxY = Math.max( maxY, y );
		
		minZ = Math.min( minZ, z );
		maxZ = Math.max( maxZ, z );
	}
	
	public int getDx( )
	{
		return maxX - minX + 1;
	}
	
	public int getDy( )
	{
		return maxY - minY + 1;
	}
	
	public int getDz( )
	{
		return maxZ - minZ + 1;
	}
	
	public boolean containsPoint( ChunkCoordinates coords )
	{
		return containsPoint( coords.posX, coords.posY, coords.posZ );
	}
	
	public boolean containsPoint( int x, int y, int z )
	{
		return x >= minX && x <= maxX
			&& y >= minY && y <= maxY
			&& z >= minZ && z <= maxZ;
	}
	
	public int getVolume( )
	{
		return getDx()*getDy()*getDz();
	}
}
