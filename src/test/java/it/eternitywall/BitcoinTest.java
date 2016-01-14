package it.eternitywall;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

import it.eternitywall.eternitywall.bitcoin.Bitcoin;
import it.eternitywall.eternitywall.wallet.EWDerivation;
import it.eternitywall.eternitywall.wallet.EWWalletService;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Riccardo Casatta @RCasatta on 04/08/15.
 */
public class BitcoinTest {

    private Transaction tx1;
    private Transaction tx2;
    private Transaction tx3;

    @Before
    public void init() {
        byte[] b1 = Bitcoin.fromHex("01000000011c3bef15830dade3d87018b1921bb2512ad547ec8194c1ff4736b7ca1ddc634d01000000db00483045022100dd3ff5c3b5916ff76506029563bcf0fb10b3195551299b414920f0edc08d7f91022011d07099e4e3e5e79749089d59eb04487aabea0dfc1a4603319ee53254a5942101483045022100f6b3435105557a39f105586bbd99c7b7bb726e44c0802f59550def2ce790286002206c1009f0ee6dadb1503e777992cfedb3cd07d22aaa858eb8d5eb515908bdfd090147522103c1706fdb635b183fa772c624ffcf373367bd1ee10ab564f2d95daf863a9a6cf62103ecd758a7efecb676b8d0a6ee1200cd4538b78bbe800d06b9b655977e0dae6fb152aeffffffff02b082d0010000000017a914d90d066c1339766b26191cf2f3b623d82734d1b1871f5e0700000000001976a91470e0efeecb2d6d93180c74ff236da6e179806c5988ac00000000");
        MainNetParams params = MainNetParams.get();
        tx1=new Transaction(params, b1);

        byte[] b2 = Bitcoin.fromHex("010000000142691c8f602ccebfd896751f203af07a9ed32b83fbc250981615829310ef72f0010000006a4730440220070284dcd6aa0525e2f21a64c3a5ee56776b35966f369128e42976b487866928022071e27b5f7b97e2275351ad71cd5b2b3fd9416fe43ece3bfcf5869de9e9fb66d8012103becb11cf84e66531f66d9e30e4fb9d97d73b7843cad4411a87c9e22e51979646ffffffff0240420f00000000001976a9142dbc8b8c170ac975052ac45a3ce6446edf76113788ac74f05100000000001976a914aa64f42e763d22f72ccc53607c302381c5fad0ba88ac00000000");
        tx2=new Transaction(params, b2);

        byte[] b3 = Bitcoin.fromHex("0100000001d5999465758b457b93dd3da5081d6ed2be062715db34fba075712bbc0d8e77a02900000049483045022100f5359901aecec21019d5a3d4b3d80ab0c8eb525ba9cee9e1326471bc856cfe74022013fd98e84e7d7ad96a87166e5d29af84877f063d3c094404df7d338b9313131501ffffffff02e8030000000000001976a9149378a7b52a1e6b6a69944eae30012fce5f38701688ac00000000000000001b6a194557205065746572206973206120646f6f646f6f206865616400000000");
        tx3=new Transaction(params, b3);

    }

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

        final List<TransactionInput> inputs = tx1.getInputs();
        for (TransactionInput input : inputs) {
            try {
                Address current = input.getScriptSig().getFromAddress(MainNetParams.get());
                System.out.println(current.toString());
            } catch (ScriptException e) {
                System.out.println("exception");
            }

        }

    }

    @Test
    public void testIsEwMesssage() {
        assertFalse(EWWalletService.isEWMessage(tx1));
        assertFalse(EWWalletService.isEWMessage(tx2));
        assertTrue(EWWalletService.isEWMessage(tx3));


    }


}
