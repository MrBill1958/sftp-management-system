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

import java.io.InputStream;
import java.security.MessageDigest;

public class FileChecksum
{

    public static String calculateMD5( InputStream inputStream ) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance( "MD5" );
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read( buffer )) > 0)
        {
            md.update( buffer, 0, read );
        }
        byte[] md5sum = md.digest();
        StringBuilder result = new StringBuilder();
        for (byte b : md5sum)
        {
            result.append( String.format( "%02x", b ) );
        }
        return result.toString();
    }

    public static String calculateSHA256( InputStream inputStream ) throws Exception
    {
        MessageDigest md = MessageDigest.getInstance( "SHA-256" );
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read( buffer )) > 0)
        {
            md.update( buffer, 0, read );
        }
        byte[] sha256sum = md.digest();
        StringBuilder result = new StringBuilder();
        for (byte b : sha256sum)
        {
            result.append( String.format( "%02x", b ) );
        }
        return result.toString();
    }
}