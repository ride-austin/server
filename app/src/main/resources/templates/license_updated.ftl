<#-- @ftlvariable name="driver" type="com.rideaustin.model.user.Driver" -->
<#-- @ftlvariable name="city" type="com.rideaustin.model.City" -->
<#include "header.ftl">

Hello ${city.getAppName()} Support,
<p>Driver ${driver.user.getFullName()} (ID ${driver.getId()}) has updated driver license information.</p>

<#include "footer.ftl">
