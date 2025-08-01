package dev.builder.auth.infrastructure;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.properties.SessionCipherProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Bean
public class EncryptedTokenStorage implements TokenPersister {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTokenStorage.class);

    private final SecretKey secretKey;
    private final Cipher cipher;

    @Inject
    public EncryptedTokenStorage(SessionCipherProperties cipherProperties){
        try {
            this.secretKey = KeyGenerator.getInstance(cipherProperties.getCipherKeyType()).generateKey();
            this.cipher = Cipher.getInstance(cipherProperties.getCipherTransformation());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
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

            try (FileOutputStream fileOut = new FileOutputStream(filename);
                 CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher);
                 ObjectOutputStream objectOut = new ObjectOutputStream(cipherOut)) {
                LOGGER.debug("Guardando sesión en: {}", new File(filename).getAbsolutePath());
                fileOut.write(iv);
                objectOut.writeObject(content);
            }
        } catch (InvalidKeyException | IOException e) {
            if(e instanceof FileNotFoundException) throw (FileNotFoundException)e;
            throw new RuntimeException(e);
        }

    }
}
