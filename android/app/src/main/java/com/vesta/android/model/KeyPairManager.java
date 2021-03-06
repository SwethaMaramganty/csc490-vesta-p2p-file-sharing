package com.vesta.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class responsible for the creation and management of Public/Private key-pairs
 *
 *
 * [IMPORTANT]
 * We never expose public keys to anything expect for this application, ONLY the methods that require the private-key will have access to it (only when needed)
 * [END IMPORTANT]
 *
 * References:
 * https://github.com/googlearchive/android-BasicAndroidKeyStore/blob/master/Application/src/main/java/com/example/android/basicandroidkeystore/BasicAndroidKeyStoreFragment.java
 */
public class KeyPairManager {

    public static final String KEY_STORE_PROVIDER_NAME = "AndroidKeyStore";
    public static final String LOG_TAG = KeyPair.class.getSimpleName();

    public static final int RSA_KEY_SIZE = 1024;
    private static PublicKey publicKeyObject;

    public static KeyStore keyStore;
    public Context context;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor sharedPrefEditor;

    public static final String DEFAULT_VALUE_KEY_DOES_NOT_EXIST = "KeyDoesNotExist";
    public static final String PUBLIC_KEY = "PublicKey";

    public KeyPairManager(Context context) {
        this.context = context;
    }

