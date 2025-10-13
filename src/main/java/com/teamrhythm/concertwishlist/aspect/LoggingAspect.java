package com.teamrhythm.concertwishlist.aspect;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging method execution across the application.
 * Uses AOP to intercept method calls and log entry, exit, and exceptions.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Pointcut for all methods in controller classes
     */
    @Pointcut("within(com.teamrhythm.concertwishlist.controller..*)")
    public void controllerMethods() {}

    /**
     * Pointcut for all methods in service classes
     */
    @Pointcut("within(com.teamrhythm.concertwishlist.service..*)")
    public void serviceMethods() {}

    /**
     * Pointcut for all methods in repository classes
     */
    @Pointcut("within(com.teamrhythm.concertwishlist.repository..*)")
    public void repositoryMethods() {}

    /**
     * Pointcut for all methods in security classes
     */
    @Pointcut("within(com.teamrhythm.concertwishlist.security..*)")
    public void securityMethods() {}

    /**
     * Combined pointcut for all application methods
     */
    @Pointcut("controllerMethods() || serviceMethods() || repositoryMethods() || securityMethods()")
    public void applicationMethods() {}

    /**
     * Around advice for logging method entry and exit
     */
    @Around("applicationMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        // Determine log level based on package
        boolean isDebugLevel = isDebugLevel(className);
        
        // Log method entry
        if (isDebugLevel) {
            if (log.isDebugEnabled()) {
                log.debug("[{}] [ENTRY] {}.{}() - Arguments: {}", 
                    timestamp, className, methodName, 
                    formatArguments(joinPoint.getArgs()));
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("[{}] [ENTRY] {}.{}()", 
                    timestamp, className, methodName);
            }
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        
        try {
            // Proceed with method execution
            result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log method exit
            if (isDebugLevel) {
                if (log.isDebugEnabled()) {
                    log.debug("[{}] [EXIT] {}.{}() - Execution time: {}ms - Return value: {}", 
                        timestamp, className, methodName, executionTime, 
                        formatReturnValue(result));
                }
            } else {
                if (log.isInfoEnabled()) {
                    log.info("[{}] [EXIT] {}.{}() - Execution time: {}ms", 
                        timestamp, className, methodName, executionTime);
                }
            }
            
            return result;
            
        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Exception logging is handled by @AfterThrowing advice
            // Just log the execution time here
            if (log.isErrorEnabled()) {
                log.error("[{}] [EXCEPTION] {}.{}() - Execution time before exception: {}ms", 
                    timestamp, className, methodName, executionTime);
            }
            
            throw ex;
        }
    }

    /**
     * After throwing advice for logging exceptions
     */
    @AfterThrowing(pointcut = "applicationMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        Logger log = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType());
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        if (log.isErrorEnabled()) {
            log.error("[{}] [EXCEPTION] {}.{}() - Exception type: {} - Message: {} - Arguments: {}", 
                timestamp, className, methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                formatArguments(joinPoint.getArgs()),
                exception);
        }
    }

    /**
     * Determines if debug level logging should be used based on class name
     */
    private boolean isDebugLevel(String className) {
        return className.contains(".repository.") || 
               className.contains(".security.");
    }

    /**
     * Formats method arguments for logging
     */
    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "none";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatArgument(args[i]));
        }
        return sb.toString();
    }

    /**
     * Formats a single argument, handling sensitive data
     */
    private String formatArgument(Object arg) {
        if (arg == null) {
            return "null";
        }
        
        String argString = arg.toString();
        String argType = arg.getClass().getSimpleName();
        
        // Mask sensitive data
        if (isSensitiveType(argType) || isSensitiveString(argString)) {
            return "[" + argType + ":***MASKED***]";
        }
        
        // Limit string length
        if (argString.length() > 100) {
            return "[" + argType + ":" + argString.substring(0, 97) + "...]";
        }
        
        return "[" + argType + ":" + argString + "]";
    }

    /**
     * Formats return value for logging
     */
    private String formatReturnValue(Object returnValue) {
        if (returnValue == null) {
            return "null";
        }
        
        String returnType = returnValue.getClass().getSimpleName();
        
        // Don't log full content of collections
        if (returnValue instanceof java.util.Collection) {
            return "[" + returnType + ":size=" + 
                ((java.util.Collection<?>) returnValue).size() + "]";
        }
        
        // Don't log full content of arrays
        if (returnValue.getClass().isArray()) {
            return "[" + returnType + ":length=" + 
                java.lang.reflect.Array.getLength(returnValue) + "]";
        }
        
        String returnString = returnValue.toString();
        if (returnString.length() > 100) {
            return "[" + returnType + ":" + returnString.substring(0, 97) + "...]";
        }
        
        return "[" + returnType + ":" + returnString + "]";
    }

    /**
     * Checks if a type name indicates sensitive data
     */
    private boolean isSensitiveType(String typeName) {
        return typeName.toLowerCase().contains("password") ||
               typeName.toLowerCase().contains("credential") ||
               typeName.toLowerCase().contains("secret") ||
               typeName.toLowerCase().contains("token");
    }

    /**
     * Checks if a string value contains sensitive data
     */
    private boolean isSensitiveString(String value) {
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("password") ||
               lowerValue.contains("secret") ||
               lowerValue.contains("token") ||
               lowerValue.contains("credential");
    }
}