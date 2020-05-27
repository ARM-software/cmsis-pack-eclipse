/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Default implementation of encryption
 */
public class Encryptor {

	public static final String DEFAULT_KEY = "OPEN_SOURCE_SECRET_KEY"; //$NON-NLS-1$
	private static final Charset CHARSET = StandardCharsets.ISO_8859_1;
	private static final String ALGORITHM = "AES"; //$NON-NLS-1$

	private SecretKeySpec secretKeySpec = null;
	private static Map<String, Encryptor> encryptors = new HashMap<>();

	/**
	 * Get the encryptor using the secret key
	 * @param secretKey the key
	 * @return an encryptor instance that uses the secret key
	 */
	public static Encryptor getEncryptor(String secretKey) {
		if (encryptors.containsKey(secretKey)) {
			return encryptors.get(secretKey);
		}
		encryptors.put(secretKey, new Encryptor(secretKey));
		return encryptors.get(secretKey);
	}

	/**
	 * Constructor for the encryptor
	 * @param secretKey
	 */
	protected Encryptor(String secretKey) {
		try {
			byte[] key = secretKey.getBytes(CHARSET);
		    MessageDigest sha = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		    key = sha.digest(key);
		    key = Arrays.copyOf(key, 16); // use only first 128 bit

		    // Generate the secret key specs.
		    secretKeySpec = new SecretKeySpec(key, ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public String encrypt(String input) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
		    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] encrypt = cipher.doFinal(input.getBytes(CHARSET));
			return new String(encrypt, CHARSET);
		} catch (InvalidKeyException | NoSuchPaddingException |
				IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return input;
		}
	}

	public String decrypt(String input) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] decrypt = cipher.doFinal(input.getBytes(CHARSET));
			return new String(decrypt, CHARSET);
		} catch (InvalidKeyException | NoSuchPaddingException |
				IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return input;
		}
	}

}
