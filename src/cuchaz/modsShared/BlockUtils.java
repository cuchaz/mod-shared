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
import java.util.Collection;
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
	
	public static enum UpdateRules
	{
		UpdateNeighbors( true, false ),
		UpdateClients( false, true ),
		UpdateNeighborsAndClients( true, true ),
		UpdateNoOne( false, false );
		
		private boolean m_updateNeighbors;
		private boolean m_updateClients;
		
		private UpdateRules( boolean updateNeighbors, boolean updateClients )
		{
			m_updateNeighbors = updateNeighbors;
			m_updateClients = updateClients;
		}
		
		public boolean shouldUpdateNeighbors( )
		{
			return m_updateNeighbors;
		}
		
		public boolean shouldUpdateClients( )
		{
			return m_updateClients;
		}
	}
	
	public static enum SearchAction
	{
		AbortSearch,
		ContinueSearching;
	}

	public static interface BlockExplorer
	{
		public boolean shouldExploreBlock( ChunkCoordinates coords );
	}
	
	public static interface BlockCallback
	{
		public SearchAction foundBlock( ChunkCoordinates coords );
	}
	
	public static interface BlockConditionChecker
	{
		public boolean isConditionMet( ChunkCoordinates coords );
	}
	
	private static Field m_chunkPrecipitationHeightMapField;
	private static Field m_chunkHeightMapField;
	private static Field m_chunkStorageArraysField;
	private static Field m_chunkIsModifiedField;
	private static Method m_chunkRelightBlockMethod;
	private static Method m_chunkPropagateSkylightOcclusionMethod;
	
	static
	{
		try
		{
			// use reflection to get access to Chunk internals
			m_chunkPrecipitationHeightMapField = Chunk.class.getDeclaredField( Environment.getRuntimeName( "precipitationHeightMap", "field_76638_b" ) );
			m_chunkPrecipitationHeightMapField.setAccessible( true );
			m_chunkHeightMapField = Chunk.class.getDeclaredField( Environment.getRuntimeName( "heightMap", "field_76634_f" ) );
			m_chunkHeightMapField.setAccessible( true );
			m_chunkStorageArraysField = Chunk.class.getDeclaredField( Environment.getRuntimeName( "storageArrays", "field_76652_q" ) );
			m_chunkStorageArraysField.setAccessible( true );
			m_chunkIsModifiedField = Chunk.class.getDeclaredField( Environment.getRuntimeName( "isModified", "field_76643_l" ) );
			m_chunkIsModifiedField.setAccessible( true );
			m_chunkRelightBlockMethod = Chunk.class.getDeclaredMethod( Environment.getRuntimeName( "relightBlock", "func_76615_h" ), int.class, int.class, int.class );
			m_chunkRelightBlockMethod.setAccessible( true );
			m_chunkPropagateSkylightOcclusionMethod = Chunk.class.getDeclaredMethod( Environment.getRuntimeName( "propagateSkylightOcclusion", "func_76595_e" ), int.class, int.class );
			m_chunkPropagateSkylightOcclusionMethod.setAccessible( true );
		}
		catch( NoSuchFieldException | NoSuchMethodException | SecurityException ex )
		{
			throw new Error( ex );
		}
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
	
	public static List<ChunkCoordinates> searchForBlocks( int x, int y, int z, int maxNumBlocks, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForBlocks( new ChunkCoordinates( x, y, z ), maxNumBlocks, explorer, neighbors );
	}
	
	public static List<ChunkCoordinates> searchForBlocks( final ChunkCoordinates source, int maxNumBlocks, BlockExplorer explorer, Neighbors neighbors )
	{
		final List<ChunkCoordinates> foundBlocks = new ArrayList<ChunkCoordinates>();
		exploreBlocks(
			source,
			maxNumBlocks,
			new BlockCallback( )
			{
				@Override
				public SearchAction foundBlock( ChunkCoordinates coords )
				{
					if( !coords.equals( source ) )
					{
						foundBlocks.add( coords );
					}
					return SearchAction.ContinueSearching;
				}
			},
			explorer,
			neighbors
		);
		return foundBlocks;
	}
	
	public static Boolean searchForCondition( int x, int y, int z, int maxNumBlocks, BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForCondition( new ChunkCoordinates( x, y, z ), maxNumBlocks, checker, explorer, neighbors );
	}
	
	public static Boolean searchForCondition( ChunkCoordinates source, int maxNumBlocks, final BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForBlock( source, maxNumBlocks, checker, explorer, neighbors ) != null;
	}
	
	public static ChunkCoordinates searchForBlock( int x, int y, int z, int maxNumBlocks, BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForBlock( new ChunkCoordinates( x, y, z ), maxNumBlocks, checker, explorer, neighbors );
	}
	
	public static ChunkCoordinates searchForBlock( final ChunkCoordinates source, int maxNumBlocks, final BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		final ChunkCoordinates[] outCoords = { null };
		exploreBlocks(
			source,
			maxNumBlocks,
			new BlockCallback( )
			{
				@Override
				public SearchAction foundBlock( ChunkCoordinates coords )
				{
					// is this our target block?
					if( !coords.equals( source ) && checker.isConditionMet( coords ) )
					{
						outCoords[0] = coords;
						return SearchAction.AbortSearch;
					}
					
					return SearchAction.ContinueSearching;
				}
			},
			explorer,
			neighbors
		);
		return outCoords[0];
	}
	
	public static void exploreBlocks( ChunkCoordinates source, int maxNumBlocks, BlockCallback callback, BlockExplorer explorer, Neighbors neighbors )
	{
		// do BFS to find valid blocks starting at the source block
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		queue.add( source );
		TreeSet<ChunkCoordinates> visitedBlocks = new TreeSet<ChunkCoordinates>();
		
		ChunkCoordinates neighborCoords = new ChunkCoordinates( 0, 0, 0 );
		while( !queue.isEmpty() )
		{
			// get the block and visit it
			ChunkCoordinates coords = queue.iterator().next();
			queue.remove( coords );
			visitedBlocks.add( coords );
			
			// report the block
			if( callback.foundBlock( coords ) == SearchAction.AbortSearch )
			{
				break;
			}
			
			// check the cap
			if( visitedBlocks.size() >= maxNumBlocks )
			{
				break;
			}
			
			// check the block's neighbors
			for( int i=0; i<neighbors.getNumNeighbors(); i++ )
			{
				neighbors.getNeighbor( neighborCoords, coords, i );
				if( isValidNeighbor( neighborCoords, explorer, queue, visitedBlocks ) )
				{
					queue.add( new ChunkCoordinates( neighborCoords ) );
				}
			}
		}
	}
	
	private static boolean isValidNeighbor( ChunkCoordinates coords, BlockExplorer explorer, Set<ChunkCoordinates> queue, Set<ChunkCoordinates> visitedBlocks )
	{
		return explorer.shouldExploreBlock( coords )
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
				new BlockExplorer( )
				{
					@Override
					public boolean shouldExploreBlock( ChunkCoordinates coords )
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
			new BlockExplorer( )
			{
				@Override
				public boolean shouldExploreBlock( ChunkCoordinates coords )
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
		return removeBlockWithoutNotifyingIt( world, x, y, z, UpdateRules.UpdateNeighborsAndClients );
	}
	
	public static boolean removeBlockWithoutNotifyingIt( World world, int x, int y, int z, UpdateRules updateRules )
	{
		return changeBlockWithoutNotifyingIt( world, x, y, z, 0, 0, updateRules );
	}
	
	public static boolean changeBlockWithoutNotifyingIt( World world, int x, int y, int z, int targetBlockId, int targetBlockMeta )
	{
		return changeBlockWithoutNotifyingIt( world, x, y, z, targetBlockId, targetBlockMeta, UpdateRules.UpdateNeighborsAndClients );
	}
	
	public static boolean changeBlockWithoutNotifyingIt( World world, int x, int y, int z, int targetBlockId, int targetBlockMeta, UpdateRules updateRules )
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
			
			ExtendedBlockStorage[] storageArrays = (ExtendedBlockStorage[])m_chunkStorageArraysField.get( chunk );
			
			// get chunk field values
			int[] precipitationHeightMap = (int[])m_chunkPrecipitationHeightMapField.get( chunk );
			int[] heightMap = (int[])m_chunkHeightMapField.get( chunk );
			
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
					m_chunkRelightBlockMethod.invoke( chunk, mx, y + 1, mz );
				}
			}
			else if( y == height - 1 )
			{
				m_chunkRelightBlockMethod.invoke( chunk, mx, y, mz );
			}
			m_chunkPropagateSkylightOcclusionMethod.invoke( chunk, mx, mz );
			
			// make the chunk dirty
			m_chunkIsModifiedField.setBoolean( chunk, true );
			
			// handle block updates
			if( updateRules.shouldUpdateNeighbors() && Environment.isServer() )
			{
                world.notifyBlockChange( x, y, z, oldBlockId );
			}
			if( updateRules.shouldUpdateClients() )
			{
				world.markBlockForUpdate( x, y, z );
			}
			
			return true;
		}
		catch( IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException ex )
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
	
	public static void worldRangeQuery( Collection<ChunkCoordinates> out, World world, AxisAlignedBB queryBox )
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
                    if( world.getBlockId( x, y, z ) != 0 )
                    {
                        out.add( new ChunkCoordinates( x, y, z ) );
                    }
                }
            }
        }
	}
}
