/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright � 2025 NearStar Incorporated. All rights reserved.
 * <p>
 * <p>
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF NEARSTAR Inc.
 * <p>
 * THIS COPYRIGHT NOTICE DOES NOT EVIDENCE ANY
 * ACTUAL OR INTENDED PUBLICATION OF SUCH SOURCE CODE.
 * This software and its source code are proprietary and confidential to NearStar Incorporated.
 * Unauthorized copying, modification, distribution, or use of this software, in whole or in part,
 * is strictly prohibited without the prior written consent of the copyright holder.
 * Portions of this software may utilize or be derived from open-source software
 * and publicly available frameworks licensed under their respective licenses.
 * <p>
 * This code may also include contributions developed with the assistance of AI-based tools.
 * All open-source dependencies are used in accordance with their applicable licenses,
 * and full attribution is maintained in the corresponding documentation (e.g., NOTICE or LICENSE files).
 * For inquiries regarding licensing or usage, please make request by going to nearstar.com.
 *
 * @file ${NAME}.java
 * @author ${USER} <${USER}@nearstar.com>
 * @version 1.0.0
 * @date ${DATE}
 * @project SFTP Site Management System
 * @package com.nearstar.sftpmanager
 * <p>
 * Copyright    ${YEAR} Nearstar
 * @license Proprietary
 * @modified
 */
package com.nearstar.sftpmanager.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class EncryptionUtil
{
    @Value("${encryption.secret.key:MySecretKey12345}")
    private String secretKey;

    private static final String ALGORITHM = "AES";

    public String encrypt( String plainText )
    {
        try
        {
            log.debug( "Encrypting password" );
            SecretKeySpec keySpec = new SecretKeySpec( padKey( secretKey ), ALGORITHM );
            Cipher cipher = Cipher.getInstance( ALGORITHM );
            cipher.init( Cipher.ENCRYPT_MODE, keySpec );
            byte[] encryptedBytes = cipher.doFinal( plainText.getBytes() );
            String encrypted = Base64.getEncoder().encodeToString( encryptedBytes );
            log.debug( "Password encrypted successfully" );
            return encrypted;
        }
        catch (Exception e)
        {
            log.error( "Error encrypting password", e );
            throw new RuntimeException( "Error encrypting password", e );
        }
    }

    public String decrypt( String encryptedText )
    {
        try
        {
            log.debug( "Decrypting password" );
            SecretKeySpec keySpec = new SecretKeySpec( padKey( secretKey ), ALGORITHM );
            Cipher cipher = Cipher.getInstance( ALGORITHM );
            cipher.init( Cipher.DECRYPT_MODE, keySpec );
            byte[] decryptedBytes = cipher.doFinal( Base64.getDecoder().decode( encryptedText ) );
            String decrypted = new String( decryptedBytes );
            log.debug( "Password decrypted successfully" );
            return decrypted;
        }
        catch (Exception e)
        {
            log.error( "Error decrypting password", e );
            throw new RuntimeException( "Error decrypting password", e );
        }
    }

    private byte[] padKey( String key )
    {
        byte[] keyBytes = new byte[16]; // AES-128
        byte[] parameterKeyBytes = key.getBytes();
        System.arraycopy( parameterKeyBytes, 0, keyBytes, 0, Math.min( parameterKeyBytes.length, keyBytes.length ) );
        return keyBytes;
    }
}
