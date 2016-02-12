package org.qcri.rheem.tests.platform;

import org.qcri.rheem.core.api.exception.RheemException;
import org.qcri.rheem.core.mapping.Mapping;
import org.qcri.rheem.core.plan.executionplan.Channel;
import org.qcri.rheem.core.plan.executionplan.ChannelInitializer;
import org.qcri.rheem.core.platform.Executor;
import org.qcri.rheem.core.platform.Platform;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;

/**
 * Dummy {@link Platform} that does not provide any {@link Mapping}s.
 */
public class MyMadeUpPlatform extends Platform {

    private static MyMadeUpPlatform instance = null;

    public static MyMadeUpPlatform getInstance() {
        if (instance == null) {
            instance = new MyMadeUpPlatform();
        }
        return instance;
    }

    public MyMadeUpPlatform() {
        super("My made up platform");
    }

    @Override
    public Executor.Factory getExecutorFactory() {
        return mock(Executor.Factory.class);
    }

    @Override
    public Collection<Mapping> getMappings() {
        return Collections.emptyList();
    }

    @Override
    public boolean isExecutable() {
        return true;
    }

    @Override
    public <T extends Channel> ChannelInitializer<T> getChannelInitializer(Class<T> channelClass) {
        throw new RheemException("Not supported: This platform has no execution operators.");
    }
}
