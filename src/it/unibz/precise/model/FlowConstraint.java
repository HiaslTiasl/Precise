package it.unibz.precise.model;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.IntSequenceGenerator;

@Entity
@JsonIdentityInfo(property="id", generator=IntSequenceGenerator.class, scope=FlowConstraint.class)
@JsonIdentityReference(alwaysAsId=false)
public class FlowConstraint implements Identifiable {
	
	public static enum Kind {
		// Unary
		EXCLUSIVE_EXISTENCE(1),
		// Binary
		PRECEDENCE(2),
		ALTERNATE_PRECEDENCE(2),
		CHAIN_PRECEDENCE(2);
		
		private int arity;

		private Kind(int arity) {
			this.arity = arity;
		}

		public int getArity() {
			return arity;
		}
	}

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@JsonIgnore
	private long id;
	private Kind kind;
	@ManyToMany
	@JoinTable(
		joinColumns=@JoinColumn(name="flowConstraint", referencedColumnName="id"),
		inverseJoinColumns=@JoinColumn(name="task", referencedColumnName="id")
	)
	private List<Task> tasks;
	private Scope scope;
	
	public FlowConstraint() {
	}
	
	public FlowConstraint(Kind kind, Scope scope, Task... tasks) {
		this.kind = kind;
		this.tasks = Arrays.asList(tasks);
		this.scope = scope;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}
	
	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}
	
	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
	public boolean checkArity() {
		return kind.getArity() == tasks.size();
	}
}
