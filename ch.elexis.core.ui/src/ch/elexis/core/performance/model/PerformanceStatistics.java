package ch.elexis.core.performance.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "performancestatistics")
@XmlAccessorType(XmlAccessType.FIELD)
public class PerformanceStatistics {
	
	@XmlElement(name = "uiStatistic")
	private List<UiStatistic> uiStatistic;
	
	@XmlElement(name = "eventStatistic")
	private List<EventStatistic> eventStatistic;
	
	public PerformanceStatistics(){
		uiStatistic = new ArrayList<UiStatistic>();
		eventStatistic = new ArrayList<EventStatistic>();
	}
	
	public List<UiStatistic> getUiStatistic(){
		return uiStatistic;
	}
	
	public void setUiStatistic(List<UiStatistic> statistic){
		this.uiStatistic = statistic;
	}
	
	public List<EventStatistic> getEventStatistic(){
		return eventStatistic;
	}
	
	public void setEventStatistic(List<EventStatistic> eventStatistic){
		this.eventStatistic = eventStatistic;
	}
	
	public UiStatistic loadStatisticFor(String uiId){
		if (!uiStatistic.isEmpty()) {
			for (UiStatistic stat : uiStatistic) {
				if (stat.getUiId().equals(uiId)) {
					return stat;
				}
			}
		}
		
		// create stat object, add it to list and return it
		UiStatistic stat = new UiStatistic();
		stat.setUiId(uiId);
		uiStatistic.add(stat);
		
		return stat;
	}
	
	private EventStatistic loadEventStatisticFor(String objectId){
		if (!eventStatistic.isEmpty()) {
			for (EventStatistic eStat : eventStatistic) {
				if (eStat.getObjectId().equals(objectId)) {
					return eStat;
				}
			}
		}
		
		EventStatistic eStat = new EventStatistic();
		eStat.setObjectId(objectId);
		eventStatistic.add(eStat);
		
		return eStat;
	}
	
	public EventType loadEventTypeFor(String objectId, int typeSort){
		EventStatistic eStat = loadEventStatisticFor(objectId);
		if (!eStat.getType().isEmpty()) {
			for (EventType et : eStat.getType()) {
				// find the correct type
				if (et.getTypeId() == typeSort) {
					return et;
				}
			}
		}
		
		EventType et = new EventType();
		et.setTypeId(typeSort);
		eStat.getType().add(et);
		
		return et;
	}
}
