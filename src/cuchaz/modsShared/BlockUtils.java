package cuchaz.modsShared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.ChunkCoordinates;

public class BlockUtils
{
	public static interface BlockValidator
	{
		public boolean isValid( ChunkCoordinates coords );
	}
	
	public static interface BlockConditionValidator extends BlockValidator
	{
		public boolean isConditionMet( ChunkCoordinates coords );
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
	
	public static List<ChunkCoordinates> searchForBlocks( int x, int y, int z, int maxNumBlocks, BlockValidator validator )
	{
		return searchForBlocks( new ChunkCoordinates( x, y, z ), maxNumBlocks, validator );
	}
	
	public static List<ChunkCoordinates> searchForBlocks( ChunkCoordinates sourceBlock, int maxNumBlocks, BlockValidator validator )
	{
		// do BFS to find valid blocks starting at the source block
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		queue.add( sourceBlock );
		HashSet<ChunkCoordinates> visitedBlocks = new HashSet<ChunkCoordinates>();
		List<ChunkCoordinates> foundBlocks = new ArrayList<ChunkCoordinates>();
		
		ChunkCoordinates neighborCoords = new ChunkCoordinates( 0, 0, 0 );
		while( !queue.isEmpty() )
		{
			// get the block and visit it
			ChunkCoordinates block = queue.iterator().next();
			queue.remove( block );
			visitedBlocks.add( block );
			
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
			
			// check the block's neighbors
			for( BlockSide side : BlockSide.values() )
			{
				neighborCoords.posX = block.posX + side.getDx();
				neighborCoords.posY = block.posY + side.getDy();
				neighborCoords.posZ = block.posZ + side.getDz();
				
				if( isValidNeighbor( neighborCoords, validator, queue, visitedBlocks ) )
				{
					queue.add( new ChunkCoordinates( neighborCoords ) );
				}
			}
		}
		
		return foundBlocks;
	}
	
	public static Boolean searchForCondition( int x, int y, int z, int maxNumBlocks, BlockConditionValidator validator )
	{
		return searchForCondition( new ChunkCoordinates( x, y, z ), maxNumBlocks, validator );
	}
	
	public static Boolean searchForCondition( ChunkCoordinates sourceBlock, int maxNumBlocks, BlockConditionValidator validator )
	{
		// do BFS to find valid blocks starting at the source block
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		queue.add( sourceBlock );
		HashSet<ChunkCoordinates> visitedBlocks = new HashSet<ChunkCoordinates>();
		
		ChunkCoordinates neighborCoords = new ChunkCoordinates( 0, 0, 0 );
		while( !queue.isEmpty() )
		{
			// get the block and visit it
			ChunkCoordinates block = queue.iterator().next();
			queue.remove( block );
			visitedBlocks.add( block );
			
			// check for the fail-safe
			if( visitedBlocks.size() > maxNumBlocks )
			{
				return null;
			}
			
			// check for the condition
			if( validator.isConditionMet( block ) )
			{
				return true;
			}
			
			// check the block's neighbors
			for( BlockSide side : BlockSide.values() )
			{
				neighborCoords.posX = block.posX + side.getDx();
				neighborCoords.posY = block.posY + side.getDy();
				neighborCoords.posZ = block.posZ + side.getDz();
				
				if( isValidNeighbor( neighborCoords, validator, queue, visitedBlocks ) )
				{
					queue.add( new ChunkCoordinates( neighborCoords ) );
				}
			}
		}
		
		return false;
	}
	
	private static boolean isValidNeighbor( ChunkCoordinates coords, BlockValidator validator, Set<ChunkCoordinates> queue, Set<ChunkCoordinates> visitedBlocks )
	{
		return validator.isValid( coords )
			&& !visitedBlocks.contains( coords )
			&& !queue.contains( coords );
	}
}
