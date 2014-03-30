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
package cuchaz.modsShared.blocks;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import cuchaz.modsShared.Environment;
import cuchaz.modsShared.Log;
import cuchaz.modsShared.perf.Profiler;

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
			public void getNeighbor( Coords out, Coords in, int i )
			{
				BlockSide side = BlockSide.values()[i];
				out.x = in.x + side.getDx();
				out.y = in.y + side.getDy();
				out.z = in.z + side.getDz();
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
			public void getNeighbor( Coords out, Coords in, int i )
			{
				if( i < Faces.getNumNeighbors() )
				{
					Faces.getNeighbor( out, in, i );
				}
				else
				{
					i -= Faces.getNumNeighbors();
					out.x = in.x + dx[i];
					out.y = in.y + dy[i];
					out.z = in.z + dz[i];
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
			public void getNeighbor( Coords out, Coords in, int i )
			{
				if( i < Edges.getNumNeighbors() )
				{
					Edges.getNeighbor( out, in, i );
				}
				else
				{
					i -= Edges.getNumNeighbors();
					out.x = in.x + dx[i];
					out.y = in.y + dy[i];
					out.z = in.z + dz[i];
				}
			}
		};
		
		public abstract int getNumNeighbors( );
		public abstract void getNeighbor( Coords out, Coords in, int i );
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
		public boolean shouldExploreBlock( Coords coords );
	}
	
	public static interface BlockCallback
	{
		public SearchAction foundBlock( Coords coords );
	}
	
	public static interface BlockConditionChecker
	{
		public boolean isConditionMet( Coords coords );
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
		catch( NoSuchFieldException ex )
		{
			throw new Error( ex );
		}
		catch( SecurityException ex )
		{
			throw new Error( ex );
		}
		catch( NoSuchMethodException ex )
		{
			throw new Error( ex );
		}
	}
	
	public static int getManhattanDistance( Coords a, Coords b )
	{
		return getManhattanDistance( a.x, a.y, a.z, b.x, b.y, b.z );
	}
	
	public static int getManhattanDistance( int ax, int ay, int az, Coords b )
	{
		return getManhattanDistance( ax, ay, az, b.x, b.y, b.z );
	}
	
	public static int getManhattanDistance( Coords a, int bx, int by, int bz )
	{
		return getManhattanDistance( a.x, a.y, a.z, bx, by, bz );
	}
	
	public static int getManhattanDistance( int ax, int ay, int az, int bx, int by, int bz )
	{
		return Math.abs( ax - bx ) + Math.abs( ay - by ) + Math.abs( az - bz );
	}
	
	public static int getXZManhattanDistance( Coords a, Coords b )
	{
		return getXZManhattanDistance( a.x, a.y, a.z, b.x, b.y, b.z );
	}
	
	public static int getXZManhattanDistance( int ax, int ay, int az,  Coords b )
	{
		return getXZManhattanDistance( ax, ay, az, b.x, b.y, b.z );
	}
	
	public static int getXZManhattanDistance( Coords a, int bx, int by, int bz )
	{
		return getXZManhattanDistance( a.x, a.y, a.z, bx, by, bz );
	}
	
	public static int getXZManhattanDistance( int ax, int ay, int az, int bx, int by, int bz )
	{
		return Math.abs( ax - bx ) + Math.abs( az - bz );
	}
	
	public static BlockSet searchForBlocks( int x, int y, int z, int maxNumBlocks, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForBlocks( new Coords( x, y, z ), maxNumBlocks, explorer, neighbors );
	}
	
	public static BlockSet searchForBlocks( final Coords source, int maxNumBlocks, BlockExplorer explorer, Neighbors neighbors )
	{
		final BlockSet foundBlocks = new BlockSet();
		exploreBlocks(
			source,
			maxNumBlocks,
			new BlockCallback( )
			{
				@Override
				public SearchAction foundBlock( Coords coords )
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
		return searchForCondition( new Coords( x, y, z ), maxNumBlocks, checker, explorer, neighbors );
	}
	
	public static Boolean searchForCondition( Coords source, int maxNumBlocks, final BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForBlock( source, maxNumBlocks, checker, explorer, neighbors ) != null;
	}
	
	public static Coords searchForBlock( int x, int y, int z, int maxNumBlocks, BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		return searchForBlock( new Coords( x, y, z ), maxNumBlocks, checker, explorer, neighbors );
	}
	
	public static Coords searchForBlock( final Coords source, int maxNumBlocks, final BlockConditionChecker checker, BlockExplorer explorer, Neighbors neighbors )
	{
		final Coords[] outCoords = { null };
		exploreBlocks(
			source,
			maxNumBlocks,
			new BlockCallback( )
			{
				@Override
				public SearchAction foundBlock( Coords coords )
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
	
	public static void exploreBlocks( Coords source, int maxNumBlocks, BlockCallback callback, BlockExplorer explorer, Neighbors neighbors )
	{
		// TEMP
		Profiler.start( "exploreBlocks" );
		
		Deque<Coords> queue = new ArrayDeque<Coords>();
		BlockSet visitedBlocks = new BlockSet();
		
		// do BFS to find valid blocks starting at the source block
		queue.add( source );
		visitedBlocks.add( source );
		
		Coords neighborCoords = new Coords( 0, 0, 0 );
		while( !queue.isEmpty() )
		{
			// check the cap
			if( visitedBlocks.size() > maxNumBlocks )
			{
				break;
			}
			
			// get the next block
			Coords coords = queue.poll();
			
			// report the block
			if( callback.foundBlock( coords ) == SearchAction.AbortSearch )
			{
				break;
			}
			
			// check the block's neighbors
			for( int i=0; i<neighbors.getNumNeighbors(); i++ )
			{
				neighbors.getNeighbor( neighborCoords, coords, i );
				if( !visitedBlocks.contains( neighborCoords ) )
				{
					if( explorer.shouldExploreBlock( neighborCoords ) )
					{
						Coords coordsToAdd = new Coords( neighborCoords );
						visitedBlocks.add( coordsToAdd );
						queue.add( coordsToAdd );
					}
				}
			}
		}
		
		// TEMP
		Profiler.stop( "exploreBlocks" );
	}
	
	public static List<BlockSet> getConnectedComponents( BlockSet blocks, Neighbors neighbors )
	{
		List<BlockSet> components = new ArrayList<BlockSet>();
		final BlockSet remainingBlocks = new BlockSet( blocks );
		while( !remainingBlocks.isEmpty() )
		{
			// get a block
			Coords coords = remainingBlocks.iterator().next();
			
			// do BFS from this block to find the connected component
			BlockSet component = new BlockSet( BlockUtils.searchForBlocks(
				coords,
				remainingBlocks.size(),
				new BlockExplorer( )
				{
					@Override
					public boolean shouldExploreBlock( Coords coords )
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
	
	public static BlockSet getBlocksAtYAndBelow( BlockSet inBlocks, int y )
	{
		// UNDONE: this could be optimized if we could answer y= queries efficiently
		
		BlockSet outBlocks = new BlockSet();
		for( Coords coords : inBlocks )
		{
			if( coords.y <= y )
			{
				outBlocks.add( coords );
			}
		}
		return outBlocks;
	}
	
	public static BlockSet getHoleFromInnerBoundary( BlockSet innerBoundary, final BlockSet blocks, Neighbors neighbors )
	{
		return getHoleFromInnerBoundary( innerBoundary, blocks, neighbors, null );
	}
	
	public static BlockSet getHoleFromInnerBoundary( BlockSet innerBoundary, final BlockSet blocks, Neighbors neighbors, final Integer yMax )
	{
		// get the number of blocks inside the shell to use as an upper bound
		BoundingBoxInt box = new BoundingBoxInt( blocks );
		int volume = box.getVolume();
		
		// use BFS to find the enclosed blocks (including the boundary)
		Coords sourceBlock = innerBoundary.iterator().next();
		BlockSet holeBlocks = BlockUtils.searchForBlocks(
			sourceBlock,
			volume,
			new BlockExplorer( )
			{
				@Override
				public boolean shouldExploreBlock( Coords coords )
				{
					return !blocks.contains( coords ) && ( yMax == null || coords.y <= yMax );
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
		return new BlockSet( holeBlocks );
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
		// can't use Java 7 because of Mac users... *grumble* *grumble*
		catch( IllegalAccessException ex )
		{
			throw new Error( "Unable to remove block! Chunk method call failed!", ex );
		}
		catch( SecurityException ex )
		{
			throw new Error( "Unable to remove block! Chunk method call failed!", ex );
		}
		catch( IllegalArgumentException ex )
		{
			throw new Error( "Unable to remove block! Chunk method call failed!", ex );
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
	
	public static void worldRangeQuery( Collection<Coords> out, World world, AxisAlignedBB queryBox )
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
                        out.add( new Coords( x, y, z ) );
                    }
                }
            }
        }
	}
	
	public static boolean isConnectedToShell( Coords coords, final BlockSet blocks, Neighbors neighbors )
	{
		return isConnectedToShell( coords, blocks, neighbors, null );
	}
	
	public static boolean isConnectedToShell( Coords coords, final BlockSet blocks, Neighbors neighbors, final Integer maxY )
	{
		// don't check more blocks than can fit in the shell
		final BoundingBoxInt box = blocks.getBoundingBox();
		int shellVolume = ( box.getDx() + 2 )*( box.getDy() + 2 )*( box.getDz() + 2 );
		
		Boolean result = BlockUtils.searchForCondition(
			coords,
			shellVolume,
			new BlockConditionChecker( )
			{
				@Override
				public boolean isConditionMet( Coords coords )
				{
					// is this a shell block?
					return !box.containsPoint( coords );
				}
			},
			new BlockExplorer( )
			{
				@Override
				public boolean shouldExploreBlock( Coords coords )
				{
					return ( maxY == null || coords.y <= maxY ) && !blocks.contains( coords );
				}
			},
			neighbors
		);
		
		// just in case...
		if( result == null )
		{
			throw new Error( "We evaluated too many blocks checking for the shell. This shouldn't have happened." );
		}
		
		return result;
	}
}
