package cuchaz.modsShared;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.util.ChunkCoordinates;

public class BlockArray implements Iterable<ChunkCoordinates>
{
	private int m_uMin;
	private int m_vMin;
	private ChunkCoordinates[][] m_blocks;
	
	public BlockArray( int uMin, int vMin, int width, int height )
	{
		m_uMin = uMin;
		m_vMin = vMin;
		m_blocks = new ChunkCoordinates[height][width];
	}
	
	public int getWidth( )
	{
		return m_blocks[0].length;
	}
	
	public int getHeight( )
	{
		return m_blocks.length;
	}
	
	public int getUMin( )
	{
		return m_uMin;
	}
	public int getUMax( )
	{
		return m_uMin + getWidth() - 1;
	}
	
	public int getVMin( )
	{
		return m_vMin;
	}
	public int getVMax( )
	{
		return m_vMin + getHeight() - 1;
	}
	
	public ChunkCoordinates getBlock( int u, int v )
	{
		return m_blocks[toZeroBasedV( v )][toZeroBasedU( u )];
	}
	
	public void setBlock( int u, int v, ChunkCoordinates coords )
	{
		m_blocks[toZeroBasedV( v )][toZeroBasedU( u )] = coords;
	}
	
	public int toZeroBasedU( int u )
	{
		return u - m_uMin;
	}
	
	public int toZeroBasedV( int v )
	{
		return v - m_vMin;
	}

	@Override
	public Iterator<ChunkCoordinates> iterator( )
	{
		// collect the blocks into a list
		List<ChunkCoordinates> blocks = new ArrayList<ChunkCoordinates>();
		for( int u=0; u<getWidth(); u++ )
		{
			for( int v=0; v<getHeight(); v++ )
			{
				if( m_blocks[v][u] != null )
				{
					blocks.add( m_blocks[v][u] );
				}
			}
		}
		return blocks.iterator();
	}
}
