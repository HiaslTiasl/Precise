package it.unibz.precise.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.unibz.precise.model.BinaryConstraint;
import it.unibz.precise.model.BinaryKind;
import it.unibz.precise.model.CASection;
import it.unibz.precise.model.Constraint;
import it.unibz.precise.model.ConstraintKind;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Scope;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;
import it.unibz.precise.model.UnaryConstraint;
import it.unibz.precise.model.UnaryKind;

public class ModelDTO {

	@JsonIgnoreProperties({"taskTypes", "constructionUnits", "tasks", "constraints"})
	private Model model;
	private List<TaskType> taskTypes;
	private List<TotalConstructionUnitRange> constructionUnits;
	private List<ConstrainedTask> tasks;
	private List<PrecedenceSpec> precedences;
	private List<PrecedenceSpec> alternatePrecedences;
	private List<PrecedenceSpec> chainPrecedences;
	
	public ModelDTO() {
	}
	
	public ModelDTO(Model model) {
		this.model = model;
		taskTypes = model.getTaskTypes();
		constructionUnits = TotalConstructionUnitRange.from(model.getConstructionUnits());
		
		Map<Task, ConstrainedTask> taskToSpec = Optional.ofNullable(model.getTasks())
			.map(ts -> ts.stream().collect(Collectors.toMap(
				Function.identity(),
				ConstrainedTask::fromTask
			))).orElseGet(Collections::emptyMap);
		
		Map<? extends ConstraintKind, List<Constraint<? extends ConstraintKind>>> constraints =
			Optional.ofNullable(model.getConstraints())
				.map(cs -> cs.stream().collect(Collectors.groupingBy(Constraint::getKind)))
				.orElseGet(Collections::emptyMap);
		
		Optional.ofNullable(constraints.get(UnaryKind.EXCLUSIVE_EXISTENCE))
			.ifPresent(ucs -> ucs.stream()
				.filter(c -> c instanceof UnaryConstraint)
				.forEach(c -> {
					Task t = ((UnaryConstraint)c).getTask();
					ConstrainedTask ct = taskToSpec.get(t);
					Scope scope = c.getScope();
					if (ct.getExclusivenessScope().compareTo(scope) < 0)
						ct.setExclusivenessScope(scope);
				})
			);
		
		tasks = new ArrayList<>(taskToSpec.values());
		precedences = toPrecedences(constraints.get(BinaryKind.PRECEDENCE), taskToSpec);
		alternatePrecedences = toPrecedences(constraints.get(BinaryKind.ALTERNATE_PRECEDENCE), taskToSpec);
		chainPrecedences = toPrecedences(constraints.get(BinaryKind.CHAIN_PRECEDENCE), taskToSpec);
	}
	
	private static List<PrecedenceSpec> toPrecedences(
			List<Constraint<? extends ConstraintKind>> constraints,
			Map<Task, ConstrainedTask> mappings)
		{
			return Optional.ofNullable(constraints)
				.map(cs -> cs.stream()
					.filter(c -> c instanceof BinaryConstraint)
					.map(c -> new PrecedenceSpec(
						mappings.get(((BinaryConstraint)c).getSource()),
						mappings.get(((BinaryConstraint)c).getTarget()),
						c.getScope()
					))
					.collect(Collectors.toList())
				).orElseGet(Collections::emptyList);
		}
		
		private static Stream<? extends BinaryConstraint> mapToKind(List<PrecedenceSpec> precs, BinaryKind kind) {
			return precs == null ? null : precs.stream()
				.map(p -> new BinaryConstraint(
					kind,
					p.getScope(),
					p.getSource().getTask(),
					p.getTarget().getTask()
				));
		}
	
	public Model toModel() {
		Model model = getModel();
		
		Map<CASection, Integer> totalRanges = constructionUnits.stream()
			.collect(Collectors.toMap(
				TotalConstructionUnitRange::getCASection,
				TotalConstructionUnitRange::getTotalNumberOfUnits
			));
		
		tasks.stream()
			.flatMap(t -> t.getCURanges().stream())
			.forEach(cur -> cur.setTotalRange(1, totalRanges.get(cur.getCASection())));
		
		model.setTaskTypes(taskTypes != null ? taskTypes
			: tasks.stream().map(ct -> new TaskType(ct.getType())).collect(Collectors.toList())
		);
		model.setConstructionUnits(TotalConstructionUnitRange.resolveAll(constructionUnits));
		model.setTasks(
			tasks.stream()
				.map(ct -> ct.toTask())
				.collect(Collectors.toList())
		);
		
		List<Constraint<? extends ConstraintKind>> constraints = Stream.of(
			tasks.stream()
				.filter(ct -> ct.getExclusivenessScope() != Scope.UNIT)
				.map(ct -> new UnaryConstraint(
					UnaryKind.EXCLUSIVE_EXISTENCE, ct.getExclusivenessScope(), ct.getTask()
				)),
			mapToKind(precedences, BinaryKind.PRECEDENCE),
			mapToKind(alternatePrecedences, BinaryKind.ALTERNATE_PRECEDENCE),
			mapToKind(chainPrecedences, BinaryKind.CHAIN_PRECEDENCE)
		).flatMap(Function.identity()).collect(Collectors.toList());
		
		model.setConstraints(constraints);
		return model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public List<TaskType> getTaskTypes() {
		return taskTypes;
	}
	
	public void setTaskTypes(List<TaskType> taskTypes) {
		this.taskTypes = taskTypes;
	}
	
	public List<TotalConstructionUnitRange> getConstructionUnits() {
		return constructionUnits;
	}
	
	public void setConstructionUnits(List<TotalConstructionUnitRange> constructionUnits) {
		this.constructionUnits = constructionUnits;
	}
	
	public List<ConstrainedTask> getTasks() {
		return tasks;
	}
	
	public void setTasks(List<ConstrainedTask> tasks) {
		this.tasks = tasks;
	}
	
	public List<PrecedenceSpec> getPrecedences() {
		return precedences;
	}
	
	public void setPrecedences(List<PrecedenceSpec> precedences) {
		this.precedences = precedences;
	}
	
	public List<PrecedenceSpec> getAlternatePrecedences() {
		return alternatePrecedences;
	}
	
	public void setAlternatePrecedences(List<PrecedenceSpec> alternatePrecedences) {
		this.alternatePrecedences = alternatePrecedences;
	}
	
	public List<PrecedenceSpec> getChainPrecedences() {
		return chainPrecedences;
	}
	
	public void setChainPrecedences(List<PrecedenceSpec> chainPrecedences) {
		this.chainPrecedences = chainPrecedences;
	}
	
}
