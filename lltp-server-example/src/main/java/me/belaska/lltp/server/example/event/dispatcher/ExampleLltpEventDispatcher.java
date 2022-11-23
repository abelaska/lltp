package me.belaska.lltp.server.example.event.dispatcher;

import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.server.example.event.PaymentAuthorizationLltpEvent;
import me.belaska.lltp.server.example.event.PaymentAuthorizationLltpEventResponse;
import me.belaska.lltp.server.example.event.RechargeLltpEvent;
import me.belaska.lltp.server.example.event.RechargeLltpEventResponse;

public interface ExampleLltpEventDispatcher extends LltpEventDispatcher {

	//
	PaymentAuthorizationLltpEventResponse paymentAuthorization(PaymentAuthorizationLltpEvent event);

	void paymentAuthorizationResponse(PaymentAuthorizationLltpEventResponse eventResponse);

	//
	RechargeLltpEventResponse recharge(RechargeLltpEvent event);

	void rechargeResponse(RechargeLltpEventResponse eventResponse);
}
