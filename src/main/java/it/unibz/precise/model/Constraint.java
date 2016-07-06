package it.unibz.precise.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name="FLOW_CONSTRAINT")	// 'constraint' is a reserved word in SQL
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id", scope=Constraint.class)
@JsonIdentityReference(alwaysAsId=false)
public abstract class Constraint<K extends ConstraintKind> extends BaseEntity {
	
	private K kind;
	private Scope scope;
	
	public Constraint() {
	}
	
	public Constraint(K kind, Scope scope) {
		this.kind = kind;
		this.scope = scope;
	}
	
	public K getKind() {
		return kind;
	}

	public void setKind(K kind) {
		this.kind = kind;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}
	
}
