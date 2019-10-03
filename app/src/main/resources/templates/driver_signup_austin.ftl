<#include "header.ftl">
<STYLE type="text/css">
    OL.arabic { list-style-type: arabic numbers }
</STYLE>

Hello ${driver.getUser().getFirstname()},

<p>Thank you for signing up to drive with Ride|Austin!</p>

<p>
    <strong>Instructions:</strong>
</p>
<ol class="arabic">
    <li>
        <p>
            We use a 3rd party payments processing company called Payoneer - please sign up for direct deposits at the
            custom link below. Most people are approved quickly, but make sure to reply to any requests from Payoneer for
            additional documentation.
        </p>
        <p>
            <a href="${payoneerLink}">Click here to setup Payoneer</a>
        </p>
    </li>
</ol>
<p>
    <strong>Steps to obtain a Chauffeur's Permit:</strong>
</p>
<ol class="arabic">
    <li>
        <p>
            Call (512) 974-1438 to verify your current fingerprint status
        </p>
    </li>
    <li>
        <p>
            Complete the <a href="https://austintexas.gov/sites/default/files/files/Transportation/Ground_Transportation/Chauffeur_Permit_application_09-21-2017.pdf" target="_blank">application</a for a Chauffeur's permit.
        </p>
    </li>
    <li>
        <p>
            Download and print your type 3A - 3 year driving record (available at: <a href="http://www.dps.texas.gov/DriverLicense/driverrecords.htm" target="_blank">http://www.dps.texas.gov/DriverLicense/driverrecords.htm</a>).
        </p>
    </li>
    <li>
        <p>
            Pass a 20 question multiple choice test on driver and passenger's safety.
        </p>
    </li>
</ol>
<p>
    Please make sure you have all the forms ready and the $20 fee in cash or check <strong>only!</strong>
</p>
<p>
    The City of Austin Ground Transportation is located at: 1111 Rio Grande
</p>
<p style="color:#0000FF">
    Hours of operation:<br/>
    Monday - Friday - 8:30am - 11:30am
</p>
<p>
    Submit a picture of your Chauffeur's permit via email at <a href="mailto:documents@example.com" target="_blank">documents@example.com</a>
    to get it approved.
</p>
<p>
    Looking forward to getting you on the road soon!
</p>
<p>&nbsp;</p>

<#include "footer.ftl">
