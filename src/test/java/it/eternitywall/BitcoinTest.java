package it.eternitywall;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Riccardo Casatta @RCasatta on 04/08/15.
 */
public class BitcoinTest {



    @Test
    public void testing() {
        final String passphrase = Bitcoin.getNewMnemonicPassphrase(Bitcoin.getLongRandomSeed());

        System.out.println("passphrase: " + passphrase);

        final DeterministicKey deterministicKey = Bitcoin.keyFromMnemonic(passphrase);

        final DeterministicKey deriveSoft = HDKeyDerivation.deriveChildKey(deterministicKey, new ChildNumber(1, false));
        final String expected = Bitcoin.keyToStringAddress(deriveSoft);


        final DeterministicKey deriveSoft2 = HDKeyDerivation.deriveChildKey(deriveSoft, new ChildNumber(1, false));
        final String expected2 = Bitcoin.keyToStringAddress(deriveSoft2);


        final String hexMasterPublicKey = Hex.toHexString(deterministicKey.getPubKey());
        System.out.println("hexMasterPublicKey: " + hexMasterPublicKey);
        final String hexChainCode       = Hex.toHexString(deterministicKey.getChainCode());
        System.out.println("hexChainCode: " + hexChainCode);

        final byte[] bytesMasterPublicKey = Bitcoin.fromHex(hexMasterPublicKey);
        final byte[] bytesChainCode       = Bitcoin.fromHex(hexChainCode);
        final DeterministicKey deterministicKeyPublic = HDKeyDerivation.createMasterPubKeyFromBytes(bytesMasterPublicKey, bytesChainCode);

        final HDKeyDerivation.RawKeyBytes deriveSoftPublicRawBytes = HDKeyDerivation.deriveChildKeyBytesFromPublic(deterministicKeyPublic, new ChildNumber(1, false), HDKeyDerivation.PublicDeriveMode.NORMAL);
        final DeterministicKey deriveSoftPublic = HDKeyDerivation.createMasterPubKeyFromBytes(deriveSoftPublicRawBytes.keyBytes, deriveSoftPublicRawBytes.chainCode );
        assertEquals(expected, Bitcoin.keyToStringAddress(deriveSoftPublic) );

        final HDKeyDerivation.RawKeyBytes deriveSoftPublicRawBytes2 = HDKeyDerivation.deriveChildKeyBytesFromPublic(deriveSoftPublic, new ChildNumber(1, false), HDKeyDerivation.PublicDeriveMode.NORMAL);
        final DeterministicKey deriveSoftPublic2 = HDKeyDerivation.createMasterPubKeyFromBytes(deriveSoftPublicRawBytes2.keyBytes, deriveSoftPublicRawBytes2.chainCode);
        assertEquals(expected2, Bitcoin.keyToStringAddress(deriveSoftPublic2) );


    }


}
