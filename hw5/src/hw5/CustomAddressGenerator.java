package hw5;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomAddressGenerator {
	private static final NetworkParameters NET_PARAMS = MainNetParams.get();
	private final static Logger LOG = LoggerFactory.getLogger(CustomAddressGenerator.class);

	/*  @param prefix	string of letters in base58 encoding
	 *  @returns 		a Bitcoin address on mainnet which starts with 1 followed prefix.
     */
	public static String get(String prefix) {

		if(containsValidBTCAddressChars(prefix)){
			long attempts = 0;
			ECKey key = new ECKey();

			while (!(key.toAddress(NET_PARAMS).toString().startsWith(1+prefix))){
				key = new ECKey();
				if(attempts%100000 == 0) LOG.info("No of attempts made: " + NumberFormat.getNumberInstance(Locale.US).format(attempts));
				attempts++;
			}

			LOG.info("Found matching address:" + key.toAddress(NET_PARAMS).toString() + "    in attempts :" +  NumberFormat.getNumberInstance(Locale.US).format(attempts) );
			return key.toAddress(NET_PARAMS).toString();
		}

		return null;
	}

	private static boolean containsValidBTCAddressChars(String s){
		if(s.length() < 35){
			String inValidChars = "0OIl";
			for (Character c : s.toCharArray()) {
				if(inValidChars.contains(c.toString())) return false;
			}

			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		System.out.println(get("VYKU"));
	}

}
