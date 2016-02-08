package it.eternitywall.eternitywall.wallet;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.util.Calendar;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;

/**
 * Created by Riccardo Casatta @RCasatta on 17/11/15.
 */
public class EWDerivation {
    private DeterministicKey deterministicKey;
    private DeterministicKey ewMaster;
    private DeterministicKey bitcoinMaster;
    private DeterministicKey firstAccountMaster;
    private DeterministicKey changesMaster;
    private DeterministicKey messagesMaster;

    private static int EW_DERIVATION = 4544288;  //0x455720

    // BIP 43   m / purpose' / *
    // BIP 44   m / purpose' / coin_type' / account' / change / address_index
    // BIP 47   m / purpose' / coin_type' / identity'

    // BIP EW   /m/ 4544288' / coin_type' / account' / type / address_index
    //          /m/ 4544288' / 0          / 0        / 0    / i

    // coin_type https://github.com/satoshilabs/slips/blob/master/slip-0044.md

    // type could be 0 for changes and 1 for messages

    //EW donation master public
    private String hexMasterPublicKey = "02982d773156ecf4809db9f25a8c827cffc2d1fff275b2691f87ad2ff608839fab";
    private String hexChainCode = "c662a4b9aea3f9cbc749e3789668451b1279be2424ab2539f2de64511198ad10";
    private DeterministicKey donationMasterPublic;

    public EWDerivation(byte[] seed) {
        deterministicKey = HDKeyDerivation.createMasterPrivateKey(seed);
        ewMaster = HDKeyDerivation.deriveChildKey(deterministicKey, new ChildNumber(EW_DERIVATION, true));   // /m/4544288'/
        bitcoinMaster = HDKeyDerivation.deriveChildKey(ewMaster, new ChildNumber(0, true));  // /m/4544288'/0'
        firstAccountMaster = HDKeyDerivation.deriveChildKey(bitcoinMaster, new ChildNumber(0, true));  // /m/4544288'/0'/0'

        changesMaster = HDKeyDerivation.deriveChildKey(firstAccountMaster, new ChildNumber(0, false));  // /m/4544288'/0'/0'/0
        messagesMaster = HDKeyDerivation.deriveChildKey(firstAccountMaster, new ChildNumber(1, false));  // /m/4544288'/0'/0'/1

        /*
        //Anonymous derivation are probably not needed
        final DeterministicKey anonymousMaster = HDKeyDerivation.deriveChildKey(ewMaster, new ChildNumber(1,false) );  // /m/4544288'/1
        anonymousChangesMaster  = HDKeyDerivation.deriveChildKey(anonymousMaster, new ChildNumber(0,false) );  // /m/4544288'/1/0
        anonymousMessagesMaster = HDKeyDerivation.deriveChildKey(anonymousMaster, new ChildNumber(1,false) );  // /m/4544288'/1/1
        */

        final byte[] bytesMasterPublicKey = Bitcoin.fromHex(hexMasterPublicKey);
        final byte[] bytesChainCode = Bitcoin.fromHex(hexChainCode);
        donationMasterPublic = HDKeyDerivation.createMasterPubKeyFromBytes(bytesMasterPublicKey, bytesChainCode);
    }

    public DeterministicKey getAlias() {
        return getChanges(0); // /m/4544288'/0'/0'/0/0
    }

    public DeterministicKey getChanges(int i) {
        return HDKeyDerivation.deriveChildKey(changesMaster, new ChildNumber(i, false));  // /m/4544288'/0'/0'/0/i
    }

    public DeterministicKey getMessages(int j) {
        return HDKeyDerivation.deriveChildKey(messagesMaster, new ChildNumber(j, false));  // /m/4544288'/0'/0'/0/j
    }

    public DeterministicKey getAccount(int k) {
        return HDKeyDerivation.deriveChildKey(bitcoinMaster, new ChildNumber(k, true));  // /m/4544288'/0'/0'/k
    }


    public DeterministicKey getDonation(int d) {
        return HDKeyDerivation.deriveChildKey(donationMasterPublic, new ChildNumber(d, false));
    }

    public DeterministicKey getTodayDonation() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        return getDonation(dayOfYear);
    }


}