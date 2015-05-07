package be.ugent.mmlab.rml.core;

public class ArgumentPosition {
	public ArgumentPosition(int argumentList, int actualPosition) {
		super();
		this.argumentList = argumentList;
		this.actualPosition = actualPosition;
	}
	public int getArgumentList() {
		return argumentList;
	}
	public void setArgumentList(int argumentList) {
		this.argumentList = argumentList;
	}
	public int getActualPosition() {
		return actualPosition;
	}
	public void setActualPosition(int actualPosition) {
		this.actualPosition = actualPosition;
	}
	private int argumentList;
	private int actualPosition;
}
