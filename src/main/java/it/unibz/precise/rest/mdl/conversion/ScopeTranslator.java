package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Scope;
import it.unibz.precise.rest.mdl.ast.MDLScopeAST;
import it.unibz.util.Util;

public class ScopeTranslator extends AbstractMDLTranslator<Scope, MDLScopeAST> {
	
	public ScopeTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public Scope createEntity(MDLScopeAST mdlScope) {
		return new Scope();
	}

	@Override
	public MDLScopeAST createMDL(Scope scope) {
		return new MDLScopeAST();
	}

	@Override
	public void updateEntity(MDLScopeAST mdlScope, Scope scope) {
		if (mdlScope != null) {
			scope.setType(mdlScope.getType());
			scope.setAttributes(Util.mapToList(mdlScope.getAttributes(), context().attributes()::toEntity));
		}
	}

	@Override
	public void updateMDL(Scope scope, MDLScopeAST mdlScope) {
		if (scope != null) {
			mdlScope.setType(scope.getType());
			mdlScope.setAttributes(Util.mapToList(scope.getAttributes(), context().attributes()::toMDL));		
		}
	}

}
