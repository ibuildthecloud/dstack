package io.cattle.platform.process.agent;

import static io.cattle.platform.core.model.tables.AccountTable.*;
import static io.cattle.platform.core.model.tables.AgentTable.*;
import io.cattle.platform.archaius.util.ArchaiusUtil;
import io.cattle.platform.core.model.Account;
import io.cattle.platform.core.model.Agent;
import io.cattle.platform.engine.handler.HandlerResult;
import io.cattle.platform.engine.process.ProcessInstance;
import io.cattle.platform.engine.process.ProcessState;
import io.cattle.platform.process.base.AbstractDefaultProcessHandler;
import io.cattle.platform.process.dao.AccountDao;

import javax.inject.Inject;
import javax.inject.Named;

import com.netflix.config.DynamicBooleanProperty;

@Named
public class AgentCreate extends AbstractDefaultProcessHandler {

    private static final DynamicBooleanProperty CREATE_ACCOUNT = ArchaiusUtil.getBoolean("process.agent.create.create.account");

    AccountDao accountDao;

    @Override
    public HandlerResult handle(ProcessState state, ProcessInstance process) {
        Agent agent = (Agent)state.getResource();
        Long accountId = agent.getAccountId();

        if ( accountId != null || ! CREATE_ACCOUNT.get() ) {
            return new HandlerResult(AGENT.ACCOUNT_ID, accountId);
        }

        Account account = createAccountObj(agent);
        create(account, state.getData());

        return new HandlerResult(AGENT.ACCOUNT_ID, account.getId());
    }

    protected Account createAccountObj(Agent agent) {
        if ( agent.getAccountId() != null ) {
            return getObjectManager().loadResource(Account.class, agent.getAccountId());
        }

        String uuid = getAccountUuid(agent);
        Account account = accountDao.findByUuid(uuid);

        if ( account == null ) {
            account = getObjectManager().create(Account.class,
                    ACCOUNT.UUID, uuid,
                    ACCOUNT.KIND, "agent");
        }

        return account;
    }

    protected String getAccountUuid(Agent agent) {
        return "agentAccount" + agent.getId();
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }

    @Inject
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

}
