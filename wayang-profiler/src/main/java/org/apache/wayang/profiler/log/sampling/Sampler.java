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

package org.apache.wayang.profiler.log.sampling;

import java.util.Collection;
import java.util.List;

/**
 * Interface that describes a sampling algorithm that can give bias towards certain elements..
 *
 * @param <T> the type of sampled elements
 */
public interface Sampler<T> {

    /**
     * Create a sample from a given {@link Collection}.
     *
     * @param set    the set of elements to be sampled from
     * @param battle lets two elements compete
     * @return the sample; may contain {@code null}s dependent on the implementation
     */
    List<T> sample(Collection<T> set, Battle<T> battle, double selectionProbability);

}
