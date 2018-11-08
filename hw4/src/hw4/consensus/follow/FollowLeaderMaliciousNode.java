package hw4.consensus.follow;

import hw4.net.Message;
import hw4.net.Send;
import hw4.net.Id;
import hw4.net.Node;
import hw4.net.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowLeaderMaliciousNode extends Node {

    private Value receivedLeaderDecisionValue;
    private boolean isLeaderAndSentInitialValue;

    public FollowLeaderMaliciousNode() {

    }

    @Override
    public List<Send> send(int round) {
        if (getIsLeader()) {
            if (getLeaderInitialValue() == null) {
                throw new RuntimeException("Leader decision not set");
            }

            Value fradulentInitalValue = null;
            for (Value v : getValueSet()) {
                if (!v.equals(getLeaderInitialValue())) {
                    fradulentInitalValue = v;
                    break;
                }
            }


            if (!isLeaderAndSentInitialValue) {
                List<Send> sends = new ArrayList();
                for (Id to : getPeerIds()) {
                    if (to.getNumber() % 2 == 0) {
                        sends.add(new Send(to, new FollowLeaderPayload(getLeaderInitialValue())));
                    } else {
                        sends.add(new Send(to, new FollowLeaderPayload(fradulentInitalValue)));
                    }
                }

                isLeaderAndSentInitialValue = true;
                return sends;
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void receive(int round, List<Message> messages) {

    }

    @Override
    public void commit() {
        if (getIsLeader()) {
            setDecisionValue(getLeaderInitialValue());
        } else {
            setDecisionValue(receivedLeaderDecisionValue);
        }
    }
}
