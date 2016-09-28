package it.unibz.precise.rest.mdl.conversion;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;

class AttributeTranslator extends AbstractMDLTranslator<Attribute, MDLAttributeAST> {
	
	AttributeTranslator(MDLContext context) {
		super(context);
	}

	@Override
	public void updateMDL(Attribute attribute, MDLAttributeAST mdlAttribute) {
		List<String> range = attribute.getRange();
		mdlAttribute.setName(attribute.getName());
		mdlAttribute.setShortName(attribute.getShortName());
		mdlAttribute.setDescription(attribute.getDescription());
		mdlAttribute.setRange(attribute.isValuesMatchPositions() ? range.size() : range);
		mdlAttribute.setOrdered(attribute.isOrdered());
	}
	
	@Override
	public void updateEntity(MDLAttributeAST mdlAttribute, Attribute attribute) {
		attribute.setName(mdlAttribute.getName());
		attribute.setShortName(mdlAttribute.getShortName());
		attribute.setDescription(mdlAttribute.getDescription());
		attribute.setRange(resolveRange(attribute, mdlAttribute.getRange()));
		attribute.setOrdered(mdlAttribute.isOrdered());
	}
	
	@Override
	public Attribute createEntity() {
		return new Attribute();
	}
	
	@Override
	public MDLAttributeAST createMDL() {
		return new MDLAttributeAST();
	}
	
	private static List<String> resolveRange(Attribute attribute, Object range) {
		Stream<String> values;
		if (range instanceof Integer) {
			values = IntStream.rangeClosed(1, (int)range).mapToObj(String::valueOf);
			attribute.setValuesMatchPositions(true);
		}
		else if (range instanceof List)
			values = ((List<?>)range).stream().map(String::valueOf);
		else
			throw new IllegalArgumentException("Range of attribute '" + attribute.getName() + "'; expected list or integer");
		return values.collect(Collectors.toList());
	}

}
