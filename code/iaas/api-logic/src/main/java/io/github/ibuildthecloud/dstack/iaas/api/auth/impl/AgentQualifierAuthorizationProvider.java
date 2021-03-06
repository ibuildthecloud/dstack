package io.github.ibuildthecloud.dstack.iaas.api.auth.impl;

import java.util.List;

import io.github.ibuildthecloud.dstack.api.auth.Policy;
import io.github.ibuildthecloud.dstack.api.auth.impl.OptionCallback;
import io.github.ibuildthecloud.dstack.api.auth.impl.PolicyOptions;
import io.github.ibuildthecloud.dstack.api.pubsub.model.Subscribe;
import io.github.ibuildthecloud.dstack.api.pubsub.util.SubscriptionUtils;
import io.github.ibuildthecloud.dstack.api.pubsub.util.SubscriptionUtils.SubscriptionStyle;
import io.github.ibuildthecloud.dstack.core.constants.AccountConstants;
import io.github.ibuildthecloud.dstack.core.model.Account;
import io.github.ibuildthecloud.dstack.core.model.Agent;
import io.github.ibuildthecloud.dstack.iaas.api.auth.AchaiusPolicyOptionsFactory;
import io.github.ibuildthecloud.dstack.iaas.api.auth.AuthorizationProvider;
import io.github.ibuildthecloud.dstack.iaas.event.IaasEvents;
import io.github.ibuildthecloud.gdapi.context.ApiContext;
import io.github.ibuildthecloud.gdapi.exception.ClientVisibleException;
import io.github.ibuildthecloud.gdapi.exception.ValidationErrorException;
import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Pagination;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManager;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerLocator;
import io.github.ibuildthecloud.gdapi.util.ResponseCodes;
import io.github.ibuildthecloud.gdapi.validation.ValidationErrorCodes;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentQualifierAuthorizationProvider implements AuthorizationProvider {

    private static final Logger log = LoggerFactory.getLogger(AgentQualifierAuthorizationProvider.class);

    AchaiusPolicyOptionsFactory optionsFactory;
    ResourceManagerLocator locator;

    @Override
    public Policy getPolicy(final Account account, ApiRequest request) {
        PolicyOptions policyOptions = optionsFactory.getOptions(account);

        boolean apply = false;
        final SubscriptionStyle accountStyle = SubscriptionUtils.getSubscriptionStyle(policyOptions);

        /* This boolean logic could be optimized but this seems more readable. */
        if ( accountStyle == SubscriptionStyle.RAW ) {
            apply = true;
        } else if ( accountStyle == SubscriptionStyle.QUALIFIED && AccountConstants.AGENT_KIND.equals(account.getKind()) ) {
            apply = true;
        }

        if ( ! apply ) {
            return null;
        }

        final PolicyOptionsWrapper options = new PolicyOptionsWrapper(policyOptions);
        AccountPolicy policy = new AccountPolicy(account, options);

        options.addCallback(Policy.AGENT_ID, new OptionCallback() {
            @Override
            public String getOption() {
                Long agentId = getAgent();
                return agentId == null ? null : Long.toString(agentId);
            }
        });

        options.addCallback(SubscriptionUtils.POLICY_SUBSCRIPTION_STYLE, new OptionCallback() {
            @Override
            public String getOption() {
                if ( accountStyle == SubscriptionStyle.RAW && getRawAgentId() != null ) {
                    return SubscriptionStyle.QUALIFIED.toString();
                }

                return accountStyle.toString();
            }
        });

        options.setOption(SubscriptionUtils.POLICY_SUBSCRIPTION_QUALIFIER, IaasEvents.AGENT_QUALIFIER);
        options.addCallback(SubscriptionUtils.POLICY_SUBSCRIPTION_QUALIFIER_VALUE, new OptionCallback() {
            @Override
            public String getOption() {
                String agentId = options.getOption(Policy.AGENT_ID);

                if ( agentId == null ) {
                    log.error("Failed to determine the proper agent ID for subscription for account [{}]", account.getId());
                    throw new ClientVisibleException(ResponseCodes.FORBIDDEN);
                }

                return agentId;
            }
        });

        return policy;
    }

    protected Long getRawAgentId() {
        ApiRequest request = ApiContext.getContext().getApiRequest();
        Subscribe subscribe = request.proxyRequestObject(Subscribe.class);
        try {
            Long agentId = subscribe.getAgentId();
            if ( agentId != null ) {
                return agentId;
            }
        } catch ( Throwable t ) {
            //ignore errors;
        }

        return null;
    }

    protected Long getAgent() {
        ApiRequest request = ApiContext.getContext().getApiRequest();
        Long agentId = getRawAgentId();
        if ( agentId != null ) {
            return agentId;
        }

        String type = request.getSchemaFactory().getSchemaName(Agent.class);
        ResourceManager rm = getLocator().getResourceManagerByType(type);
        List<?> agents = rm.list(type, null,Pagination.limit(2));

        if ( agents.size() > 1 ) {
            throw new ValidationErrorException(ValidationErrorCodes.MISSING_REQUIRED, "agentId");
        }

        return agents.size() == 0 ? null : ((Agent)agents.get(0)).getId();
    }

    @Override
    public SchemaFactory getSchemaFactory(Account account, ApiRequest request) {
        return null;
    }

    public AchaiusPolicyOptionsFactory getOptionsFactory() {
        return optionsFactory;
    }

    @Inject
    public void setOptionsFactory(AchaiusPolicyOptionsFactory optionsFactory) {
        this.optionsFactory = optionsFactory;
    }

    public ResourceManagerLocator getLocator() {
        return locator;
    }

    @Inject
    public void setLocator(ResourceManagerLocator locator) {
        this.locator = locator;
    }

}
