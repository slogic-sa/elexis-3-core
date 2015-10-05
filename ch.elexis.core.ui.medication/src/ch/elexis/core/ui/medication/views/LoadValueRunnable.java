package ch.elexis.core.ui.medication.views;

public abstract class LoadValueRunnable implements Runnable {
	protected PrescriptionDescriptor pd;
	
	public LoadValueRunnable(PrescriptionDescriptor pd){
		this.pd = pd;
	}
	
	@Override
	public void run(){
		loadValue();
		MedicationComposite.updatePrescription(pd);
	}
	
	public abstract void loadValue();
	
}
