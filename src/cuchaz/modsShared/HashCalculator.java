package cuchaz.modsShared;

public class HashCalculator
{
	/**************************
	 *   Static Methods
	 **************************/
	
	public static int combineHashes( int ... nums )
	{
		int hashCode = 1;
		for( int i : nums )
		{
			hashCode = hashCode * 31 + i;
		}
		return hashCode;
	}
	
	public static int combineHashesCommutative( int ... nums )
	{
		int hashCode = 1;
		for( int i : nums )
		{
			hashCode += i;
		}
		return hashCode;
	}
	
	public static int hashIds( int ... nums )
	{
		int hashCode = 1;
		for( int i : nums )
		{
			hashCode = hashCode * 37 ^ ( i + 1 );
		}
		return hashCode;
	}
}
