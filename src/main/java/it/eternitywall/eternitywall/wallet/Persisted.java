package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Riccardo Casatta @RCasatta on 27/11/15.
 */
public class Persisted implements Serializable {
    private Map<Sha256Hash,Transaction> txMap = new HashMap<>();
    private Map<Address, List<TransactionOutput>> utxo = new HashMap<>();
    private Set<Address> used      = new HashSet<>();
    private Integer nextMessageId = 0 ;
    private Integer nextChange = 0 ;

    public Integer incrementChange() {
        nextChange++;
        return nextChange;
    }

    public Integer getNextChange() {
        return nextChange;
    }

    public void setNextChange(Integer nextChange) {
        this.nextChange = nextChange;
    }

    public Integer incrementNextMessageId() {
        nextMessageId++;
        return nextMessageId;
    }

    public Integer getNextMessageId() {
        return nextMessageId;
    }

    public void setNextMessageId(Integer nextMessageId) {
        this.nextMessageId = nextMessageId;
    }

    public Map<Sha256Hash, Transaction> getTxMap() {
        return txMap;
    }

    public void setTxMap(Map<Sha256Hash, Transaction> txMap) {
        this.txMap = txMap;
    }

    public Set<Address> getUsed() {
        return used;
    }

    public void setUsed(Set<Address> used) {
        this.used = used;
    }

    public Map<Address, List<TransactionOutput>> getUtxo() {
        return utxo;
    }

    public void setUtxo(Map<Address, List<TransactionOutput>> utxo) {
        this.utxo = utxo;
    }


    @Override
    public String toString() {
        return "Persisted{" +
                "nextChange=" + nextChange +
                ", txMap=" + txMap +
                ", utxo=" + utxo +
                ", used=" + used +
                ", nextMessageId=" + nextMessageId +
                '}';
    }
}
