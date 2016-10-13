package it.unibz.precise.rest.mdl.conversion;

import it.unibz.precise.model.Craft;
import it.unibz.precise.rest.mdl.ast.MDLCraftAST;

public class CraftTranslator extends AbstractMDLTranslator<Craft, MDLCraftAST> {

	public CraftTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateEntity(MDLCraftAST mdlCraft, Craft craft) {
		craft.setName(mdlCraft.getName());
		craft.setShortName(mdlCraft.getShortName());
	}

	@Override
	public void updateMDL(Craft craft, MDLCraftAST mdlCraft) {
		mdlCraft.setName(craft.getName());
		mdlCraft.setShortName(craft.getShortName());
	}
	
	@Override
	public Craft createEntity(MDLCraftAST mdl) {
		return new Craft();
	}
	
	@Override
	public MDLCraftAST createMDL(Craft entity) {
		return new MDLCraftAST();
	}

}
