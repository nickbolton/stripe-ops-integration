package com.tubebreakup.stripe;

import com.tubebreakup.model.ExternalEntity;

public interface LocalPaymentMethod extends ExternalEntity {
    void setType(String type);
    void setLast4(String last4);
    void setExpirationMonth(Long expirationMonth);
    void setExpirationYear(Long expirationYear);
}
