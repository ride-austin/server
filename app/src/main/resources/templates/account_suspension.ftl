<#include "reminder_header.ftl">
                    <tr>
                        <td valign="top" id="templateBody"
                            style="mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;background: #FFFFFF none no-repeat center;background-size: cover;border-top: 0;border-bottom: 2px solid #EAEAEA;padding-top: 0;padding-bottom: 9px;">
                            <table border="0" cellpadding="0" cellspacing="0" width="100%" class="mcnTextBlock"
                                   style="min-width: 100%;border-collapse: collapse;mso-table-lspace: 0;mso-table-rspace: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;">
                                <tbody class="mcnTextBlockOuter">
                                <tr>
                                    <td valign="top" class="mcnTextBlockInner"
                                        style="padding-top: 9px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;">
                                        <!--[if mso]>
                                        <table align="left" border="0" cellspacing="0" cellpadding="0" width="100%"
                                               style="width:100%;">
                                            <tr>
                                        <![endif]-->

                                        <!--[if mso]>
                                        <td valign="top" width="600" style="width:600px;">
                                        <![endif]-->
                                        <table align="left" border="0" cellpadding="0" cellspacing="0"
                                               style="max-width: 100%;min-width: 100%;border-collapse: collapse;mso-table-lspace: 0;mso-table-rspace: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;"
                                               width="100%" class="mcnTextContentContainer">
                                            <tbody>
                                            <tr>

                                                <td valign="top" class="mcnTextContent"
                                                    style="padding: 0 18px 9px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;word-break: break-word;color: #202020;font-family: Helvetica;font-size: 16px;line-height: 150%;text-align: left;">

                                                    <div style="text-align: center;"><font color="#0000cd"><span
                                                            style="font-size:24px">Account Suspension</span></font>
                                                    </div>

                                                    <div style="text-align: left;"><br>
                                                        ${driver.firstname}<br>
                                                        <br>
                                                        Your account has been suspended ${content}.<br>
                                                        <br>
                                                        We will be reviewing your account and will determine whether
                                                        you will be re-activated or deactivated permanently.<br>
                                                        <br>
                                                        Thank you for your cooperation,<br>
                                                        <br>
                                                        Team ${city.appName}<br>
                                                        &nbsp;</div>

                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                        <!--[if mso]>
                                        </td>
                                        <![endif]-->

                                        <!--[if mso]>
                                        </tr>
                                        </table>
                                        <![endif]-->
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
<#include "reminder_footer.ftl">
