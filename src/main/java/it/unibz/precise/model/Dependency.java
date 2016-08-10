package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"source_id", "target_id"})
})
public class Dependency extends BaseEntity {

	@ManyToOne
	private Task source;
	
	@ManyToOne
	private Task target;
	
	private boolean alternate;
	private boolean chain;
	
	@ManyToMany
	private List<Attribute> scope;
	
	private boolean globalScope;
	
	@ManyToOne
	private Model model;

	public Task getSource() {
		return source;
	}

	public void setSource(Task source) {
		this.source = source;
	}

	public Task getTarget() {
		return target;
	}

	public void setTarget(Task target) {
		this.target = target;
	}

	public boolean isAlternate() {
		return alternate;
	}

	public void setAlternate(boolean alternate) {
		this.alternate = alternate;
	}

	public boolean isChain() {
		return chain;
	}

	public void setChain(boolean chain) {
		this.chain = chain;
	}

	public List<Attribute> getScope() {
		return scope;
	}

	public void setScope(List<Attribute> scope) {
		this.scope = scope;
	}

	public boolean isGlobalScope() {
		return globalScope;
	}

	public void setGlobalScope(boolean globalScope) {
		this.globalScope = globalScope;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.DEPENDENCIES.setOne(this, model);
	}

	void internalSetModel(Model model) {
		this.model = model;
	}
}
