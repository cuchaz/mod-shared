package cuchaz.modsShared;

public class Util
{
	public static int realModulus( int a, int b )
	{
		// NOTE: Java's % operator is not a true modulus
		// it's a remainder operator
		return ( a % b + b ) % b;
	}
}
