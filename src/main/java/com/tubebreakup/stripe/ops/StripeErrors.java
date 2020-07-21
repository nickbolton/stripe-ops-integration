package com.tubebreakup.stripe.ops;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tubebreakup.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum StripeErrors implements ErrorCode {

    // stripe error codes
    STRIPE_UNKNOWN_ERROR(-10000, "unknown stripe error"),
    STRIPE_AUTHENTICATION_REQUIRED(-10001, "authentication_required"),
    STRIPE_APPROVE_WITH_ID(-10002, "approve_with_id"),
    STRIPE_CALL_ISSUER(-10003, "call_issuer"),
    STRIPE_CARD_NOT_SUPPORTED(-10004, "card_not_supported"),
    STRIPE_CARD_VELOCITY_EXCEEDED(-10005, "card_velocity_exceeded"),
    STRIPE_CURRENCY_NOT_SUPPORTED(-10006, "currency_not_supported"),
    STRIPE_DO_NOT_HONOR(-10007, "do_not_honor"),
    STRIPE_DO_NOT_TRY_AGAIN(-10008, "do_not_try_again"),
    STRIPE_DUPLICATE_TRANSACTION(-10009, "duplicate_transaction"),
    STRIPE_EXPIRED_CARD(-10010, "expired_card"),
    STRIPE_FRAUDULENT(-10011, "fraudulent"),
    STRIPE_GENERIC_DECLINE(-10012, "generic_decline"),
    STRIPE_INCORRECT_NUMBER(-10013, "incorrect_number"),
    STRIPE_INCORRECT_CVC(-10014, "incorrect_cvc"),
    STRIPE_INCORRECT_PIN(-10015, "incorrect_pin"),
    STRIPE_INCORRECT_ZIP(-10016, "incorrect_zip"),
    STRIPE_INSUFFICIENT_FUNDS(-10017, "insufficient_funds"),
    STRIPE_INVALID_ACCOUNT(-10018, "invalid_account"),
    STRIPE_INVALID_AMOUNT(-10019, "invalid_amount"),
    STRIPE_INVALID_CVC(-10020, "invalid_cvc"),
    STRIPE_INVALID_EXPIRY_YEAR(-10021, "invalid_expiry_year"),
    STRIPE_INVALID_NUMBER(-10022, "invalid_number"),
    STRIPE_INVALID_PIN(-10023, "invalid_pin"),
    STRIPE_ISSUER_NOT_AVAILABLE(-10024, "issuer_not_available"),
    STRIPE_LOST_CARD(-10025, "lost_card"),
    STRIPE_MERCHANT_BLACKLIST(-10026, "merchant_blacklist"),
    STRIPE_NEW_ACCOUNT_INFORMATION_AVAILABLE(-10027, "new_account_information_available"),
    STRIPE_NO_ACTION_TAKEN(-10028, "no_action_taken"),
    STRIPE_NOT_PERMITTED(-10029, "not_permitted"),
    STRIPE_OFFLINE_PIN_REQUIRED(-10030, "offline_pin_required"),
    STRIPE_ONLINE_OR_OFFLINE_PIN_REQUIRED(-10031, "online_or_offline_pin_required"),
    STRIPE_PICKUP_CARD(-10032, "pickup_card"),
    STRIPE_PIN_TRY_EXCEEDED(-10033, "pin_try_exceeded"),
    STRIPE_PROCESSING_ERROR(-10034, "processing_error"),
    STRIPE_REENTER_TRANSACTION(-10035, "reenter_transaction"),
    STRIPE_RESTRICTED_CARD(-10036, "restricted_card"),
    STRIPE_REVOCATION_OF_ALL_AUTHORIZATIONS(-10037, "revocation_of_all_authorizations"),
    STRIPE_REVOCATION_OF_AUTHORIZATION(-10038, "revocation_of_authorization"),
    STRIPE_SECURITY_VIOLATION(-10039, "security_violation"),
    STRIPE_SERVICE_NOT_ALLOWED(-10040, "service_not_allowed"),
    STRIPE_STOLEN_CARD(-10041, "stolen_card"),
    STRIPE_STOP_PAYMENT_ORDER(-10042, "stop_payment_order"),
    STRIPE_TESTMODE_DECLINE(-10043, "testmode_decline"),
    STRIPE_TRANSACTION_NOT_ALLOWED(-10044, "transaction_not_allowed"),
    STRIPE_TRY_AGAIN_LATER(-10045, "try_again_later"),
    STRIPE_WITHDRAWAL_COUNT_LIMIT_EXCEEDED(-10046, "withdrawal_count_limit_exceeded"),

    STRIPE_ACCOUNT_ALREADY_EXISTS(-10047, "account_already_exists"),
    STRIPE_ACCOUNT_COUNTRY_INVALID_ADDRESS(-10048, "account_country_invalid_address"),
    STRIPE_ACCOUNT_INVALID(-10049, "account_invalid"),
    STRIPE_ACCOUNT_NUMBER_INVALID(-10050, "account_number_invalid"),
    STRIPE_ALIPAY_UPGRADE_REQUIRED(-10051, "alipay_upgrade_required"),
    STRIPE_AMOUNT_TOO_LARGE(-10052, "amount_too_large"),
    STRIPE_AMOUNT_TOO_SMALL(-10053, "amount_too_small"),
    STRIPE_API_KEY_EXPIRED(-10054, "api_key_expired"),
    STRIPE_BALANCE_INSUFFICIENT(-10056, "balance_insufficient"),
    STRIPE_BANK_ACCOUNT_DECLINED(-10057, "bank_account_declined"),
    STRIPE_BANK_ACCOUNT_EXISTS(-10058, "bank_account_exists"),
    STRIPE_BANK_ACCOUNT_UNUSABLE(-10059, "bank_account_unusable"),
    STRIPE_BANK_ACCOUNT_UNVERIFIED(-10060, "bank_account_unverified"),
    STRIPE_BANK_ACCOUNT_VERIFICATION_FAILED(-10061, "bank_account_verification_failed"),
    STRIPE_BITCOIN_UPGRADE_REQUIRED(-10062, "bitcoin_upgrade_required"),
    STRIPE_CARD_DECLINE_RATE_LIMIT_EXCEEDED(-10063, "card_decline_rate_limit_exceeded"),
    STRIPE_CARD_DECLINED(-10064, "card_declined"),
    STRIPE_CHARGE_ALREADY_CAPTURED(-10065, "charge_already_captured"),
    STRIPE_CHARGE_ALREADY_REFUNDED(-10066, "charge_already_refunded"),
    STRIPE_CHARGE_DISPUTED(-10067, "charge_disputed"),
    STRIPE_CHARGE_EXCEEDS_SOURCE_LIMIT(-10068, "charge_exceeds_source_limit"),
    STRIPE_CHARGE_EXPIRED_FOR_CAPTURE(-10069, "charge_expired_for_capture"),
    STRIPE_CHARGE_INVALID_PARAMETER(-10070, "charge_invalid_parameter"),
    STRIPE_COUNTRY_UNSUPPORTED(-10071, "country_unsupported"),
    STRIPE_COUPON_EXPIRED(-10072, "coupon_expired"),
    STRIPE_CUSTOMER_MAX_PAYMENT_METHODS(-10073, "customer_max_payment_methods"),
    STRIPE_CUSTOMER_MAX_SUBSCRIPTIONS(-10074, "customer_max_subscriptions"),
    STRIPE_EMAIL_INVALID(-10075, "email_invalid"),
    STRIPE_IDEMPOTENCY_KEY_IN_USE(-10077, "idempotency_key_in_use"),
    STRIPE_INCORRECT_ADDRESS(-10078, "incorrect_address"),
    STRIPE_INSTANT_PAYOUTS_UNSUPPORTED(-10082, "instant_payouts_unsupported"),
    STRIPE_INVALID_CARD_TYPE(-10083, "invalid_card_type"),
    STRIPE_INVALID_CHARACTERS(-10084, "invalid_characters"),
    STRIPE_INVALID_CHARGE_AMOUNT(-10085, "invalid_charge_amount"),
    STRIPE_INVALID_EXPIRY_MONTH(-10087, "invalid_expiry_month"),
    STRIPE_INVALID_SOURCE_USAGE(-10090, "invalid_source_usage"),
    STRIPE_INVOICE_NO_CUSTOMER_LINE_ITEMS(-10091, "invoice_no_customer_line_items"),
    STRIPE_INVOICE_NO_SUBSCRIPTION_LINE_ITEMS(-10092, "invoice_no_subscription_line_items"),
    STRIPE_INVOICE_NOT_EDITABLE(-10093, "invoice_not_editable"),
    STRIPE_INVOICE_PAYMENT_INTENT_REQUIRES_ACTION(-10094, "invoice_payment_intent_requires_action"),
    STRIPE_INVOICE_UPCOMING_NONE(-10095, "invoice_upcoming_none"),
    STRIPE_LIVEMODE_MISMATCH(-10096, "livemode_mismatch"),
    STRIPE_LOCK_TIMEOUT(-10097, "lock_timeout"),
    STRIPE_NOT_ALLOWED_ON_STANDARD_ACCOUNT(-10098, "not_allowed_on_standard_account"),
    STRIPE_ORDER_CREATION_FAILED(-10099, "order_creation_failed"),
    STRIPE_ORDER_REQUIRED_SETTINGS(-10100, "order_required_settings"),
    STRIPE_ORDER_STATUS_INVALID(-10101, "order_status_invalid"),
    STRIPE_ORDER_UPSTREAM_TIMEOUT(-10102, "order_upstream_timeout"),
    STRIPE_OUT_OF_INVENTORY(-10103, "out_of_inventory"),
    STRIPE_PARAMETER_INVALID_EMPTY(-10104, "parameter_invalid_empty"),
    STRIPE_PARAMETER_INVALID_INTEGER(-10105, "parameter_invalid_integer"),
    STRIPE_PARAMETER_INVALID_STRING_BLANK(-10106, "parameter_invalid_string_blank"),
    STRIPE_PARAMETER_INVALID_STRING_EMPTY(-10107, "parameter_invalid_string_empty"),
    STRIPE_PARAMETER_MISSING(-10108, "parameter_missing"),
    STRIPE_PARAMETER_UNKNOWN(-10109, "parameter_unknown"),
    STRIPE_PARAMETERS_EXCLUSIVE(-10110, "parameters_exclusive"),
    STRIPE_PAYMENT_INTENT_ACTION_REQUIRED(-10111, "payment_intent_action_required"),
    STRIPE_PAYMENT_INTENT_AUTHENTICATION_FAILURE(-10112, "payment_intent_authentication_failure"),
    STRIPE_PAYMENT_INTENT_INCOMPATIBLE_PAYMENT_METHOD(-10113, "payment_intent_incompatible_payment_method"),
    STRIPE_PAYMENT_INTENT_INVALID_PARAMETER(-10114, "payment_intent_invalid_parameter"),
    STRIPE_PAYMENT_INTENT_PAYMENT_ATTEMPT_FAILED(-10115, "payment_intent_payment_attempt_failed"),
    STRIPE_PAYMENT_INTENT_UNEXPECTED_STATE(-10116, "payment_intent_unexpected_state"),
    STRIPE_PAYMENT_METHOD_INVALID_PARAMETER(-10117, "payment_method_invalid_parameter"),
    STRIPE_PAYMENT_METHOD_UNACTIVATED(-10118, "payment_method_unactivated"),
    STRIPE_PAYMENT_METHOD_UNEXPECTED_STATE(-10119, "payment_method_unexpected_state"),
    STRIPE_PAYOUTS_NOT_ALLOWED(-10120, "payouts_not_allowed"),
    STRIPE_PLATFORM_API_KEY_EXPIRED(-10121, "platform_api_key_expired"),
    STRIPE_POSTAL_CODE_INVALID(-10122, "postal_code_invalid"),
    STRIPE_PRODUCT_INACTIVE(-10124, "product_inactive"),
    STRIPE_RATE_LIMIT(-10125, "rate_limit"),
    STRIPE_RESOURCE_ALREADY_EXISTS(-10126, "resource_already_exists"),
    STRIPE_RESOURCE_MISSING(-10127, "resource_missing"),
    STRIPE_ROUTING_NUMBER_INVALID(-10128, "routing_number_invalid"),
    STRIPE_SECRET_KEY_REQUIRED(-10129, "secret_key_required"),
    STRIPE_SEPA_UNSUPPORTED_ACCOUNT(-10130, "sepa_unsupported_account"),
    STRIPE_SETUP_ATTEMPT_FAILED(-10131, "setup_attempt_failed"),
    STRIPE_SETUP_INTENT_AUTHENTICATION_FAILURE(-10132, "setup_intent_authentication_failure"),
    STRIPE_SETUP_INTENT_INVALID_PARAMETER(-10133, "setup_intent_invalid_parameter"),
    STRIPE_SETUP_INTENT_UNEXPECTED_STATE(-10134, "setup_intent_unexpected_state"),
    STRIPE_SHIPPING_CALCULATION_FAILED(-10135, "shipping_calculation_failed"),
    STRIPE_SKU_INACTIVE(-10136, "sku_inactive"),
    STRIPE_STATE_UNSUPPORTED(-10137, "state_unsupported"),
    STRIPE_TAX_ID_INVALID(-10138, "tax_id_invalid"),
    STRIPE_TAXES_CALCULATION_FAILED(-10139, "taxes_calculation_failed"),
    STRIPE_TESTMODE_CHARGES_ONLY(-10140, "testmode_charges_only"),
    STRIPE_TLS_VERSION_UNSUPPORTED(-10141, "tls_version_unsupported"),
    STRIPE_TOKEN_ALREADY_USED(-10142, "token_already_used"),
    STRIPE_TOKEN_IN_USE(-10143, "token_in_use"),
    STRIPE_TRANSFERS_NOT_ALLOWED(-10144, "transfers_not_allowed"),
    STRIPE_UPSTREAM_ORDER_CREATION_FAILED(-10145, "upstream_order_creation_failed"),
    STRIPE_URL_INVALID(-10146, "url_invalid"),

    ;

    private static Logger logger = LoggerFactory.getLogger(StripeErrors.class);

    @JsonProperty
    private Integer value;

    @JsonProperty
    private String message;

    private StripeErrors(final Integer value, final String message) {
        this.value = value;
        this.message = message;
    }

    public Integer value() { return value; }
    public String message() { return message; }

    static StripeErrors from(String name) {
        try {
            return StripeErrors.valueOf(name);
        } catch (IllegalArgumentException e) {
            logger.warn("No stripe error defined named: {}", name);
        }
        return StripeErrors.STRIPE_UNKNOWN_ERROR;
    }
}
