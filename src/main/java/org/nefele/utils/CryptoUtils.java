/*
 * The MIT License
 *
 * Copyright (c) 2020 Nefele <https://github.com/nefele-org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.nefele.utils;

import org.nefele.Application;

import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;

import static java.util.Objects.requireNonNull;

public final class CryptoUtils {

    private final static String DEFAULT_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static SecretKey secretKey = null;


    public static void initialize() {

        if(secretKey != null)
            return;


        final var algorithm = Application.getInstance().getConfig()
                .getString("core.mfs.encryption.algorithm")
                .orElse(DEFAULT_ALGORITHM);


        Application.log(CryptoUtils.class, "Loading KeyStore with '%s' Algorithm", algorithm);

        try {

            final var keyPath = Application.getInstance().getDataPath().resolve("secret.key");
            final var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final var imNotAPasswordForKeyStore = "36ea1c24-6539-4184-9c17-210978f54b99".toCharArray();

            if(Files.notExists(keyPath))
                keyStore.load(null, imNotAPasswordForKeyStore);
            else
                keyStore.load(Files.newInputStream(keyPath), imNotAPasswordForKeyStore);


            if(keyStore.containsAlias("nefele-secret-key"))
                secretKey = ((KeyStore.SecretKeyEntry) keyStore
                        .getEntry("nefele-secret-key", new KeyStore.PasswordProtection(imNotAPasswordForKeyStore)))
                        .getSecretKey();

            else
                secretKey = KeyGenerator.getInstance(algorithm.split("/")[0]).generateKey();



            keyStore.setEntry("nefele-secret-key",
                    new KeyStore.SecretKeyEntry(secretKey),
                    new KeyStore.PasswordProtection(imNotAPasswordForKeyStore));

            keyStore.store(Files.newOutputStream(keyPath), imNotAPasswordForKeyStore);


        } catch (NoSuchAlgorithmException | IOException | CertificateException | KeyStoreException | UnrecoverableEntryException e) {
            Application.panic(CryptoUtils.class, e);
        }


    }

    public static ByteBuffer encrypt(ByteBuffer byteBuffer) {

        requireNonNull(secretKey, "CryptoUtils not initialized!");


        if(!byteBuffer.hasRemaining())
            return byteBuffer;

        try {

            Cipher cipher = Cipher.getInstance(Application.getInstance().getConfig()
                    .getString("core.mfs.encryption.algorithm")
                    .orElse(DEFAULT_ALGORITHM));

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);


            ByteBuffer outputBuffer = ByteBuffer
                    .allocateDirect(cipher.getOutputSize(byteBuffer.remaining()));

            cipher.doFinal(byteBuffer, outputBuffer);
            return outputBuffer.flip();

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | ShortBufferException | InvalidKeyException e) {
            Application.panic(CryptoUtils.class, e);
        }


        throw new IllegalStateException();

    }

    public static InputStream decrypt(InputStream inputStream) {

        requireNonNull(secretKey, "CryptoUtils not initialized!");


        try {

            if(inputStream.available() == 0)
                return inputStream;


            Cipher cipher = Cipher.getInstance(Application.getInstance().getConfig()
                .getString("core.mfs.encryption.algorithm")
                .orElse(DEFAULT_ALGORITHM));

            cipher.init(Cipher.DECRYPT_MODE, secretKey);


            ByteArrayInputStream stream;

            try(var cipherInputStream = new CipherInputStream(inputStream, cipher)) {
                stream = new ByteArrayInputStream(cipherInputStream.readAllBytes());
            }

            return stream;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e) {
            Application.panic(CryptoUtils.class, e);
        }


        throw new IllegalStateException();

    }

}
