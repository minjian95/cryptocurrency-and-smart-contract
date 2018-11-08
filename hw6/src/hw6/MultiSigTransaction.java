package hw6;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.ArrayList;
import java.util.Random;

import static org.bitcoinj.script.ScriptOpCodes.*;

public class MultiSigTransaction extends ScriptTester {

    private DeterministicKey key1;
    private DeterministicKey key2;
    private DeterministicKey key3;

    public MultiSigTransaction(WalletAppKit kit) {
        super(kit);
        key1 = kit.wallet().freshReceiveKey();
        key2 = kit.wallet().freshReceiveKey();
        key3 = kit.wallet().freshReceiveKey();
    }

    @Override
    public Script createLockingScript() {
        ScriptBuilder builder = new ScriptBuilder();

        builder.op(OP_1);
        builder.data(key1.getPubKey());
        builder.data(key2.getPubKey());
        builder.data(key3.getPubKey());
        builder.op(OP_3);
        builder.op(OP_CHECKMULTISIG);
        return builder.build();

    }

    @Override
    public Script createUnlockingScript(Transaction unsignedTransaction) {
        Random r = new Random();
        int num = (r.nextInt(3) + 1);
        DeterministicKey[] keys = new DeterministicKey[]{key1, key2, key3};
        TransactionSignature txSig = sign(unsignedTransaction, keys[num - 1]);
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(OP_0);
        builder.data(txSig.encodeToBitcoin());
        return builder.build();
    }

    public static void main(String[] args) throws InsufficientMoneyException, InterruptedException {
        WalletInitTest wit = new WalletInitTest();
        new MultiSigTransaction(wit.getKit()).run();
        wit.monitor();
     }

}
