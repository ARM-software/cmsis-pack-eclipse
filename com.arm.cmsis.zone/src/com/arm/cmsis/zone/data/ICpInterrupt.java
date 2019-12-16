package com.arm.cmsis.zone.data;

/**
 * Interface for an interrupt tag.
 */
public interface ICpInterrupt extends ICpResourceItem {

	/**
	 * Get the interrupt number as string
	 * @return interrupt number string
	 */
	String getIrqNumberString();

	/**
	 * Get the interrupt number decoded as long
	 * @return interrupt number
	 */
	Long getIrqNumber();

}