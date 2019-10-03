<#include "header.ftl">

Hello ${user.getFullName()},
<p>Someone requested a password reset for your account ${user.getEmail()}. If it was you, please click the
<a href="${link}">link</a>. If you didn't request a password reset, you can ignore this message, the password
    reset request will expire in 24 hours.
</p>

<#include "footer.ftl">