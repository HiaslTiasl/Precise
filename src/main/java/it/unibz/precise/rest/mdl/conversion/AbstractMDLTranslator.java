package it.unibz.precise.rest.mdl.conversion;

public abstract class AbstractMDLTranslator<E, MDL> implements MDLTranslator<E, MDL> {
	
	private final MDLContext context;
	
	AbstractMDLTranslator(MDLContext context) {
		this.context = context;
	}
	
	public MDLContext context() {
		return context;
	}
	
	protected abstract void updateMDLImpl(E entity, MDL mdl);
	protected abstract void updateEntityImpl(MDL mdl, E entity);
	
	@Override
	public void updateMDL(E entity, MDL mdl) {
		if (entity != null && mdl != null)
			updateMDLImpl(entity, mdl);
	}
	
	@Override
	public void updateEntity(MDL mdl, E entity) {
		if (mdl != null && entity != null )
			updateEntityImpl(mdl, entity);
		
	}
	
	public MDL toMDL(E entity) {
		MDL mdl = entity == null ? null : createMDL();
		updateMDL(entity, mdl);
		return mdl;
	}
	
	public E toEntity(MDL mdl) {
		E entity = mdl == null ? null : createEntity();
		updateEntity(mdl, entity);
		return entity;
	}
	
}
