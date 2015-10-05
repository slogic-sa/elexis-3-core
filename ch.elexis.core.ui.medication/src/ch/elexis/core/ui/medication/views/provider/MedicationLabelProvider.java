package ch.elexis.core.ui.medication.views.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.medication.views.Messages;
import ch.elexis.core.ui.medication.views.PrescriptionDescriptor;
import ch.elexis.data.Prescription.EntryType;

public class MedicationLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {
	
	@Override
	public Image getColumnImage(Object element, int columnIndex){
		if (columnIndex == 0) {
			PrescriptionDescriptor pd = (PrescriptionDescriptor) element;
			EntryType type = pd.getEntryTypeAsync();
			
			if (type == null) {
				return Images.IMG_EMPTY_TRANSPARENT.getImage();
			}
			
			switch (type) {
			case FIXED_MEDICATION:
				return Images.IMG_FIX_MEDI.getImage();
			case RESERVE_MEDICATION:
				return Images.IMG_RESERVE_MEDI.getImage();
			case SELF_DISPENSED:
				return Images.IMG_VIEW_CONSULTATION_DETAIL.getImage();
			case RECIPE:
				return Images.IMG_VIEW_RECIPES.getImage();
			case APPLICATION:
				return Images.IMG_SYRINGE.getImage();
			}
		}
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex){
		PrescriptionDescriptor pd = (PrescriptionDescriptor) element;
		
		switch (columnIndex) {
		case 0: // image icon
			return "";
		case 1: // article
			return pd.getLabel();
		case 2: // dosage
			String dosage = pd.getDosage();
			return (dosage.equals(StringConstants.ZERO) ? Messages.MedicationComposite_stopped
					: dosage);
		case 3: // prescribed last
			return pd.getLastDisposedAsync();
		case 4: // supplied until
			return pd.getSuppliedUntilAsync();
		case 5: // stop date
			return pd.getStopDateAsync();
		case 6: // comment
			return pd.getIntakeOrder();
		case 7: // stop reason
			return pd.getStopReasonAsync();
		default:
			return null;
		}
	}
	
}
