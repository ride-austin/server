<#-- @ftlvariable name="driver" type="com.rideaustin.model.user.Driver" -->
<#-- @ftlvariable name="car" type="com.rideaustin.model.ride.Car" -->
<#-- @ftlvariable name="city" type="com.rideaustin.model.City" -->
<#include "header.ftl">

Hello ${city.getAppName()} Support,
<p>Driver ${driver.user.getFullName()} (ID ${driver.getId()}) has updated insurance information for ${car.getMake()} ${car.getModel()} (${car.getYear()}) (ID ${car.getId()}).</p>

<#include "footer.ftl">
