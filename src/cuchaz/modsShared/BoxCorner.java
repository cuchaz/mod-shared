package cuchaz.modsShared;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public enum BoxCorner
{
	BottomNorthEast
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.maxX;
			out.yCoord = box.minY;
			out.zCoord = box.maxZ;
		}
	},
	BottomNorthWest
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.maxX;
			out.yCoord = box.minY;
			out.zCoord = box.minZ;
		}
	},
	BottomSouthWest
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.minX;
			out.yCoord = box.minY;
			out.zCoord = box.minZ;
		}
	},
	BottomSouthEast
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.minX;
			out.yCoord = box.minY;
			out.zCoord = box.maxZ;
		}
	},
	TopNorthEast
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.maxX;
			out.yCoord = box.maxY;
			out.zCoord = box.maxZ;
		}
	},
	TopNorthWest
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.maxX;
			out.yCoord = box.maxY;
			out.zCoord = box.minZ;
		}
	},
	TopSouthWest
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.minX;
			out.yCoord = box.maxY;
			out.zCoord = box.minZ;
		}
	},
	TopSouthEast
	{
		@Override
		public void getPoint( Vec3 out, AxisAlignedBB box )
		{
			out.xCoord = box.minX;
			out.yCoord = box.maxY;
			out.zCoord = box.maxZ;
		}
	};
	
	public abstract void getPoint( Vec3 out, AxisAlignedBB box );
}
