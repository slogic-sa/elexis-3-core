package ch.elexis.core.performance.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "type")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventType {
	@XmlAttribute
	private String name;
	private long called;
	private String priority;
	private FireTime fireTime;
	@XmlTransient
	private int typeId;
	
	public EventType(){
		fireTime = new FireTime();
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String typeName){
		this.name = typeName;
	}
	
	public int getTypeId(){
		return typeId;
	}
	
	public void setTypeId(int typeId){
		this.typeId = typeId;
		this.name = getTypeNameFor(typeId);
	}
	
	public long getCalled(){
		return called;
	}
	
	public void setCalled(long called){
		this.called = called;
	}
	
	public String getPriority(){
		return priority;
	}
	
	public void setPriority(String priority){
		this.priority = priority;
	}
	
	public void setPriority(int priority){
		this.priority = getPriorityNameOf(priority);
	}
	
	public FireTime getFireTime(){
		return fireTime;
	}
	
	public void setFireTime(FireTime fireTime){
		this.fireTime = fireTime;
	}
	
	public void increaseCallCounter(){
		called++;
	}
	
	private String getTypeNameFor(int type){
		switch (type) {
		case 0x0001:
			return "create";
		case 0x0002:
			return "delete";
		case 0x0004:
			return "update";
		case 0x0008:
			return "reload";
		case 0x0010:
			return "selected";
		case 0x0020:
			return "deselected";
		case 0x0040:
			return "userChanged";
		case 0x0080:
			return "mandatorChanged";
		case 0x0100:
			return "elexisStatus";
		case 0x0200:
			return "operationProgress";
		case 0x0400:
			return "notification";
		default:
			return "unknown";
		}
	}
	
	private String getPriorityNameOf(int priority){
		switch (priority) {
		case 1:
			return "sync";
		case 1000:
			return "high";
		case 10000:
			return "normal";
		default:
			return "unknown";
		}
	}
}
