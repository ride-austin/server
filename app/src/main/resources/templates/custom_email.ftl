<#include "header.ftl">

<p>Dear ${driver.fullName},</p>

<#outputformat "HTML">
    ${content?replace("\n", "<br />")?no_esc}
</#outputformat>

<#include "footer.ftl">
