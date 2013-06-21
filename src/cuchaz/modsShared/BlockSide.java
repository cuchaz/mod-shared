package cuchaz.modsShared;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.Icon;

public enum BlockSide
{
	// NOTE: order is important
	Bottom( 0, -1, 0 ) // y-axis (-)
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceYNeg( block, (double)x, (double)y, (double)z, icon );
		}
	},
	Top( 0, 1, 0 ) // y-axis (+)
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceYPos( block, (double)x, (double)y, (double)z, icon );
		}
	},
	East( 0, 0, 1 ) // z-axis (+)
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceZPos( block, (double)x, (double)y, (double)z, icon );
		}
	},
	West( 0, 0, -1 ) // z-axis (-)
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceZNeg( block, (double)x, (double)y, (double)z, icon );
		}
	},
	North( 1, 0, 0 ) // x-axis (+)
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceXPos( block, (double)x, (double)y, (double)z, icon );
		}
	},
	South( -1, 0, 0 ) // x-axis (-)
	{
		@Override
		public void renderSide( RenderBlocks renderBlocks, Block block, double x, double y, double z, Icon icon )
		{
			renderBlocks.renderFaceXNeg( block, (double)x, (double)y, (double)z, icon );
		}
	};
	
	private BlockSide m_oppositeSide;
	private BlockSide m_xzNextSide;
	private int m_dx;
	private int m_dy;
	private int m_dz;
	
	private BlockSide( int dx, int dy, int dz )
	{
		m_dx = dx;
		m_dy = dy;
		m_dz = dz;
		
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
}
