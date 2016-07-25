package it.unibz.precise.rest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import it.unibz.precise.model.CASection;
import it.unibz.precise.model.ConstructionUnit;

public class TotalConstructionUnitRange extends AbstractConstructionUnitRange {

	private int totalNumberOfUnits;

	public TotalConstructionUnitRange() {
	}

	public TotalConstructionUnitRange(CASection caSection, int totalNumberOfUnits) {
		super(caSection);
		this.totalNumberOfUnits = totalNumberOfUnits;
	}

	public int getTotalNumberOfUnits() {
		return totalNumberOfUnits;
	}

	public void setTotalNumberOfUnits(int totalNumberOfUnits) {
		this.totalNumberOfUnits = totalNumberOfUnits;
	}
	
	@Override
	public IntStream getRange() {
		return IntStream.rangeClosed(1, totalNumberOfUnits);
	}
	
	public static List<TotalConstructionUnitRange> from(List<ConstructionUnit> constructionUnits) {
		return constructionUnits.stream()
			.collect(Collectors.groupingBy(
				ConstructionUnit::getCaSection,
				Collectors.counting()
			)).entrySet().stream().map(e -> {
				return new TotalConstructionUnitRange(e.getKey(), e.getValue().intValue());
			}).collect(Collectors.toList());
	}
	
}
