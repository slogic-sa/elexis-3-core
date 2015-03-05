package ch.elexis.core.data.events;

public interface IElexisPerformanceAnalyzer {
	public void writePerformanceStatisticsLog();
	
	public void addListener(String listenerId, Class<?> uiClass, Class<?> objectClass);
	
	public void removeListener(String listenerId);
	
	public void addEventCall(Class<?> objectClass, int type, int priority);
	
	public void addEventFireTime(Class<?> objectClass, int type, long time);
}
