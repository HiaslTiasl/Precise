package it.unibz.precise.rest.mdl.conversion;

import java.util.stream.Collectors;

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
	protected void updateEntityImpl(MDLScopeAST mdlScope, Scope scope) {
		if (mdlScope != null) {
			scope.setType(mdlScope.getType());
			scope.setAttributes(
				mdlScope.getAttributes() == null ? null
				: mdlScope.getAttributes().stream()
					.map(context().attributes()::toEntity)
					.filter(a -> a != null)
					.collect(Collectors.toList())
			);
		}
	}

	@Override
	protected void updateMDLImpl(Scope scope, MDLScopeAST mdlScope) {
		if (scope != null) {
			mdlScope.setType(scope.getType());
			mdlScope.setAttributes(Util.mapToList(scope.getAttributes(), context().attributes()::toMDL));		
		}
	}

}
