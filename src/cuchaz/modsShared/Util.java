package cuchaz.modsShared;

public class Util
{
	public static final int TicksPerSecond = 20; // constant set in Minecraft.java, inaccessible by methods
	
	public static double secondsToTicks( double x )
	{
		return x*TicksPerSecond;
	}
	
	public static double ticksToSeconds( double x )
	{
		return x/TicksPerSecond;
	}
	
	public static double perSecond2ToPerTick2( double x )
	{
		return x/TicksPerSecond/TicksPerSecond;
	}
	
	public static int realModulus( int a, int b )
	{
		// NOTE: Java's % operator is not a true modulus
		// it's a remainder operator
		return ( a % b + b ) % b;
	}
}
