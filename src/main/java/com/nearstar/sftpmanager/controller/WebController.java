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
package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.dto.UserSession;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController
{

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index( HttpSession session )
    {
        UserSession user = (UserSession) session.getAttribute( "user" );
        if ( user == null )
        {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login( HttpSession session )
    {
        UserSession user = (UserSession) session.getAttribute( "user" );
        if ( user != null )
        {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit( @RequestParam(required = false) String username,
                               @RequestParam(required = false) String password,
                               HttpSession session,
                               HttpServletRequest request,
                               Model model )
    {

        System.out.println( "=== LOGIN DEBUG ===" );
        System.out.println( "Username parameter: '" + username + "'" );
        System.out.println( "Password parameter: '" + (password != null ? "[PROVIDED]" : "null") + "'" );
        System.out.println( "All parameters: " + request.getParameterMap().keySet() );
        System.out.println( "Content type: " + request.getContentType() );
        System.out.println( "Method: " + request.getMethod() );

        // Check if parameters are missing
        if ( username == null || username.trim().isEmpty() )
        {
            System.out.println( "ERROR: Username is missing or empty" );
            model.addAttribute( "error", "Username is required" );
            return "login";
        }

        if ( password == null || password.trim().isEmpty() )
        {
            System.out.println( "ERROR: Password is missing or empty" );
            model.addAttribute( "error", "Password is required" );
            return "login";
        }

        try
        {
            System.out.println( "Attempting authentication for user: " + username );

            // Authenticate user using your existing service method
            User user = userService.authenticate( username, password );
            System.out.println( "Authentication result: " + (user != null ? "SUCCESS" : "FAILED") );

            if ( user != null )
            {
                System.out.println( "User details - ID: " + user.getId() + ", Active: " + user.isActive() + ", Locked: " + user.isLocked() );

                if ( !user.isLocked() && user.isActive() && user.isEnabled() )
                {
                    // Use the existing createUserSession method from your service
                    UserSession userSession = userService.createUserSession( user );
                    System.out.println( "Created user session for: " + userSession.getUsername() );

                    // Store in session
                    session.setAttribute( "user", userSession );
                    System.out.println( "Stored user session, redirecting to dashboard" );

                    // Update last login
                    userService.updateLastLogin( user.getId() );

                    return "redirect:/dashboard";
                }
                else if ( user.isLocked() )
                {
                    System.out.println( "User is locked" );
                    model.addAttribute( "error", "Account is locked due to too many failed login attempts" );
                    return "login";
                }
                else if ( !user.isActive() )
                {
                    System.out.println( "User is not active" );
                    model.addAttribute( "error", "Account is disabled" );
                    return "login";
                }
                else if ( !user.isEnabled() )
                {
                    System.out.println( "User is not enabled" );
                    model.addAttribute( "error", "Account is not enabled" );
                    return "login";
                }
            }
            else
            {
                System.out.println( "Authentication failed - user is null" );
                model.addAttribute( "error", "Invalid username or password" );
                return "login";
            }
        }
        catch (Exception e)
        {
            System.out.println( "Login exception: " + e.getMessage() );
            e.printStackTrace();
            model.addAttribute( "error", "Login failed: " + e.getMessage() );
            return "login";
        }

        System.out.println( "Fallthrough - returning to login" );
        model.addAttribute( "error", "Login failed" );
        return "login";
    }

    @GetMapping("/logout")
    public String logout( HttpSession session )
    {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard( HttpSession session, Model model )
    {
        UserSession user = (UserSession) session.getAttribute( "user" );
        if ( user == null )
        {
            return "redirect:/login";
        }

        // Add user data to the model for the template
        model.addAttribute( "user", user );
        model.addAttribute( "isAdmin", user.isAdmin() );

        return "dashboard";
    }

    @GetMapping("/sites")
    public String sites( HttpSession session )
    {
        UserSession user = (UserSession) session.getAttribute( "user" );
        if ( user == null )
        {
            return "redirect:/login";
        }
        // Only admins can access site management
        if ( !"Administrator".equals( user.getRole() ) && !"ADMIN".equals( user.getRole() ) )
        {
            return "redirect:/dashboard";
        }
        return "sites";
    }

    @GetMapping("/file-manager")
    public String fileManager( HttpSession session )
    {
        UserSession user = (UserSession) session.getAttribute( "user" );
        if ( user == null )
        {
            return "redirect:/login";
        }
        return "file-manager";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword()
    {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit( @RequestParam String email, Model model )
    {
        // TODO: Implement password reset logic
        // For now, just show a message
        model.addAttribute( "message", "Password reset instructions have been sent to your email." );
        return "forgot-password";
    }

    @GetMapping("/contact-admin")
    public String contactAdmin()
    {
        return "contact-admin";
    }

    @PostMapping("/contact-admin")
    public String contactAdminSubmit( @RequestParam String name,
                                      @RequestParam String email,
                                      @RequestParam String message,
                                      Model model )
    {
        // TODO: Implement contact form logic (send email, save to database, etc.)
        // For now, just show a success message
        model.addAttribute( "success", "Your message has been sent to the administrator." );
        return "contact-admin";
    }
}