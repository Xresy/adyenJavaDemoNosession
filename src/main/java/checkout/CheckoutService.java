package checkout;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import com.adyen.Client;
import com.adyen.constants.ApiConstants;
import com.adyen.enums.Environment;
import com.adyen.model.*;
import com.adyen.model.checkout.*;
import com.adyen.service.Checkout;
import com.adyen.service.exception.ApiException;
import com.adyen.service.resource.storedvalue.Issue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CheckoutService {

    private final Checkout checkout;
    private final String merchantAccount;

    public CheckoutService(final Properties prop) {
        merchantAccount = prop.getProperty("merchantAccount");
        Client client = new Client(prop.getProperty("apiKey"), Environment.LIVE, "7cc06625ff786a83-TestCompany");
        checkout = new Checkout(client);
    }

    public PaymentMethodsResponse getPaymentMethods() throws IOException, ApiException {
        PaymentMethodsRequest paymentMethodsRequest = new PaymentMethodsRequest();
        paymentMethodsRequest.setMerchantAccount(merchantAccount);
//        paymentMethodsRequest.setShopperLocale("zh-CN");
//        绑卡操作
//        paymentMethodsRequest.setShopperReference("test_recurring");

        paymentMethodsRequest.setChannel(PaymentMethodsRequest.ChannelEnum.WEB);

//        Amount amout = new Amount();
//        amout.currency("PLN");
//        amout.value(1000L);
//        paymentMethodsRequest.amount(amout);
//        paymentMethodsRequest.setCountryCode("PL");
        System.out.println("/paymentMethods context:\n" + paymentMethodsRequest.toString());
        PaymentMethodsResponse response = checkout.paymentMethods(paymentMethodsRequest);

//Customized payment response

//        PaymentMethodsResponse response2 = new PaymentMethodsResponse();
//
//        PaymentMethod paymentMethod = new PaymentMethod();
//        paymentMethod.setName("Online Banking");
//        paymentMethod.setType("dragonpay_ebanking");
//
//        PaymentMethodIssuer issuer = new PaymentMethodIssuer();
//        issuer.id("DPAY");
//        issuer.name("Dragonpay Prepaid Credits");
//        paymentMethod.issuers(Collections.singletonList(issuer));
//
//        response2.addPaymentMethodsItem(paymentMethod);

        System.out.println("/paymentMethods response:\n" + response);
        return response;
    }

    public PaymentsResponse makePayment(PaymentsRequest paymentsRequest) throws IOException, ApiException {
        String type = paymentsRequest.getPaymentMethod().getType();
        paymentsRequest.setAmount(getAmount(type));

        //Set Browser Info
//        BrowserInfo browserInfo = new BrowserInfo();
//        browserInfo.setUserAgent("");
//        browserInfo.setAcceptHeader("");
//        browserInfo.setLanguage("nl-NL");
//        browserInfo.setColorDepth(24);
//        browserInfo.setScreenHeight(723);
//        browserInfo.setScreenWidth(1536);
//        browserInfo.setTimeZoneOffset(0);
//        browserInfo.setJavaEnabled(true);
//        paymentsRequest.setBrowserInfo(browserInfo);

        paymentsRequest.setChannel(PaymentsRequest.ChannelEnum.WEB);
        paymentsRequest.setMerchantAccount(merchantAccount);
//        paymentsRequest.setSessionValidity("2022-04-06T16:30:00+08:00");


        String orderRef = UUID.randomUUID().toString();
        paymentsRequest.setReference(orderRef);
        paymentsRequest.setReturnUrl("http://localhost:8080/api/handleShopperRedirect?orderRef=" + orderRef);

//绑卡操作
//        paymentsRequest.setShopperReference("test_recurring");
//        paymentsRequest.setShopperInteraction(PaymentsRequest.ShopperInteractionEnum.CONTAUTH);
//        paymentsRequest.setRecurringProcessingModel(PaymentsRequest.RecurringProcessingModelEnum.CARD_ON_FILE);

        if (type.equals("alipay")) {
            paymentsRequest.setCountryCode("CN");

        } else if (type.contains("klarna")) {
            paymentsRequest.setShopperEmail("aaron.zhu@adyen.com");
            paymentsRequest.setShopperLocale("en_US");
            paymentsRequest.setCountryCode("DE");

            addLineItems(paymentsRequest);

        } else if (type.equals("directEbanking") || type.equals("giropay")) {
            paymentsRequest.countryCode("DE");

        } else if (type.equals("dotpay")) {
            paymentsRequest.countryCode("PL");
            paymentsRequest.getAmount().setCurrency("PLN");

        } else if (type.equals("vipps")) {
            paymentsRequest.telephoneNumber("98258879");
            paymentsRequest.shopperStatement("test vipps");
            paymentsRequest.channel(PaymentsRequest.ChannelEnum.ANDROID);

        } else if (type.equals("mbway")) {
            paymentsRequest.telephoneNumber("+351234567890");
            paymentsRequest.shopperStatement("Hi Shopper-c5");

        } else if (type.equals("atome")) {
            addLineItems(paymentsRequest);
            Name shopperName = new Name();
            shopperName.setFirstName("Aaron");
            shopperName.setLastName("Zhu");
            paymentsRequest.setShopperName(shopperName);
            paymentsRequest.setCountryCode("SG");
            paymentsRequest.setShopperEmail("aaron.zhu@adyen.com");
            paymentsRequest.setTelephoneNumber("80002018");
            Address billingAddress = new Address();
            billingAddress.setCity("Sydney");
            billingAddress.setCountry("AU");
            billingAddress.setHouseNumberOrName("123");
            billingAddress.setPostalCode("2000");
            billingAddress.setStateOrProvince("NSW");
            billingAddress.setStreet("Happy Street");
            paymentsRequest.setBillingAddress(billingAddress);
            Address deliveryAddress = new Address();
            deliveryAddress.setCity("Sydney");
            deliveryAddress.setCountry("AU");
            deliveryAddress.setHouseNumberOrName("123");
            deliveryAddress.setPostalCode("2000");
            deliveryAddress.setStateOrProvince("NSW");
            deliveryAddress.setStreet("Happy Street");
            paymentsRequest.setDeliveryAddress(deliveryAddress);

        } else if (type.equals("knet")) {

            paymentsRequest.setShopperEmail("aaron.zhu@adyen.com");
            paymentsRequest.setTelephoneNumber("+965 566 66666");
            Name shopperName = new Name();
            shopperName.setFirstName("Aaron");
            shopperName.setLastName("Zhu");
            shopperName.setGender(Name.GenderEnum.UNKNOWN);
            paymentsRequest.setShopperName(shopperName);
            paymentsRequest.setCountryCode("KW");

        } else if (type.equals("scheme")) {
            paymentsRequest.setOrigin("http://localhost:8080");
            paymentsRequest.putAdditionalDataItem("allow3DS2", "true");
            paymentsRequest.setShopperIP("0.0.0.1");

        } else if (type.equals("ach") || type.equals("paypal")) {
            paymentsRequest.countryCode("US");
        } else if (type.equals("afterpaytouch")){
            paymentsRequest.setCountryCode("US");
            paymentsRequest.setTelephoneNumber("+12123123124");
            paymentsRequest.setShopperEmail("aaron.zhu@adyen.com");
            Name shopperName = new Name();
            shopperName.setFirstName("Aaron");
            shopperName.setLastName("Zhu");
            paymentsRequest.setShopperName(shopperName);
            Address billingAddress = new Address();
            billingAddress.setCity("New York");
            billingAddress.setCountry("US");
            billingAddress.setHouseNumberOrName("123");
            billingAddress.setPostalCode("2000");
            billingAddress.setStateOrProvince("NY");
            billingAddress.setStreet("Happy Street");
            paymentsRequest.setBillingAddress(billingAddress);
            Address deliveryAddress = new Address();
            deliveryAddress.setCity("New York");
            deliveryAddress.setCountry("US");
            deliveryAddress.setHouseNumberOrName("123");
            deliveryAddress.setPostalCode("2000");
            deliveryAddress.setStateOrProvince("NY");
            deliveryAddress.setStreet("Happy Street");
            paymentsRequest.setDeliveryAddress(deliveryAddress);
            addLineItems(paymentsRequest);
        }else if (type.equals("clearpay")){
            paymentsRequest.setCountryCode("FR");
            paymentsRequest.setTelephoneNumber("+44 2 8520 3890");
            paymentsRequest.setShopperEmail("aaron.zhu@adyen.com");
            Name shopperName = new Name();
            shopperName.setFirstName("Aaron");
            shopperName.setLastName("Zhu");
            paymentsRequest.setShopperName(shopperName);
            Address billingAddress = new Address();
            billingAddress.setCity("Paris");
            billingAddress.setCountry("FR");
            billingAddress.setHouseNumberOrName("123");
            billingAddress.setPostalCode("2000");
            billingAddress.setStateOrProvince("NSW");
            billingAddress.setStreet("Happy Street");
            paymentsRequest.setBillingAddress(billingAddress);
            Address deliveryAddress = new Address();
            deliveryAddress.setCity("Paris");
            deliveryAddress.setCountry("FR");
            deliveryAddress.setHouseNumberOrName("123");
            deliveryAddress.setPostalCode("2000");
            deliveryAddress.setStateOrProvince("NSW");
            deliveryAddress.setStreet("Happy Street");
            paymentsRequest.setDeliveryAddress(deliveryAddress);
            addLineItems(paymentsRequest);
        }
        System.out.println("/payments request:\n" + paymentsRequest.toString());
        PaymentsResponse response = checkout.payments(paymentsRequest);

        System.out.println("/payments response:\n" + response);
        return response;
    }

    public PaymentsDetailsResponse submitPaymentsDetails(PaymentsDetailsRequest paymentsDetailsRequest) throws IOException, ApiException {
        System.out.println("/paymentsDetails request:" + paymentsDetailsRequest.toString());
        PaymentsDetailsResponse paymentsDetailsResponse = checkout.paymentsDetails(paymentsDetailsRequest);
        System.out.println("paymentsDetails response:\n" + paymentsDetailsResponse.toString());
        return paymentsDetailsResponse;
    }


    private Amount getAmount(String type) {

        String currency;

        switch (type) {
            case "alipay":
                currency = "CNY";
                break;
            case "dotpay":
                currency = "PLN";
                break;
            case "afterpaytouch":
                currency = "USD";
                break;
            case "boletobancario":
                currency = "BRL";
                break;
            case "ach":
            case "paypal":
                currency = "USD";
                break;
            case "blik":
                currency = "PLN";
                break;
            case "econtext_stores":
                currency = "JPY";
                break;
            case "econtext_seven_eleven":
                currency = "JPY";
                break;
            case "econtext_atm":
                currency = "JPY";
                break;
            case "econtext_online":
                currency = "JPY";
                break;
            case "kakaopay":
                currency = "KRW";
                break;
            case "kcp_payco":
                currency = "KRW";
                break;
            case "korean_local_card":
                currency = "KRW";
                break;
            case "twint":
                currency = "CHF";
                break;
            case "knet":
                currency = "KWD";
                break;
            case "vipps":
                currency = "NOK";
                break;
            case "dragonpay_otc_banking":
                currency = "PHP";
                break;
            case "atome":
                currency = "SGD";
                break;
            case "molpay_ebanking_TH":
                currency = "THB";
                break;
            default:
                currency = "EUR";
        }
        Amount amount = new Amount();

        amount.setCurrency(currency);
        amount.setValue(1000L);
        return amount;
    }

    private static void addLineItems(PaymentsRequest paymentsRequest) {
        String item1 = "{\n" +
                "                \"quantity\": \"1\",\n" +
                "                \"amountExcludingTax\": \"450\",\n" +
                "                \"taxPercentage\": \"1111\",\n" +
                "                \"description\": \"Sunglasses\",\n" +
                "                \"id\": \"Item #1\",\n" +
                "                \"taxAmount\": \"50\",\n" +
                "                \"amountIncludingTax\": \"500\",\n" +
                "                \"taxCategory\": \"High\"\n" +
                "            }";
        String item2 = "{\n" +
                "                \"quantity\": \"1\",\n" +
                "                \"amountExcludingTax\": \"450\",\n" +
                "                \"taxPercentage\": \"1111\",\n" +
                "                \"description\": \"Headphones\",\n" +
                "                \"id\": \"Item #2\",\n" +
                "                \"taxAmount\": \"50\",\n" +
                "                \"amountIncludingTax\": \"500\",\n" +
                "                \"taxCategory\": \"High\"\n" +
                "            }";

        Gson gson = new GsonBuilder().create();
        LineItem lineItem1 = gson.fromJson(item1, LineItem.class);
        LineItem lineItem2 = gson.fromJson(item2, LineItem.class);

        paymentsRequest.addLineItemsItem(lineItem1);
        paymentsRequest.addLineItemsItem(lineItem2);
    }
}
