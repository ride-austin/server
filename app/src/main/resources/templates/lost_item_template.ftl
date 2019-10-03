<#include "header.ftl">
<p>
    Rider ${riderName} (ID: #{riderId}, Email: <a href="mailto:${riderEmail}">${riderEmail}</a>)
    has lost an item during the ride #{rideId} with driver ${driverName} (${driverEmail}).
</p>

<p>
    <strong>Item description:</strong> <pre>${description}</pre>
</p>
<p>
    <strong>Details of incident:</strong> <pre>${details}</pre>
</p>

<p>
    Rider contact details: ${riderPhone}
</p>

<#include "footer.ftl">