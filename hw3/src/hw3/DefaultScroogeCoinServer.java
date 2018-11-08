package hw3;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.Array;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

//Scrooge creates coins by adding outputs to a transaction to his public key.
//In ScroogeCoin, Scrooge can create as many coins as he wants.
//No one else can create a coin.
//A user owns a coin if a coin is transfer to him from its current owner
public class DefaultScroogeCoinServer implements ScroogeCoinServer {

	private KeyPair scroogeKeyPair;
	private ArrayList<Transaction> ledger = new ArrayList<>();

	//Set scrooge's key pair
	@Override
	public synchronized void init(KeyPair scrooge) {
		this.scroogeKeyPair = scrooge;
	}


	//For every 10 minute epoch, this method is called with an unordered list of proposed transactions
	// 		submitted during this epoch.
	//This method goes through the list, checking each transaction for correctness, and accepts as
	// 		many transactions as it can in a "best-effort" manner, but it does not necessarily return
	// 		the maximum number possible.
	//If the method does not accept an valid transaction, the user must try to submit the transaction
	// 		again during the next epoch.
	//Returns a list of hash pointers to transactions accepted for this epoch

	public synchronized List<HashPointer> epochHandler(List<Transaction> txs)  {

		List<HashPointer> acceptedTxHashPointers = new ArrayList<>();

		List<Transaction> remainingTxs = new ArrayList<>(txs);
		while (!remainingTxs.isEmpty()){
			List<Transaction> validTransactions = new ArrayList<>();
			for (Transaction tx : remainingTxs){
				if(this.isValid(tx)){
					this.ledger.add(tx);
					acceptedTxHashPointers.add(new HashPointer(tx.getHash(), ledger.size()-1));
					validTransactions.add(tx);
				}
			}
			if(validTransactions.isEmpty()){
				break;
			}
			remainingTxs.removeAll(validTransactions);
		}

		return acceptedTxHashPointers;

	}

	//Returns true if and only if transaction tx meets the following conditions:
	//CreateCoin transaction
	//	(1) no inputs
	//	(2) all outputs are given to Scrooge's public key
	//	(3) all of tx’s output values are positive
	//	(4) Scrooge's signature of the transaction is included

	//PayCoin transaction
	//	(1) all inputs claimed by tx are in the current unspent (i.e. in getUTOXs()),
	//	(2) the signatures on each input of tx are valid,
	//	(3) no UTXO is claimed multiple times by tx,
	//	(4) all of tx’s output values are positive, and
	//	(5) the sum of tx’s input values is equal to the sum of its output values;
	@Override
	public synchronized boolean isValid(Transaction tx) {
		switch (tx.getType()){
			case Create: return isValidCreateTx(tx);
			case Pay: return isValidPayTx(tx);
			default: return false;
		}
	}

	private synchronized boolean isValidPayTx(@NotNull Transaction tx) {

		Set<UTXO> utxos = getUTXOs();
		double inputSum = 0;

		for (int i = 0; i< tx.numInputs(); i++) {
			Transaction.Input input = tx.getInput(i);

			int ledgerIndex = getLedgerIndexOfOutputTxHash(input.getHashOfOutputTx(), utxos, input.getIndexOfTxOutput(), input);

			HashPointer hashPtrOfOutputTx = new HashPointer(input.getHashOfOutputTx(), ledgerIndex);

			UTXO utxo = new UTXO(hashPtrOfOutputTx, input.getIndexOfTxOutput());

			//if input is not in UTXO set, tx is not valid
			if(!utxos.contains(utxo)){
				return false;
			}
			//removing so that if double spent in same tx, it will return false
			utxos.remove(utxo);

			Transaction.Output output = ledger.get(ledgerIndex).getOutput(input.getIndexOfTxOutput());

			//verifying the signature on input
			try {
				Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
				signature.initVerify(output.getPublicKey());
				signature.update(tx.getRawDataToSign(i));
				if(!signature.verify(input.getSignature())){
					return false;
				}
			} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
				throw new RuntimeException();
			}

			inputSum += output.getValue();


		}

		double outputSum = 0;
		for (Transaction.Output output: tx.getOutputs()) {
			//all the outputs should be positive
			if(output.getValue() <= 0){
				return false;
			}

			outputSum += output.getValue();
		}

		//check for inputSum and outputSum equality
		return Math.abs(inputSum-outputSum) < 0.000001;
	}

	private synchronized boolean isValidCreateTx(@NotNull Transaction tx) {
		if(tx.numInputs() != 0){
			return false;
		}

		for (int i = 0; i < tx.numOutputs(); i++) {
			Transaction.Output output = tx.getOutput(i);
			if(output.getPublicKey() != scroogeKeyPair.getPublic()){
				return false;
			}
			if(output.getValue() <= 0){
				return false;
			}
		}

		if(tx.getSignature() == null){
			return false;
		}

		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initVerify(scroogeKeyPair.getPublic());
			signature.update(tx.getRawBytes());
			if(signature.verify(tx.getSignature())){
				return true;
			}
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			e.printStackTrace();
		}

		throw new RuntimeException();
	}

	//Returns the complete set of currently unspent transaction outputs on the ledger
	@Override
	public synchronized Set<UTXO> getUTXOs() {
		Set<UTXO> utxos = new HashSet<>();

		for (int i = 0; i < ledger.size(); i++) {

			Transaction tx = ledger.get(i);

			//removing all inputs from the set
			tx.getInputs().forEach(input -> {
				HashPointer hashPtrOfOutputTx = new HashPointer(
						input.getHashOfOutputTx(),
						getLedgerIndexOfOutputTxHash(input.getHashOfOutputTx(), utxos, input.getIndexOfTxOutput(), input)
				);


				UTXO utxo = new UTXO(hashPtrOfOutputTx, input.getIndexOfTxOutput());
				utxos.remove(utxo);
			});

			HashPointer hashPtrOfTx = new HashPointer(tx.getHash(), i);

			//adding all outputs to the set
			tx.getOutputs().forEach(output -> {
				UTXO utxo = new UTXO(hashPtrOfTx, tx.getIndex(output));
				utxos.add(utxo);
			});

		}

		return utxos;
	}

	private synchronized int getLedgerIndexOfOutputTxHash(byte[] hashOfOutputTx, Set<UTXO> utxos, int outputIndex, Transaction.Input input){
		for (int i = 0; i < ledger.size(); i++) {
			if(Arrays.equals(ledger.get(i).getHash(), hashOfOutputTx)){

				HashPointer txHashPointer = new HashPointer(input.getHashOfOutputTx(), i);

				//checking if the output is from UTXO set
				if(utxos.contains(new UTXO(txHashPointer, outputIndex))){
					return i;
				}
			}
		}
		return -1;
	}

}
