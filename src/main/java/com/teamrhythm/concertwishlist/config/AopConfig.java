package com.teamrhythm.concertwishlist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration class to enable AspectJ auto-proxying for AOP logging.
 * This allows the LoggingAspect to intercept method calls across the application.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
    // No additional beans are needed; the @EnableAspectJAutoProxy annotation
    // activates the processing of Spring's @Aspect annotations.
}