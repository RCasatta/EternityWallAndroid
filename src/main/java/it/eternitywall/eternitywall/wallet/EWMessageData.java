package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;

/**
 * Created by Riccardo Casatta @RCasatta on 26/11/15.
 */
public class EWMessageData {
    private Address messageId;
    private Address change;
    private ECKey input;

    public Address getChange() {
        return change;
    }

    public void setChange(Address change) {
        this.change = change;
    }

    public ECKey getInput() {
        return input;
    }

    public void setInput(ECKey input) {
        this.input = input;
    }

    public Address getMessageId() {
        return messageId;
    }

    public void setMessageId(Address messageId) {
        this.messageId = messageId;
    }
}
