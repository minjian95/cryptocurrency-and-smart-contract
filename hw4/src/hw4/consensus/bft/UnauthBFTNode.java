package hw4.consensus.bft;

import hw4.consensus.majority.MajorityVotingPayload;
import hw4.net.*;

import java.util.*;

public class UnauthBFTNode extends Node {

    private EIGTree tree;
    private Value receivedLeaderDecisionValue;
    private boolean isLeaderAndSentInitialValue;
    public UnauthBFTNode() {

    }

    @Override
    public List<Send> send(int round) {
        List<Send> sends = new ArrayList<>();
        if(getIsLeader()){
            if (getLeaderInitialValue() == null) {
                throw new RuntimeException("Leader decision not set");
            }

            if (!isLeaderAndSentInitialValue) {
                for (Id to : getPeerIds()) {
                    sends.add(new Send(to, new UnauthBFTPayload(Trace.EMPTY, getLeaderInitialValue())));
                }

                isLeaderAndSentInitialValue = true;
            }
        }else {

            if(receivedLeaderDecisionValue != null){
                if(round == 1){
                    for (Id to: getPeerIds()) {
                        if (to.equals(getLeaderNodeId())) continue;
                        sends.add(new Send(to, new UnauthBFTPayload(Trace.EMPTY, receivedLeaderDecisionValue)));
                    }
                    sends.add(new Send(getId(), new UnauthBFTPayload(Trace.EMPTY, receivedLeaderDecisionValue)));
                    return sends;
                }

                List<Trace> traces = tree.getTracesOfRound(round-1);
                for (Id to: getPeerIds()){
                    if(to.equals(getLeaderNodeId())){
                        continue;
                    }
                    for(Trace t : traces){
                        if(!t.getTrace().contains(getId())){
                            sends.add(new Send(to, new UnauthBFTPayload(t, tree.getValue(t))));
                        }
                    }
                }

            }
        }

        return sends;
    }

    @Override
    public void receive(int round, List<Message> messages) {
        if(!getIsLeader()){
            for(Message m : messages){
                UnauthBFTPayload payload = m.getSend().getPayload(UnauthBFTPayload.class);
                if(m.getFrom().equals(getLeaderNodeId())){
                    if(receivedLeaderDecisionValue == null){
                        receivedLeaderDecisionValue = payload.getDecisionValue();
                        tree = new EIGTree(payload.getDecisionValue());

                    }
                }else {
                    if(round > 0 ){
                        tree.addNode(m.getFrom(), payload.getTrace(), payload.getDecisionValue());
                    }
                }
            }
        }
    }

    @Override
    public void commit() {
        if(getIsLeader()){
            setDecisionValue(getLeaderInitialValue());
        }else {
            setDecisionValue(tree.getDecisionValue() != null? tree.getDecisionValue() : getDefaultValue());
        }
    }
}
