<#-- @ftlvariable name="driver" type="com.rideaustin.model.user.Driver" -->
<#include "header.ftl">

Hello ${driver.user.getFullName()},
<p>Your driver license will expire on <span
        style="font-weight: bold;">${driver.getLicenseExpiryDate()?string("MM/dd/yyyy")}</span>.</p>

<#include "footer.ftl">
