package ch.elexis.core.ui.medication.views.provider;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.ui.medication.views.PrescriptionDescriptor;

public class MedicationFilter extends ViewerFilter {
	private String searchString;
	private Viewer viewer;
	
	public MedicationFilter(Viewer viewer){
		this.viewer = viewer;
	}
	
	public void setSearchText(String s){
		s = s.replace("*", "");
		this.searchString = ".*" + s.toLowerCase() + ".*";
		
		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.getControl().setRedraw(true);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		PrescriptionDescriptor pd = (PrescriptionDescriptor) element;
		
		// check match of article name
		String mediName = "??";
		if (pd.getLabel() != null) {
			mediName = pd.getLabel().toLowerCase();
		}
		
		if (mediName.matches(searchString)) {
			return true;
		}
		return false;
	}
	
	public void clearSearchText(){
		this.searchString = "";
	}
	
}
