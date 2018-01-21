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

package ch.elexis.core.ui.contacts.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.KontaktUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.rgw.tools.StringTool;

public class ContactActions {
	private static Logger log = LoggerFactory.getLogger(ContactActions.class);
	
	/**
	 * TODO: Should each field be capable of cleaning its content ? (Jörg Sigle & Niklaus Giger)
	 * <br>
	 * TODO: We must find a way to handle different languages + research actual content of database
	 * columns <br>
	 * TODO: Configurability following preferences of diffferent users <br>
	 * <br>
	 * please note if at least one field of a contact is changed, all fields of the contact will be
	 * appended to the clipboard. The result can be pasted into a spreadshead, and a macro exists to
	 * highlight then changed fields This allows checking whether your algorithm is good or not <br>
	 * Clean selected address(es): <br>
	 * * For all selected addresses do: <br>
	 * * If FLD_IS_PATIENT==true, then set FLD_IS_PERSON=true (otherwise, invalid xml invoices may
	 * be produced, addressed to institutions instead of persons) <br>
	 * * For each address field: remove leading and trailing spaces.
	 * 
	 * @param iSelection
	 *            CommonViewer
	 */
	public static Action getTidySelectedAddressesAction(StructuredViewer viewer){
		
		/*
		 * TODO: Should each field be capable of cleaning its content ? (Jörg Sigle &
		 * Niklaus Giger) TODO: We must find a way to handle different languages +
		 * research actual content of database columns TODO: Configurability following
		 * preferences of diffferent users
		 * 
		 * @remark please note if at least one field of a contact is changed, all fields
		 * of the contact will be appended to the clipboard. The result can be pasted
		 * into a spreadshead, and a macro exists to highlight then changed fields This
		 * allows checking whether your algorithm is good or not
		 * 
		 * Clean selected address(es): For all selected addresses do: If
		 * FLD_IS_PATIENT==true, then set FLD_IS_PERSON=true (otherwise, invalid xml
		 * invoices may be produced, addressed to institutions instead of persons) For
		 * each address field: remove leading and trailing spaces.
		 */
		Action tidySelectedAddressesAction =
			new Action(Messages.KontakteView_tidySelectedAddresses) {
				{
					setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
					setToolTipText(Messages.KontakteView_tidySelectedAddresses);
				}
				
				@Override
				public void run(){
					
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Object[] sel = selection.toArray();
					StringBuffer SelectedContactInfosChangedList = new StringBuffer();
					int nrChanged = 0;
					
					if (sel != null && sel.length > 0) {
						
						for (int i = 0; i < sel.length; i++) {
							
							if (i % 100 == 0) {
								log.debug(
									"KontakteView tidySelectedAddressesAction.run Processing entry "
										+ i + "...");
							}
							;
							
							Kontakt k = getKontactFromSelected(sel[i]);
							if (k == null) {
								break;
							}
							StringBuffer changed = KontaktUtil.tidyContactInfo(k);
							if (changed.length() > 0) {
								nrChanged++;
								SelectedContactInfosChangedList.append(changed);
							}
						}
						
						/*
						 * In order to export the list of addresses that might warrant a manual review
						 * of Postadresse to the clipboard, I have added the clipboard export routine
						 * also used in the copyToClipboard... methods further below. If not for this
						 * purpose, building up the stringsBuffer content would not have been required,
						 * and neither would have been any kind of clipboard interaction.
						 *
						 * I would prefer to move the following code portions down behind the
						 * "if sel not empty" block, so that (a) debugging output can be produced and
						 * (b) the clipboard will be emptied when NO Contacts have been selected. I did
						 * this to avoid the case where a user would assume they had selected some
						 * address, copied data to the clipboard, and pasted them - and, even when they
						 * erred about their selection, which was indeed empty, they would not
						 * immediately notice that because some (old, unchanged) content would still
						 * come out of the clipboard.
						 * 
						 * But if I do so, and there actually is no address selected, I get an error
						 * window: Unhandled Exception ... not valid. So to avoid that message without
						 * any further research (I need to get this work fast now), I move the code back
						 * up and leave the clipboard unchanged for now, if no Contacts had been
						 * selected to process.
						 * 
						 * (However, I may disable the toolbar icon / menu entry for this action in that
						 * case later on.)
						 */
						
						// Copy some generated object.toString() to the clipoard
						if ((SelectedContactInfosChangedList != null)
							&& (SelectedContactInfosChangedList.length() > 0)) {
							
							Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
							TextTransfer textTransfer = TextTransfer.getInstance();
							Transfer[] transfers = new Transfer[] {
								textTransfer
							};
							Object[] data = new Object[] {
								SelectedContactInfosChangedList.toString()
							};
							clipboard.setContents(data, transfers);
							clipboard.dispose();
						}
						
					} // if sel not empty
					
					Kontakt k = getKontactFromSelected(selection.getFirstElement());
					if (k != null) {
						ElexisEventDispatcher.fireSelectionEvent(Kontakt.load(k.getId()));
					}
					
					String msgTitle = String.format(
						"%s von %s ausgewählten Adressen geputzt\nIn der Zwischenablage ist:\n",
						nrChanged, sel.length);
					if (sel.length > 1) {
						MessageDialog.openInformation(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							msgTitle, String.format("Folgende Anpassungen wurden gemacht:\n%s",
								SelectedContactInfosChangedList.toString()));
					}
					log.debug(msgTitle);
				}
			}; // tidySelectedAddressesAction = new Action()
		return tidySelectedAddressesAction;
	}
	
