/*******************************************************************************
 * Copyright (c) 2014 jeff.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     jeff - initial API and implementation
 ******************************************************************************/
package cuchaz.modsShared.blocks;

import java.util.Collection;
import java.util.TreeSet;

import net.minecraft.util.ChunkCoordinates;

public class BlockSet extends TreeSet<ChunkCoordinates>
{
	private static final long serialVersionUID = -1018340715197554750L;
	
	public BlockSet( )
	{
		super();
	}
	
	public BlockSet( BlockSet other )
	{
		super( other );
	}
	
	public BlockSet( Collection<ChunkCoordinates> blocks )
	{
		super();
		addAll( blocks );
	}
	
	public BoundingBoxInt getBoundingBox( )
	{
		return new BoundingBoxInt( this );
	}
}
