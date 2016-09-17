package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.unibz.precise.model.Dependency;
import it.unibz.precise.model.Position;
import it.unibz.util.Util;

public class MDLDependencyAST {

	@JsonIgnore
	private Dependency dependency;
	private boolean alternate;
	private boolean chain;
	private MDLTaskAST source;
	private MDLTaskAST target;
	private Position sourceVertex;
	private Position targetVertex;
	private boolean globalScope;
	private List<MDLAttributeAST> scope;
	private List<Position> vertices;

	public MDLDependencyAST() {
	}
	
	public MDLDependencyAST(MDLFileContext context, Dependency dependency) {
		this.dependency = dependency;
		alternate = dependency.isAlternate();
		chain = dependency.isChain();
		source = context.translate(dependency.getSource());
		target = context.translate(dependency.getTarget());
		sourceVertex = dependency.getSourceVertex();
		targetVertex = dependency.getTargetVertex();
		vertices = dependency.getVertices();
		globalScope = dependency.isGlobalScope();
		scope = Util.mapToList(dependency.getScope(), context::translate);
	}
	
	public Dependency toDependency() {
		if (dependency == null) {
			dependency = new Dependency();
			dependency.setAlternate(alternate);
			dependency.setChain(chain);
			dependency.setSource(source.toTask());
			dependency.setTarget(target.toTask());
			dependency.setSourceVertex(sourceVertex);
			dependency.setTargetVertex(targetVertex);
			dependency.setVertices(vertices);
			dependency.setGlobalScope(globalScope);
			dependency.setScope(Util.mapToList(scope, MDLAttributeAST::toAttribute));
		}
		return dependency;
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

	public MDLTaskAST getSource() {
		return source;
	}

	public void setSource(MDLTaskAST source) {
		this.source = source;
	}

	public MDLTaskAST getTarget() {
		return target;
	}

	public void setTarget(MDLTaskAST target) {
		this.target = target;
	}

	public Position getSourceVertex() {
		return sourceVertex;
	}

	public void setSourceVertex(Position sourceVertex) {
		this.sourceVertex = sourceVertex;
	}

	public Position getTargetVertex() {
		return targetVertex;
	}

	public void setTargetVertex(Position targetVertex) {
		this.targetVertex = targetVertex;
	}

	public List<Position> getVertices() {
		return vertices;
	}

	public void setVertices(List<Position> vertices) {
		this.vertices = vertices;
	}
	
	public boolean isGlobalScope() {
		return globalScope;
	}

	public void setGlobalScope(boolean globalScope) {
		this.globalScope = globalScope;
	}

	public List<MDLAttributeAST> getScope() {
		return scope;
	}

	public void setScope(List<MDLAttributeAST> scope) {
		this.scope = scope;
	}

	
}
