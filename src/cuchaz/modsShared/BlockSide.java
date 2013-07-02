package cuchaz.modsShared;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Icon;

public enum BlockSide
{
	// NOTE: order is important
	// y-axis (-)
	Bottom( 0, -1, 0, new BoxCorner[] { BoxCorner.BottomNorthEast, BoxCorner.BottomNorthWest, BoxCorner.BottomSouthWest, BoxCorner.BottomSouthEast } )
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceYNeg( block, (double)x, (double)y, (double)z, icon );
		}
		
		@Override
		public int getWidth( int dx, int dy, int dz )
		{
			return dx;
		}
		
		@Override
		public int getHeight( int dx, int dy, int dz )
		{
			return dz;
		}

		@Override
		public int getU( int x, int y, int z )
		{
			return x;
		}

		@Override
		public int getV( int x, int y, int z )
		{
			return z;
		}
		
		@Override
		public boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo )
		{
			return compareWith.posY < compareTo.posY;
		}
	},
	// y-axis (+)
	Top( 0, 1, 0, new BoxCorner[] { BoxCorner.TopNorthEast, BoxCorner.TopNorthWest, BoxCorner.TopSouthWest, BoxCorner.TopSouthEast } )
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceYPos( block, (double)x, (double)y, (double)z, icon );
		}
		
		@Override
		public int getWidth( int dx, int dy, int dz )
		{
			return dx;
		}
		
		@Override
		public int getHeight( int dx, int dy, int dz )
		{
			return dz;
		}

		@Override
		public int getU( int x, int y, int z )
		{
			return x;
		}

		@Override
		public int getV( int x, int y, int z )
		{
			return z;
		}
		
		@Override
		public boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo )
		{
			return compareWith.posY > compareTo.posY;
		}
	},
	// z-axis (+)
	East( 0, 0, 1, new BoxCorner[] { BoxCorner.TopNorthEast, BoxCorner.TopSouthEast, BoxCorner.BottomSouthEast, BoxCorner.BottomNorthEast } )
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceZPos( block, (double)x, (double)y, (double)z, icon );
		}
		
		@Override
		public int getWidth( int dx, int dy, int dz )
		{
			return dx;
		}
		
		@Override
		public int getHeight( int dx, int dy, int dz )
		{
			return dy;
		}

		@Override
		public int getU( int x, int y, int z )
		{
			return x;
		}

		@Override
		public int getV( int x, int y, int z )
		{
			return y;
		}
		
		@Override
		public boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo )
		{
			return compareWith.posZ > compareTo.posZ;
		}
	},
	// z-axis (-)
	West( 0, 0, -1, new BoxCorner[] { BoxCorner.TopSouthWest, BoxCorner.TopNorthWest, BoxCorner.BottomNorthWest, BoxCorner.BottomSouthWest } )
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceZNeg( block, (double)x, (double)y, (double)z, icon );
		}
		
		@Override
		public int getWidth( int dx, int dy, int dz )
		{
			return dx;
		}
		
		@Override
		public int getHeight( int dx, int dy, int dz )
		{
			return dy;
		}

		@Override
		public int getU( int x, int y, int z )
		{
			return x;
		}

		@Override
		public int getV( int x, int y, int z )
		{
			return y;
		}
		
		@Override
		public boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo )
		{
			return compareWith.posZ < compareTo.posZ;
		}
	},
	// x-axis (+)
	North( 1, 0, 0, new BoxCorner[] { BoxCorner.TopNorthWest, BoxCorner.TopNorthEast, BoxCorner.BottomNorthEast, BoxCorner.BottomNorthWest } )
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceXPos( block, (double)x, (double)y, (double)z, icon );
		}
		
		@Override
		public int getWidth( int dx, int dy, int dz )
		{
			return dz;
		}
		
		@Override
		public int getHeight( int dx, int dy, int dz )
		{
			return dy;
		}
		
		@Override
		public int getU( int x, int y, int z )
		{
			return z;
		}

		@Override
		public int getV( int x, int y, int z )
		{
			return y;
		}
		
		@Override
		public boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo )
		{
			return compareWith.posX > compareTo.posX;
		}
	},
	// x-axis (-)
	South( -1, 0, 0, new BoxCorner[] { BoxCorner.TopSouthEast, BoxCorner.TopSouthWest, BoxCorner.BottomSouthWest, BoxCorner.BottomSouthEast } )
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceXNeg( block, (double)x, (double)y, (double)z, icon );
		}
		
		@Override
		public int getWidth( int dx, int dy, int dz )
		{
			return dz;
		}
		
		@Override
		public int getHeight( int dx, int dy, int dz )
		{
			return dy;
		}

		@Override
		public int getU( int x, int y, int z )
		{
			return z;
		}

		@Override
		public int getV( int x, int y, int z )
		{
			return y;
		}
		
		@Override
		public boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo )
		{
			return compareWith.posX < compareTo.posX;
		}
	};
	
	private static BlockSide[] m_xzSides;
	
	private BlockSide m_oppositeSide;
	private BlockSide m_xzNextSide;
	private int m_dx;
	private int m_dy;
	private int m_dz;
	private BoxCorner[] m_corners;
	
	private BlockSide( int dx, int dy, int dz, BoxCorner[] corners )
	{
		m_dx = dx;
		m_dy = dy;
		m_dz = dz;
		m_corners = corners;
		
		m_oppositeSide = null;
		m_xzNextSide = null;
	}
	
	static
	{
		// set opposite sides
		Bottom.m_oppositeSide = Top;
		Top.m_oppositeSide = Bottom;
		East.m_oppositeSide = West;
		West.m_oppositeSide = East;
		North.m_oppositeSide = South;
		South.m_oppositeSide = North;
		
		// set zx plane order
		North.m_xzNextSide = West;
		West.m_xzNextSide = South;
		South.m_xzNextSide = East;
		East.m_xzNextSide = North;
		
		m_xzSides = new BlockSide[] { North, East, South, West };
	}
	
	public int getId( )
	{
		return ordinal();
	}
	
	public int getDx( )
	{
		return m_dx;
	}
	
	public int getDy( )
	{
		return m_dy;
	}
	
	public int getDz( )
	{
		return m_dz;
	}
	
	public BoxCorner[] getCorners( )
	{
		return m_corners;
	}
	
	public BlockSide getOppositeSide( )
	{
		return m_oppositeSide;
	}
	
	public BlockSide getXZNextSide( )
	{
		return m_xzNextSide;
	}
	
	public static BlockSide getById( int side )
	{
		return values()[side];
	}
	
	public static BlockSide getByXZOffset( BlockSide start, int offset )
	{
		BlockSide side = start;
		for( int i=0; i<offset; i++ )
		{
			side = side.getXZNextSide();
		}
		return side;
	}
	
	public abstract void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon );
	public abstract int getWidth( int dx, int dy, int dz );
	public abstract int getHeight( int dx, int dy, int dz );
	public abstract int getU( int x, int y, int z );
	public abstract int getV( int x, int y, int z );
	public abstract boolean isMoreExtremal( ChunkCoordinates compareWith, ChunkCoordinates compareTo );

	public static BlockSide[] xzSides( )
	{
		return m_xzSides;
	}
}
