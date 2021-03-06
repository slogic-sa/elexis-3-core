package ch.elexis.core.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;

public class Mandator extends Contact implements IMandator {
	
	public Mandator(Kontakt model){
		super(model);
	}
	
	@Override
	public IContact getBiller(){
		String billerId = (String) getExtInfo(MandatorConstants.BILLER);
		if (billerId != null) {
			return CoreModelServiceHolder.get().load((String) billerId, IContact.class)
				.orElse(null);
		}
		// fallback to self billing
		return this;
	}
	
	@Override
	public void setBiller(IContact value){
		setExtInfo(MandatorConstants.BILLER, value.getId());
	}
	
	@Override
	public String getLabel(){
		if (StringUtils.isNotBlank(getDescription3())) {
			return getDescription3();
		} else {
			return StringUtils.defaultString(getDescription1()) + " "
				+ StringUtils.defaultString(getDescription2());
		}
	}
}