	/**
	 * Copy selected contact data (complete) to the clipboard, so it/they can be easily pasted into
	 * a target document for various further usage. This variant produces a more complete data set
	 * than copySelectedAddresses... below; it also includes the phone numbers and does not use the
	 * postal address, but all the individual data fields. Two actions with identical / similar code
	 * has also been added to PatientenListeView.java
	 * 
	 * @param cv
	 *            CommonViewer
	 */
	public static Action getCopySelectedContactInfosToClipboardAction(StructuredViewer viewer){
		Action copySelectedContactInfosToClipboardAction =
			new Action(Messages.KontakteView_copySelectedContactInfosToClipboard) {
				{
					setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
					setToolTipText(Messages.KontakteView_copySelectedContactInfosToClipboard);
				}
				
				@Override
				public void run(){
					
					// Convert the selected contacts into a list
					StringBuffer SelectedContactInfosText = new StringBuffer();
					
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Object[] sel = selection.toArray();
					if (sel != null && sel.length > 0) {
						for (int i = 0; i < sel.length; i++) {
							Kontakt k = getKontactFromSelected(sel[i]);
							if (k == null) {
								break;
							}
							
							SelectedContactInfosText.append(KontaktUtil.getContactInfo(k));
							// Add another empty line (or rather: paragraph), if at least one more address
							// will follow.
							if (i < sel.length - 1) {
								SelectedContactInfosText
									.append(System.getProperty("line.separator"));
							}
						}
						
						/*
						 * I would prefer to move the following code portions down behind the
						 * "if sel not empty" block, so that (a) debugging output can be produced and
						 * (b) the clipboard will be emptied when NO Contacts have been selected. I did
						 * this to avoid the case where a user would assume they had selected some
						 * address, copied data to the clipboard, and pasted them - and, even when they
						 * erred about their selection, which was indeed empty, they would not
						 * immediately notice that because some (old, unchanged) content would still
						 * come out of the clipboard.
						 * 
						 * But if I do so, and there actually is no address selected, I get an error
						 * window: Unhandled Exception ... not valid. So to avoid that message without
						 * any further research (I need to get this work fast now), I move the code back
						 * up and leave the clipboard unchanged for now, if no Contacts had been
						 * selected to process.
						 * 
						 * (However, I may disable the toolbar icon / menu entry for this action in that
						 * case later on.)
						 */
						
						// For Patientenblatt2.java, PatientenListeView.java, KontakteView.java
						// This function was primarily introduced because the above algorithm
						// returns a double space in the phone number area. Maybe that from a field
						// containing a space,
						// or truly generated in the code above.
						
						// Right now I have no time, and the postprocessing has the advantage of
						// correcting unwanted
						// content inside the fields as well. So I only add this today.
						
						// TO DO Please look up the reason for the extra space above.
						// TO DO Please make sure that multibyte Unicode characters are correctly
						// handled below.
						// TO DO Add similar postprocessing to copySelectedAdressesToClipboard of all
						// three .java files
						// TO DO Move the processing into a separate method/function, when the copy...
						// methods are refactored.
						
						// Postprocess selectedContactInfosText:
						// Remove any leading or trailing spaces;
						// Replace any " ," by ",";
						// Replace any " ." by ".";
						// Replace any multiple spaces by single spaces.
						int n = 0;
						while (n < SelectedContactInfosText.length()) {
							if (SelectedContactInfosText.codePointAt(n) == StringTool.space
								.codePointAt(0)
								&& (n == SelectedContactInfosText.length()
									|| SelectedContactInfosText.codePointAt(n + 1) == (",")
										.codePointAt(0)
									|| SelectedContactInfosText.codePointAt(n + 1) == (".")
										.codePointAt(0)
									|| SelectedContactInfosText
										.codePointAt(n + 1) == StringTool.space.codePointAt(0))) {
								SelectedContactInfosText.deleteCharAt(n);
							} else {
								n = n + 1;
							}
						}
						;
						Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
						TextTransfer textTransfer = TextTransfer.getInstance();
						Transfer[] transfers = new Transfer[] {
							textTransfer
						};
						Object[] data = new Object[] {
							SelectedContactInfosText.toString()
						};
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					}
				}
			};
		return copySelectedContactInfosToClipboardAction;
	}
	
