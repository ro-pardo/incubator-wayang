package org.apache.wayang.core.plan.wayangplan.test;

import org.apache.wayang.core.plan.wayangplan.UnarySink;
import org.apache.wayang.core.types.DataSetType;

/**
 * Another dummy sink for testing purposes.
 */
public class TestSink2<T> extends UnarySink<T> {

    public TestSink2(DataSetType<T> inputType) {
        super(inputType);
    }
}