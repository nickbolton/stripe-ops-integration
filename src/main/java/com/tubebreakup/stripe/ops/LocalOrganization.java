package com.tubebreakup.stripe.ops;

import com.tubebreakup.model.ExternalEntity;
import com.tubebreakup.stripe.SubscriptionUpdateResult;

public interface LocalOrganization {
    String getUuid();
    String getName();
    ExternalEntity getLocalPaymentMethod();
    ExternalEntity getLocalCustomer();
    ExternalEntity getLocalSubscription();
    void updateFrom(SubscriptionUpdateResult result);
}
