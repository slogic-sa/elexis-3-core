package ch.elexis.core.performance.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "uiStatistic")
@XmlAccessorType(XmlAccessType.FIELD)
public class UiStatistic {
	@XmlAttribute
	private String uiId;
	@XmlElement(name = "listener")
	private List<UiListener> listeners = new ArrayList<UiListener>();
	
	public String getUiId(){
		return uiId;
	}
	
	public void setUiId(String uiId){
		this.uiId = uiId;
	}
	
	public List<UiListener> getListeners(){
		return listeners;
	}
	
	public void setListeners(List<UiListener> listeners){
		this.listeners = listeners;
	}
	
	public UiListener loadListenerFor(String listenerId){
		if (!listeners.isEmpty()) {
			for (UiListener sl : listeners) {
				if (sl.getId().equals(listenerId)) {
					return sl;
				}
			}
		}
		
		UiListener sl = new UiListener();
		sl.setId(listenerId);
		listeners.add(sl);
		
		return sl;
	}
}
