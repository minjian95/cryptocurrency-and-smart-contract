package hw2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;


import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;


public class GenerateScroogeKeyPair {

    private static final String KEY_ALGORITHM   = "ECDSA";
    private static final String PROVIDER        = "BC";
    private static final String CURVE_NAME      = "secp256k1";



    public static void main(String[] args) throws Exception {
        String password = "123456";

        Security.addProvider(new BouncyCastleProvider());
        SecureRandom random = SecureRandom.getInstanceStrong();
        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(CURVE_NAME);

        KeyPairGenerator keyGen_ = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
        keyGen_.initialize(ecGenSpec, random);
        KeyPair kp = keyGen_.generateKeyPair();

        {
            System.out.println("Writing public key to file");

            String pkFilename = "scrooge_pk.pem";
            StringWriter sw = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(sw);
            pemWriter.writeObject(kp.getPublic());
            pemWriter.close();
            FileWriter fw = new FileWriter(pkFilename);
            fw.write(sw.toString());
            fw.close();

            System.out.println("Completed writing public key ");
        }

        System.out.println();

        {
            System.out.println("Writing secret key to file");

            String skFilename = "scrooge_sk.pem";
            JcaPEMWriter privWriter = new JcaPEMWriter(new FileWriter(skFilename));
            PEMEncryptor penc = (new JcePEMEncryptorBuilder("AES-256-CFB"))
                    .build(password.toCharArray());
            privWriter.writeObject(kp.getPrivate(), penc);
            privWriter.close();
            System.out.println("Completed writing secret key");
        }
	}
}
