package it.unibz.precise.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"model_id", "name"})
})
public class Attribute extends BaseEntity implements ShortNameProvider {

	@Column(nullable=false)
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
	 * Indicates whether the values range from 1 to range.size().
	 * Typically the case for "unit"-like attributes. 
	 */
	private boolean valuesMatchPositions;
	
	@ManyToOne
	private Model model;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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
	
	public boolean hasPosition(int pos) {
		return pos >= 1 && pos <= range.size();
	}
	
	public boolean hasValue(String value) {
		return range.contains(value);
	}

	private boolean hasPositionValue(int value) {
		return isValuesMatchPositions() && hasPosition(value);
	}
	
	public String checkValue(Object value) {
		String s = value == null ? null : value.toString();
		if (!(value instanceof Integer && hasPositionValue((int)value)) && !hasValue(s))
			throw new InvalidAttributeValueException(this, s);
		return s;
	}
	
}
