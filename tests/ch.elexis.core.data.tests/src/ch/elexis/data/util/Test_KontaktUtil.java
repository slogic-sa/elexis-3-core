/*******************************************************************************
 * Copyright (c) 2018,  and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    N. Giger - initial implementation
 *******************************************************************************/
package ch.elexis.data.util;

import org.junit.Assert;
import org.junit.Test;

import ch.elexis.core.data.util.KontaktUtil;
import ch.elexis.data.AbstractPersistentObjectTest;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.rgw.tools.JdbcLink;

public class Test_KontaktUtil extends AbstractPersistentObjectTest {
	
	public Test_KontaktUtil(JdbcLink link){
		super(link);
	}

	@Test
	public void testTidyContactInfo(){
		
		final String familyNameWithApostrophe = "D'Andrea";
		Patient male = new Patient("Mustermann", "Max", "1.1.2000", "m");
		male.set(Patient.FLD_NAME, familyNameWithApostrophe);
		male.set(Kontakt.FLD_NAME3,"Facharzt FMH f.");

		male.set(Patient.FLD_ANSCHRIFT, "prof.");
		StringBuffer SelectedContactInfosChangedList = KontaktUtil.tidyContactInfo(male);
		Assert.assertEquals("Facharzt FMH für",  male.get(Kontakt.FLD_NAME3));
		Assert.assertNotEquals(0, SelectedContactInfosChangedList.toString().length());
		Assert.assertEquals("Prof.",  male.get(Kontakt.FLD_ANSCHRIFT));
	}

	@Test
	public void testTidyContactInfoNothingToChange(){
		final String familyNameWithApostrophe = "D'Andrea";
		final String anrede = "Prof. Dr. med. FMH";
		Patient male = new Patient("Mustermann", "Max", "1.1.2000", "m");
		male.set(Patient.FLD_NAME, familyNameWithApostrophe);
		male.set(Kontakt.FLD_NAME3,"Facharzt FMH für");

		male.set(Patient.FLD_ANSCHRIFT, anrede);
		StringBuffer SelectedContactInfosChangedList = KontaktUtil.tidyContactInfo(male);
		Assert.assertEquals("Facharzt FMH für",  male.get(Kontakt.FLD_NAME3));
		Assert.assertEquals(0, SelectedContactInfosChangedList.toString().length());
		Assert.assertEquals(anrede,  male.get(Kontakt.FLD_ANSCHRIFT));
	}

	@Test
	public void testGetContactInfo(){
		
		String[] fieldnames = {Patient.FLD_NAME, Patient.FLD_NAME1,
				Patient.FLD_NAME2, Patient.FLD_NAME3, Patient.FLD_REMARK,
				Patient.FLD_NAME3, Patient.FLD_ANSCHRIFT, Patient.FLD_STREET,
				// Patient.FLD_COUNTRY,
				Patient.FLD_ZIP,
				Patient.FLD_PLACE,
				Patient.FLD_PHONE1,
				Patient.FLD_PHONE1,
				Patient.FLD_MOBILEPHONE,
				Patient.FLD_FAX,
				Patient.FLD_E_MAIL
				};
		
		final String familyNameWithApostrophe = "D'Andrea";
		Patient male = new Patient("Mustermann", "Max", "1.1.2000", "m");
		for (String field : fieldnames ) {
			male.set(field, field);			
		
		}
		male.set(Patient.FLD_NAME, familyNameWithApostrophe);
		male.set(Patient.FLD_REMARK, "Bemerkung");
		male.set(Patient.FLD_NAME3, "FLD_NAME3");
		male.set(Patient.FLD_ANSCHRIFT, "prof.");
		StringBuffer info = KontaktUtil.getContactInfo(male);
		Assert.assertEquals(ch.elexis.core.data.util.Messages.KontakteView_SalutationM +
				" Max D'Andrea, Bemerkung, FLD_NAME3, 01.01.2000, Strasse, Plz Ort,  Telefon1, NatelNr NatelNr, Fax Fax, E-Mail",  info.toString());
	}

}
