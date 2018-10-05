/**
 * Copyright (c) 2018 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import ch.rgw.tools.Result;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IBillable Optifier</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see ch.elexis.core.model.ModelPackage#getIBillableOptifier()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IBillableOptifier {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model type="ch.elexis.core.types.Result&lt;ch.elexis.core.model.IBilled&gt;"
	 * @generated
	 */
	Result<IBilled> add(IBillable billable, IEncounter encounter, double amount);

} // IBillableOptifier
