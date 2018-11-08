package hw2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;


import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class GenerateDigitalSignature {
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";


    public static void main(String[] args) throws Throwable {
        Security.addProvider(new BouncyCastleProvider());


        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);

        SecureRandom random = SecureRandom.getInstanceStrong();

        signature.initSign(getPrivateKey(), random);

        String messageStr = "Pay 3 bitcoins to Alice";
        byte[] message1 = messageStr.getBytes(StandardCharsets.UTF_8);
        signature.update(message1);
        byte[] sigBytes1 = signature.sign();
        System.out.println("Signature: msg=" + messageStr + " sig.len=" + sigBytes1.length + " sig=" + DatatypeConverter.printHexBinary(sigBytes1));


    }

    public static PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, PKCSException, OperatorCreationException {
        String password = "123456";
        File secretKeyFile = new File("scrooge_sk.pem");
        PEMParser pemParser = new PEMParser(new FileReader(secretKeyFile));
        Object object = pemParser.readObject();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        KeyPair kp = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
        return kp.getPrivate();
    }

    //signature = 30450221009A479AE2EE61B6068D63FC08FEF10E60B8A1A2B5B78D8A0E011E672B34A5E8D502204E6E611324BA69954CE47A098A3159E4FED19E9010D5E3B0AB483035C7BD1740
}
