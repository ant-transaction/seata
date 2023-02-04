/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.seata.saga;

import org.apache.seata.rm.tcc.api.BusinessActionContext;
import org.apache.seata.rm.tcc.api.BusinessActionContextParameter;
import org.apache.seata.rm.tcc.api.LocalTCC;
import org.apache.seata.saga.rm.api.CompensationBusinessAction;

import java.util.List;

/**
 * The interface saga action.
 *
 * @author zhangsen
 */
@LocalTCC
public interface NormalSagaAction {

    /**
     * Prepare boolean.
     *
     * @param actionContext the action context
     * @param a             the a
     * @param b             the b
     * @param sagaParam     the saga param
     * @return the boolean
     */
    @CompensationBusinessAction(name = "sagaActionForTest", compensationMethod = "compensation", compensationArgsClasses = {BusinessActionContext.class, SagaParam.class})
    String prepare(BusinessActionContext actionContext,
                   @BusinessActionContextParameter("a") int a,
                   @BusinessActionContextParameter(paramName = "b", index = 0) List b,
                   @BusinessActionContextParameter(isParamInProperty = true) SagaParam sagaParam);

    /**
     * Rollback boolean.
     *
     * @param actionContext the action context
     * @return the boolean
     */
    boolean compensation(BusinessActionContext actionContext, @BusinessActionContextParameter("sagaParam") SagaParam param);
}
