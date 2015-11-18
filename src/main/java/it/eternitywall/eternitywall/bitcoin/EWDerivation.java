package it.eternitywall.eternitywall.bitcoin;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

/**
 * Created by Riccardo Casatta @RCasatta on 17/11/15.
 */
public class EWDerivation {
    private DeterministicKey changesMaster;
    private DeterministicKey messagesMaster;

    private static int EW_DERIVATION = 4544288;  //0x455720

    public EWDerivation(byte[] seed) {
        final DeterministicKey deterministicKey = HDKeyDerivation.createMasterPrivateKey(seed);
        final DeterministicKey ewMaster = HDKeyDerivation.deriveChildKey(deterministicKey, new ChildNumber(EW_DERIVATION, true));   // /m/4544288'/

        final DeterministicKey knownMaster     = HDKeyDerivation.deriveChildKey(ewMaster, new ChildNumber(0,false) );  // /m/4544288'/0
        changesMaster  = HDKeyDerivation.deriveChildKey(knownMaster, new ChildNumber(0,false) );  // /m/4544288'/0/0
        messagesMaster = HDKeyDerivation.deriveChildKey(knownMaster, new ChildNumber(1,false) );  // /m/4544288'/0/1

        /*
        //Anonymous derivation are probably not needed
        final DeterministicKey anonymousMaster = HDKeyDerivation.deriveChildKey(ewMaster, new ChildNumber(1,false) );  // /m/4544288'/1
        anonymousChangesMaster  = HDKeyDerivation.deriveChildKey(anonymousMaster, new ChildNumber(0,false) );  // /m/4544288'/1/0
        anonymousMessagesMaster = HDKeyDerivation.deriveChildKey(anonymousMaster, new ChildNumber(1,false) );  // /m/4544288'/1/1
        */
    }

    public DeterministicKey getAlias() {
        return getKnownChanges(0); // /m/4544288'/0/0/0
    }

    public DeterministicKey getKnownChanges(int i) {
        return HDKeyDerivation.deriveChildKey(changesMaster, new ChildNumber(i, false));  // /m/4544288'/0/0/i
    }

    public DeterministicKey getKnownMessages(int j) {
        return HDKeyDerivation.deriveChildKey(messagesMaster, new ChildNumber(j, false));  // /m/4544288'/0/1/i
    }



}
