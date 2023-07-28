package xyz.vcluster.cassiopeia.framework.aspectj;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import xyz.vcluster.cassiopeia.common.annotation.DataCreateOperateRecord;
import xyz.vcluster.cassiopeia.common.annotation.DataOperateRecord;
import xyz.vcluster.cassiopeia.common.annotation.DataUpdateOperateRecord;
import xyz.vcluster.cassiopeia.common.core.domain.BaseEntity;
import xyz.vcluster.cassiopeia.common.core.domain.entity.SysUser;
import xyz.vcluster.cassiopeia.common.core.domain.model.LoginUser;
import xyz.vcluster.cassiopeia.common.exception.ServiceException;
import xyz.vcluster.cassiopeia.common.utils.SecurityUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 数据操作记录处理
 *
 * @author cassiopeia
 */
@Aspect
@Component
public class DataOperateRecordAspect {

    @Bean
    public Advisor dataOperateRecordAdvisor() {
        Pointcut pointcut = new AnnotationMatchingPointcut(DataOperateRecord.class, true);
        Advice advice = new MethodAroundAdvice();

        return new DefaultPointcutAdvisor(pointcut, advice);
    }

    private static class MethodAroundAdvice implements MethodBeforeAdvice, AfterReturningAdvice {

        protected final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void before(Method method, Object[] args, Object target) throws Throwable {
            logger.info("before {} called", method.getName());

            if (args.length > 0) {
                Object params = args[0];
                if (StringUtils.isNotNull(params) && params instanceof BaseEntity) {
                    BaseEntity baseEntity = (BaseEntity) params;
                    Date currentTime = new Date();

                    Annotation dataCreateOperateRecord = method.getAnnotation(DataCreateOperateRecord.class);
                    Annotation dataUpdateOperateRecord = method.getAnnotation(DataUpdateOperateRecord.class);
                    if (dataCreateOperateRecord instanceof DataCreateOperateRecord
                            || dataUpdateOperateRecord instanceof DataUpdateOperateRecord) {
                        if (dataCreateOperateRecord instanceof DataCreateOperateRecord) {
                            baseEntity.setCreateTime(currentTime);
                        } else {
                            baseEntity.setCreateTime(null);
                        }
                        baseEntity.setUpdateTime(currentTime);

                        String userName = null;
                        LoginUser loginUser = null;
                        try {
                            loginUser = SecurityUtils.getLoginUser();
                        } catch (ServiceException e) {
                            logger.info("unauthorized");
                        }
                        if (StringUtils.isNotNull(loginUser)) {
                            SysUser currentUser = loginUser.getUser();
                            if (StringUtils.isNotNull(currentUser)) {
                                userName = currentUser.getUserName();
                            }
                        }
                        if (dataCreateOperateRecord instanceof DataCreateOperateRecord) {
                            baseEntity.setCreateBy(userName);
                        } else {
                            baseEntity.setCreateBy(null);
                        }
                        baseEntity.setUpdateBy(userName);
                    }
                }
            }
        }

        @Override
        public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
            logger.info("after {} called", method.getName());
        }
    }
}
