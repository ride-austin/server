<#include "header.ftl">
<p>
    Driver ${driverName} (ID: #{avatarId}, Email: <a href="mailto:${driverEmail}">${driverEmail}</a>)
    has found an item.
</p>


<p>
    <strong>Item found at:</strong> ${foundOn}
</p>
<p>
    <strong>Ride ID:</strong> <pre>${rideId}</pre>
</p>
<p>
    <strong>Ride description:</strong> <pre>${rideInfo}</pre>
</p>
<p>
    <strong>Item description:</strong> <pre>${itemDetails}</pre>
    <#if url?has_content>
        <img src="${url}"/>
    </#if>
</p>

<#if sharingAllowed>
    <p>
        Driver allowed to share their contact details: ${driverPhone}.
    </p>
</#if>
<#if !sharingAllowed>
    <p>
        Driver disallowed to share their contact details.
    </p>
</#if>

<#include "footer.ftl">