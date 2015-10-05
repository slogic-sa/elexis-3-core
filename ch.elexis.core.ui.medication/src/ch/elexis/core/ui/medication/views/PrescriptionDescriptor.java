package ch.elexis.core.ui.medication.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.elexis.data.Query;
import ch.elexis.data.Rezept;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class PrescriptionDescriptor {
	private Prescription prescription;
	private String label;
	private String dosage;
	private String intakeOrder;
	private int sortOrder;
	private EntryType type;
	private String lastDisposed;
	private String lastDisposedLabel;
	private String suppliedUntil;
	private String stopDate;
	private String stopReason;
	
	private static Executor executor = Executors.newCachedThreadPool();
	
	public PrescriptionDescriptor(Prescription p, String label, String dosage, String intakeOrder,
		int sortOrder){
		this.prescription = p;
		this.label = label;
		this.dosage = dosage;
		this.intakeOrder = intakeOrder;
		this.sortOrder = sortOrder;
	}
	
	public static List<PrescriptionDescriptor> fetchForPatient(Patient pat){
		List<PrescriptionDescriptor> prescDescriptors = new ArrayList<PrescriptionDescriptor>();
		
		//get all prescriptions of this patient
		Query<Prescription> qbe = new Query<Prescription>(Prescription.class);
		qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, pat.getId());
		List<Prescription> execute = qbe.execute();
		if (execute != null && !execute.isEmpty()) {
			for (Prescription presc : execute) {
				String[] values = presc.get(true, Prescription.FLD_ARTICLE, Prescription.FLD_DOSAGE,
					Prescription.FLD_REMARK, Prescription.FLD_SORT_ORDER);
					
				Artikel article = null;
				if (StringTool.isNothing(values[0])) {
					article = Artikel.load(presc.get(Prescription.FLD_ARTICLE_ID));
				}
				article = (Artikel) CoreHub.poFactory.createFromString(values[0]);
				
				PrescriptionDescriptor pd =
					new PrescriptionDescriptor(presc, resolveArticleLabel(article), values[1],
						values[2], resolveSortOrder(values[3]));
				prescDescriptors.add(pd);
			}
		}
		return prescDescriptors;
	}
	
	private static String resolveArticleLabel(Artikel arti){
		if (arti == null) {
			return "?";
		}
		
		String label = arti.getLabel();
		if (label == null || label.isEmpty()) {
			return "?";
		}
		return label;
	}
	
	private static int resolveSortOrder(String value){
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return 999;
		}
	}
	
	public Prescription getPrescription(){
		return prescription;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setDosage(String dosage){
		this.dosage = dosage;
	}
	
	public String getDosage(){
		return dosage;
	}
	
	public void setIntakeOrder(String intakeOrder){
		this.intakeOrder = intakeOrder;
	}
	
	public String getIntakeOrder(){
		return intakeOrder;
	}
	
	public void setSortOrder(int sortOrder){
		this.sortOrder = sortOrder;
	}
	
	public int getSortOrder(){
		return sortOrder;
	}
	
	public EntryType getEntryTypeAsync(){
		if (type == null) {
			loadEntryType();
			return null;
		}
		return type;
	}
	
	public void setEntryType(EntryType type){
		this.type = type;
	}
	
	private void loadEntryType(){
		executor.execute(new LoadEntryTypeRunnable(this));
	}
	
	public String getLastDisposedAsync(){
		if (lastDisposed == null) {
			loadLastDisposed();
			return "";
		}
		return lastDisposed;
	}
	
	public void setLastDisposed(String lastDisposed){
		this.lastDisposed = lastDisposed;
	}
	
	private void loadLastDisposed(){
		executor.execute(new LoadLastDisposedRunnable(this));
	}
	
	public String getLastDisposedLabelAsync(){
		if (lastDisposedLabel == null) {
			loadLastDisposed();
			return "";
		}
		return lastDisposedLabel;
	}
	
	public void setLastDisposedLabel(String lastDisposedLabel){
		this.lastDisposedLabel = lastDisposedLabel;
	}
	
	public String getSuppliedUntilAsync(){
		if (suppliedUntil == null) {
			loadSuppliedUntil();
			return "";
		}
		return suppliedUntil;
	}
	
	public void setSuppliedUntil(String suppliedUntil){
		this.suppliedUntil = suppliedUntil;
	}
	
	private void loadSuppliedUntil(){
		executor.execute(new LoadSuppliedUntilRunnable(this));
	}
	
	public String getStopDateAsync(){
		if (stopDate == null) {
			loadStopDate();
			return "";
		}
		return stopDate;
	}
	
	public void setStopDate(String stopDate){
		this.stopDate = stopDate;
	}
	
	private void loadStopDate(){
		executor.execute(new LoadStopDateRunnable(this));
	}
	
	public String getStopReasonAsync(){
		if (stopReason == null) {
			loadStopReason();
			return "";
		}
		return stopReason;
	}
	
	public void setStopReason(String stopReason){
		this.stopReason = stopReason;
	}
	
	private void loadStopReason(){
		executor.execute(new LoadStopReasonRunnable(this));
	}
	
	private class LoadEntryTypeRunnable extends LoadValueRunnable {
		public LoadEntryTypeRunnable(PrescriptionDescriptor pd){
			super(pd);
		}
		
		@Override
		public void loadValue(){
			EntryType entryType = pd.getPrescription().getEntryType();
			pd.setEntryType(entryType);
		}
	}
	
	private class LoadLastDisposedRunnable extends LoadValueRunnable {
		
		public LoadLastDisposedRunnable(PrescriptionDescriptor pd){
			super(pd);
		}
		
		@Override
		public void loadValue(){
			TimeTool time = new TimeTool(pd.getPrescription().getBeginDate());
			
			String lastDisposed = "";
			String ldFullLabel = "";
			IPersistentObject po = pd.getPrescription().getLastDisposed();
			if (po != null) {
				if (po instanceof Rezept) {
					Rezept recipe = (Rezept) po;
					lastDisposed = recipe.getDate();
					ldFullLabel =
						MessageFormat.format(Messages.MedicationComposite_recipeFrom, lastDisposed);
				} else if (po instanceof Verrechnet) {
					Verrechnet v = (Verrechnet) po;
					if (v.getKons() == null) {
						ldFullLabel = Messages.MedicationComposite_consMissing;
					} else {
						lastDisposed = v.getKons().getDatum();
						ldFullLabel = MessageFormat.format(Messages.MedicationComposite_consFrom,
							lastDisposed);
					}
				}
			}
			
			if (lastDisposed != null && !lastDisposed.isEmpty()) {
				time.set(lastDisposed);
			}
			pd.setLastDisposed(time.toString(TimeTool.DATE_GER_SHORT));
			pd.setLastDisposedLabel(ldFullLabel);
		}
		
	}
	
	private class LoadSuppliedUntilRunnable extends LoadValueRunnable {
		
		public LoadSuppliedUntilRunnable(PrescriptionDescriptor pd){
			super(pd);
		}
		
		@Override
		public void loadValue(){
			Prescription p = pd.getPrescription();
			if (!p.isFixedMediation() || p.isReserveMedication()) {
				pd.setSuppliedUntil("");
			} else {
				TimeTool time = p.getSuppliedUntilDate();
				if (time != null && time.isAfterOrEqual(new TimeTool())) {
					pd.setSuppliedUntil("OK");
				} else {
					pd.setSuppliedUntil("?");
				}
			}
			
		}
		
	}
	
	private class LoadStopDateRunnable extends LoadValueRunnable {
		public LoadStopDateRunnable(PrescriptionDescriptor pd){
			super(pd);
		}
		
		@Override
		public void loadValue(){
			String endDate = pd.getPrescription().getEndDate();
			if (endDate != null && endDate.length() > 4) {
				TimeTool tt = new TimeTool(endDate);
				pd.setStopDate(tt.toString(TimeTool.DATE_GER_SHORT));
			} else {
				pd.setStopDate("");
			}
		}
	}
	
	private class LoadStopReasonRunnable extends LoadValueRunnable {
		public LoadStopReasonRunnable(PrescriptionDescriptor pd){
			super(pd);
		}
		
		@Override
		public void loadValue(){
			String reason = pd.getPrescription().getStopReason();
			if (reason == null || reason.isEmpty()) {
				pd.setStopReason("");
			} else {
				pd.setStopReason(reason);
			}
		}
	}
}
