package it.unibz.precise.rest.mdl.conversion;

public interface MDLTranslator<E, MDL> {
	
	MDLContext context();
	
	E createEntity();
	MDL createMDL();
	
	void updateEntity(MDL mdl, E entity);
	void updateMDL(E entity, MDL mdl);

	MDL toMDL(E entity);
	E toEntity(MDL mdl);
	
}
