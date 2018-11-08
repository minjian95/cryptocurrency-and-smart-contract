package hw4.consensus.bft;

import hw4.net.Value;

import java.util.ArrayList;
import java.util.List;

public class EIGNode {
	Trace trace;
	Value value;

	List<EIGNode> children;
	public EIGNode(Trace trace, Value value){
		this.trace = trace;
		this.value = value;
		children = new ArrayList<>();
	}
}
