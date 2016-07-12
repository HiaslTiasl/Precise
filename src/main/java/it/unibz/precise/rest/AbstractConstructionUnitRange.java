package it.unibz.precise.rest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import it.unibz.precise.model.CASection;
import it.unibz.precise.model.ConstructionUnit;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskConstructionUnit;

public abstract class AbstractConstructionUnitRange {

	@JsonUnwrapped
	private CASection caSection;
	
	public AbstractConstructionUnitRange() {
	}

	public AbstractConstructionUnitRange(CASection caSection) {
		this.caSection = caSection;
	}

	public CASection getCASection() {
		return caSection;
	}

	public void setCASection(CASection caSection) {
		this.caSection = caSection;
	}

	protected abstract IntStream getRange();
	
	protected Stream<ConstructionUnit> getResolveStream() {
		return getRange().mapToObj(u -> new ConstructionUnit(getCASection(), u));
	}
	
	private static Stream<ConstructionUnit> resolveAllImpl(List<? extends AbstractConstructionUnitRange> ranges) {
		return ranges.stream().flatMap(cur -> cur.getResolveStream());
	}
	
	public static List<ConstructionUnit> resolveAll(List<? extends AbstractConstructionUnitRange> ranges) {
		return resolveAllImpl(ranges).collect(Collectors.toList());
	}

	public static List<TaskConstructionUnit> resolveAll(
		List<? extends AbstractConstructionUnitRange> ranges,
		Task task)
	{
		return TaskConstructionUnit.setPositionToIndex(
			resolveAllImpl(ranges)
			.map(cu -> new TaskConstructionUnit(task, cu, 1))
			.collect(Collectors.toList())
		);
		
	}
	
}
