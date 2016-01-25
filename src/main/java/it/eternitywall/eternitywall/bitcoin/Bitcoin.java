
package it.eternitywall.eternitywall.bitcoin;

import com.google.common.base.Joiner;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by casatta on 25/02/15.
 */
public class Bitcoin {

    public static Pattern invalidChars = Pattern.compile("[0OIl]");

    public static boolean basicValidation(String address) {
        if (address == null)
            return false;
        if (address.length() > 34 || address.length() < 27)
            return false;
        if (invalidChars.matcher(address).find())
            return false;

        return true;
    }

    public static boolean isValidAddress(String address) {
        if(address==null)
            return false;
        if (!basicValidation(address))
            return false;
        try {
            new Address( BitcoinNetwork.getInstance().get().getParams() , address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }

    public static boolean isValidHash(String hashString) {
        if(hashString==null || hashString.isEmpty())
            return false;
        try {
            Sha256Hash hash = new Sha256Hash(hashString);
            return hash!=null;
        } catch (Exception e) {

        }
        return false;
    }


    public final static String TAG = "WalletClient";

    public static byte[] getRandomSeed() {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] seed = new byte[128 / 8];  //Now thinking the 12 words are enough
        secureRandom.nextBytes(seed);
        return seed;
    }

    public static byte[] getLongRandomSeed() {
        final SecureRandom secureRandom = new SecureRandom();
        final byte[] seed = new byte[256 / 8];  //Now thinking the 12 words are enough
        secureRandom.nextBytes(seed);
        return seed;
    }

    public static List<String> split(final String words) {
        return new ArrayList<>(Arrays.asList(words.split("\\s+")));
    }

    public static DeterministicKey keyFromMnemonic(String mnemonic) {
        final byte[] mySeed = getSeedFromPassphrase(mnemonic);
        final DeterministicKey deterministicKey = HDKeyDerivation.createMasterPrivateKey(mySeed);

        return deterministicKey;

    }

    public static byte[] getSeedFromPassphrase(String mnemonic) {
        return MnemonicCode.toSeed(split(mnemonic), "");
    }

    public static byte[] getEntropyFromPassphrase(String mnemonic) {
        try {
            final MnemonicCode mnemonicCode = newMnemonicCode();
            if(mnemonicCode!=null)
                return mnemonicCode.toEntropy(split(mnemonic));
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicWordException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicChecksumException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String keyToStringAddress(ECKey key) {

        return key.toAddress(BitcoinNetwork.getInstance().get().getParams()).toString();
    }


    public static byte[] fromHex(String s) {
        if (s != null) {
            try {
                StringBuilder sb = new StringBuilder(s.length());
                for (int i = 0; i < s.length(); i++) {
                    char ch = s.charAt(i);
                    if (!Character.isWhitespace(ch)) {
                        sb.append(ch);
                    }
                }
                s = sb.toString();
                int len = s.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    int hi = (Character.digit(s.charAt(i), 16) << 4);
                    int low = Character.digit(s.charAt(i + 1), 16);
                    if (hi >= 256 || low < 0 || low >= 16) {
                        return null;
                    }
                    data[i / 2] = (byte) (hi | low);
                }
                return data;
            } catch (Exception ignored) {
            }
        }
        return null;
    }


    public static String transactionToHex(Transaction transaction) {
        final StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            transaction.bitcoinSerialize(os);
            byte[] bytes = os.toByteArray();
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }

            return sb.toString();
        } catch (IOException e) {
            return null;
        } finally {
            formatter.close();
        }
    }


    public static List<TransactionOutput> getMyOutputs(Transaction tx , DeterministicKey key) {
        List<TransactionOutput> mines=new ArrayList<>();
        for (TransactionOutput curr : tx.getOutputs()) {
            boolean isMine = false;

            String to = null;
            Address add = curr.getAddressFromP2PKHScript(BitcoinNetwork.getInstance().get().getParams());
            if (add != null)
                to = add.toString();
            else {
                add = curr.getAddressFromP2SH(BitcoinNetwork.getInstance().get().getParams());
                if (add != null)
                    to = add.toString();
            }
            if (to != null) { //VERIFICATION BY ADDRESS
                isMine = to.equals( keyToStringAddress(key) );
            } else {  //VERIFICATION BY PUBKEY
                final byte[] pubKeyCurr = curr.getScriptPubKey().getPubKey();
                isMine = Arrays.equals(key.getPubKey(), pubKeyCurr);
            }

            if (isMine) {
                mines.add(curr);
                break;
            }
        }
        return mines;
    }

    public static String getNewMnemonicPassphrase() {
        return getNewMnemonicPassphrase(getRandomSeed());
    }

    public static MnemonicCode newMnemonicCode() {
        InputStream wordsInputStream=null;
        try {
            wordsInputStream  = Bip39WordList.getBip39WordListAsStream();
            //return new MnemonicCode(wordsInputStream, Bip39WordList.BIP39_ENGLISH_SHA256);
            return new MnemonicCode(wordsInputStream, Bip39WordList.BIP39_ENGLISH_SHA256);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if(wordsInputStream!=null)
                try {
                    wordsInputStream.close();
                } catch (IOException e) {
                }
        }
        return null;
    }

    public static String getNewMnemonicPassphrase(byte[] randomSeed) {
        try {
            MnemonicCode mnemonicCode = newMnemonicCode();
            if(mnemonicCode!=null) {
                return Joiner.on(" ").join(
                        mnemonicCode
                                .toMnemonic(randomSeed));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

}
