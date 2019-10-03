<#include "header.ftl">

Support message from <b>${userFullName} (${userEmail})</b>

<ul>
    <li>Submitter : <b>${userFullName}</b></li>
    <li>Ride ID : <b>${rideId}</b></li>
    <#if rideDate??>
        <li>Ride Date : <b>${rideDate}</b></li>
    </#if>
    <li>Application : <b>${application}</b></li>
    <li>Topic : <b>${topic}</b></li>
    <#if subTopic??>
        <li>Issue : <b>${subTopic}</b></li>
    </#if>
    <li>
        Details:
         <p>
            <span style="display: inline-block; width: 450px;">
                <i>${message}</i>
            <span>
          </p>
    </li>
</ul>

</div>
</div>
<div style="font-family:'Helvetica Heue', Arial, sans-serif;padding:15px 25px;color:#6C6C67;border-top:2px solid #dddddd; line-height: 18px;">
    <table width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
        <tr>
            <td>
                <div style="font-size:10px;color:#6C6C67;text-align:left">
                    <a href="${city.pageUrl}/about-us/" style="color:#6C6C67;text-decoration:none;"
                       target="_blank">About Us</a>
                    |
                    <a href="${city.pageUrl}/faq/" style="color:#6C6C67;text-decoration:none;"
                       target="_blank">FAQ</a>
                    |
                    <a href="${city.pageUrl}/new-page/" style="color:#6C6C67;text-decoration:none;"
                       target="_blank">Donate</a>
                    |
                    <a href="${city.pageUrl}/contact/" style="color:#6C6C67;text-decoration:none;"
                       target="_blank">Contact</a>
                </div>
                <p style="color:#808080;font-family: 'Helvetica Heue', Arial, sans-serif; font-size: 10px; line-height: 18px;margin:0">&copy;
                    ${city.appName}</p>
            </td>
            <td>
                <div style="font-size:8px;color:#6C6C67;text-align:right">
                    <p style="color:#808080;font-family: 'Helvetica Heue', Arial, sans-serif; font-size: 10px; line-height: 18px;margin:0">
                        <a
                            href="${rideDetailsLink}" target="_blank"
                            style="Margin:0;color:#191313;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-weight:400;line-height:1.3;margin:0;padding:0;text-align:left;text-decoration:none">
                            <i>Reference</i>
                         </a>
                    </p>
                </div>
             </td>
        </tr>
    </table>
</div>
</div>
</td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</div>
</body>
</html>
