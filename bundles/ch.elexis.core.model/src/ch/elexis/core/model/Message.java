package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Transient;

import com.google.gson.Gson;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.message.MessageParty;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class Message extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Message>
		implements Identifiable, IMessage {
	
	private final Gson gson;
	
	public Message(ch.elexis.core.jpa.entities.Message entity){
		super(entity);
		gson = new Gson();
	}
	
	@Override
	public IMessageParty getSender(){
		Kontakt origin = getEntity().getOrigin();
		return findFirstUserForContact(origin).map(e -> new MessageParty(e.getId(), 0))
			.orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSender(IMessageParty value){
		String identifier = value.getIdentifier();
		if (value.getType() == 0) {
			Optional<IUser> user = CoreModelServiceHolder.get().load(identifier, IUser.class);
			if (user.isPresent()) {
				IContact assignedContact = user.get().getAssignedContact();
				Kontakt entity = ((AbstractIdModelAdapter<Kontakt>) assignedContact).getEntity();
				getEntityMarkDirty().setOrigin(entity);
			}
		}
		// TODO support for station, silently ignored by now
	}
	
	/**
	 * convenience method
	 * 
	 * @param user
	 */
	@Override
	public void setSender(IUser user){
		setSender(new MessageParty(user.getId(), 0));
	}
	
	@Override
	public List<IMessageParty> getReceiver(){
		// TODO support for station
		Kontakt destination = getEntity().getDestination();
		Optional<MessageParty> messageParty =
			findFirstUserForContact(destination).map(e -> new MessageParty(e.getId(), 0));
		return (messageParty.isPresent()) ? Collections.singletonList(messageParty.get())
				: new ArrayList<>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addReceiver(IMessageParty messageParty){
		List<IMessageParty> receiver = getReceiver();
		receiver.add(messageParty);
		// TODO suppport multiple receivers
		String identifier = messageParty.getIdentifier();
		if (messageParty.getType() == 0) {
			Optional<IUser> user = CoreModelServiceHolder.get().load(identifier, IUser.class);
			if (user.isPresent()) {
				Kontakt contact =
					((AbstractIdModelAdapter<Kontakt>) user.get().getAssignedContact()).getEntity();
				getEntityMarkDirty().setDestination(contact);
			}
		}
	}
	
	@Override
	public boolean isSenderAcceptsAnswer(){
		// TODO support
		return true;
	}
	
	@Override
	public void setSenderAcceptsAnswer(boolean value){
		// TODO support
	}
	
	@Override
	public LocalDateTime getCreateDateTime(){
		return getEntity().getDateTime();
	}
	
	@Override
	public void setCreateDateTime(LocalDateTime value){
		getEntityMarkDirty().setDateTime(value);
	}
	
	@Override
	public String getMessageText(){
		return getEntity().getMsg();
	}
	
	@Override
	public void setMessageText(String value){
		getEntityMarkDirty().setMsg(value);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getMessageCodes(){
		String json = getEntity().getMessageCodes();
		if (json != null) {
			return gson.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}
	
	@Override
	public void setMessageCodes(Map<String, String> value){
		String json = gson.toJson(value);
		getEntityMarkDirty().setMessageCodes(json);
	}
	
	@Override
	public void addMessageCode(String key, String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getMessagePriority(){
		// TODO support
		return 0;
	}
	
	@Override
	public void setMessagePriority(int value){
		// TODO support
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		// not supported
		return false;
	}
	
	@Override
	public IXid getXid(String domain){
		// not supported
		return null;
	}
	
	@Transient
	private Optional<IUser> findFirstUserForContact(Kontakt kontakt){
		INamedQuery<IUser> namedQuery =
			ModelUtil.getModelService().getNamedQuery(IUser.class, "kontakt");
		List<IUser> users =
			namedQuery.executeWithParameters(namedQuery.getParameterMap("kontakt", kontakt));
		if (!users.isEmpty()) {
			return Optional.of(users.get(0));
		}
		return Optional.empty();
	}
	
}
