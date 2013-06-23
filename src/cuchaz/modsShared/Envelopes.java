package cuchaz.modsShared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.util.ChunkCoordinates;

public class Envelopes
{
	private Map<BlockSide,List<ChunkCoordinates>> m_envelopes;
	private Map<BlockSide,BlockArray> m_surfaces;
	private BoundingBoxInt m_boundingBox;
	
	public Envelopes( Iterable<ChunkCoordinates> blocks )
	{
		// compute the bounding box
		m_boundingBox = new BoundingBoxInt();
		for( ChunkCoordinates coords : blocks )
		{
			m_boundingBox.expandBoxToInclude( coords.posX, coords.posY, coords.posZ );
		}
		
		// init the extreme arrays
		m_surfaces = new TreeMap<BlockSide,BlockArray>();
		for( BlockSide side : BlockSide.values() )
		{
			BlockArray surface = new BlockArray(
				side.getU( m_boundingBox.minX, m_boundingBox.minY, m_boundingBox.minZ ),
				side.getV( m_boundingBox.minX, m_boundingBox.minY, m_boundingBox.minZ ),
				side.getWidth( m_boundingBox.getDx(), m_boundingBox.getDy(), m_boundingBox.getDz() ),
				side.getHeight( m_boundingBox.getDx(), m_boundingBox.getDy(), m_boundingBox.getDz() )
			);
			for( int u=surface.getUMin(); u<=surface.getUMax(); u++ )
			{
				for( int v=surface.getVMin(); v<=surface.getVMax(); v++ )
				{
					surface.setBlock( u, v, null );
				}
			}
			m_surfaces.put( side, surface );
		}
		
		// compute the envelopes
		for( ChunkCoordinates coords : blocks )
		{
			for( BlockSide side : BlockSide.values() )
			{
				BlockArray surface = m_surfaces.get( side );
				int u = side.getU( coords.posX, coords.posY, coords.posZ );
				int v = side.getV( coords.posX, coords.posY, coords.posZ );
				ChunkCoordinates extremalCoords = surface.getBlock( u, v );
				if( extremalCoords == null || side.isMoreExtremal( coords, extremalCoords ) )
				{
					surface.setBlock( u, v, coords );
				}
			}
		}
		
		// collect the surfaces into lists for the envelopes
		m_envelopes = new TreeMap<BlockSide,List<ChunkCoordinates>>();
		for( BlockSide side : BlockSide.values() )
		{
			List<ChunkCoordinates> envelope = new ArrayList<ChunkCoordinates>();
			BlockArray surface = m_surfaces.get( side );
			for( int u=surface.getUMin(); u<=surface.getUMax(); u++ )
			{
				for( int v=surface.getVMin(); v<=surface.getVMax(); v++ )
				{
					ChunkCoordinates block = surface.getBlock( u, v );
					if( block != null )
					{
						envelope.add( block );
					}
				}
			}
			m_envelopes.put( side, envelope );
		}
	}
	
	public BoundingBoxInt getBoundingBox( )
	{
		return m_boundingBox;
	}
	
	public BlockArray getSurface( BlockSide side )
	{
		return m_surfaces.get( side );
	}
	
	public List<ChunkCoordinates> getEnvelope( BlockSide side )
	{
		return m_envelopes.get( side );
	}
}
