package hw6;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.util.Random;

import static org.bitcoinj.script.ScriptOpCodes.*;

public class GroupMultiSigTransaction extends ScriptTester {

    private DeterministicKey keyBank;
    private DeterministicKey keyCus1;
    private DeterministicKey keyCus2;
    private DeterministicKey keyCus3;

    public GroupMultiSigTransaction(WalletAppKit kit) {
        super(kit);
        keyBank = kit.wallet().freshReceiveKey();
        keyCus1 = kit.wallet().freshReceiveKey();
        keyCus2 = kit.wallet().freshReceiveKey();
        keyCus3 = kit.wallet().freshReceiveKey();
    }

    @Override
    public Script createLockingScript() {
        ScriptBuilder builder = new ScriptBuilder();

        builder.data(keyBank.getPubKey());
        builder.op(OP_CHECKSIGVERIFY);
        builder.op(OP_1);
        builder.data(keyCus1.getPubKey());
        builder.data(keyCus2.getPubKey());
        builder.data(keyCus3.getPubKey());
        builder.op(OP_3);
        builder.op(OP_CHECKMULTISIG);



        return builder.build();
    }

    @Override
    public Script createUnlockingScript(Transaction unsignedTransaction) {
        Random r = new Random();
        int num = (r.nextInt(3) + 1);
        DeterministicKey[] keys = new DeterministicKey[]{keyCus1, keyCus2, keyCus3};
        TransactionSignature txSigBank = sign(unsignedTransaction, keyBank);
        TransactionSignature txSigCus = sign(unsignedTransaction, keys[num - 1]);
        ScriptBuilder builder = new ScriptBuilder();

        builder.smallNum(OP_0);
        builder.data(txSigCus.encodeToBitcoin());
        builder.data(txSigBank.encodeToBitcoin());


        return builder.build();
    }

    public static void main(String[] args) throws InsufficientMoneyException, InterruptedException {
        WalletInitTest wit = new WalletInitTest();
        new GroupMultiSigTransaction(wit.getKit()).run();
        wit.monitor();
    }

}
