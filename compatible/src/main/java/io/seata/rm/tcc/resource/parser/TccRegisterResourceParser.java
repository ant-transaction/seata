/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.seata.rm.tcc.resource.parser;

import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.apache.seata.common.exception.FrameworkException;
import org.apache.seata.common.util.ReflectionUtil;
import org.apache.seata.common.util.StringUtils;
import org.apache.seata.rm.DefaultResourceManager;
import org.apache.seata.rm.tcc.TCCResource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TccRegisterResourceParser extends org.apache.seata.rm.tcc.resource.parser.TccRegisterResourceParser {

    @Override
    public void registerResource(Object target, String beanName) {
        try {
            //service bean, registry resource
            Class<?> serviceClass = target.getClass();
            this.executeRegisterResource(target, new HashSet<>(Arrays.asList(serviceClass.getMethods())), target.getClass());
            Set<Class<?>> interfaceClasses = ReflectionUtil.getInterfaces(serviceClass);
            for (Class<?> interClass : interfaceClasses) {
                this.executeRegisterResource(target, new HashSet<>(Arrays.asList(interClass.getMethods())), interClass);
            }
        } catch (Throwable t) {
            throw new FrameworkException(t, "parser remoting service error");
        }
    }

    private void executeRegisterResource(Object target, Set<Method> methods, Class<?> targetServiceClass) throws NoSuchMethodException {
        for (Method m : methods) {
            TwoPhaseBusinessAction twoPhaseBusinessAction = m.getAnnotation(TwoPhaseBusinessAction.class);
            if (twoPhaseBusinessAction != null) {
                TCCResource tccResource = new TCCResource();
                if (StringUtils.isBlank(twoPhaseBusinessAction.name())) {
                    throw new FrameworkException("TCC bean name cannot be null or empty");
                }
                tccResource.setActionName(twoPhaseBusinessAction.name());
                tccResource.setTargetBean(target);
                tccResource.setPrepareMethod(m);
                tccResource.setCommitMethodName(twoPhaseBusinessAction.commitMethod());
                tccResource.setCommitMethod(targetServiceClass.getMethod(twoPhaseBusinessAction.commitMethod(),
                        twoPhaseBusinessAction.commitArgsClasses()));
                tccResource.setRollbackMethodName(twoPhaseBusinessAction.rollbackMethod());
                tccResource.setRollbackMethod(targetServiceClass.getMethod(twoPhaseBusinessAction.rollbackMethod(),
                        twoPhaseBusinessAction.rollbackArgsClasses()));
                // set argsClasses
                tccResource.setCommitArgsClasses(twoPhaseBusinessAction.commitArgsClasses());
                tccResource.setRollbackArgsClasses(twoPhaseBusinessAction.rollbackArgsClasses());
                // set phase two method's keys
                tccResource.setPhaseTwoCommitKeys(this.getTwoPhaseArgs(tccResource.getCommitMethod(),
                        twoPhaseBusinessAction.commitArgsClasses()));
                tccResource.setPhaseTwoRollbackKeys(this.getTwoPhaseArgs(tccResource.getRollbackMethod(),
                        twoPhaseBusinessAction.rollbackArgsClasses()));
                //registry tcc resource
                DefaultResourceManager.get().registerResource(tccResource);
            }
        }
    }
}
