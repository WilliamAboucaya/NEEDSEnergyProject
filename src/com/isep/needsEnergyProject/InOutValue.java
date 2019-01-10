package com.isep.needsEnergyProject;

/**
 * The object representation of the amount used and produced of an element with a specific unit.
 * 
 * @author William Aboucaya
 */
public class InOutValue {
	
	private final String name;
	
	private double inAmount;
	private double outAmount;
	
	private String unit;
	
	public InOutValue(String name) {
		this.name = name;
		this.inAmount = 0;
		this.outAmount = 0;
		this.unit = "";
	}

	public String getName() {
		return name;
	}

	public double getInAmount() {
		return inAmount;
	}

	public void setInAmount(double inAmount) {
		this.inAmount = inAmount;
	}
	
	public void addToInAmount(double value) {
		inAmount += value;
	}

	public double getOutAmount() {
		return outAmount;
	}

	public void setOutAmount(double outAmount) {
		this.outAmount = outAmount;
	}

	public void addToOutAmount(double value) {
		outAmount += value;
	}
	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * @return a readable representation of this object to display it on a CLI.
	 */
	public String toDescriptionString() {
		return new StringBuilder(name)
				.append(" : \nInput : ")
				.append(inAmount)
				.append(" ")
				.append(unit)
				.append("\nOutput : ")
				.append(outAmount)
				.append(" ")
				.append(unit)
				.toString();
	}
}