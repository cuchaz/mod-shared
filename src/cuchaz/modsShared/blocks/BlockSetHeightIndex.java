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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



public class BlockSetHeightIndex
{
	private Map<Integer,List<BlockSet>> m_map;
	
	public BlockSetHeightIndex( Iterable<BlockSet> sets )
	{
		m_map = new TreeMap<Integer,List<BlockSet>>();
		for( BlockSet set : sets )
		{
			// get the min y of the set
			int minY = new BoundingBoxInt( set ).minY;
			addSet( minY, set );
		}
	}
	
	public List<BlockSet> getByMinY( int minY )
	{
		List<BlockSet> blocks = m_map.get( minY );
		if( blocks == null )
		{
			blocks = new ArrayList<BlockSet>();
		}
		return blocks;
	}
	
	private void addSet( int minY, BlockSet set )
	{
		// does this y have a list already?
		List<BlockSet> level = m_map.get( minY );
		if( level == null )
		{
			level = new ArrayList<BlockSet>();
			m_map.put( minY, level );
		}
		level.add( set );
	}
}
