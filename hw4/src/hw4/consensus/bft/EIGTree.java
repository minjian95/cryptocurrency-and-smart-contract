package hw4.consensus.bft;

import hw4.net.Id;
import hw4.net.Value;

import java.util.*;

public class EIGTree {

	private EIGNode root;

	public EIGTree(Value value){
		this.root = new EIGNode(Trace.EMPTY, value);
	}

	public void addNode(Id id, Trace previousTrace, Value value){
		LinkedList<EIGNode> nodeLinkedList = new LinkedList<>();

		nodeLinkedList.push(root);
		while (!nodeLinkedList.isEmpty()){
			EIGNode node = nodeLinkedList.poll();
			assert node != null;
			if(node.trace.equals(previousTrace)){
				Trace newTrace = Trace.append(previousTrace, id);
				node.children.add(new EIGNode(newTrace, value ));
				break;
			}else {
				for (EIGNode n: node.children) {
					nodeLinkedList.push(n);
				}
			}
		}

	}


	public List<Trace> getTracesOfRound(int round) {
		HashMap<Integer, List<EIGNode>> level = new HashMap<>();

		getTreeLevel(root, level, 0);

		List<Trace> traces = new ArrayList<>();

		for (EIGNode node : level.get(round)){
			traces.add(node.trace);
		}

		return traces;

	}

	private void getTreeLevel(EIGNode node, HashMap<Integer,List<EIGNode>> level, int i) {

		if(!level.containsKey(i)){
			level.put(i, new ArrayList<>());
		}
		List<EIGNode> nodes = level.get(i);
		nodes.add(node);
		level.put(i, nodes);

		for (EIGNode n: node.children) {
			getTreeLevel(n, level, i+1);
		}

	}




	public Value getValue(Trace t) {
		LinkedList<EIGNode> nodeLinkedList = new LinkedList<>();

		nodeLinkedList.push(root);
		while (!nodeLinkedList.isEmpty()){
			EIGNode node = nodeLinkedList.poll();
			assert node != null;
			if(node.trace.equals(t)){
				return node.value;
			}else {
				for (EIGNode n: node.children) {
					nodeLinkedList.push(n);
				}
			}
		}
		return null;
	}

	public Value getDecisionValue() {

		return getDecisionValue(root);
	}

	private Value getDecisionValue(EIGNode node) {

		if(node.children.isEmpty()){
			return node.value;
		}

		HashMap<Value, List<EIGNode>> node2Value = new HashMap<>();
		for (EIGNode n: node.children) {
			Value v = getDecisionValue(n);
			if(!node2Value.containsKey(v)){
				node2Value.put(v, new ArrayList<>());
			}

			List<EIGNode> nodeList = node2Value.get(v);
			nodeList.add(n);
			node2Value.put(v, nodeList);
		}

		return Collections.max(node2Value.entrySet(), Comparator.comparingInt(e -> e.getValue().size())).getKey();
	}
}
