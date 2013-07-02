package cuchaz.modsShared;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class RotatedBB
{
	private AxisAlignedBB m_box;
	private float m_yaw;
	
	public RotatedBB( AxisAlignedBB box, float yaw )
	{
		m_box = box;
		m_yaw = yaw;
	}
	
	public double getMinY( )
	{
		return m_box.minY;
	}
	
	public double getMaxY( )
	{
		return m_box.maxY;
	}
	
	@Override
	public String toString( )
	{
		StringBuilder buf = new StringBuilder();
		buf.append( "[RotatedBB]" );
		buf.append( String.format( " y=[%.2f,%.2f]", m_box.minY, m_box.maxY ) );
		Vec3 p = Vec3.createVectorHelper( 0, 0, 0 );
		for( BoxCorner corner : BlockSide.Top.getCorners() )
		{
			getCorner( p, corner );
			buf.append( String.format( " (%.2f,%.2f)", p.xCoord, p.zCoord ) );
		}
		return buf.toString();
	}
	
	public void getCorner( Vec3 out, BoxCorner corner )
	{
		getAACorner( out, corner );
		
		// get the centroid of the box
		double cx = ( m_box.maxX + m_box.minX )/2;
		double cz = ( m_box.maxZ + m_box.minZ )/2;
		
		// translate so the box is centered at the origin
		out.xCoord -= cx;
		out.zCoord -= cz;
		
		// rotate by the yaw
		float yawRad = (float)Math.toRadians( m_yaw );
		float cos = MathHelper.cos( yawRad );
		float sin = MathHelper.sin( yawRad );
		double x = out.xCoord*cos - out.zCoord*sin;
		double z = out.xCoord*sin + out.zCoord*cos;
		out.xCoord = x;
		out.zCoord = z;
		
		// translate back to the box coords
		out.xCoord += cx;
		out.zCoord += cz;
	}
	
	public boolean containsPoint( double x, double y, double z )
	{
		// y is easy
		if( y < m_box.minY || y > m_box.maxY )
		{
			return false;
		}
		
		// get the centroid of the box
		double cx = ( m_box.maxX + m_box.minX )/2;
		double cz = ( m_box.maxZ + m_box.minZ )/2;
		
		// translate so the box is centered at the origin
		x -= cx;
		z -= cz;
		
		// rotate the query point into box space
		float yawRad = (float)Math.toRadians( -m_yaw );
		float cos = MathHelper.cos( yawRad );
		float sin = MathHelper.sin( yawRad );
		double newx = x*cos - z*sin;
		double newz = x*sin + z*cos;
		x = newx;
		z = newz;
		
		// translate back to the box coords
		x += cx;
		z += cz;
		
		// finally, perform the check
		return x >= m_box.minX
			&& x <= m_box.maxX
			&& z >= m_box.minZ
			&& z <= m_box.maxZ;
	}
	
	private void getAACorner( Vec3 out, BoxCorner corner )
	{
		corner.getPoint( out, m_box );
	}
}
