/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.contacts.views;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.data.util.KontaktUtil;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.FlatDataLoader;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.PersistentObjectLoader;
import ch.elexis.core.ui.contacts.Activator;
import ch.elexis.core.ui.dialogs.GenericPrintDialog;
import ch.elexis.core.ui.dialogs.KontaktErfassenDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockedRestrictedAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class KontakteView extends ViewPart implements ControlFieldListener, ISaveablePart2 {
	public static final String ID = "ch.elexis.Kontakte"; //$NON-NLS-1$
	private CommonViewer cv;
	private ViewerConfigurer vc;
	private static Logger log = LoggerFactory.getLogger(KontakteView.class);

	IAction dupKontakt, delKontakt, createKontakt, printList,
		tidySelectedAddressesAction,
		copySelectedContactInfosToClipboardAction,
		copySelectedAddressesToClipboardAction;

	PersistentObjectLoader loader;

	private final String[] fields = { Kontakt.FLD_SHORT_LABEL + Query.EQUALS + Messages.KontakteView_shortLabel,
			Kontakt.FLD_NAME1 + Query.EQUALS + Messages.KontakteView_text1,
			Kontakt.FLD_NAME2 + Query.EQUALS + Messages.KontakteView_text2,
			Kontakt.FLD_STREET + Query.EQUALS + Messages.KontakteView_street,
			Kontakt.FLD_ZIP + Query.EQUALS + Messages.KontakteView_zip,
			Kontakt.FLD_PLACE + Query.EQUALS + Messages.KontakteView_place };
	private ViewMenus menu;

	public KontakteView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		cv = new CommonViewer();
		loader = new FlatDataLoader(cv, new Query<Kontakt>(Kontakt.class));
		loader.setOrderFields(
				new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_STREET, Kontakt.FLD_PLACE });
		vc = new ViewerConfigurer(loader, new KontaktLabelProvider(), new DefaultControlFieldProvider(cv, fields),
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.MULTI, null));
		cv.create(vc, parent, SWT.NONE, getViewSite());
		makeActions();
		cv.setObjectCreateAction(getViewSite(), createKontakt);
		menu = new ViewMenus(getViewSite());
		menu.createViewerContextMenu(cv.getViewerWidget(), delKontakt, dupKontakt);
		menu.createMenu(tidySelectedAddressesAction);
		menu.createMenu(copySelectedContactInfosToClipboardAction);
		menu.createMenu(copySelectedAddressesToClipboardAction);
		menu.createMenu(printList);
		
		menu.createToolbar(tidySelectedAddressesAction);
		menu.createToolbar(copySelectedContactInfosToClipboardAction);
		menu.createToolbar(copySelectedAddressesToClipboardAction);
		menu.createToolbar(printList);
		vc.getContentProvider().startListening();
		vc.getControlFieldProvider().addChangeListener(this);
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
			public void doubleClicked(PersistentObject obj, CommonViewer cv) {
				try {
					KontaktDetailView kdv = (KontaktDetailView) getSite().getPage().showView(KontaktDetailView.ID);
					ElexisEventDispatcher.fireSelectionEvent(obj);
					//					kdv.kb.catchElexisEvent(new ElexisEvent(obj, obj.getClass(), ElexisEvent.EVENT_SELECTED));
				} catch (PartInitException e) {
					ElexisStatus es = new ElexisStatus(ElexisStatus.ERROR, Activator.PLUGIN_ID, ElexisStatus.CODE_NONE,
							"Fehler beim Öffnen", e);
					ElexisEventDispatcher.fireElexisStatusEvent(es);
				}

			}
		});
	}

	public void dispose() {
		vc.getContentProvider().stopListening();
		vc.getControlFieldProvider().removeChangeListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		vc.getControlFieldProvider().setFocus();
	}

	public void changed(HashMap<String, String> values) {
		ElexisEventDispatcher.clearSelection(Kontakt.class);
	}

	public void reorder(String field) {
		loader.reorder(field);
	}

	/**
	 * ENTER has been pressed in the control fields, select the first listed
	 * patient
	 */
	// this is also implemented in PatientenListeView
	public void selected() {
		StructuredViewer viewer = cv.getViewerWidget();
		Object[] elements = cv.getConfigurer().getContentProvider().getElements(viewer.getInput());

		if (elements != null && elements.length > 0) {
			Object element = elements[0];
			/*
			 * just selecting the element in the viewer doesn't work if the
			 * control fields are not empty (i. e. the size of items changes):
			 * cv.setSelection(element, true); bug in TableViewer with style
			 * VIRTUAL? work-arount: just globally select the element without
			 * visual representation in the viewer
			 */
			if (element instanceof PersistentObject) {
				// globally select this object
				ElexisEventDispatcher.fireSelectionEvent((PersistentObject) element);
			}
		}
	}

	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir
	 * benötigen das Interface nur, um das Schliessen einer View zu verhindern,
	 * wenn die Perspektive fixiert ist. Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose() {
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL : ISaveablePart2.NO;
	}

	public void doSave(IProgressMonitor monitor) { /* leer */
	}

	public void doSaveAs() { /* leer */
	}

	public boolean isDirty() {
		return true;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	private void makeActions() {
		delKontakt = new LockedRestrictedAction<Kontakt>(AccessControlDefaults.KONTAKT_DELETE,
				Messages.KontakteView_delete) {
			@Override
			public void doRun(Kontakt k) {
				if (SWTHelper.askYesNo("Wirklich löschen?", k.getLabel())) {
					k.delete();
					cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
				}
			}

			@Override
			public Kontakt getTargetedObject() {
				return (Kontakt) cv.getViewerWidgetFirstSelection();
			}
		};
		dupKontakt = new Action(Messages.KontakteView_duplicate) {
			@Override
			public void run() {
				Object[] o = cv.getSelection();
				if (o != null) {
					Kontakt k = (Kontakt) o[0];
					Kontakt dup;
					if (k.istPerson()) {
						Person p = Person.load(k.getId());
						dup = new Person(p.getName(), p.getVorname(), p.getGeburtsdatum(), p.getGeschlecht());
					} else {
						Organisation org = Organisation.load(k.getId());
						dup = new Organisation(org.get(Organisation.FLD_NAME1), org.get(Organisation.FLD_NAME2));
					}
					dup.setAnschrift(k.getAnschrift());
					cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
					// cv.getViewerWidget().refresh();
				}
			}
		};
		createKontakt = new Action(Messages.KontakteView_create) {
			@Override
			public void run() {
				String[] flds = cv.getConfigurer().getControlFieldProvider().getValues();
				String[] predef = new String[] { flds[1], flds[2], StringConstants.EMPTY, flds[3], flds[4], flds[5] };
				KontaktErfassenDialog ked = new KontaktErfassenDialog(getViewSite().getShell(), predef);
				ked.open();
			}
		};

		printList = new Action("Markierte Adressen drucken") {
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Die in der Liste markierten Kontakte als Tabelle ausdrucken");
			}

			public void run() {
				Object[] sel = cv.getSelection();
				String[][] adrs = new String[sel.length][];
				if (sel != null && sel.length > 0) {
					GenericPrintDialog gpl = new GenericPrintDialog(getViewSite().getShell(), "Adressliste",
							"Adressliste");
					gpl.create();
					for (int i = 0; i < sel.length; i++) {
						Kontakt k = (Kontakt) sel[i];
						String[] f = new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_NAME3,
								Kontakt.FLD_STREET, Kontakt.FLD_ZIP, Kontakt.FLD_PLACE, Kontakt.FLD_PHONE1 };
						String[] v = new String[f.length];
						k.get(f, v);
						adrs[i] = new String[4];
						adrs[i][0] = new StringBuilder(v[0]).append(StringConstants.SPACE).append(v[1])
								.append(StringConstants.SPACE).append(v[2]).toString();
						adrs[i][1] = v[3];
						adrs[i][2] = new StringBuilder(v[4]).append(StringConstants.SPACE).append(v[5]).toString();
						adrs[i][3] = v[6];
					}
					gpl.insertTable("[Liste]", adrs, null);
					gpl.open();
				}
			}
		};

	/*
	 * TODO: Should each field be capable of cleaning its content ? (Jörg Sigle & Niklaus Giger)
	 * TODO: We must find a way to handle different languages + research actual content of database columns
	 * TODO: Configurability following preferences of diffferent users
	 *   
	 * @remark please note if at least one field of a contact is changed, all fields of the contact will be appended
	 *         to the clipboard. The result can be pasted into a spreadshead, and a macro exists to highlight then changed fields
	 *         This allows checking whether your algorithm is good or not 
	 * 
	 * Clean selected address(es):
	 * For all selected addresses do:
	 * If FLD_IS_PATIENT==true, then set FLD_IS_PERSON=true (otherwise, invalid xml invoices may be produced, addressed to institutions instead of persons)
	 * For each address field: remove leading and trailing spaces. 
	 */
	tidySelectedAddressesAction = new Action(Messages.KontakteView_tidySelectedAddresses) {
		{
			setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
			setToolTipText(Messages.KontakteView_tidySelectedAddresses);
		}
		
		@Override
		public void run(){
			
			Object[] sel = cv.getSelection();
			StringBuffer SelectedContactInfosChangedList = new StringBuffer();
							
			if (sel != null && sel.length > 0) {
				
				
				for (int i = 0; i < sel.length; i++) {

					if (i % 100 == 0) {
						log.debug("KontakteView tidySelectedAddressesAction.run Processing entry "+i+"...");		
					};
					
					
					Kontakt k = (Kontakt) sel[i];
					SelectedContactInfosChangedList.append(KontaktUtil.tidyContactInfo(k));
				}
			
				/*
				 * In order to export the list of addresses that might warrant a manual review of Postadresse to the clipboard,
				 * I have added the clipboard export routine also used in the copyToClipboard... methods further below.
				 * If not for this purpose, building up the stringsBuffer content would not have been required,
				 * and neither would have been any kind of clipboard interaction.
				 *
				 * I would prefer to move the following code portions down behind the "if sel not empty" block,
				 * so that (a) debugging output can be produced and (b) the clipboard will be emptied
				 * when NO Contacts have been selected. I did this to avoid the case where a user would assume
				 * they had selected some address, copied data to the clipboard, and pasted them - and, even
				 * when they erred about their selection, which was indeed empty, they would not immediately
				 * notice that because some (old, unchanged) content would still come out of the clipboard.
				 * 
				 * But if I do so, and there actually is no address selected, I get an error window:
				 * Unhandled Exception ... not valid. So to avoid that message without any further research
				 * (I need to get this work fast now), I move the code back up and leave the clipboard
				 * unchanged for now, if no Contacts had been selected to process.
				 * 
				 * (However, I may disable the toolbar icon / menu entry for this action in that case later on.) 
			 	 */				 	 

				//Copy some generated object.toString() to the clipoard
				if ( (SelectedContactInfosChangedList != null) && (SelectedContactInfosChangedList.length()>0) ) {
				
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

			}			//if sel not empty
		
			//update display to reflect changed information
				
			//This updates the "Kontakte" window, but not the "Kontakt Detail" window:
			log.debug("KontakteView tidySelectedAddressesAction.run Triggering update of the 'Kontakte' Window.");
			log.debug("KontakteView tidySelectedAddressesAction.run This will lose the selection, but sadly NOT update 'Kontakt Detail'.");			
			cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
	
			if (sel.length >1 ) {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell(), String.format("%s Adressen geputzt", sel.length),
						 String.format("Folgende Anpassungen wurden gemacht:\n%s", SelectedContactInfosChangedList.toString()));
				}

			// TODO: Wenn nur ein einziger Kontakt markiert und geputzt wurde, sollte die Anzeige in KontaktDetail
			//       anschliessend sofort aktualisiert werden, damit der Nutzer den neuen Content sieht.
			
			//Wenn mehrere Einträge aktualisiert waren, und nach obigem die Selektion verschwindet,
			//wird inhärent auch der Inhalt von Kontakt Detail geändert (weil wahrscheinlich ein anderer Eintrag selektiert wird),
			//und somit auch dieses aktualisiert. Bei nur einem a priori selektierten und per tidy... behandelten Eintrag
			//erfolgt das Update aber erst, wenn man einen anderen und dann wieder diesen Patienten selektiert. :-(
			
			//Nachfolgend noch diverse Versuche, das zu erreichen - aber da sitz ich schon wieder Stunden,
			//möglicherweise bräuchte KontaktDetailView oder KontaktBlatt dafür eine Methode,
			//und/oder man muss irgendwas mit parent.layout aufrufen etc. Das ist jetzt NICHT vordringlich - keine Zeit dafür.
			
			//Inform the system that all objects of a given class have to be loaded from storage.
			//Sorry, but it will NOT cause any window content to be updated.
			//ElexisEventDispatcher.reload(Kontakt.class);
			
			log.debug("KontakteView tidySelectedAddressesAction.run end");
		}
	};		//tidySelectedAddressesAction = new Action()

		
	/* Copy selected contact data (complete) to the clipboard, so it/they can be easily pasted into a target document
	 * for various further usage. This variant produces a more complete data set than copySelectedAddresses... below;
	 * it also includes the phone numbers and does not use the postal address, but all the individual data fields.
	 * Two actions with identical / similar code has also been added to PatientenListeView.java 
	 */
	copySelectedContactInfosToClipboardAction = new Action(Messages.KontakteView_copySelectedContactInfosToClipboard) {
		{
			setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
			setToolTipText(Messages.KontakteView_copySelectedContactInfosToClipboard);
		}
		
		@Override
		public void run(){
			
			//Convert the selected contacts into a list
			StringBuffer SelectedContactInfosText = new StringBuffer();
							
			Object[] sel = cv.getSelection();
			if (sel != null && sel.length > 0) {				
				for (int i = 0; i < sel.length; i++) {
					Kontakt k = (Kontakt) sel[i];
					
					SelectedContactInfosText.append(KontaktUtil.getContactInfo(k));
					//Add another empty line (or rather: paragraph), if at least one more address will follow.
					if (i<sel.length-1) {
						SelectedContactInfosText.append(System.getProperty("line.separator"));
					}
				}

				/*
				 * I would prefer to move the following code portions down behind the "if sel not empty" block,
				 * so that (a) debugging output can be produced and (b) the clipboard will be emptied
				 * when NO Contacts have been selected. I did this to avoid the case where a user would assume
				 * they had selected some address, copied data to the clipboard, and pasted them - and, even
				 * when they erred about their selection, which was indeed empty, they would not immediately
				 * notice that because some (old, unchanged) content would still come out of the clipboard.
				 * 
				 * But if I do so, and there actually is no address selected, I get an error window:
				 * Unhandled Exception ... not valid. So to avoid that message without any further research
				 * (I need to get this work fast now), I move the code back up and leave the clipboard
				 * unchanged for now, if no Contacts had been selected to process.
				 * 
				 * (However, I may disable the toolbar icon / menu entry for this action in that case later on.) 
			 	 */				 	 

				//For Patientenblatt2.java, PatientenListeView.java, KontakteView.java
				//This function was primarily introduced because the above algorithm
				//returns a double space in the phone number area. Maybe that from a field containing a space,
				//or truly generated in the code above.
				
				//Right now I have no time, and the postprocessing has the advantage of correcting unwanted
				//content inside the fields as well. So I only add this today.
				
				// TO DO Please look up the reason for the extra space above.
				// TO DO Please make sure that multibyte Unicode characters are correctly handled below. 
				// TO DO Add similar postprocessing to copySelectedAdressesToClipboard of all three .java files
				// TO DO Move the processing into a separate method/function, when the copy... methods are refactored.
					
				//Postprocess selectedContactInfosText:
				//Remove any leading or trailing spaces;
				//Replace any " ," by ",";
				//Replace any " ." by ".";
				//Replace any multiple spaces by single spaces.
				int n=0;
				while (n<SelectedContactInfosText.length()) {
					if (   SelectedContactInfosText.codePointAt(n)==StringTool.space.codePointAt(0)
						&& ( n==SelectedContactInfosText.length()
							|| SelectedContactInfosText.codePointAt(n+1)==(",").codePointAt(0)
							|| SelectedContactInfosText.codePointAt(n+1)==(".").codePointAt(0)
							|| SelectedContactInfosText.codePointAt(n+1)==StringTool.space.codePointAt(0) )
						){ 
						SelectedContactInfosText.deleteCharAt(n);
						} else { 
						n=n+1;}
				};
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
		};  	//copySelectedContactInfosToClipboardAction.run()
	};

	/* Copy selected address(es) to the clipboard, so it/they can be easily pasted into a letter for printing.
	 * Two actions with identical / similar code has also been added to PatientenListeView.java 
	 */
	copySelectedAddressesToClipboardAction = new Action(Messages.KontakteView_copySelectedAddressesToClipboard) {
		{
			setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
			setToolTipText(Messages.KontakteView_copySelectedAddressesToClipboard);
		}
		
		@Override
		public void run(){
			StringBuffer selectedAddressesText = new StringBuffer();							
			Object[] sel = cv.getSelection();
			
			if (sel != null && sel.length > 0) {
				
				for (int i = 0; i < sel.length; i++) {
					Kontakt k = (Kontakt) sel[i];

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
					selectedAddressesText.append(k.getPostAnschriftPhoneFaxEmail(true,true));

					//Add another empty line (or rather: paragraph), if at least one more address will follow.
					if (i<sel.length-1) {
						selectedAddressesText.append(System.getProperty("line.separator"));
									
					}
				}		//for each element in sel do

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
			}			//if sel not empty
		};  	//copySelectedAddressesToClipboardAction.run()

	};
}


	class KontaktLabelProvider extends DefaultLabelProvider {

		@Override
		public String getText(Object element) {
			String[] fields = new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_NAME3,
					Kontakt.FLD_STREET, Kontakt.FLD_ZIP, Kontakt.FLD_PLACE, Kontakt.FLD_PHONE1 };
			String[] values = new String[fields.length];
			((Kontakt) element).get(fields, values);
			return StringTool.join(values, StringConstants.COMMA);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
}
