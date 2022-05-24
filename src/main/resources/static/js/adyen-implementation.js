const clientKey = document.getElementById('client-key').innerHTML;
const type = document.getElementById('integration-type').innerHTML;

async function initCheckout() {
    try {
        const paymentMethodsResponse = await callServer("/api/getPaymentMethods");
        const configuration = {
            paymentMethodsResponse: filterUnimplemented(paymentMethodsResponse),
            clientKey,
            locale: "en_US",
            environment: "test",
            showPayButton: true,
            paymentMethodsConfiguration: {
                ideal: {
                    showImage: true,
                },
                card: {
                    installmentOptions: {
                            card: {
                                // Shows 1, 2, and 3 as the numbers of monthly installments shoppers can choose.
                                values: [1, 2, 3],
                                // Shows regular and revolving as plans shoppers can choose.
                                plans: [ 'regular', 'revolving' ]
                            },
                          // Shows payment amount per installment.
                    showInstallmentAmounts: true
                    },
                    hasHolderName: true,
                    holderNameRequired: true,
                    name: "Credit or debit card",
                    amount: {
                        value: 1000,
                        currency: "EUR",
                    },
                },
                paypal: {
                    amount: {
                        currency: "EUR",
                        value: 1000
                    },
                    environment: "test", // Change this to "live" when you're ready to accept live PayPal payments
                    countryCode: "US", // Only needed for test. This will be automatically retrieved when you are in production.
                    onCancel: (data, component) => {
                        component.setStatus('ready');
                    },
                },
                applepay: {

                amount: {
                        value: 1000,
                        currency: "EUR"
                    },
                countryCode: "DE"

                }
//                googlepay: {
//                    amount: {
//                        value: 1000,
//                        currency: "EUR"
//                    },
//                    countryCode: "NL",
//                    //Set this to PRODUCTION when you're ready to accept live payments
//                    environment: "TEST"
//                }
            },
            onSubmit: (state, component) => {
                if (state.isValid) {
                    handleSubmission(state, component, "/api/initiatePayment");
                }
            },
//            onChange:function(test){console.log(test)},
            onChange: (state, component) => {
            console.log(state);
            console.log(component);
            },
            onAdditionalDetails: (state, component) => {
                handleSubmission(state, component, "/api/submitAdditionalDetails");
            },
        };

        const checkout = new AdyenCheckout(configuration);
        checkout.create(type).mount("#component");
    } catch (error) {
        console.error(error);
        alert("Error occurred. Look at console for details");
    }
}

function filterUnimplemented(pm) {
    pm.paymentMethods = pm.paymentMethods.filter((it) =>
        [
            "scheme",
            "ideal",
            "dotpay",
            "giropay",
            // "sepadirectdebit",
            "directEbanking",
            "ach",
            "alipay",
            "klarna_paynow",
            "klarna",
            "klarna_account",
            "paypal",
            "boletobancario_santander",
            "blik",
            "dragonpay_ebanking",
            "paywithgoogle",
            "wechatpayWeb",
            "applepay",
        ].includes(it.type)
    );
    return pm;
}

// Event handlers called when the shopper selects the pay button,
// or when additional information is required to complete the payment
async function handleSubmission(state, component, url) {
    try {
        const res = await callServer(url, state.data);
        handleServerResponse(res, component);
    } catch (error) {
        console.error(error);
        alert("Error occurred. Look at console for details");
    }
}

// Calls your server endpoints
async function callServer(url, data) {
    const res = await fetch(url, {
        method: "POST",
        body: data ? JSON.stringify(data) : "",
        headers: {
            "Content-Type": "application/json",
        },
    });

    return await res.json();
}

// Handles responses sent from your server to the client
function handleServerResponse(res, component) {
    if (res.action) {
        component.handleAction(res.action);
    } else {
        switch (res.resultCode) {
            case "Authorised":
                window.location.href = "/result/success";
                break;
            case "Pending":
            case "Received":
                window.location.href = "/result/pending";
                break;
            case "Refused":
                window.location.href = "/result/failed";
                break;
            default:
                window.location.href = "/result/error";
                break;
        }
    }
}

initCheckout();
