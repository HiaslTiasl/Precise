package it.unibz.precise.rest.mdl.conversion;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import it.unibz.precise.model.Attribute;
import it.unibz.precise.rest.mdl.ast.MDLAttributeAST;

/**
 * {@link MDLTranslator} for attributes.
 * 
 * @author MatthiasP
 *
 */
class AttributeTranslator extends AbstractMDLTranslator<Attribute, MDLAttributeAST> {
	
	AttributeTranslator(MDLContext context) {
		super(context);
	}

	@Override
	protected void updateMDLImpl(Attribute attribute, MDLAttributeAST mdlAttribute) {
		List<String> range = attribute.getRange();
		mdlAttribute.setName(attribute.getName());
		mdlAttribute.setShortName(attribute.getShortName());
		mdlAttribute.setDescription(attribute.getDescription());
		mdlAttribute.setRange(attribute.isValuesMatchPositions() ? range.size() : range);
		mdlAttribute.setOrdered(attribute.isOrdered());
	}
	
	@Override
	protected void updateEntityImpl(MDLAttributeAST mdlAttribute, Attribute attribute) {
		attribute.setName(mdlAttribute.getName());
		attribute.setShortName(mdlAttribute.getShortName());
		attribute.setDescription(mdlAttribute.getDescription());
		setRange(attribute, mdlAttribute.getRange());
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
	
	/**
	 * Applies the given {@code range} as specified in MDL to the given attribute.
	 * The {@code range} may be given either as an integer {@code n} denoting the range
	 * from 1 to {@code n}, or as a {@link List}.
	 * @throws IllegalArgumentException if {@code range} is neither an integer nor a list.
	 */
	private void setRange(Attribute attribute, Object range) {
		Stream<String> values;
		if (range instanceof Integer) {
			values = IntStream.rangeClosed(1, (int)range).mapToObj(String::valueOf);
			attribute.setValuesMatchPositions(true);
		}
		else if (range instanceof List)
			values = ((List<?>)range).stream().map(String::valueOf);
		else
			throw new IllegalArgumentException("Range of attribute '" + attribute.getName() + "'; expected list or integer");
		attribute.setRange(values.collect(Collectors.toList()));
	}

}
