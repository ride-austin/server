<#-- @ftlvariable name="ride" type="com.rideaustin.model.ride.Ride" -->
<#-- @ftlvariable name="message" type="java.lang.String" -->
<#include "header.ftl">

<#assign rider = ride.getRider()>
<p>An error occurred while charging rider #${rider.getId()?c}, ${rider.getFullName()?html}, ${rider.getEmail()?html}
    for ride #${ride.getId()?c}:</p>
<p>${message}</p>

<#include "footer.ftl">