    /**
     * Generates a Public/Private key-pair that can be used for encryption and decryption, or returns the KeyPair if it already exists in the KeyStore
     * @param keyPairAlias String, the name of the key-pair that will be saved in the KeyStore
     * @return KeyPair, returns the KeyPair that was created which contains the PublicKey and PrivateKey
     */
    public KeyPair generateRsaEncryptionKeyPair(final String keyPairAlias) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, UnrecoverableEntryException, InvalidAlgorithmParameterException {
        if (keyPairAlias == null || keyPairAlias.length() == 0) {
            throw new IllegalArgumentException(String.format("%s[%s]: %s", KeyPair.class.getSimpleName(), "generateRsaEncryptionKeyPair()", "Illegal argument String:keyPairAlias"));
        }

        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER_NAME);
        keyStore.load(null);

        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);

        kpGenerator.initialize(new KeyGenParameterSpec.Builder(
                keyPairAlias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setKeySize(RSA_KEY_SIZE)
                .setKeySize(512)
                .build());

        return kpGenerator.generateKeyPair();
    }


    /**
     * Returns the the Base64 encoding of the RSA Key
     * @param rsaKey Key, the Key which will be converted to a Base64 String
     * @return String, the Base64 encoding of the Key
     */
    public static String convertRsaKeyToBase64String(Key rsaKey) {
        return Base64.encodeToString(rsaKey.getEncoded(), Base64.NO_PADDING);
    }


    /**
     * Stores the public key object in the shared preferences
     * @param sharedPrefName
     * @param context, which is a subclass of context
     * @param publicKey
     */
    public static void storePublicKeySharedPref(String sharedPrefName, Context context, String publicKey) {

        //Convert the string public key to public key object
        try {
            publicKeyObject = KeyPairManager.convertBase64StringToPublicKey(publicKey);
            System.out.println("MAAA PUB KEY OBJ " + publicKeyObject);
            Log.i("PublicKeyObject", publicKeyObject.toString());
            sharedPreferences =  context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
            sharedPrefEditor = sharedPreferences.edit();

            //Log.i("PublicKeyObject", publicKeyObject.toString());
            System.out.println("MAAA PUB KEY OBJ** " + publicKeyObject);
            //Storing the object representation, used toString() to bypass error
            sharedPrefEditor.putString(PUBLIC_KEY, publicKeyObject.toString());

            sharedPrefEditor.apply();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

    }


    /**
     * Retrieves the PublicKey from the specified SharePreferences file
     *
     * @param sharedPrefsFileName String, the name of the SharePrefs file that you want to retrieve the PublicKey from
     * @param context Context, the context objec that SharedPrefs will need to gain access to global application data/services
     * @return String, the string representation of the PublicKey requested, or the default value if the PublicKey string does not exist
     */
    public static String retrievePublicKeySharedPrefsFile(String sharedPrefsFileName, Context context) {

        Log.i("Pub_Key_Shared_Pref",
                context.getSharedPreferences(sharedPrefsFileName, Context.MODE_PRIVATE)
                        .getString(PUBLIC_KEY, DEFAULT_VALUE_KEY_DOES_NOT_EXIST));
        try {
            Log.i("PubKeyFromKeyStore ", getKeyPairFromKeystore("userKeys").getPublic().toString());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        return context.getSharedPreferences(sharedPrefsFileName, Context.MODE_PRIVATE).getString(PUBLIC_KEY, DEFAULT_VALUE_KEY_DOES_NOT_EXIST);
    }


    /**
     * Removes PublicKey stored in SharedPreferences
     */
    public static void removePublicKeySharedPref() {
        sharedPrefEditor = sharedPreferences.edit();

        if (!sharedPreferences.getString(PUBLIC_KEY, DEFAULT_VALUE_KEY_DOES_NOT_EXIST).equals(DEFAULT_VALUE_KEY_DOES_NOT_EXIST)) {
            sharedPrefEditor.remove(PUBLIC_KEY);
            sharedPrefEditor.apply();
        }
    }


    /**
     * Returns the KeyPair contained within the KeyStore for the given alias
     * @param keyPairAlias String, the alias of the KeyPair t hat the PublicKey is stored within
     * @return KeyPair, the KeyPair associated with the alias
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws UnrecoverableEntryException
     *
     * References:
     * https://stackoverflow.com/questions/42110123/save-and-retrieve-keypair-in-androidkeystore?rq=1
     *
     * Known Bugs:
     *
     * Retrieving KeyPair throws a android.os.ServiceSpecificException (code 7) Exception: https://stackoverflow.com/questions/52024752/android-9-keystore-exception-android-os-servicespecificexception
     */
    public static KeyPair getKeyPairFromKeystore(String keyPairAlias) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException {

        if (keyPairAlias == null || keyPairAlias.length() == 0) {
            throw new IllegalArgumentException(String.format("%s[%s]: %s", KeyPair.class.getSimpleName(), "getKeyPairFromKeyStore()", "Illegal argument String:keyPairAlias"));
        }

        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER_NAME);
        keyStore.load(null);

        System.out.println("ALIASSSESSSS");
        for (Enumeration<String> e = keyStore.aliases(); e.hasMoreElements();) {
            System.out.println(e.nextElement());
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyPairAlias, null);
        PublicKey publicKey = keyStore.getCertificate(keyPairAlias).getPublicKey();

        return new KeyPair(publicKey, privateKey);
    }


    /**
     * Converts a Base64 encoding of a PublicKey to a PublicKey object that can be used to encrypt data
     * @param base64EncodedPublicKey String, the Base64 intepretation of a PublicKey that will be converted to a PublicKey
     * @return PublicKey, the PublicKey object representing the base64EncodedPublicKey string argument passed in
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     *
     * References:
     * https://stackoverflow.com/questions/45754277/how-to-generate-publickey-from-string-java
     */
    public static PublicKey convertBase64StringToPublicKey(String base64EncodedPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Log.i("Converting Pub Key ", base64EncodedPublicKey);
        byte[] encodedPublicKey = Base64.decode(base64EncodedPublicKey, Base64.DEFAULT);
        return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA).generatePublic(new X509EncodedKeySpec(encodedPublicKey));
    }


    /**
     * Deletes the keys associated with the keyPairAlias from the KeyStore
     * @param keyPairAlias String, the alia of the KeyPair that is going to be deleted from the KeyStore
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
    public static void deleteKeysFromKeystore(String keyPairAlias) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        if (keyPairAlias == null || keyPairAlias.length() == 0) {
            throw new IllegalArgumentException(String.format("%s[%s]: %s", KeyPair.class.getSimpleName(), "getKeyPairFromKeyStore()", "Illegal argument String:keyPairAlias"));
        }

        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER_NAME);
        keyStore.load(null);

        keyStore.deleteEntry(keyPairAlias);
    }


    /**
     * Encrypts data using the PublicKey associated with the keyPairAlias given
     * @param keyPairAlias String, the alias of the KeyPair that will be used to encrypt the data given
     * @param stringToEncrypt String, the data that needs to be encrypted
     * @return String, dataToEncrypt encrypted using the keyPairAlias' PrivateKey
     */
    public static String encrypt(String keyPairAlias, String stringToEncrypt) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, UnrecoverableEntryException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException {

        if (stringToEncrypt == null || stringToEncrypt.trim().length() == 0) {
            throw new IllegalArgumentException(String.format("%s[%s]: %s", KeyPair.class.getSimpleName(), "encrypt()", "Illegal argument String:dataToEncrypt"));
        }

        keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER_NAME);
        keyStore.load(null);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround"); //or try with "RSA"
        cipher.init(Cipher.ENCRYPT_MODE, getKeyPairFromKeystore(keyPairAlias).getPublic());
        byte[] encrypted = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    /**
     * Decrypts data using the PrivateKey associated with the keyPairAlias given
     * @param keyPairAlias String, the alias of the KeyPair that will be used to encrypt the data given
     * @param stringToDecrypt String, the encrypted data that needs to be decrypted
     * @return String, dataToEncrypt encrypted using the keyPairAlias' PrivateKey
     */
    public static String decrypt(String keyPairAlias, String stringToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, CertificateException, UnrecoverableEntryException, KeyStoreException, IOException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround");
        cipher.init(Cipher.DECRYPT_MODE, getKeyPairFromKeystore(keyPairAlias).getPrivate());
        byte[] cipherText = cipher.doFinal(Base64.decode(stringToDecrypt, Base64.DEFAULT));
        return new String(cipherText);
    }
}