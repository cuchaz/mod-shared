package cuchaz.modsShared;

public class DelayTimer
{
	private int m_numTicks;
	private int m_timer;
	
	public DelayTimer( int numTicks )
	{
		m_numTicks = numTicks;
		m_timer = 0;
	}
	
	public boolean isDelayedUpdate( )
	{
		boolean isDelay = m_timer == 0;
		m_timer = ( m_timer + 1 ) % m_numTicks;
		return isDelay;
	}
}
