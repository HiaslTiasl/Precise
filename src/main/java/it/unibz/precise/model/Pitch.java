package it.unibz.precise.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Encapsulates pitch parameters regarding crews, productivities, quantities and durations.
 * 
 * Any parameter equal to zero is interpreted as missing.
 * 
 * @author MatthiasP
 *
 */
@Embeddable
public class Pitch {
	
	@Min(0)
	@JsonInclude(value=Include.NON_DEFAULT)
	private int crewSize;
	
	@Min(0)
	@JsonInclude(value=Include.NON_DEFAULT)
	private int crewCount;
	
	@Min(0)
	@JsonInclude(value=Include.NON_DEFAULT)
	private int durationDays;
	
	@JsonInclude(value=Include.NON_DEFAULT)
	private float quantityPerDay;

	@JsonInclude(value=Include.NON_DEFAULT)
	private int totalQuantity;

	public int getCrewSize() {
		return crewSize;
	}

	public void setCrewSize(int crewSize) {
		this.crewSize = crewSize;
	}

	public int getCrewCount() {
		return crewCount;
	}

	public void setCrewCount(int crewCount) {
		this.crewCount = crewCount;
	}

	public int getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(int durationDays) {
		this.durationDays = durationDays;
	}

	public float getQuantityPerDay() {
		return quantityPerDay;
	}

	public void setQuantityPerDay(Float quantityPerDay) {
		this.quantityPerDay = quantityPerDay;
	}
	
	public int getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	/** Returns the durations in days as a floating point value. */
	private float exactDurationDays() {
		float totalQuantityPerDay = crewCount * quantityPerDay;
		return totalQuantity != 0 && totalQuantityPerDay != 0
			? totalQuantity / totalQuantityPerDay
			: durationDays;
	}
	
	/** Compute duration in days from other fields. */
	private int computeDurationDays() {
		return (int)Math.ceil(totalQuantity / (crewCount * quantityPerDay));
	}
	
	/** Compute crew count from other fields. */
	private int computeCrewCount() {
		return (int)Math.ceil(totalQuantity / (durationDays * quantityPerDay));
	}
	
	/** Compute quantity per day (i.e. crew productivity) from other fields. */
	private float computeQuantityPerDay() {
		return (float)totalQuantity / (crewCount * durationDays);
	}
	
	/** Compute total quantity from other fields. */
	private int computeTotalQuantity() {
		return (int)Math.ceil(crewCount * quantityPerDay * durationDays);
	}
	
	/** Returns man days as a floating point number. */
	float exactManDays() {
		return crewCount * crewSize * exactDurationDays();
	}
	
	/**
	 * Update pitch fields.
	 * If exactly one of crew count, productivity, total quantity, or duration is missing,
	 * it can be computed by the others according to:
	 * <pre> {@code
	 * 	totalQuantity = crewCount * quantityPerDay * durationDays
	 * }</pre>
	 * If all fields are specified, it is checked whether they are consistent.
	 * Otherwise nothing is done.
	 */
	public boolean update() {
		// Assign each field to a bit index
		// N.B. crew size is independent from other fields
		final int MISSING_DURATION_DAYS    = 0x01;
		final int MISSING_CREW_COUNT       = 0x02;
		final int MISSING_QUANTITY_PER_DAY = 0x04;
		final int MISSING_TOTAL_QUANTITY   = 0x08;
		
		int durationDaysMask   = durationDays   != 0 ? 0 : MISSING_DURATION_DAYS;
		int crewCountMask      = crewCount      != 0 ? 0 : MISSING_CREW_COUNT;
		int quantityPerDayMask = quantityPerDay != 0 ? 0 : MISSING_QUANTITY_PER_DAY;
		int totalQuantityMask  = totalQuantity  != 0 ? 0 : MISSING_TOTAL_QUANTITY;
		
		// Compute bitmask: 0 = available, 1 = missing (Because of Integer.lowestOneBit)
		int totalMask = durationDaysMask | crewCountMask | quantityPerDayMask | totalQuantityMask;
		
		boolean consistent = true;
		
		if (totalMask == 0)								// All available -> check consistency
			consistent = checkPitchConsistency();
		else if (Integer.bitCount(totalMask) == 1) {	
			switch (Integer.lowestOneBit(totalMask)) {	// Exactly one missing -> compute it
			case MISSING_DURATION_DAYS:
				durationDays = computeDurationDays();
				break;
			case MISSING_CREW_COUNT:
				crewCount = computeCrewCount();
				break;
			case MISSING_QUANTITY_PER_DAY:
				quantityPerDay = computeQuantityPerDay();
				break;
			case MISSING_TOTAL_QUANTITY:
				totalQuantity = computeTotalQuantity();
				break;
			}
		}
		return consistent;
	}
	
	/** Checks whether the given parameters are consistent. */
	public boolean checkPitchConsistency() {
		return durationDays == 0 || totalQuantity == 0 || crewCount == 0 || quantityPerDay == 0
			|| durationDays == computeDurationDays();
	}
	
}
