/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.error;

import java.util.HashMap;
import java.util.Map;

import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.zone.data.ICpZoneItem;

/**
 * CMSIS-Zone errors/warnings/massages
 */
public class CmsisZoneError extends CmsisError {

	// CMSIS-Zone error codes


	// properties and physical arrange errors
	public static final String Z1_MASK = "Z1*"; //$NON-NLS-1$
	public static final String Z10_MASK = "Z10*"; //$NON-NLS-1$
	public static final String Z101 = "Z101"; //$NON-NLS-1$
	public static final String Z102 = "Z102"; //$NON-NLS-1$
	public static final String Z103 = "Z103"; //$NON-NLS-1$
	public static final String Z104 = "Z104"; //$NON-NLS-1$
	public static final String Z105 = "Z105"; //$NON-NLS-1$
	public static final String Z106 = "Z106"; //$NON-NLS-1$
	public static final String Z107 = "Z107"; //$NON-NLS-1$
	public static final String Z108 = "Z108"; //$NON-NLS-1$

	//	MPU allocation
	public static final String Z11_MASK = "Z11*"; //$NON-NLS-1$
	public static final String Z110 = "Z110"; //$NON-NLS-1$
	public static final String Z111 = "Z111"; //$NON-NLS-1$
	public static final String Z112 = "Z112"; //$NON-NLS-1$

	// startup region
	public static final String Z12_MASK = "Z12*"; //$NON-NLS-1$
	public static final String Z120 = "Z120"; //$NON-NLS-1$
	public static final String Z121 = "Z121"; //$NON-NLS-1$

	public static final String Z172 = "Z172"; //$NON-NLS-1$

	public static final String Z2_MASK = "Z2*"; //$NON-NLS-1$
	public static final String Z201 = "Z201"; //$NON-NLS-1$
	public static final String Z202 = "Z202"; //$NON-NLS-1$
	public static final String Z203 = "Z203"; //$NON-NLS-1$

	public static final String Z401 = "Z401"; //$NON-NLS-1$
	public static final String Z402 = "Z402"; //$NON-NLS-1$
	public static final String Z403 = "Z403"; //$NON-NLS-1$
	public static final String Z404 = "Z404"; //$NON-NLS-1$
	public static final String Z405 = "Z405"; //$NON-NLS-1$
	public static final String Z406 = "Z406"; //$NON-NLS-1$
	public static final String Z407 = "Z407"; //$NON-NLS-1$
	public static final String Z408 = "Z408"; //$NON-NLS-1$


	public static final String Z501 = "Z600"; //$NON-NLS-1$

	public static final String Z604 = "Z604"; //$NON-NLS-1$
	public static final String Z601 = "Z601"; //$NON-NLS-1$
	public static final String Z602 = "Z602"; //$NON-NLS-1$
	public static final String Z603 = "Z603"; //$NON-NLS-1$



	private static Map<String, String> fZoneErrors = null;


	private static void fillErrorMap() {
		fZoneErrors = new HashMap<>();

		fZoneErrors.put(Z101, Messages.getString("CmsisZoneError.Z101")); //$NON-NLS-1$
		fZoneErrors.put(Z102, Messages.getString("CmsisZoneError.Z102")); //$NON-NLS-1$
		fZoneErrors.put(Z103, Messages.getString("CmsisZoneError.Z103")); //$NON-NLS-1$
		fZoneErrors.put(Z104, Messages.getString("CmsisZoneError.Z104")); //$NON-NLS-1$
		fZoneErrors.put(Z105, Messages.getString("CmsisZoneError.Z105")); //$NON-NLS-1$
		fZoneErrors.put(Z106, Messages.getString("CmsisZoneError.Z106")); //$NON-NLS-1$

		fZoneErrors.put(Z107, Messages.getString("CmsisZoneError.Z107")); //$NON-NLS-1$
		fZoneErrors.put(Z108, Messages.getString("CmsisZoneError.Z108")); //$NON-NLS-1$

		fZoneErrors.put(Z110, Messages.getString("CmsisZoneError.Z110")); //$NON-NLS-1$
		fZoneErrors.put(Z111, Messages.getString("CmsisZoneError.Z111")); //$NON-NLS-1$
		fZoneErrors.put(Z172, Messages.getString("CmsisZoneError.Z112")); //$NON-NLS-1$


		fZoneErrors.put(Z120, Messages.getString("CmsisZoneError.Z120")); //$NON-NLS-1$
		fZoneErrors.put(Z121, Messages.getString("CmsisZoneError.Z121")); //$NON-NLS-1$

		fZoneErrors.put(Z201, Messages.getString("CmsisZoneError.Z201")); //$NON-NLS-1$
		fZoneErrors.put(Z202, Messages.getString("CmsisZoneError.Z202")); //$NON-NLS-1$
		fZoneErrors.put(Z203, Messages.getString("CmsisZoneError.Z203")); //$NON-NLS-1$

		fZoneErrors.put(Z401, Messages.getString("CmsisZoneError.Z401")); //$NON-NLS-1$
		fZoneErrors.put(Z402, Messages.getString("CmsisZoneError.Z402"));		 //$NON-NLS-1$
		fZoneErrors.put(Z403, Messages.getString("CmsisZoneError.Z403"));		 //$NON-NLS-1$
		fZoneErrors.put(Z404, Messages.getString("CmsisZoneError.Z404")); //$NON-NLS-1$
		fZoneErrors.put(Z405, Messages.getString("CmsisZoneError.Z405")); //$NON-NLS-1$
		fZoneErrors.put(Z406, Messages.getString("CmsisZoneError.Z406")); //$NON-NLS-1$
		fZoneErrors.put(Z407, Messages.getString("CmsisZoneError.Z407")); //$NON-NLS-1$
		fZoneErrors.put(Z408, Messages.getString("CmsisZoneError.Z408")); //$NON-NLS-1$

		fZoneErrors.put(Z501, Messages.getString("CmsisZoneError.Z501")); //$NON-NLS-1$

		fZoneErrors.put(Z601, Messages.getString("CmsisZoneError.Z601")); //$NON-NLS-1$
		fZoneErrors.put(Z602, Messages.getString("CmsisZoneError.Z602")); //$NON-NLS-1$
		fZoneErrors.put(Z603, Messages.getString("CmsisZoneError.Z603")); //$NON-NLS-1$
		fZoneErrors.put(Z604, Messages.getString("CmsisZoneError.Z604")); //$NON-NLS-1$

	}

	@Override
	protected String getMessageFromId() {
		if(fZoneErrors == null) {
			 fillErrorMap();
		}
		return fZoneErrors.get(getId());
	}

	/**
	 * Serial ID needed by Serializable
	 */
	private static final long serialVersionUID = 1L;

	public CmsisZoneError() {
		super();
	}

	public CmsisZoneError(ESeverity severity, String id, String message, String file, int line, int col) {
		super(severity, id, message, file, line, col);
	}


	public CmsisZoneError(ESeverity severity, String id) {
		super(severity, id);
	}

	public CmsisZoneError(ICpZoneItem item, ESeverity severity, String id) {
		super(item, severity, id, null);
	}


	public CmsisZoneError(Throwable cause, ESeverity severity, String id) {
		super(cause, severity, id);
	}

	public CmsisZoneError(String file, ESeverity severity, String id) {
		super(file, severity, id);
	}


}
