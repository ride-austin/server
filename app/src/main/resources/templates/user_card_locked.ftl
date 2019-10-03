<#-- @ftlvariable name="driver" type="com.rideaustin.model.user.Driver" -->
<#include "header.ftl">

Hello ${user.getFullName()},
<p>Credit card attached to your account is locked due to unsuccessful payment. Please contact ${city.appName} Support for more information.</p>

<#include "footer.ftl">
