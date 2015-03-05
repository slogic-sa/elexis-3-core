package ch.elexis.core.performance.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listener")
@XmlAccessorType(XmlAccessType.FIELD)
public class UiListener {
	@XmlAttribute
	private String id;
	private String objectId;
	private long added;
	private long removed;
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getObjectId(){
		return objectId;
	}
	
	public void setObjectId(String objectId){
		this.objectId = objectId;
	}
	
	public long getAdded(){
		return added;
	}
	
	public void setAdded(long added){
		this.added = added;
	}
	
	public void increaseAddedCounter(){
		added++;
	}
	
	public long getRemoved(){
		return removed;
	}
	
	public void setRemoved(long removed){
		this.removed = removed;
	}
	
	public void increaseRemoveCounter(){
		removed++;
	}
	
}