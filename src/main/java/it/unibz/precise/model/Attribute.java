package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * A named range of possibly ordered values.
 * Used to identify locations.
 * 
 * @author MatthiasP
 *
 */
@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(name=Attribute.UC_NAME, columnNames={"model_id", "name"})
})
public class Attribute extends BaseEntity implements ShortNameProvider {
	
	public static final String UC_NAME = "UC_ATTRIBUTE_NAME";

	@Column(nullable=false)
	@NotNull(message="{attribute.name.required}")
	private String name;
	
	private String shortName;
	
	private String description;
	
	@ElementCollection
	@OrderColumn(name="position")
	@Column(name="\"range\"")
	private List<String> range;
	
	/**
	 * Indicates whether the order on the values of the range actually matters.
	 * Determines if the attribute can be used for expressing an order on the
	 * locations of a task.
	 */
	private boolean ordered;
	
	/**
	 * Indicates whether the attribute may have a different meaning per phase.
	 * If two phases share an attribute where this flag is set, the attributes
	 * are considered different.
	 * This is useful e.g. to distinguish the "unit" attribute in the exterior
	 * phase, which refers to parts of outside walls, from the one in the interior
	 * phase, which enumerates rooms of the same type (section).
	 */
	private boolean perPhase;
	
	/**
	 * Indicates whether the values range from 1 to range.size().
	 * Typically the case for "unit"-like attributes. 
	 */
	private boolean valuesMatchPositions;
	
	@ManyToOne
	private Model model;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getShortName() {
		return shortName != null ? shortName : name;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getRange() {
		return range;
	}

	public void setRange(List<String> range) {
		this.range = range;
	}

	public boolean isOrdered() {
		return ordered;
	}

	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public boolean isPerPhase() {
		return perPhase;
	}

	public void setPerPhase(boolean perPhase) {
		this.perPhase = perPhase;
	}

	public boolean isValuesMatchPositions() {
		return valuesMatchPositions;
	}

	public void setValuesMatchPositions(boolean valuesMatchPositions) {
		this.valuesMatchPositions = valuesMatchPositions;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		ModelToMany.ATTRIBUTES.setOne(this, model);
	}
	
	void internalSetModel(Model model) {
		this.model = model;
	}
	
	/** Indicates whether the range has a value at the given position. */
	public boolean hasPosition(int pos) {
		return pos >= 1 && pos <= range.size();
	}
	
	/** Indicates whether the given value is contained in the range. */
	public boolean hasValue(String value) {
		return range.contains(value);
	}

	/** Indicates whether the given value is contained in the range that goes from 1 to {@code n}. */
	private boolean hasPositionValue(int value) {
		return isValuesMatchPositions() && hasPosition(value);
	}
	
	/**
	 * Checks whether the given value is contained in the range.
	 * If successful, a string representation of the value is returned,
	 * otherwise an exception is thrown.
	 */
	public String checkValue(Object value) {
		String s = value == null ? null : value.toString();
		if (!(value instanceof Integer && hasPositionValue((int)value)) && !hasValue(s))
			throw new InvalidAttributeValueException(this, s);
		return s;
	}

	@Override
	public String toString() {
		return "Attribute [id=" + getId() + ", name=" + name + "]";
	}
	
}
