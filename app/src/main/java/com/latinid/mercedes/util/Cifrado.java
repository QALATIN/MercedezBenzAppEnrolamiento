package com.latinid.mercedes.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Cifrado {
    public static final String TAG = "YourAppName";

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";

    private static Cipher _cipher;
    private static SecretKey _password;
    private static IvParameterSpec _IVParamSpec;

    public static void en(){
        try
        {
            _password = new SecretKeySpec("zc$eb8p@pCmSe5!378pUK@G@3wXVfz2F".getBytes(), ALGORITHM);
            _cipher = Cipher.getInstance(TRANSFORMATION);
            _IVParamSpec = new IvParameterSpec(".TNuO0uT&-sh}@Vw".getBytes());//.TNuO0uT&–sh}@Vw
        }//.TNuO0uT&-sh}@Vw
        catch (NoSuchAlgorithmException e)
        {
            //Log.e(TAG, "No such algorithm " + ALGORITHM, e);
        }
        catch (NoSuchPaddingException e)
        {
            //Log.e(TAG, "No such padding PKCS7", e);
        }
    }


    public static String encrypt(byte[] text)
    {
        en();
        byte[] encryptedData;
        try
        {
            _cipher.init(Cipher.ENCRYPT_MODE, _password, _IVParamSpec);
            encryptedData = _cipher.doFinal(text);

        } catch (InvalidKeyException e) {
            //     Log.e(TAG, "Invalid key  (invalid encoding, wrong length, uninitialized, etc).", e);
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            //   Log.e(TAG, "Invalid or inappropriate algorithm parameters for " + ALGORITHM, e);
            return null;
        } catch (IllegalBlockSizeException e) {
            //   Log.e(TAG, "The length of data provided to a block cipher is incorrect", e);
            return null;
        } catch (BadPaddingException e) {
            //   Log.e(TAG, "The input data but the data is not padded properly.", e);
            return null;
        }

        return Base64.getEncoder().encodeToString(encryptedData);

    }

    public static String decrypt(String text) {
        en();
        try {
            _cipher.init(Cipher.DECRYPT_MODE, _password, _IVParamSpec);

            byte[] decodedValue = new byte[0];

            decodedValue = Base64.getDecoder().decode(text.getBytes());

            byte[] decryptedVal = _cipher.doFinal(decodedValue);
            return new String(decryptedVal);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();

            return null;
        }
        // Log.e(TAG, "Invalid or inappropriate algorithm parameters for " + ALGORITHM, e);
        // Log.e(TAG, "The length of data provided to a block cipher is incorrect", e);
        //  Log.e(TAG, "The input data but the data is not padded properly.", e);

    }


}
