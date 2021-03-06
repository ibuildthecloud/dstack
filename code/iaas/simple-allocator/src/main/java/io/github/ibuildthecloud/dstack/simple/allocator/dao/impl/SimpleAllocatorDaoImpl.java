package io.github.ibuildthecloud.dstack.simple.allocator.dao.impl;

import static io.github.ibuildthecloud.dstack.core.model.tables.AgentTable.*;
import static io.github.ibuildthecloud.dstack.core.model.tables.HostTable.*;
import static io.github.ibuildthecloud.dstack.core.model.tables.StoragePoolHostMapTable.*;
import static io.github.ibuildthecloud.dstack.core.model.tables.StoragePoolTable.*;
import io.github.ibuildthecloud.dstack.allocator.service.AllocationCandidate;
import io.github.ibuildthecloud.dstack.archaius.util.ArchaiusUtil;
import io.github.ibuildthecloud.dstack.core.constants.CommonStatesConstants;
import io.github.ibuildthecloud.dstack.db.jooq.dao.impl.AbstractJooqDao;
import io.github.ibuildthecloud.dstack.object.ObjectManager;
import io.github.ibuildthecloud.dstack.simple.allocator.dao.QueryOptions;
import io.github.ibuildthecloud.dstack.simple.allocator.dao.SimpleAllocatorDao;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Record2;
import org.jooq.impl.DSL;

import com.netflix.config.DynamicBooleanProperty;

public class SimpleAllocatorDaoImpl extends AbstractJooqDao implements SimpleAllocatorDao {

    private static final DynamicBooleanProperty SPREAD = ArchaiusUtil.getBoolean("simple.allocator.spread");

    ObjectManager objectManager;

    @Override
    public Iterator<AllocationCandidate> iteratorPools(List<Long> volumes, QueryOptions options) {
        return iteratorHosts(volumes, options, false);
    }

    @Override
    public Iterator<AllocationCandidate> iteratorHosts(List<Long> volumes, QueryOptions options) {
        return iteratorHosts(volumes, options, true);
    }

    protected Iterator<AllocationCandidate> iteratorHosts(List<Long> volumes, QueryOptions options, boolean hosts) {
        final Cursor<Record2<Long,Long>> cursor = create()
                .select(HOST.ID, STORAGE_POOL.ID)
                .from(HOST)
                .leftOuterJoin(STORAGE_POOL_HOST_MAP)
                    .on(STORAGE_POOL_HOST_MAP.HOST_ID.eq(HOST.ID)
                        .and(STORAGE_POOL_HOST_MAP.REMOVED.isNull()))
                .join(STORAGE_POOL)
                    .on(STORAGE_POOL.ID.eq(STORAGE_POOL_HOST_MAP.STORAGE_POOL_ID))
                .leftOuterJoin(AGENT)
                    .on(AGENT.ID.eq(HOST.AGENT_ID))
                .where(
                    AGENT.ID.isNull().or(AGENT.STATE.eq(CommonStatesConstants.ACTIVE))
                    .and(HOST.STATE.eq(CommonStatesConstants.ACTIVE))
                    .and(STORAGE_POOL.STATE.eq(CommonStatesConstants.ACTIVE))
                    .and(getQueryOptionCondition(options)))
                .orderBy(SPREAD.get() ? HOST.COMPUTE_FREE.asc() : HOST.COMPUTE_FREE.desc())
                .fetchLazy();

        return new AllocationCandidateIterator(objectManager, cursor, volumes, hosts);
    }

    protected Condition getQueryOptionCondition(QueryOptions options) {
        Condition condition = null;

        if ( options.getHosts().size() > 0 ) {
            condition = append(condition, HOST.ID.in(options.getHosts()));
        }

        if ( options.getCompute() != null ) {
            condition = append(condition, HOST.COMPUTE_FREE.ge(options.getCompute()));
        }

        if ( options.getKind() != null ) {
            condition = append(condition,
                    HOST.KIND.eq(options.getKind()).and(STORAGE_POOL.KIND.eq(options.getKind())));
        }

        return condition == null ? DSL.trueCondition() : condition;
    }

    protected Condition append(Condition base, Condition next) {
        if ( base == null ) {
            return next;
        } else {
            return base.and(next);
        }
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    @Inject
    public void setObjectManager(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

}
