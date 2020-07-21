package com.tubebreakup.stripe;

public interface SubscriptionMetaProvider {
    Long getQuantity();
    String getPriceId();
}
