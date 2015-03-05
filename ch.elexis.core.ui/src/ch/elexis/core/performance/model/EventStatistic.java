package ch.elexis.core.performance.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "eventStatistic")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventStatistic {
	@XmlAttribute
	private String objectId;
	@XmlElement(name = "type")
	private List<EventType> type = new ArrayList<EventType>();
	
	public String getObjectId(){
		return objectId;
	}
	
	public void setObjectId(String objectId){
		this.objectId = objectId;
	}
	
	public List<EventType> getType(){
		return type;
	}
	
	public void setType(List<EventType> type){
		this.type = type;
	}
	
}
