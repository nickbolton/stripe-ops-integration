package com.tubebreakup.stripe;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionUpdateResult {
    private String subscriptionExternalId;
    private String subscriptionStatus;
    private String paymentIntentId;
    private String paymentIntentStatus;
}
