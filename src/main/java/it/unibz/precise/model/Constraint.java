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
//@DiscriminatorColumn(name="arity", discriminatorType=DiscriminatorType.INTEGER)
//@JsonTypeInfo(property="arity", use=Id.NAME, include=As.PROPERTY)
//@JsonSubTypes({
//	@Type(name="1", value=UnaryConstraint.class),
//	@Type(name="2", value=BinaryConstraint.class)
//})
public abstract class Constraint<K extends ConstraintKind> extends BaseEntity {
	
//	@JsonTypeInfo(property="arity", use=Id.NAME, include=As.EXTERNAL_PROPERTY)
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
