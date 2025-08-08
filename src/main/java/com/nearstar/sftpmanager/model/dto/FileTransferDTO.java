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
package com.nearstar.sftpmanager.model.dto;

import java.time.LocalDateTime;

public class FileTransferDTO
{
    private Long id;
    private String fileName;
    private String sourcePath;
    private String destinationPath;
    private long fileSize;
    private long bytesTransferred;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private double progressPercentage;

    // Constructors
    public FileTransferDTO()
    {
    }

    public FileTransferDTO( String fileName, String sourcePath, String destinationPath, long fileSize )
    {
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.fileSize = fileSize;
        this.startTime = LocalDateTime.now();
        this.status = "IN_PROGRESS";
    }

    // Getters and Setters
    public Long getId()
    {
        return id;
    }

    public void setId( Long id )
    {
        this.id = id;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName( String fileName )
    {
        this.fileName = fileName;
    }

    public String getSourcePath()
    {
        return sourcePath;
    }

    public void setSourcePath( String sourcePath )
    {
        this.sourcePath = sourcePath;
    }

    public String getDestinationPath()
    {
        return destinationPath;
    }

    public void setDestinationPath( String destinationPath )
    {
        this.destinationPath = destinationPath;
    }

    public long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize( long fileSize )
    {
        this.fileSize = fileSize;
    }

    public long getBytesTransferred()
    {
        return bytesTransferred;
    }

    public void setBytesTransferred( long bytesTransferred )
    {
        this.bytesTransferred = bytesTransferred;
        this.progressPercentage = fileSize > 0 ? (bytesTransferred * 100.0 / fileSize) : 0;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus( String status )
    {
        this.status = status;
    }

    public LocalDateTime getStartTime()
    {
        return startTime;
    }

    public void setStartTime( LocalDateTime startTime )
    {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime()
    {
        return endTime;
    }

    public void setEndTime( LocalDateTime endTime )
    {
        this.endTime = endTime;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
    }

    public double getProgressPercentage()
    {
        return progressPercentage;
    }

    public void setProgressPercentage( double progressPercentage )
    {
        this.progressPercentage = progressPercentage;
    }
}
