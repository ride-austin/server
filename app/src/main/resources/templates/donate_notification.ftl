<#include "header.ftl">

Hello!

${fullName} (${email}) has left a donation at ${address} (<a href="https://www.google.com/maps/search/?api=1&query=${location}">View on Google Maps</a>).

<#if comment??>
User comment: ${comment}
</#if>

<#include "footer.ftl">
