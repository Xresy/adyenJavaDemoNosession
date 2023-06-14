const clientKey = document.getElementById('client-key').innerHTML;
const type = document.getElementById('integration-type').innerHTML;

async function initCheckout() {
    try {
        const paymentMethodsResponse = await callServer("/api/getPaymentMethods");
        const configuration = {
            paymentMethodsResponse: filterUnimplemented(paymentMethodsResponse),
            clientKey,
//            locale: "ar",
            environment: "live",
            showPayButton: true,
            paymentMethodsConfiguration: {
                ideal: {
                    showImage: true,
                },
                card: {
                    koreanAuthenticationRequired: true,
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
//                    enableStoreDetails: true,
//                    hideCVC: true,
                    hasHolderName: true,
                    holderNameRequired: true,
                    name: "Credit or debit card",
                    brands: ['mc','visa','amex','cartebancaire','cup'],
                    amount: {
                        value: 1000,
                        currency: "EUR",
                    },
                },
                paypal: {
                    amount: {
                        currency: "USD",
                        value: 1000
                    },
                    environment: "test", // Change this to "live" when you're ready to accept live PayPal payments
                    countryCode: "US", // Only needed for test. This will be automatically retrieved when you are in production.
                    onCancel: (data, component) => {
                        component.setStatus('ready');
                    },
                },
                googlepay: {
                    amount: {
                        value: 1000,
                        currency: "EUR"
                    },
                    countryCode: "NL",
                    //Set this to PRODUCTION when you're ready to accept live payments
                    environment: "TEST",
//                    showPayButton: false,
                },
            },
            onSubmit: (state, component) => {
                if (state.isValid) {
                    console.log(state.data);
                    handleSubmission(state, component, "/api/initiatePayment");
                }
            },
            onChange: (state, component) => {
                            console.log(state.data);
                        },




//            onChange:function(test){console.log(test)},
//            onChange: (state, component) => {
//            console.log(state);
//            console.log(component);
//            },
            onAdditionalDetails: (state, component) => {
                handleSubmission(state, component, "/api/submitAdditionalDetails");
            },
        };

        const checkout = new AdyenCheckout(configuration);
        const test = checkout.create(type).mount("#component");
////        //自定义按钮
//        document.getElementById('test').addEventListener('click', function() {
//              test.submit()
//          });

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
            "dragonpay_otc_banking",
            "paywithgoogle",
            "afterpaytouch",
            "econtext_stores",
            "econtext_seven_eleven",
            "econtext_atm",
            "econtext_online",
            "clearpay",
            "kakaopay",
            "kcp_payco",
            "korean_local_card",
            "twint",
            "paysafecard",
            "wechatpayQR",
            "knet",
            "vipps",
            "mbway",
            "netaxept_bankaxess",
            "atome",
            "eps",
            "molpay_ebanking_TH",
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
        console.log(res.action);
//        component.handleAction(res.action);

document.getElementById("component2").remove()
      console.log("unmounted the div");
      let div = document.createElement("div");
      div.className = "payment";
      div.id = "component2" ;
      document.body.appendChild(div);
      console.log("re-mounted the div");

    const configuration = {
//         locale: "en_US",
         environment: "test",
         clientKey: "test_GBI6RMQCHZCJHA5XQM2KSRQLYQFEMRZC",
         onAdditionalDetails: (state, component) => {
                         handleSubmission(state, component, "/api/submitAdditionalDetails");
                     },
     };


     const checkout2 = new AdyenCheckout(configuration);

     const threeDSConfiguration = {
       challengeWindowSize: '02'
        // Set to any of the following:
        // '02': ['390px', '400px'] -  The default window size
        // '01': ['250px', '400px']
        // '03': ['500px', '600px']
        // '04': ['600px', '400px']
        // '05': ['100%', '100%']
     }
     checkout2.createFromAction(res.action,threeDSConfiguration).mount('#component2');


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
//            component.unmount();
            console.log("here");
    }
}

initCheckout();
