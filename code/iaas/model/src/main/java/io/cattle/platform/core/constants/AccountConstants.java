package io.cattle.platform.core.constants;

import io.cattle.platform.core.model.Account;
import io.cattle.platform.core.util.ConstantsUtils;

public class AccountConstants {

    public static final String AGENT_KIND = "agent";

    public static final String ACCOUNT_ID = ConstantsUtils.property(Account.class, "accountId");
    public static final String SYSTEM_UUID = "system";

}
