<#-- @ftlvariable name="ride" type="com.rideaustin.model.ride.Ride" -->
<#-- @ftlvariable name="message" type="java.lang.String" -->
<#include "header.ftl">

<#assign rider = ride.getRider()>
<p>Hello ${rider.getFullName()}!</p>

<#if card??>
<p>Sorry, your credit card ****${card.getCardNumber()} was declined. Please try another card.</p>
<#else>
<p>Sorry, your payment was declined. Please try another payment option.</p>
</#if>

<#include "footer.ftl">
