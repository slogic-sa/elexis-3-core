package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.Prescription;
import ch.rgw.tools.TimeTool;

public enum ViewerSortOrder {
		MANUAL("manuell", 0, new ManualViewerComparator()),
		DEFAULT("standard", 1, new DefaultViewerComparator());
		
	final String label;
	final int val;
	final ViewerComparator vc;
	
	private static final int DESCENDING = 1;
	private static int direction = DESCENDING;
	private static int propertyIdx = 0;
	private static TimeTool time1 = new TimeTool();
	private static TimeTool time2 = new TimeTool();
	
	private ViewerSortOrder(String label, int val, ViewerComparator vc){
		this.label = label;
		this.val = val;
		this.vc = vc;
	}
	
	public int getDirection(){
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}
	
	public void setColumn(int column){
		if (column == this.propertyIdx) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIdx = column;
			direction = DESCENDING;
		}
	}
	
	/**
	 * sort the medication order by manual ordering as stored in {@link Prescription#FLD_SORT_ORDER}
	 */
	public static class ManualViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			PrescriptionDescriptor p1 = (PrescriptionDescriptor) e1;
			PrescriptionDescriptor p2 = (PrescriptionDescriptor) e2;
			
			return Integer.compare(p1.getSortOrder(), p2.getSortOrder());
		}
	}
	
	/**
	 * sort the medication table viewer first by group (fixed medication or pro re nata medication),
	 * and then by natural article name
	 */
	public static class DefaultViewerComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			PrescriptionDescriptor p1 = (PrescriptionDescriptor) e1;
			PrescriptionDescriptor p2 = (PrescriptionDescriptor) e2;
			int rc = 0;
			switch (propertyIdx) {
			case 0:
				rc = 0;
				break;
			case 1:
				rc = p1.getLabel().compareTo(p2.getLabel());
				break;
			case 2:
				String dose1 = getDose(p1.getDosage());
				String dose2 = getDose(p2.getDosage());
				rc = dose1.compareTo(dose2);
				break;
			case 3:
				time1.set(p1.getLastDisposedAsync());
				time2.set(p2.getLastDisposedAsync());
				rc = time1.compareTo(time2);
				break;
			case 4:
				String supUntil1 = p1.getSuppliedUntilAsync();
				String supUntil2 = p2.getSuppliedUntilAsync();
				rc = supUntil1.compareTo(supUntil2);
				break;
			case 5:
				// stopped column is optional 
				boolean stop1IsValid = isStopped(p1.getStopDateAsync());
				boolean stop2IsValid = isStopped(p2.getStopDateAsync());
				
				if (stop1IsValid && stop2IsValid) {
					time1.set(p1.getStopDateAsync());
					time2.set(p2.getStopDateAsync());
					rc = time1.compareTo(time2);
				} else {
					if (stop1IsValid && !stop2IsValid)
						rc = -1;
					else if (!stop1IsValid && stop2IsValid)
						rc = 1;
					else
						rc = 0;
				}
				break;
			case 6:
				String com1 = p1.getIntakeOrder();
				String com2 = p2.getIntakeOrder();
				rc = com1.compareTo(com2);
				break;
			case 7:
				String stopReason1 = p1.getStopReasonAsync();
				if (stopReason1 == null)
					stopReason1 = "";
					
				String stopReason2 = p2.getStopReasonAsync();
				if (stopReason2 == null)
					stopReason2 = "";
					
				rc = stopReason1.compareTo(stopReason2);
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
		
		private String getDose(String dose){
			return (dose.equals(StringConstants.ZERO) ? "gestoppt" : dose);
		}
		
		private boolean isStopped(String endDate){
			if (endDate != null && endDate.length() > 4) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * 
	 * @param i
	 * @return the respective {@link ViewerSortOrder} for i, or {@link ViewerSortOrder#DEFAULT} if
	 *         invalid or not found
	 */
	public static ViewerSortOrder getSortOrderPerValue(int i){
		for (ViewerSortOrder cvso : ViewerSortOrder.values()) {
			if (cvso.val == i)
				return cvso;
		}
		return null;
	}
}
