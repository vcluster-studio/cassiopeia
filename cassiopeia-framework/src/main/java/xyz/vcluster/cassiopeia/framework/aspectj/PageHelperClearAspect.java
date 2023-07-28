package xyz.vcluster.cassiopeia.framework.aspectj;

import com.github.pagehelper.PageHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

/**
 * PageHelper Clear处理
 *
 * @author cassiopeia
 */
@Aspect
@Component
public class PageHelperClearAspect {

    private static final Logger logger = LoggerFactory.getLogger(PageHelperClearAspect.class);

    @Before("within(xyz.vcluster.cassiopeia.common.core.controller.BaseController+)")
    public void doBefore(JoinPoint point) throws Throwable {
        if (point.getSignature() instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            Method method = methodSignature.getMethod();
            RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
            if (requestMapping != null) {
                PageHelper.clearPage();
                logger.debug("done page helper clear");
            }
        }
    }
}
