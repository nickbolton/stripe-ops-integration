package com.tubebreakup.stripe.ops;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import com.tubebreakup.exception.CommonErrors;
import com.tubebreakup.exception.ErrorCode;
import com.tubebreakup.exception.ErrorCodedHttpException;
import com.tubebreakup.model.ExternalEntity;
import com.tubebreakup.model.NameProvider;
import com.tubebreakup.model.config.AppConfigManager;
import com.tubebreakup.stripe.LocalPaymentMethod;
import com.tubebreakup.stripe.SubscriptionMetaProvider;
import com.tubebreakup.stripe.SubscriptionUpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Service
public class StripeService implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static String organizationKey = "org_id";

    @Autowired
    private AppConfigManager appConfigManager;

    @Value("${stripe.secretApiKey}")
    private String secretApiKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        Stripe.apiKey = secretApiKey;
    }

    public String createWebhookEndpointIfNecessary() {
        String url = "https://api.talentdrop.com:8080/webhooks";
        WebhookEndpoint webhookEndpoint = fetchWebhookEndpoint(url);
        if (webhookEndpoint != null) {
            return webhookEndpoint.getId();
        }
        try {
            WebhookEndpointCreateParams.EnabledEvent[] events = {
                    WebhookEndpointCreateParams.EnabledEvent.PAYMENT_INTENT__SUCCEEDED,
                    WebhookEndpointCreateParams.EnabledEvent.PAYMENT_METHOD__ATTACHED,
                    WebhookEndpointCreateParams.EnabledEvent.INVOICE__PAYMENT_ACTION_REQUIRED,
                    WebhookEndpointCreateParams.EnabledEvent.INVOICE__PAYMENT_FAILED,
                    WebhookEndpointCreateParams.EnabledEvent.INVOICE__UPCOMING,
                    WebhookEndpointCreateParams.EnabledEvent.INVOICE__PAYMENT_SUCCEEDED,
                    WebhookEndpointCreateParams.EnabledEvent.CUSTOMER__SUBSCRIPTION__UPDATED, };

            WebhookEndpointCreateParams params = WebhookEndpointCreateParams.builder()
                    .setUrl(url)
                    .addAllEnabledEvent(Arrays.asList(events))
                    .build();

            webhookEndpoint = WebhookEndpoint.create(params);
            return webhookEndpoint.getId();
        } catch (StripeException e) {
            logger.error("Failed communicating with payment service", e);
            throw new ErrorCodedHttpException(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrors.SERVER_ERROR,
                    "Failed communicating with payment service", e);
        }
    }

    public WebhookEndpoint fetchWebhookEndpoint(String url) {
        if (url == null) {
            return null;
        }
        try {
            WebhookEndpointListParams params = WebhookEndpointListParams.builder()
                    .build();
            WebhookEndpointCollection collection = WebhookEndpoint.list(params);
            List<WebhookEndpoint> endpoints = collection.getData();
            if (endpoints != null) {
                for (WebhookEndpoint endpoint: endpoints) {
                    if (url.equals(endpoint.getUrl())) {
                        return endpoint;
                    }
                }
            }
            return null;
        } catch (StripeException e) {
            logger.error("Failed communicating with payment service", e);
            throw new ErrorCodedHttpException(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrors.SERVER_ERROR,
                    "Failed communicating with payment service", e);
        }
    }

    // create a subscription
    // updates the subscription object
    // updates the organization's customer object
    // updates the organization's payment method object
    public void createSubscription(NameProvider user, LocalOrganization organization, SubscriptionMetaProvider metaProvider, Long trialDays) {
        if (organization.getLocalPaymentMethod() == null || organization.getLocalPaymentMethod().getExternalId() == null) {
            logger.info("Stripe: skipped creating subscription: no payment method ({}) ({})", organization.getUuid(), user.getEmail());
            return;
        }
        if (organization.getLocalCustomer() == null || organization.getLocalCustomer().getExternalId() == null) {
            createCustomerIfNecessary(user, organization);
        }

        Long quantity = metaProvider.getQuantity();
        String priceId = metaProvider.getPriceId();

        SubscriptionCreateParams.Item item = SubscriptionCreateParams.Item.builder()
                .setPrice(priceId)
                .setQuantity(quantity)
                .build();

        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setDefaultPaymentMethod(organization.getLocalPaymentMethod().getExternalId())
                .addItem(item)
                .setCancelAtPeriodEnd(false)
                .addExpand("latest_invoice.payment_intent")

                .setCollectionMethod(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
                .setCustomer(organization.getLocalCustomer().getExternalId())
                .setTrialPeriodDays(trialDays != null ? trialDays : 0l)
                .build();
        try {
            com.stripe.model.Subscription subscription = com.stripe.model.Subscription.create(params);
            organization.updateFrom(buildSubscriptionUpdateResult(subscription));
        } catch (StripeException e) {
            logger.error("Stripe: failed creating subscription ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed creating subscription");
        }
    }

    private SubscriptionUpdateResult buildSubscriptionUpdateResult(Subscription subscription) {
        return new SubscriptionUpdateResult(subscription.getId(),
                subscription.getStatus(),
                subscription.getLatestInvoiceObject().getPaymentIntentObject().getId(),
                subscription.getLatestInvoiceObject().getPaymentIntentObject().getStatus());
    }

    public com.stripe.model.Subscription getSubscription(String id) {
        try {
            return com.stripe.model.Subscription.retrieve(id);
        } catch (StripeException e) {
            logger.error("Stripe: failed getting subscription", e);
            throw buildException(e, "Stripe: failed getting subscription");
        }
    }

    // updates the subscription object
    // updates the organization's customer object
    // updates the organization's payment method object
    public void updateSubscriptionPlan(NameProvider user, LocalOrganization organization, SubscriptionMetaProvider metaProvider) {
        if (organization.getLocalSubscription().getExternalId() == null) {
            return;
        }
        if (organization.getLocalPaymentMethod().getExternalId() == null) {
            logger.info("Stripe: skipped creating subscription: no payment method ({}) ({})", organization.getUuid(), user.getEmail());
            return;
        }
        if (organization.getLocalCustomer().getExternalId() == null) {
            createCustomerIfNecessary(user, organization);
        }

        Long prorationDate = System.currentTimeMillis() / 1000L;
        Long quantity = metaProvider.getQuantity();
        String priceId = metaProvider.getPriceId();

        SubscriptionUpdateParams.Item item = SubscriptionUpdateParams.Item.builder()
                .setPrice(priceId)
                .setQuantity(quantity)
                .build();

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setDefaultPaymentMethod(organization.getLocalPaymentMethod().getExternalId())
                .setProrationDate(prorationDate)
                .addItem(item)
                .setCancelAtPeriodEnd(false)
                .addExpand("latest_invoice.payment_intent")
                .build();

        try {
            com.stripe.model.Subscription subscription = com.stripe.model.Subscription.retrieve(organization.getLocalSubscription().getExternalId());
            subscription = subscription.update(params);
            organization.updateFrom(buildSubscriptionUpdateResult(subscription));
        } catch (StripeException e) {
            logger.error("Stripe: failed updating subscription ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed updating subscription");
        }
    }

    // cancel the subscription
    public void cancelSubscription(NameProvider user, LocalOrganization organization) {
        if (!StringUtils.hasLength(organization.getLocalSubscription().getExternalId())) {
            return;
        }
        try {
            com.stripe.model.Subscription sub = com.stripe.model.Subscription.retrieve(organization.getLocalSubscription().getExternalId());
            sub.cancel();
            organization.getLocalSubscription().setExternalId(null);
        } catch (StripeException e) {
            logger.error("Stripe: failed cancelling subscription ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed cancelling subscription");
        }
    }

    // create the customer
    private com.stripe.model.Customer createCustomerIfNecessary(NameProvider user, LocalOrganization organization) {
        com.stripe.model.Customer cust = fetchCustomer(user, organization);
        if (cust != null) {
            cust = updateCustomer(cust, user, organization);
        } else {
            cust = createCustomer(user, organization);
        }
        if (organization.getLocalCustomer().getExternalId() == null) {
            logger.info("Stripe: skipped creating subscription: Failed to create backend payment provider customer object ({}) ({})", organization.getUuid(), user.getEmail());
            throw new ErrorCodedHttpException(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrors.SERVER_ERROR,
                    "Stripe: Failed to create backend payment provider customer object");
        }
        return cust;
    }

    private com.stripe.model.Customer fetchCustomer(NameProvider user, LocalOrganization organization) {
        if (organization == null || organization.getLocalCustomer() == null) {
            return null;
        }
        ExternalEntity customer = organization.getLocalCustomer();
        if (!StringUtils.hasLength(customer.getExternalId())) {
            return null;
        }
        try {
            return com.stripe.model.Customer.retrieve(customer.getExternalId());
        } catch (StripeException e) {
            logger.error("Stripe: failed fetching customer ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed fetching customer");
        }
    }

    private com.stripe.model.Customer createCustomer(NameProvider user, LocalOrganization organization) {

        String paymentMethodId = organization.getLocalPaymentMethod().getExternalId();

        CustomerCreateParams.InvoiceSettings invoiceSettings = CustomerCreateParams.InvoiceSettings.builder()
                .setDefaultPaymentMethod(paymentMethodId)
                .build();

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .putMetadata(organizationKey, organization.getUuid())
                .setDescription(organization.getName())
                .setPaymentMethod(paymentMethodId)
                .setInvoiceSettings(invoiceSettings)
                .build();

        try {
            com.stripe.model.Customer customer = com.stripe.model.Customer.create(params);
            organization.getLocalCustomer().setExternalId(customer.getId());
            return customer;
        } catch (StripeException e) {
            logger.error("Stripe: failed creating customer ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed creating customer");
        }
    }


    private com.stripe.model.Customer updateCustomer(com.stripe.model.Customer customer, NameProvider user, LocalOrganization organization) {

        String paymentMethodId = organization.getLocalPaymentMethod().getExternalId();

        CustomerUpdateParams.InvoiceSettings invoiceSettings = CustomerUpdateParams.InvoiceSettings.builder()
                .setDefaultPaymentMethod(paymentMethodId)
                .build();

        CustomerUpdateParams params = CustomerUpdateParams.builder()
                .setInvoiceSettings(invoiceSettings)
                .setEmail(user.getEmail())
                .setDescription(organization.getName())
                .build();

        try {
            customer = customer.update(params);
            return customer;
        } catch (StripeException e) {
            logger.error("Stripe: failed updating customer ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed updating customer");
        }
    }

    public String createPaymentMethod(NameProvider user) {

        try {

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            Integer month = cal.get(Calendar.MONTH);
            Integer year = cal.get(Calendar.YEAR);

            PaymentMethodCreateParams.CardDetails cardDetails = PaymentMethodCreateParams.CardDetails.builder()
                    .setNumber("4242424242424242")
                    .setExpMonth(month.longValue())
                    .setExpYear(year.longValue())
                    .setCvc("333")
                    .build();

            PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
                    .setType(PaymentMethodCreateParams.Type.CARD)
                    .setCard(cardDetails)
                    .build();

            return com.stripe.model.PaymentMethod.create(params).getId();

        } catch (StripeException e) {
            logger.error("Stripe: failed creating payment method ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed creating payment method");
        }
    }

    private void attachPaymentMethod(NameProvider user, com.stripe.model.Customer customer, String paymentMethodId) {
        try {
            com.stripe.model.PaymentMethod pm = com.stripe.model.PaymentMethod.retrieve(paymentMethodId);
            pm.attach(PaymentMethodAttachParams.builder().setCustomer(customer.getId()).build());
        } catch (StripeException e) {
            logger.error("Stripe: failed attaching payment method ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed attaching customer");
        }
    }

    public com.stripe.model.PaymentMethod createPaymentMethod(NameProvider user, PaymentMethodCreateParams params) {
        try {
            return com.stripe.model.PaymentMethod.create(params);
        } catch (StripeException e) {
            logger.error("Stripe: failed creating payment method ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed creating payment method");
        }
    }

    public com.stripe.model.PaymentMethod refreshPaymentMethod(
            NameProvider user,
            LocalPaymentMethod localPaymentMethod,
            String paymentMethodId
    ) {

        if (!StringUtils.hasLength(paymentMethodId)) {
            return null;
        }

        try {
            com.stripe.model.PaymentMethod pm = com.stripe.model.PaymentMethod.retrieve(paymentMethodId);
            localPaymentMethod.setExternalId(pm.getId());
            localPaymentMethod.setType(pm.getType());
            localPaymentMethod.setLast4(pm.getCard().getLast4());
            localPaymentMethod.setExpirationMonth(pm.getCard().getExpMonth());
            localPaymentMethod.setExpirationYear(pm.getCard().getExpYear());
            return pm;
        } catch (StripeException e) {
            logger.error("Stripe: failed refreshing payment method ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed refreshing payment method");
        }
    }

    public void updateSubscription(
            NameProvider user,
            LocalOrganization organization,
            com.stripe.model.PaymentMethod pm) {

        if (pm == null) {
            return;
        }
        voidInvoiceIfNecessary(user, organization);
        updateCustomerPaymentMethod(user, organization, pm);
        updateInvoiceStatus(user, organization);
    }

    public void refreshSubscriptionStatus(NameProvider user, LocalOrganization organization) {
        updateInvoiceStatus(user, organization);
    }

    private void updateCustomerPaymentMethod(NameProvider user, LocalOrganization organization, com.stripe.model.PaymentMethod pm) {

        if (pm == null) {
            return;
        }

        com.stripe.model.Customer cust = null;

        if (organization.getLocalCustomer().getExternalId() == null) {
            cust = createCustomerIfNecessary(user, organization);
        }

        final String customerId = organization.getLocalCustomer().getExternalId();

        try {
            if (cust == null) {
                cust = com.stripe.model.Customer.retrieve(customerId);
            }
            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build();

            pm = pm.attach(attachParams);

            CustomerUpdateParams.InvoiceSettings invoiceSettings = CustomerUpdateParams.InvoiceSettings.builder()
                    .setDefaultPaymentMethod(pm.getId())
                    .build();

            CustomerUpdateParams customerParams = CustomerUpdateParams.builder()
                    .setInvoiceSettings(invoiceSettings)
                    .build();
            cust.update(customerParams);

        } catch (StripeException e) {
            logger.error("Stripe: failed updating customer payment method ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed updating customer payment method");
        }
    }

    private void voidInvoiceIfNecessary(NameProvider user, LocalOrganization organization) {
        try {
            ExternalEntity subscription = organization.getLocalSubscription();
            if (!StringUtils.hasLength(subscription.getExternalId())) {
                return;
            }
            com.stripe.model.Subscription sub = com.stripe.model.Subscription.retrieve(subscription.getExternalId());

            if (sub.getLatestInvoice() != null) {
                InvoicePayParams params = InvoicePayParams.builder()
                        .addExpand("payment_intent")
                        .build();

                InvoiceRetrieveParams invoiceParams = InvoiceRetrieveParams.builder()
                        .addExpand("payment_intent")
                        .build();
                Invoice invoice = Invoice.retrieve(sub.getLatestInvoice(), invoiceParams, null);
                PaymentIntent intent = invoice.getPaymentIntentObject();

                if ("incomplete".equals(sub.getStatus()) &&
                        invoice.getAttempted() && "open".equals(invoice.getStatus()) &&
                        ("requires_payment_method".equals(intent.getStatus()) || "requires_action".equals(intent.getStatus()))) {
                    invoice.voidInvoice();
                    subscription.setExternalId(null);
                }
            }
        } catch (StripeException e) {
            logger.error("Stripe: failed updating invoice ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed updating invoice");
        }
    }

    private void updateInvoiceStatus(NameProvider user, LocalOrganization organization) {
        try {
            ExternalEntity subscription = organization.getLocalSubscription();
            if (!StringUtils.hasLength(subscription.getExternalId())) {
                return;
            }
            com.stripe.model.Subscription sub = com.stripe.model.Subscription.retrieve(subscription.getExternalId());

            if (sub.getLatestInvoice() != null) {
                InvoicePayParams params = InvoicePayParams.builder()
                        .addExpand("payment_intent")
                        .build();

                InvoiceRetrieveParams invoiceParams = InvoiceRetrieveParams.builder()
                        .addExpand("payment_intent")
                        .build();
                Invoice invoice = Invoice.retrieve(sub.getLatestInvoice(), invoiceParams, null);
                PaymentIntent intent = invoice.getPaymentIntentObject();

                if ("incomplete".equals(sub.getStatus()) &&
                        invoice.getAttempted() && "open".equals(invoice.getStatus()) &&
                        ("requires_payment_method".equals(intent.getStatus()) || "requires_action".equals(intent.getStatus()))) {

                    intent.cancel();
                    invoice = invoice.pay(params);
                    sub = com.stripe.model.Subscription.retrieve(subscription.getExternalId());

                    SubscriptionUpdateResult result = new SubscriptionUpdateResult(
                            sub.getId(),
                            sub.getStatus(),
                            invoice.getPaymentIntentObject().getId(),
                            invoice.getPaymentIntentObject().getStatus());
                    organization.updateFrom(result);
                }
            }
        } catch (StripeException e) {
            logger.error("Stripe: failed updating invoice ({})", user.getEmail(), e);
            throw buildException(e, "Stripe: failed updating invoice");
        }
    }

    private ErrorCode parseStripeDeclineCode(StripeException exception) {
        String code = exception.getCode() != null ? exception.getCode() : "";
        String name = new StringBuilder("STRIPE_").append(code.toUpperCase()).toString();
        return StripeErrors.from(name);
    }

    private ErrorCodedHttpException buildException(StripeException exception, String message) {
        ErrorCode errorCode = parseStripeDeclineCode(exception);
        return new ErrorCodedHttpException(HttpStatus.UNAUTHORIZED, errorCode, exception);
    }
}