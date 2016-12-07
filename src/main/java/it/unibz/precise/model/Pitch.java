package it.unibz.precise.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

	private float exactDurationDays() {
		float totalQuantityPerDay = crewCount * quantityPerDay;
		return totalQuantity != 0 && totalQuantityPerDay != 0
			? totalQuantity / totalQuantityPerDay
			: durationDays;
	}
	
	private int computeDurationDays() {
		return (int)Math.ceil(totalQuantity / (crewCount * quantityPerDay));
	}
	
	private int computeCrewCount() {
		return (int)Math.ceil(totalQuantity / (durationDays * quantityPerDay));
	}
	
	private float computeQuantityPerDay() {
		return (float)totalQuantity / (durationDays * crewCount);
	}
	
	private int computeTotalQuantity() {
		return (int)Math.ceil(durationDays * crewCount * quantityPerDay);
	}
	
	@JsonIgnore
	float getManDaysExact() {
		return crewCount * crewSize * exactDurationDays();
	}
	
	public int getManDays() {
		return (int)Math.ceil(getManDaysExact());
	}
	
	/**
	 * Update pitch fields.
	 * If exactly one field is not specified, it is computed based on the others.
	 * If all fields are specified, it is checked whether they are consistent.
	 * Otherwise nothing is done.
	 * @throws InconsistentPitchException if pitch fields are inconsistent.
	 */
	public boolean update() {
		final int MISSING_DURATION_DAYS = 0x01;
		final int MISSING_CREW_COUNT = 0x02;
		final int MISSING_QUANTITY_PER_DAY = 0x04;
		final int MISSING_TOTAL_QUANTITY = 0x08;
		int durationDaysMask   = durationDays   != 0 ? 0 : MISSING_DURATION_DAYS;
		int crewCountMask      = crewCount      != 0 ? 0 : MISSING_CREW_COUNT;
		int quantityPerDayMask = quantityPerDay != 0 ? 0 : MISSING_QUANTITY_PER_DAY;
		int totalQuantityMask  = totalQuantity  != 0 ? 0 : MISSING_TOTAL_QUANTITY;
		int totalMask = durationDaysMask
			| crewCountMask
			| quantityPerDayMask
			| totalQuantityMask;
		
		if (totalMask == 0)
			return checkPitchConsistency();
		else if (Integer.bitCount(totalMask) == 1) {
			switch (Integer.lowestOneBit(totalMask)) {
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
		return true;
	}
	
	public boolean checkPitchConsistency() {
		return durationDays == 0 || totalQuantity == 0 || crewCount == 0 || quantityPerDay == 0
			|| durationDays == computeDurationDays();
	}
	
}
