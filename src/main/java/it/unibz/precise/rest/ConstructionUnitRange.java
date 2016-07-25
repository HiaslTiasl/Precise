package it.unibz.precise.rest;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import it.unibz.precise.model.CASection;
import it.unibz.precise.model.TaskConstructionUnit;
import it.unibz.util.Util;

public class ConstructionUnitRange extends AbstractConstructionUnitRange {
	
	private static class UnitRange {
		private int from = 1;
		private int to;

		private UnitRange(int to) {
			this.to = to;
		}
		
		private UnitRange(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + from;
			result = prime * result + to;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UnitRange other = (UnitRange) obj;
			if (from != other.from)
				return false;
			if (to != other.to)
				return false;
			return true;
		}
		
	}

	private UnitRange units;
	
	
	@JsonBackReference
	private ConstrainedTask task;
	
	@JsonIgnore
	private UnitRange totalRange;

	public ConstructionUnitRange() {
	}

	public ConstructionUnitRange(CASection caSection, UnitRange units) {
		super(caSection);
		this.units = units;
	}

	public UnitRange getUnits() {
		return units;
	}

	public void setUnits(UnitRange units) {
		if (units == null || !units.equals(totalRange))
			this.units = null;
		else
			this.units = units;
	}
	
	public ConstrainedTask getTask() {
		return task;
	}

	public void setTask(ConstrainedTask task) {
		this.task = task;
	}

	public UnitRange getTotalRange() {
		return totalRange;
	}

	public void setTotalRange(int from, int to) {
		this.totalRange = new UnitRange(from, to);
		if (units != null && units.equals(totalRange))
			units = null;
	}

	@Override
	protected IntStream getRange() {
		UnitRange range = units != null ? units : totalRange;
		return IntStream.rangeClosed(range.from, range.to);
	}
	
	public static List<ConstructionUnitRange> from(List<TaskConstructionUnit> constructionUnits) {
		return constructionUnits.stream()
			.collect(Collectors.groupingBy(
				tcu -> tcu.getConstructionUnit().getCaSection(),
				Collectors.mapping(tcu -> tcu.getConstructionUnit().getUnit(), Util.toBitSetCollector())
			)).entrySet().stream().map(e -> {
				CASection caSection = e.getKey();
				BitSet unitBitSet = e.getValue();
				List<ConstructionUnitRange> unitRanges = new ArrayList<>();
				for (int from = unitBitSet.nextSetBit(0), to; from >= 0; from = unitBitSet.nextSetBit(to)) {
					to = unitBitSet.nextClearBit(from);
					unitRanges.add(new ConstructionUnitRange(caSection, new UnitRange(from, to)));
				}
				return unitRanges;
			}).flatMap(List::stream).collect(Collectors.toList());
	}

}
