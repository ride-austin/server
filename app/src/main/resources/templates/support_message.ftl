<#include "header.ftl">

Support message from <b>${userFullName} (${userEmail})</b> <#if application??>Application: ${application}</#if>

<p>Message content: <i>${message}</i></p>

<#if isRidePresent>
Additional ride details:
<ul>
    <li>ID: <b>${rideId}</b></li>
    <li>Ride status: <b>${rideStatus}</b></li>
    <#if rideStartedOn??>
    <li>Start date: <b>${rideStartedOn}</b></li>
    </#if>
    <#if rideCompletedOn??>
    <li>Completed date: <b>${rideCompletedOn}</b></li>
    </#if>
    <#if isRiderPresent>
    <li>Rider details: <b>${riderFullName} (${riderEmail}) </b>
        <i>(ID:${riderId})</i></li>
    </#if>
    <#if isDriverPresent>
    <li>Driver details: <b>${driverFullName} (${driverEmail}) </b>
        <i>(ID:${avatarId})</i></li>
    </#if>
</ul>

</#if>

</div>
</div>
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
