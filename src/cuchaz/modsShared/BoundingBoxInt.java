package cuchaz.modsShared;

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
}
