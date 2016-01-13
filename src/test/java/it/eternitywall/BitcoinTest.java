package it.eternitywall;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.EWDerivation;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Riccardo Casatta @RCasatta on 04/08/15.
 */
public class BitcoinTest {

    /**
     * hexMasterPublicKey: 02982d773156ecf4809db9f25a8c827cffc2d1fff275b2691f87ad2ff608839fab
     * hexChainCode: c662a4b9aea3f9cbc749e3789668451b1279be2424ab2539f2de64511198ad10
     */

    @Test
    public void testEWClientMaster() {
        byte[] entropyFromPassphrase = Bitcoin.getEntropyFromPassphrase("The passphrase is the Eternity Wall one");
        EWDerivation ewDerivation = new EWDerivation(entropyFromPassphrase);
        String s = ewDerivation.getAlias().toAddress(MainNetParams.get()).toString();
        assertEquals("15uVaRjnY8atrWbe4v9nFimiR2JA3z1Sq8",s);

        DeterministicKey account = ewDerivation.getAccount(1);
        String s1 = account.toAddress(MainNetParams.get()).toString();
        assertEquals("1CUMBk95wj6K4BPvQLybJH3G61MDacMaEo",s1);


        final DeterministicKey deriveSoft = HDKeyDerivation.deriveChildKey(account, new ChildNumber(1, false));
        final String expected = Bitcoin.keyToStringAddress(deriveSoft);

        final DeterministicKey deriveSoft2 = HDKeyDerivation.deriveChildKey(deriveSoft, new ChildNumber(1, false));
        final String expected2 = Bitcoin.keyToStringAddress(deriveSoft2);

        final String hexMasterPublicKey = Hex.toHexString(account.getPubKey());
        System.out.println("hexMasterPublicKey: " + hexMasterPublicKey);
        final String hexChainCode       = Hex.toHexString(account.getChainCode());
        System.out.println("hexChainCode: " + hexChainCode);

        assertEquals("02982d773156ecf4809db9f25a8c827cffc2d1fff275b2691f87ad2ff608839fab", hexMasterPublicKey);
        assertEquals("c662a4b9aea3f9cbc749e3789668451b1279be2424ab2539f2de64511198ad10",hexChainCode);

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

    @Test
    public void testEWClientMasterPublicx() {

        EWDerivation ewDerivation = new EWDerivation(Bitcoin.getRandomSeed());

        for(int i =0;i<367;i++) {
            DeterministicKey donation = ewDerivation.getDonation(i);
            MainNetParams PARAMS = MainNetParams.get();
            Address address = donation.toAddress(PARAMS);
            System.out.println(address.toString());
        }


    }

    @Test
    public void pshTx() {
        byte[] b = Bitcoin.fromHex("01000000011c3bef15830dade3d87018b1921bb2512ad547ec8194c1ff4736b7ca1ddc634d01000000db00483045022100dd3ff5c3b5916ff76506029563bcf0fb10b3195551299b414920f0edc08d7f91022011d07099e4e3e5e79749089d59eb04487aabea0dfc1a4603319ee53254a5942101483045022100f6b3435105557a39f105586bbd99c7b7bb726e44c0802f59550def2ce790286002206c1009f0ee6dadb1503e777992cfedb3cd07d22aaa858eb8d5eb515908bdfd090147522103c1706fdb635b183fa772c624ffcf373367bd1ee10ab564f2d95daf863a9a6cf62103ecd758a7efecb676b8d0a6ee1200cd4538b78bbe800d06b9b655977e0dae6fb152aeffffffff02b082d0010000000017a914d90d066c1339766b26191cf2f3b623d82734d1b1871f5e0700000000001976a91470e0efeecb2d6d93180c74ff236da6e179806c5988ac00000000");
        Transaction tx =new Transaction(MainNetParams.get(), b);
        final List<TransactionInput> inputs = tx.getInputs();
        for (TransactionInput input : inputs) {
            try {
                Address current = input.getScriptSig().getFromAddress(MainNetParams.get());
                System.out.println(current.toString());
            } catch (ScriptException e) {
                System.out.println("exception");
            }

        }

    }


}
