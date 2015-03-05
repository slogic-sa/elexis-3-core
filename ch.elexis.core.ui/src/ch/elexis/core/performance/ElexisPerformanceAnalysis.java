package ch.elexis.core.performance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.IElexisPerformanceAnalyzer;
import ch.elexis.core.performance.model.EventType;
import ch.elexis.core.performance.model.PerformanceStatistics;
import ch.elexis.core.performance.model.UiListener;
import ch.elexis.core.performance.model.UiStatistic;

public class ElexisPerformanceAnalysis implements IElexisPerformanceAnalyzer {
	private PerformanceStatistics performanceStatistics;
	private File performanceXML;
	
	public ElexisPerformanceAnalysis(){
		performanceStatistics = new PerformanceStatistics();
		
		try {
			// create performance directory if not existing
			File directory = new File(CoreHub.getWritableUserDir(), "performance");
			if (!directory.exists()) {
				directory.mkdir();
			}
			
			// get file or create it if not existing
			performanceXML =
				new File(CoreHub.getWritableUserDir(), "performance" + File.separator
					+ "performanceAnalysis.xml");
			if (!performanceXML.exists()) {
				performanceXML.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writePerformanceStatisticsLog(){
		try {
			File file = new File(performanceXML.getAbsolutePath());
			JAXBContext jaxbContext = JAXBContext.newInstance(PerformanceStatistics.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			
			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(performanceStatistics, file);
			jaxbMarshaller.marshal(performanceStatistics, System.out);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addListener(String listenerId, Class<?> uiClass, Class<?> objectClass){
		listenerId = getNonEmptyString(listenerId);
		String uiId = removeEnding(getClassName(uiClass));
		String objectId = getClassName(objectClass);
		
		UiStatistic statistic = performanceStatistics.loadStatisticFor(uiId);
		UiListener listener = statistic.loadListenerFor(listenerId);
		listener.setObjectId(objectId);
		listener.increaseAddedCounter();
	}
	
	@Override
	public void removeListener(String listenerId){
		List<UiStatistic> statistic = performanceStatistics.getUiStatistic();
		for (UiStatistic s : statistic) {
			for (UiListener sl : s.getListeners()) {
				if (sl.getId().equals(listenerId)) {
					sl.increaseRemoveCounter();
				}
			}
		}
	}
	
	@Override
	public void addEventCall(Class<?> objectClass, int type, int priority){
		EventType eventType =
			performanceStatistics.loadEventTypeFor(getClassName(objectClass), type);
		
		if (eventType.getPriority() == null || eventType.getPriority().isEmpty()) {
			eventType.setPriority(priority);
		}
		eventType.increaseCallCounter();
	}
	
	@Override
	public void addEventFireTime(Class<?> objectClass, int type, long time){
		EventType eventType =
			performanceStatistics.loadEventTypeFor(getClassName(objectClass), type);
		eventType.getFireTime().addTime(time);
	}
	
	private String removeEnding(String uiId){
		int dollarIdx = uiId.indexOf("$");
		if (dollarIdx != -1) {
			uiId = uiId.substring(0, dollarIdx);
		}
		return uiId;
	}
	
	private String getNonEmptyString(String id){
		if (id == null || id.isEmpty() || id.equals("[]")) {
			return "unknown";
		}
		return id;
	}
	
	private String getClassName(Class<?> someClass){
		if (someClass == null) {
			return "unknown";
		}
		return getNonEmptyString(someClass.getName());
	}
}
