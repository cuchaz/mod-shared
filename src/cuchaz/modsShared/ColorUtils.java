package cuchaz.modsShared;

import java.awt.Color;

public class ColorUtils
{
	private static final int DefaultAlpha = 255;
	
	public static int getColor( Color c )
	{
		return getColor( c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() );
	}
	
	public static int getGrey( int grey )
	{
		return getColor( grey, grey, grey, DefaultAlpha );
	}
	
	public static int getGrey( int grey, int a )
	{
		return getColor( grey, grey, grey, a );
	}
	
	public static int getColor( int r, int g, int b )
	{
		return getColor( r, g, b, DefaultAlpha );
	}
	
	public static int getColor( int r, int g, int b, int a )
	{
		return ( b & 0xff )
			| ( g & 0xff ) << 8
			| ( r & 0xff ) << 16
			| ( a & 0xff ) << 24;
	}
}
