package it.unibz.precise.rest.mdl.ast;

import java.util.List;

import it.unibz.precise.model.Dependency.LabelPosition;
import it.unibz.precise.model.Position;

public class MDLDependencyAST {

	private boolean alternate;
	private boolean chain;
	private MDLTaskAST source;
	private MDLTaskAST target;
	private Position sourceVertex;
	private Position targetVertex;
	private MDLScopeAST scope;
	private List<Position> vertices;
	private LabelPosition labelPosition;

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
	
	public MDLScopeAST getScope() {
		return scope;
	}

	public void setScope(MDLScopeAST scope) {
		this.scope = scope;
	}

	public LabelPosition getLabelPosition() {
		return labelPosition;
	}

	public void setLabelPosition(LabelPosition labelPosition) {
		this.labelPosition = labelPosition;
	}
	
}
