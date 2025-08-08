/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright © 2025 NearStar Incorporated. All rights reserved.
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
package com.nearstar.sftpmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer
{

    /**
     * CORS configuration - Fixed to use allowedOriginPatterns
     */
    @Override
    public void addCorsMappings( CorsRegistry registry )
    {
        registry.addMapping( "/api/**" )
                .allowedOriginPatterns( "*" )  // Use patterns instead of origins with credentials
                .allowedMethods( "GET", "POST", "PUT", "DELETE", "OPTIONS" )
                .allowedHeaders( "*" )
                .allowCredentials( true )
                .maxAge( 3600 );
    }

    /**
     * CORS filter bean - Fixed configuration
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration()
    {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials( true );
        // Use allowedOriginPatterns instead of allowedOrigins when credentials are true
        config.setAllowedOriginPatterns( List.of( "*" ) );
        config.setAllowedHeaders( List.of( "*" ) );
        config.setAllowedMethods( Arrays.asList( "GET", "POST", "PUT", "DELETE", "OPTIONS" ) );
        config.setExposedHeaders( Arrays.asList( "Authorization", "Content-Type", "X-Total-Count" ) );
        config.setMaxAge( 3600L );
        source.registerCorsConfiguration( "/**", config );
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>( new CorsFilter( source ) );
        bean.setOrder( Ordered.HIGHEST_PRECEDENCE );
        return bean;
    }

    /**
     * Static resource handling
     */
    @Override
    public void addResourceHandlers( ResourceHandlerRegistry registry )
    {
        registry.addResourceHandler( "/static/**" )
                .addResourceLocations( "classpath:/static/" )
                .setCacheControl( CacheControl.maxAge( 365, TimeUnit.DAYS ) )
                .resourceChain( true );

        registry.addResourceHandler( "/webjars/**" )
                .addResourceLocations( "classpath:/META-INF/resources/webjars/" )
                .setCacheControl( CacheControl.maxAge( 365, TimeUnit.DAYS ) );

        registry.addResourceHandler( "/uploads/**" )
                .addResourceLocations( "file:uploads/" )
                .setCacheControl( CacheControl.maxAge( 7, TimeUnit.DAYS ) );
    }

    /**
     * View controllers for static pages
     */
    @Override
    public void addViewControllers( ViewControllerRegistry registry )
    {
        registry.addViewController( "/" ).setViewName( "forward:/index.html" );
        registry.addViewController( "/login" ).setViewName( "forward:/login.html" );
        registry.addViewController( "/admin" ).setViewName( "forward:/admin.html" );
        registry.addViewController( "/file-manager" ).setViewName( "forward:/file-manager.html" );
        registry.addViewController( "/scheduler" ).setViewName( "forward:/scheduler.html" );
        registry.addViewController( "/error/404" ).setViewName( "forward:/error/404.html" );
        registry.addViewController( "/error/500" ).setViewName( "forward:/error/500.html" );
    }

    /**
     * Message converters configuration
     */
    @Override
    public void configureMessageConverters( List<HttpMessageConverter<?>> converters )
    {
        converters.add( jsonMessageConverter() );
    }

    /**
     * Jackson configuration
     */
    @Bean
    public MappingJackson2HttpMessageConverter jsonMessageConverter()
    {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper( objectMapper() );
        return converter;
    }

    @Bean
    public ObjectMapper objectMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule( new JavaTimeModule() );
        mapper.disable( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );
        mapper.enable( SerializationFeature.INDENT_OUTPUT );
        return mapper;
    }

    /**
     * Multipart resolver for file uploads
     */
    @Bean
    public MultipartResolver multipartResolver()
    {
        return new StandardServletMultipartResolver();
    }

    /**
     * View resolver
     */
    @Bean
    public InternalResourceViewResolver viewResolver()
    {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix( "/templates/" );
        resolver.setSuffix( ".html" );
        return resolver;
    }

    /**
     * Path matching configuration
     */
    @Override
    public void configurePathMatch( PathMatchConfigurer configurer )
    {
        configurer.setUseTrailingSlashMatch( false );
        configurer.setUseSuffixPatternMatch( false );
    }

    /**
     * Content negotiation
     */
    @Override
    public void configureContentNegotiation( ContentNegotiationConfigurer configurer )
    {
        configurer
                .favorParameter( false )
                .ignoreAcceptHeader( false )
                .defaultContentType( MediaType.APPLICATION_JSON )
                .mediaType( "json", MediaType.APPLICATION_JSON )
                .mediaType( "xml", MediaType.APPLICATION_XML );
    }
}