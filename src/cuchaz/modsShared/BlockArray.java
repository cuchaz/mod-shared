package cuchaz.modsShared;

import net.minecraft.util.ChunkCoordinates;

public class BlockArray
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
		return m_blocks[toInternalV( v )][toInternalU( u )];
	}
	
	public void setBlock( int u, int v, ChunkCoordinates coords )
	{
		m_blocks[toInternalV( v )][toInternalU( u )] = coords;
	}
	
	private int toInternalU( int u )
	{
		return u - m_uMin;
	}
	
	private int toInternalV( int v )
	{
		return v - m_vMin;
	}
}
