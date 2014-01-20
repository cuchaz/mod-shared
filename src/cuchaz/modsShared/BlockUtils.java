/*******************************************************************************
 * Copyright (c) 2013 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jeff Martin - initial API and implementation
 ******************************************************************************/
package cuchaz.modsShared;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class BlockUtils
{
	public static enum Neighbors
	{
		Faces
		{
			@Override
			public int getNumNeighbors( )
			{
				return BlockSide.values().length;
			}
			
			@Override
			public void getNeighbor( ChunkCoordinates out, ChunkCoordinates in, int i )
			{
				BlockSide side = BlockSide.values()[i];
				out.posX = in.posX + side.getDx();
				out.posY = in.posY + side.getDy();
				out.posZ = in.posZ + side.getDz();
			}
		},
		Edges
		{
			private int[] dx = {  0, -1,  1,  0, -1,  1, -1,  1,  0, -1,  1,  0 };
			private int[] dy = { -1,  0,  0,  1, -1, -1,  1,  1, -1,  0,  0,  1 };
			private int[] dz = { -1, -1, -1, -1,  0,  0,  0,  0,  1,  1,  1,  1 };
			
			@Override
			public int getNumNeighbors( )
			{
				return Faces.getNumNeighbors() + dx.length;
			}
			
			@Override
			public void getNeighbor( ChunkCoordinates out, ChunkCoordinates in, int i )
			{
				if( i < Faces.getNumNeighbors() )
				{
					Faces.getNeighbor( out, in, i );
				}
				else
				{
					i -= Faces.getNumNeighbors();
					out.posX = in.posX + dx[i];
					out.posY = in.posY + dy[i];
					out.posZ = in.posZ + dz[i];
				}
			}
		},
		Vertices
		{
			private int[] dx = { -1,  1, -1,  1, -1,  1, -1,  1 };
			private int[] dy = { -1, -1,  1,  1, -1, -1,  1,  1 };
			private int[] dz = { -1, -1, -1, -1,  1,  1,  1,  1 };
			
			@Override
			public int getNumNeighbors( )
			{
				return Edges.getNumNeighbors() + dx.length;
			}
			
			@Override
			public void getNeighbor( ChunkCoordinates out, ChunkCoordinates in, int i )
			{
				if( i < Edges.getNumNeighbors() )
				{
					Edges.getNeighbor( out, in, i );
				}
				else
				{
					i -= Edges.getNumNeighbors();
					out.posX = in.posX + dx[i];
					out.posY = in.posY + dy[i];
					out.posZ = in.posZ + dz[i];
				}
			}
		};
		
		public abstract int getNumNeighbors( );
		public abstract void getNeighbor( ChunkCoordinates out, ChunkCoordinates in, int i );
	}
	
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
	
	public static List<ChunkCoordinates> searchForBlocks( int x, int y, int z, int maxNumBlocks, BlockValidator validator, Neighbors neighbors )
	{
		return searchForBlocks( new ChunkCoordinates( x, y, z ), maxNumBlocks, validator, neighbors );
	}
	
	public static List<ChunkCoordinates> searchForBlocks( ChunkCoordinates sourceBlock, int maxNumBlocks, BlockValidator validator, Neighbors neighbors )
	{
		// do BFS to find valid blocks starting at the source block
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		queue.add( sourceBlock );
		TreeSet<ChunkCoordinates> visitedBlocks = new TreeSet<ChunkCoordinates>();
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
			for( int i=0; i<neighbors.getNumNeighbors(); i++ )
			{
				neighbors.getNeighbor( neighborCoords, block, i );
				if( isValidNeighbor( neighborCoords, validator, queue, visitedBlocks ) )
				{
					queue.add( new ChunkCoordinates( neighborCoords ) );
				}
			}
		}
		
		return foundBlocks;
	}
	
	public static Boolean searchForCondition( int x, int y, int z, int maxNumBlocks, BlockConditionValidator validator, Neighbors neighbors )
	{
		return searchForCondition( new ChunkCoordinates( x, y, z ), maxNumBlocks, validator, neighbors );
	}
	
	public static Boolean searchForCondition( ChunkCoordinates sourceBlock, int maxNumBlocks, BlockConditionValidator validator, Neighbors neighbors )
	{
		// do BFS to find valid blocks starting at the source block
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		queue.add( sourceBlock );
		TreeSet<ChunkCoordinates> visitedBlocks = new TreeSet<ChunkCoordinates>();
		
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
			for( int i=0; i<neighbors.getNumNeighbors(); i++ )
			{
				neighbors.getNeighbor( neighborCoords, block, i );
				if( isValidNeighbor( neighborCoords, validator, queue, visitedBlocks ) )
				{
					queue.add( new ChunkCoordinates( neighborCoords ) );
				}
			}
		}
		
		return false;
	}
	
	public static ChunkCoordinates searchForBlock( int x, int y, int z, int maxNumBlocks, BlockConditionValidator validator, Neighbors neighbors )
	{
		return searchForBlock( new ChunkCoordinates( x, y, z ), maxNumBlocks, validator, neighbors );
	}
	
	public static ChunkCoordinates searchForBlock( ChunkCoordinates sourceBlock, int maxNumBlocks, BlockConditionValidator validator, Neighbors neighbors )
	{
		// do BFS to find the first valid block starting at the source block
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		queue.add( sourceBlock );
		TreeSet<ChunkCoordinates> visitedBlocks = new TreeSet<ChunkCoordinates>();
		
		ChunkCoordinates neighborCoords = new ChunkCoordinates( 0, 0, 0 );
		while( !queue.isEmpty() )
		{
			// get the block and visit it
			ChunkCoordinates block = queue.iterator().next();
			queue.remove( block );
			visitedBlocks.add( block );
			
			// is this our target block?
			if( !block.equals( sourceBlock ) && validator.isConditionMet( block ) )
			{
				return block;
			}
			
			// check the block cap
			if( visitedBlocks.size() >= maxNumBlocks )
			{
				return null;
			}
			
			// check the block's neighbors
			for( int i=0; i<neighbors.getNumNeighbors(); i++ )
			{
				neighbors.getNeighbor( neighborCoords, block, i );
				if( isValidNeighbor( neighborCoords, validator, queue, visitedBlocks ) )
				{
					queue.add( new ChunkCoordinates( neighborCoords ) );
				}
			}
		}
		
		return null;
	}
	
	private static boolean isValidNeighbor( ChunkCoordinates coords, BlockValidator validator, Set<ChunkCoordinates> queue, Set<ChunkCoordinates> visitedBlocks )
	{
		return validator.isValid( coords )
			&& !visitedBlocks.contains( coords )
			&& !queue.contains( coords );
	}
	
	public static List<TreeSet<ChunkCoordinates>> getConnectedComponents( Set<ChunkCoordinates> blocks, Neighbors neighbors )
	{
		List<TreeSet<ChunkCoordinates>> components = new ArrayList<TreeSet<ChunkCoordinates>>();
		final TreeSet<ChunkCoordinates> remainingBlocks = new TreeSet<ChunkCoordinates>( blocks );
		while( !remainingBlocks.isEmpty() )
		{
			// get a block
			ChunkCoordinates coords = remainingBlocks.first();
			
			// do BFS from this block to find the connected component
			TreeSet<ChunkCoordinates> component = new TreeSet<ChunkCoordinates>( BlockUtils.searchForBlocks(
				coords,
				remainingBlocks.size(),
				new BlockValidator( )
				{
					@Override
					public boolean isValid( ChunkCoordinates coords )
					{
						return remainingBlocks.contains( coords );
					}
				},
				neighbors
			) );
			component.add( coords );
			
			// remove the component from the boundary blocks
			remainingBlocks.removeAll( component );
			
			components.add( component );
		}
		return components;
	}
	
	public static TreeSet<ChunkCoordinates> getBlocksAtYAndBelow( Set<ChunkCoordinates> inBlocks, int y )
	{
		// UNDONE: this could be optimized if we could answer y= queries efficiently
		
		TreeSet<ChunkCoordinates> outBlocks = new TreeSet<ChunkCoordinates>();
		for( ChunkCoordinates coords : inBlocks )
		{
			if( coords.posY <= y )
			{
				outBlocks.add( coords );
			}
		}
		return outBlocks;
	}
	
	public static TreeSet<ChunkCoordinates> getHoleFromInnerBoundary( Set<ChunkCoordinates> innerBoundary, final Set<ChunkCoordinates> blocks, Neighbors neighbors )
	{
		return getHoleFromInnerBoundary( innerBoundary, blocks, neighbors, null );
	}
	
	public static TreeSet<ChunkCoordinates> getHoleFromInnerBoundary( Set<ChunkCoordinates> innerBoundary, final Set<ChunkCoordinates> blocks, Neighbors neighbors, final Integer yMax )
	{
		// get the number of blocks inside the shell to use as an upper bound
		BoundingBoxInt box = new BoundingBoxInt( blocks );
		int volume = box.getVolume();
		
		// use BFS to find the enclosed blocks (including the boundary)
		ChunkCoordinates sourceBlock = innerBoundary.iterator().next();
		List<ChunkCoordinates> holeBlocks = BlockUtils.searchForBlocks(
			sourceBlock,
			volume,
			new BlockValidator( )
			{
				@Override
				public boolean isValid( ChunkCoordinates coords )
				{
					return !blocks.contains( coords ) && ( yMax == null || coords.posY <= yMax );
				}
			},
			neighbors
		);
		
		// just in case...
		if( holeBlocks == null )
		{
			throw new Error( "Found too many enclosed blocks!" );
		}
		
		holeBlocks.add( sourceBlock );
		return new TreeSet<ChunkCoordinates>( holeBlocks );
	}
	
	public static boolean removeBlockWithoutNotifyingIt( World world, int x, int y, int z )
	{
		return removeBlockWithoutNotifyingIt( world, x, y, z, true );
	}
	
	public static boolean removeBlockWithoutNotifyingIt( World world, int x, int y, int z, boolean sendNotifications )
	{
		return changeBlockWithoutNotifyingIt( world, x, y, z, 0, 0, sendNotifications );
	}
	
	public static boolean changeBlockWithoutNotifyingIt( World world, int x, int y, int z, int targetBlockId, int targetBlockMeta )
	{
		return changeBlockWithoutNotifyingIt( world, x, y, z, targetBlockId, targetBlockMeta, true );
	}
	
	public static boolean changeBlockWithoutNotifyingIt( World world, int x, int y, int z, int targetBlockId, int targetBlockMeta, boolean sendNotifications )
	{
		// NOTE: this method emulates Chunk.setBlockIDWithMetadata()
		
		try
		{
			// get the masked coords
			int mx = x & 15;
			int my = y & 15;
			int mz = z & 15;
			
			// get the chunk from the world
			Chunk chunk = world.getChunkFromBlockCoords( x, z );
			
			// if the block didn't change, bail
			int oldBlockId = chunk.getBlockID( mx, y, mz );
			int oldBlockMeta = chunk.getBlockMetadata( mx, y, mz );
			if( oldBlockId == targetBlockId )
			{
				if( oldBlockMeta != targetBlockMeta )
				{
					Log.logger.warning( String.format( "Ignoring block metadata change for block %d at (%d,%d,%d)", oldBlockId, x, y, z ) );
				}
				return false;
			}
			
			// use reflection to get access to Chunk internals
			Field chunkPrecipitationHeightMapField = Chunk.class.getDeclaredField( RuntimeMapping.getRuntimeName( "precipitationHeightMap", "field_76638_b" ) );
			chunkPrecipitationHeightMapField.setAccessible( true );
			Field chunkHeightMapField = Chunk.class.getDeclaredField( RuntimeMapping.getRuntimeName( "heightMap", "field_76634_f" ) );
			chunkHeightMapField.setAccessible( true );
			Field chunkStorageArraysField = Chunk.class.getDeclaredField( RuntimeMapping.getRuntimeName( "storageArrays", "field_76652_q" ) );
			chunkStorageArraysField.setAccessible( true );
			ExtendedBlockStorage[] storageArrays = (ExtendedBlockStorage[])chunkStorageArraysField.get( chunk );
			Field chunkIsModifiedField = Chunk.class.getDeclaredField( RuntimeMapping.getRuntimeName( "isModified", "field_76643_l" ) );
			chunkIsModifiedField.setAccessible( true );
			Method chunkRelightBlockMethod = Chunk.class.getDeclaredMethod( RuntimeMapping.getRuntimeName( "relightBlock", "func_76615_h" ), int.class, int.class, int.class );
			chunkRelightBlockMethod.setAccessible( true );
			Method chunkPropagateSkylightOcclusionMethod = Chunk.class.getDeclaredMethod( RuntimeMapping.getRuntimeName( "propagateSkylightOcclusion", "func_76595_e" ), int.class, int.class );
			chunkPropagateSkylightOcclusionMethod.setAccessible( true );
			
			// get chunk field values
			int[] precipitationHeightMap = (int[])chunkPrecipitationHeightMapField.get( chunk );
			int[] heightMap = (int[])chunkHeightMapField.get( chunk );
			
			// update rain map
			int heightMapIndex = mz << 4 | mx;
			if( y >= precipitationHeightMap[heightMapIndex] - 1 )
			{
				precipitationHeightMap[heightMapIndex] = -999;
			}
			int height = heightMap[heightMapIndex];
			
			// clean up any tile entities
			if( oldBlockId != 0 && Block.blocksList[oldBlockId] != null && Block.blocksList[oldBlockId].hasTileEntity( oldBlockMeta ) )
			{
				// remove it from the chunk
				TileEntity tileEntity = chunk.getChunkBlockTileEntity( mx, y, mz );
				if( tileEntity != null )
				{
					tileEntity.invalidate();
					chunk.removeChunkBlockTileEntity( mx, y, mz );
				}
			}
			
			// save the block info
			ExtendedBlockStorage extendedblockstorage = storageArrays[y >> 4];
			if( extendedblockstorage == null )
			{
				extendedblockstorage = new ExtendedBlockStorage( y >> 4 << 4, !world.provider.hasNoSky );
				storageArrays[y >> 4] = extendedblockstorage;
			}
			extendedblockstorage.setExtBlockID( mx, my, mz, targetBlockId );
			extendedblockstorage.setExtBlockMetadata( mx, my, mz, targetBlockMeta );
			
			// update lighting
			if( chunk.getBlockLightOpacity( mx, y, mz ) > 0 )
			{
				if( y >= height )
				{
					chunkRelightBlockMethod.invoke( chunk, mx, y + 1, mz );
				}
			}
			else if( y == height - 1 )
			{
				chunkRelightBlockMethod.invoke( chunk, mx, y, mz );
			}
			chunkPropagateSkylightOcclusionMethod.invoke( chunk, mx, mz );
			
			// make the chunk dirty
			chunkIsModifiedField.setBoolean( chunk, true );
			
			// handle block updates
			if( sendNotifications )
			{
				world.markBlockForUpdate( x, y, z );
	            if( !world.isRemote )
	            {
	                world.notifyBlockChange( x, y, z, oldBlockId );
	            }
			}
			
			return true;
		}
		catch( NoSuchFieldException ex )
		{
			throw new Error( "Unable to remove block! Chunk class has changed!", ex );
		}
		catch( NoSuchMethodException ex )
		{
			throw new Error( "Unable to remove block! Chunk class has changed!", ex );
		}
		catch( IllegalAccessException ex )
		{
			throw new Error( "Unable to remove block! Access to Chunk instance was denied!", ex );
		}
		catch( SecurityException ex )
		{
			throw new Error( "Unable to remove block! Access to Chunk instance was denied!", ex );
		}
		catch( IllegalArgumentException ex )
		{
			throw new Error( "Unable to remove block! Chunk method has changed!", ex );
		}
		catch( InvocationTargetException ex )
		{
			throw new Error( "Unable to remove block! Chunk method call failed!", ex );
		}
	}

	public static void getWorldCollisionBoxes( List<AxisAlignedBB> out, World world, AxisAlignedBB queryBox )
	{
		int minX = MathHelper.floor_double( queryBox.minX );
        int maxX = MathHelper.floor_double( queryBox.maxX );
        int minY = MathHelper.floor_double( queryBox.minY );
        int maxY = MathHelper.floor_double( queryBox.maxY );
        int minZ = MathHelper.floor_double( queryBox.minZ );
        int maxZ = MathHelper.floor_double( queryBox.maxZ );
        for( int x=minX; x<=maxX; x++ )
        {
            for( int z=minZ; z<=maxZ; z++ )
            {
                for( int y=minY; y<=maxY; y++ )
                {
                    Block block = Block.blocksList[world.getBlockId( x, y, z )];
                    if( block != null )
                    {
                        block.addCollisionBoxesToList( world, x, y, z, queryBox, out, null );
                    }
                }
            }
        }
	}
	
	public static void worldRangeQuery( List<ChunkCoordinates> out, World world, AxisAlignedBB queryBox )
	{
		int minX = MathHelper.floor_double( queryBox.minX );
        int maxX = MathHelper.floor_double( queryBox.maxX );
        int minY = MathHelper.floor_double( queryBox.minY );
        int maxY = MathHelper.floor_double( queryBox.maxY );
        int minZ = MathHelper.floor_double( queryBox.minZ );
        int maxZ = MathHelper.floor_double( queryBox.maxZ );
        for( int x=minX; x<=maxX; x++ )
        {
            for( int z=minZ; z<=maxZ; z++ )
            {
                for( int y=minY; y<=maxY; y++ )
                {
                    if( Block.blocksList[world.getBlockId( x, y, z )] != null )
                    {
                        out.add( new ChunkCoordinates( x, y, z ) );
                    }
                }
            }
        }
	}
}
