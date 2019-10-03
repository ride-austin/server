<#include "header.ftl">

${user.getFullName()}  wants you to follow their ${city.appName} trip at <a href="${url}">${url}</a>

<br/>
<br/>
<div style="font-family:'Helvetica Heue', Arial, sans-serif;padding:15px 25px;color:#6C6C67;border-top:2px solid #dddddd; line-height: 18px;">
    <table width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
        <tr>
            <td>
                <p style="color:#808080;font-family: 'Helvetica Heue', Arial, sans-serif; font-size: 10px; line-height: 18px;margin:0">&copy;
                    ${city.appName}</p>
            </td>
        </tr>
    </table>
</div>

<#include "footer.ftl">

