package ch.elexis.core.jpa.entities;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.ElexisExtInfoMapConverter;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "LOGS")
@EntityListeners(EntityWithIdListener.class)
public class DBLog implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Basic(fetch = FetchType.LAZY)
	@Convert(converter = ElexisExtInfoMapConverter.class)
	@Column(columnDefinition = "BLOB")
	protected Map<Object, Object> extInfo = new Hashtable<Object, Object>();
	
	@Column(length = 255)
	protected String oid;
	
	@Column(length = 8)
	protected LocalDate datum;

	@Column(length = 20)
	protected String typ;
	
	@Column(length = 25)
	protected String userId;
	
	@Column(length = 255)
	protected String station;

	@Override
	public Map<Object, Object> getExtInfo(){
		return extInfo;
	}
	
	@Override
	public void setExtInfo(Map<Object, Object> extInfo){
		this.extInfo = extInfo;
	}
	
	@Override
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public BigInteger getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(BigInteger lastupdate){
		this.lastupdate = lastupdate;
	}
}