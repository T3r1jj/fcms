package io.github.t3r1jj.fcms.backend.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Around("execution(* io.github.t3r1jj.fcms.backend.service.ReplicationService.*(..))")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Signature signature = proceedingJoinPoint.getSignature();
        Object[] arguments = proceedingJoinPoint.getArgs();
        logger.info("Entered {} with args: {}", signature.toString(), Arrays.toString(arguments));
        Object proceed = proceedingJoinPoint.proceed();
        logger.info("Successfully exited {} with args: {}", signature.toString(), Arrays.toString(arguments));
        return proceed;
    }
}
