/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wayang.spark.compiler;

import org.apache.spark.api.java.function.Function;
import org.apache.wayang.core.function.ExecutionContext;
import org.apache.wayang.spark.execution.SparkExecutionContext;

/**
 * Implements a {@link Function} that calls {@link org.apache.wayang.core.function.ExtendedFunction#open(ExecutionContext)}
 * of its implementation before delegating the very first {@link Function#call(Object)}.
 */
public class ExtendedFunction<InputType, OutputType> implements Function<InputType, OutputType> {

    private final Function<InputType, OutputType> impl;

    private final SparkExecutionContext executionContext;

    private boolean isFirstRun = true;

    public <T extends Function<InputType, OutputType> & org.apache.wayang.core.function.ExtendedFunction>
    ExtendedFunction(T extendedFunction,
                     SparkExecutionContext sparkExecutionContext) {
        this.impl = extendedFunction;
        this.executionContext = sparkExecutionContext;
    }

    @Override
    public OutputType call(InputType v1) throws Exception {
        if (this.isFirstRun) {
            ((org.apache.wayang.core.function.ExtendedFunction) this.impl).open(this.executionContext);
            this.isFirstRun = false;
        }

        return this.impl.call(v1);
    }

}