	/**
	 * Copy selected address(es) to the clipboard, so it/they can be easily pasted into a letter for
	 * printing. Two actions with identical / similar code has also been added to
	 * PatientenListeView.java
	 * 
	 * @param cv
	 *            CommonViewer
	 */
	public static Action getCopySelectedAddressesToClipboardAction(StructuredViewer viewer){
		Action copySelectedContactInfosToClipboardAction =
			new Action(Messages.KontakteView_copySelectedAddressesToClipboard) {
				{
					setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
					setToolTipText(Messages.KontakteView_copySelectedAddressesToClipboard);
				}
				
				@Override
				public void run(){
					StringBuffer selectedAddressesText = new StringBuffer();
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Object[] sel = selection.toArray();
					
					if (sel != null && sel.length > 0) {
						
						for (int i = 0; i < sel.length; i++) {
							Kontakt k = getKontactFromSelected(sel[i]);
							if (k == null) {
								break;
							}
							
							/*
							 * Synthesize the address lines to output from the entries in Kontakt k;
							 * added to implement the output format desired for the copyAddressToClipboard()
							 * buttons added to version 2.1.6.js as of 2012-01-28ff
							 *
							 * We might synthesize our own "Anschrift" for each Kontakt,
							 * completely according to our own requirements,
							 * OR use any of the methods defined for Kontakt like:
							 * getLabel...(), getPostAnschrift, createStandardAnschrift, List<BezugsKontakt>... -
							 * 
							 * The Declaration of Kontakt with field definitions is available in Kontakt.java, please look
							 * therein for additional details, please. Click-Right -> Declaration on Kontakt in Eclipse works.
							 * You can also look above to see the fields that printList would use.
							 */
							
							//getPostAnschriftPhoneFaxEmail() already returns a line separator after the address
							//The first parameter controls multiline or single line output
							//The second parameter controls whether the phone numbers shall be included
							selectedAddressesText
								.append(k.getPostAnschriftPhoneFaxEmail(true, true));
							
							//Add another empty line (or rather: paragraph), if at least one more address will follow.
							if (i < sel.length - 1) {
								selectedAddressesText.append(System.getProperty("line.separator"));
								
							}
						} //for each element in sel do
						
						/*
						 * I would prefer to move the following code portions down behind the "if sel not empty" block,
						 * so that (a) debugging output can be produced and (b) the clipboard will be emptied
						 * when NO addresses have been selected. I did this to avoid the case where a user would assume
						 * they had selected some address, copied data to the clipboard, and pasted them - and, even
						 * when they erred about their selection, which was indeed empty, they would not immediately
						 * notice that because some (old, unchanged) content would still come out of the clipboard.
						 * 
						 * But if I do so, and there actually is no address selected, I get an error window:
						 * Unhandled Exception ... not valid. So to avoid that message without any further research
						 * (I need to get this work fast now), I move the code back up and leave the clipboard
						 * unchanged for now, if no addresses had been selected to process.
						 * 
						 * (However, I may disable the toolbar icon / menu entry for this action in that case later on.) 
						 */
						
						Clipboard clipboard = new Clipboard(UiDesk.getDisplay());
						TextTransfer textTransfer = TextTransfer.getInstance();
						Transfer[] transfers = new Transfer[] {
							textTransfer
						};
						Object[] data = new Object[] {
							selectedAddressesText.toString()
						};
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					} //if sel not empty
				}; //copySelectedAddressesToClipboardAction.run()
			};
		return copySelectedContactInfosToClipboardAction;
	}
	
	private static Kontakt getKontactFromSelected(Object obj){
		Kontakt k = null;
		if (obj.getClass().equals(Kontakt.class)) {
			return (Kontakt) obj;
		} else if (obj.getClass().equals(ContactBean.class)) {
			ContactBean bean = (ContactBean) obj;
			if (bean.isPatient()) {
				return Patient.loadByPatientID(bean.getPatientNr());
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
