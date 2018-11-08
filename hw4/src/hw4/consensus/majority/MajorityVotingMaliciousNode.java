package hw4.consensus.majority;

import hw4.consensus.follow.FollowLeaderPayload;
import hw4.net.*;
import hw4.util.HashMapList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MajorityVotingMaliciousNode extends Node {

    private static List<Id> sybilNodeIds = new ArrayList<>();
    private boolean isLeaderAndSentInitialValue;
    private boolean hasRelayedLeaderValue;
    private Value receivedLeaderDecisionValue;
    public MajorityVotingMaliciousNode() {

    }

    @Override
    public List<Send> send(int round) {
        List<Send> sends = new ArrayList<>();

        if (getIsLeader()) {
            if (getLeaderInitialValue() == null) {
                throw new RuntimeException("Leader decision not set");
            }

            Value fraudulentInitialValue = null;
            for (Value v : getValueSet()) {
                if (!v.equals(getLeaderInitialValue())) {
                    fraudulentInitialValue = v;
                    break;
                }
            }

            if (!isLeaderAndSentInitialValue) {
                for (Id to : getPeerIds()) {
                    if (to.getNumber() % 2 == 0 || sybilNodeIds.contains(to)) {
                        sends.add(new Send(to, new MajorityVotingPayload(getLeaderInitialValue())));
                    } else {
                        sends.add(new Send(to, new MajorityVotingPayload(fraudulentInitialValue)));
                    }
                }

                isLeaderAndSentInitialValue = true;
                return sends;
            }
        } else {

            if(receivedLeaderDecisionValue != null && !hasRelayedLeaderValue){

                Value fraudulentInitialValue = null;
                for (Value v : getValueSet()) {
                    if (!v.equals(receivedLeaderDecisionValue)) {
                        fraudulentInitialValue = v;
                        break;
                    }
                }

                for (Id to : getPeerIds()) {
                    if(sybilNodeIds.contains(to) || to.getNumber() % 2 == 0){
                        sends.add(new Send(to, new MajorityVotingPayload(receivedLeaderDecisionValue)));

                    }else {
                        sends.add(new Send(to, new MajorityVotingPayload(fraudulentInitialValue)));
                    }

                }
                hasRelayedLeaderValue = true;
            }
        }
        return sends;
    }

    @Override
    public void receive(int round, List<Message> messages) {

        if (!getIsLeader()) {
            for (Message m : messages) {
                MajorityVotingPayload payload = m.getSend().getPayload(MajorityVotingPayload.class);
                if (payload != null) {
                    if (m.getFrom().equals(getLeaderNodeId())) {
                        if (receivedLeaderDecisionValue == null) {
                            receivedLeaderDecisionValue = payload.getDecisionValue();

                        }
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
            setDecisionValue(getDefaultValue());
        }

    }

    public void addSybil(MajorityVotingMaliciousNode n) {
        sybilNodeIds.add(n.getId());
    }
}
