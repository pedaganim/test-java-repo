package com.myorg.myapp.config.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeTakenAspect {

  private static final Logger log = LoggerFactory.getLogger(TimeTakenAspect.class);

  @Around(
      "@annotation(com.myorg.myapp.config.aop.TimeTaken) || @within(com.myorg.myapp.config.aop.TimeTaken)")
  public Object aroundTimed(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    Throwable thrown = null;
    try {
      return pjp.proceed();
    } catch (Throwable t) {
      thrown = t;
      throw t;
    } finally {
      long durationNs = System.nanoTime() - start;
      MethodSignature sig = (MethodSignature) pjp.getSignature();
      String method = sig.getDeclaringType().getSimpleName() + "." + sig.getMethod().getName();
      String label = extractLabel(sig);
      String msg =
          String.format(
              "time_taken method=%s label=%s duration_ms=%.3f",
              method, label, durationNs / 1_000_000.0);
      if (thrown == null) {
        log.info(msg);
      } else {
        log.warn(msg + " error_class=" + thrown.getClass().getSimpleName());
      }
    }
  }

  private String extractLabel(MethodSignature sig) {
    TimeTaken ann = sig.getMethod().getAnnotation(TimeTaken.class);
    if (ann != null && !ann.value().isBlank()) {
      return ann.value();
    }
    Class<?> declaring = sig.getDeclaringType();
    TimeTaken classAnn = declaring.getAnnotation(TimeTaken.class);
    if (classAnn != null && !classAnn.value().isBlank()) {
      return classAnn.value();
    }
    return "";
  }
}
