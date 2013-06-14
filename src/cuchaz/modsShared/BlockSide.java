package cuchaz.modsShared;

public enum BlockSide
{
	// NOTE: order is important
	Bottom, // y-axis (+)
	Top, //           (-)
	East, // z-axis (+)
	West, //        (-)
	North, // x-axis (+)
	South; //        (-)
	
	private BlockSide m_oppositeSide;
	private BlockSide m_xzNextSide;
	
	private BlockSide( )
	{
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
}
