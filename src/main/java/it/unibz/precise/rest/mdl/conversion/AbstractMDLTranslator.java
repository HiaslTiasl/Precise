package it.unibz.precise.rest.mdl.conversion;

public abstract class AbstractMDLTranslator<E, MDL> implements MDLTranslator<E, MDL> {
	
	private final MDLContext context;
	
	public AbstractMDLTranslator(MDLContext context) {
		this.context = context;
	}
	
	public MDLContext context() {
		return context;
	}
	
	public MDL toMDL(E entity) {
		MDL mdl = createMDL(entity);
		updateMDL(entity, mdl);
		return mdl;
	}
	
	public E toEntity(MDL mdl) {
		E entity = createEntity(mdl);
		updateEntity(mdl, entity);
		return entity;
	}
	
}
