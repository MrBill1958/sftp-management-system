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
package com.nearstar.sftpmanager.model.enums;

public enum DayOfWeek
{
    MON( "Monday", 1 ),
    TUE( "Tuesday", 2 ),
    WED( "Wednesday", 3 ),
    THU( "Thursday", 4 ),
    FRI( "Friday", 5 ),
    SAT( "Saturday", 6 ),
    SUN( "Sunday", 7 );

    private final String displayName;
    private final int dayNumber;

    DayOfWeek( String displayName, int dayNumber )
    {
        this.displayName = displayName;
        this.dayNumber = dayNumber;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public int getDayNumber()
    {
        return dayNumber;
    }

    public static DayOfWeek fromDayNumber( int dayNumber )
    {
        for (DayOfWeek day : values())
        {
            if ( day.dayNumber == dayNumber )
            {
                return day;
            }
        }
        throw new IllegalArgumentException( "Invalid day number: " + dayNumber );
    }
}