package cuchaz.modsShared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;

public class BlockUtils
{
	public static interface BlockValidator
	{
		public boolean isValid( IBlockAccess world, int x, int y, int z );
	}
	
	public static int getManhattanDistance( ChunkCoordinates a, ChunkCoordinates b )
	{
		return getManhattanDistance( a.posX, a.posY, a.posZ, b.posX, b.posY, b.posZ );
	}
	
	public static int getManhattanDistance( int ax, int ay, int az, ChunkCoordinates b )
	{
		return getManhattanDistance( ax, ay, az, b.posX, b.posY, b.posZ );
	}
	
	public static int getManhattanDistance( ChunkCoordinates a, int bx, int by, int bz )
	{
		return getManhattanDistance( a.posX, a.posY, a.posZ, bx, by, bz );
	}
	
	public static int getManhattanDistance( int ax, int ay, int az, int bx, int by, int bz )
	{
		return Math.abs( ax - bx ) + Math.abs( ay - by ) + Math.abs( az - bz );
	}
	
	public static int getXZManhattanDistance( ChunkCoordinates a, ChunkCoordinates b )
	{
		return getXZManhattanDistance( a.posX, a.posY, a.posZ, b.posX, b.posY, b.posZ );
	}
	
	public static int getXZManhattanDistance( int ax, int ay, int az,  ChunkCoordinates b )
	{
		return getXZManhattanDistance( ax, ay, az, b.posX, b.posY, b.posZ );
	}
	
	public static int getXZManhattanDistance( ChunkCoordinates a, int bx, int by, int bz )
	{
		return getXZManhattanDistance( a.posX, a.posY, a.posZ, bx, by, bz );
	}
	
	public static int getXZManhattanDistance( int ax, int ay, int az, int bx, int by, int bz )
	{
		return Math.abs( ax - bx ) + Math.abs( az - bz );
	}
	
	public static List<ChunkCoordinates> graphSearch( IBlockAccess world, int x, int y, int z, int maxNumBlocks, BlockValidator validator )
	{
		ChunkCoordinates sourceBlock = new ChunkCoordinates( x, y, z );
		
		// grab all connected wood/leaf blocks with the same meta up to a few blocks away in the xz plane
		// using BFS
		ChunkCoordinates origin = new ChunkCoordinates( x, y, z );
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		HashSet<ChunkCoordinates> visitedBlocks = new HashSet<ChunkCoordinates>();
		List<ChunkCoordinates> foundBlocks = new ArrayList<ChunkCoordinates>();
		queue.add( origin );
		
		while( !queue.isEmpty() )
		{
			// get the block and visit it
			ChunkCoordinates block = queue.iterator().next();
			queue.remove( block );
			visitedBlocks.add( block );
			
			if( !validator.isValid(  world, block.posX, block.posY, block.posZ ) )
			{
				continue;
			}
			
			// this is a target block! Add it to the list (as long as it's not the source block)
			if( !block.equals( sourceBlock ) )
			{
				// check for the fail-safe
				if( foundBlocks.size() >= maxNumBlocks )
				{
					return null;
				}
				
				foundBlocks.add( block );
			}
			
			// queue up the block's neighbors
			List<ChunkCoordinates> neighbors = new ArrayList<ChunkCoordinates>();
			for( int dx : new int[] { -1, 1 } )
			{
				neighbors.add( new ChunkCoordinates( block.posX + dx, block.posY, block.posZ ) );
			}
			for( int dy : new int[] { -1, 1 } )
			{
				neighbors.add( new ChunkCoordinates( block.posX, block.posY + dy, block.posZ ) );
			}
			for( int dz : new int[] { -1, 1 } )
			{
				neighbors.add( new ChunkCoordinates( block.posX, block.posY, block.posZ + dz ) );
			}
			
			for( ChunkCoordinates neighbor : neighbors )
			{
				if( !visitedBlocks.contains( neighbor ) && !queue.contains( neighbor ) )
				{
					queue.add( neighbor );
				}
			}
		}
		
		return foundBlocks;
	}
}
