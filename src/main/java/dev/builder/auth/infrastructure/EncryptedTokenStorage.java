package dev.builder.auth.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.SessionCipherProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Bean
public class EncryptedTokenStorage implements TokenPersister {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTokenStorage.class);

    private final SecretKey secretKey;
    private final Cipher cipher;

    @Inject
    public EncryptedTokenStorage(SessionCipherProperties cipherProperties) {
        try {
            this.secretKey = deriveKeyFromPassword(
                    cipherProperties.getCipherPassword(),
                    cipherProperties.getCipherSalt(),
                    cipherProperties.getCipherIterations(),
                    cipherProperties.getCipherKeyLength(),
                    cipherProperties.getCipherKeyType()
            );

            this.cipher = Cipher.getInstance(cipherProperties.getCipherTransformation());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SecretKey deriveKeyFromPassword(String password, String base64Salt, int iterations, int keyLength, String algorithm)
            throws Exception {

        byte[] salt = Base64.getDecoder().decode(base64Salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), algorithm);
    }


    @Override
    public SessionToken read(String filename) throws FileNotFoundException {
        String content;

        try (FileInputStream fileIn = new FileInputStream(filename)) {
            byte[] fileIv = new byte[16];
            fileIn.read(fileIv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));

            try (
                    CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
                    ObjectInputStream reader = new ObjectInputStream(cipherIn)
            ) {
                LOGGER.debug("Leyendo sesión de: {}", new File(filename).getAbsolutePath());
                return (SessionToken) reader.readObject();
            }

        } catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | ClassNotFoundException e) {
            if(e instanceof FileNotFoundException) throw (FileNotFoundException)e;
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(SessionToken content, String filename) throws FileNotFoundException{
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();

            try (FileOutputStream fileOut = new FileOutputStream(filename)) {
                fileOut.write(iv);
                try (
                        CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher);
                        ObjectOutputStream objectOut = new ObjectOutputStream(cipherOut)
                ) {
                    objectOut.writeObject(content);
                }
            }

        } catch (InvalidKeyException | IOException e) {
            if(e instanceof FileNotFoundException) throw (FileNotFoundException)e;
            throw new RuntimeException(e);
        }

    }
}
