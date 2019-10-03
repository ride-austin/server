<#-- @ftlvariable name="earnings" type="com.rideaustin.service.model.DriverEarnings" -->
<#setting locale="en_US">
<#macro formatMoney money>$${money.getAmount()?string("#.00")}</#macro>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en" style="background:#000">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600" rel="stylesheet" type="text/css">
    <title>My Email Subject</title></head>
<body style="-moz-box-sizing:border-box;-ms-text-size-adjust:100%;-webkit-box-sizing:border-box;-webkit-text-size-adjust:100%;Margin:0;background:#000;box-sizing:border-box;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;margin:0;min-width:100%;padding:0;text-align:left;width:100%!important">
<style>@media only screen {
    html {
        min-height: 100%;
        background: #f3f3f3
    }
}

@media only screen and (max-width: 656px) {
    table.body img {
        width: auto !important;
        height: auto !important
    }

    table.body center {
        min-width: 0 !important
    }

    table.body .container {
        width: 95% !important
    }

    td.small-6, th.small-6 {
        display: inline-block !important;
        width: 50% !important
    }

    td.small-12, th.small-12 {
        display: inline-block !important;
        width: 100% !important
    }

    .column td.small-12, .column th.small-12, .columns td.small-12, .columns th.small-12 {
        display: block !important;
        width: 100% !important
    }

    table.menu td, table.menu th {
        width: auto !important;
        display: inline-block !important
    }

    table.menu.small-vertical td, table.menu.small-vertical th, table.menu.vertical td, table.menu.vertical th {
        display: block !important
    }

}

@media only screen and (max-width: 656px) {
    .right-border {
        border-right: 0 solid #c7c7c7
    }
}

@media only screen and (max-width: 656px) {
    .width20 {
        max-width: 20px
    }

    .bottom10sm {
        margin-bottom: 10px !important
    }
}

@media only screen and (max-width: 656px) {
    table.body .container.content-header .header h1 {
        font-size: 16px
    }
}
</style>
<table class="body"
       style="Margin:0;background:#000;border-collapse:collapse;border-spacing:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;height:100%;line-height:19px;padding:0;text-align:left;vertical-align:top;width:100%">
    <tr style="padding:0;text-align:left;vertical-align:top">
        <td class="center" align="center" valign="top"
            style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;hyphens:auto;line-height:19px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
            <center data-parsed="" style="min-width:640px;width:100%">
                <table class="container header float-center"
                       style="Margin:0 auto;background:#000;border-collapse:collapse;border-spacing:0;float:none;padding:0;text-align:center;vertical-align:top;width:640px">
                    <tbody>
                    <tr style="padding:0;text-align:left;vertical-align:top">
                        <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;hyphens:auto;line-height:19px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                            <table class="spacer"
                                   style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <td height="50px"
                                        style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:50px;font-weight:400;hyphens:auto;line-height:50px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                                        &#xA0;</td>
                                </tr>
                                </tbody>
                            </table>
                            <table class="row"
                                   style="border-collapse:collapse;border-spacing:0;display:table;padding:0;position:relative;text-align:left;vertical-align:top;width:100%">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <th class="logo small-6 large-6 columns first"
                                        style="Margin:0 auto;background:#000;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0 8px 0 0;text-align:left;width:304px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <img class="width140"
                                                         src="${city.logoUrl}"
                                                         alt="${city.appName} logo"
                                                         style="-ms-interpolation-mode:bicubic;clear:both;display:block;max-width:140px;outline:0;text-decoration:none;width:auto;">
                                                </th>
                                            </tr>
                                        </table>
                                    </th>
                                    <th class="date small-6 large-6 columns last"
                                        style="Margin:0 auto;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;letter-spacing:0.4px;line-height:19px;padding: 15px 0 0 8px;text-align:left;width:304px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <p class="text-right"
                                                       style="Margin:0;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;padding:0;text-align:right;text-transform:uppercase">
                                                    ${.now?string("MMM d, yyyy")}</p></th>
                                            </tr>
                                        </table>
                                    </th>
                                </tr>
                                </tbody>
                            </table>
                            <table class="spacer"
                                   style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <td height="15px"
                                        style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:15px;font-weight:400;hyphens:auto;line-height:15px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                                        &#xA0;</td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <table class="container content-header float-center"
                       style="-moz-border-radius:4px 4px 0 0;-ms-border-radius:4px 4px 0 0;-o-border-radius:4px 4px 0 0;-webkit-border-radius:4px 4px 0 0;Margin:0 auto;background:#fefefe;border-collapse:collapse;border-radius:4px 4px 0 0;border-spacing:0;float:none;padding:0;text-align:center;vertical-align:top;width:640px">
                    <tbody>
                    <tr style="padding:0;text-align:left;vertical-align:top">
                        <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;hyphens:auto;line-height:19px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                            <table class="row"
                                   style="border-collapse:collapse;border-spacing:0;display:table;padding:0;position:relative;text-align:left;vertical-align:top;width:100%">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <th class="header small-12 large-12 columns first last"
                                        style="-moz-border-radius:4px 4px 0 0;-ms-border-radius:4px 4px 0 0;-o-border-radius:4px 4px 0 0;-webkit-border-radius:4px 4px 0 0;Margin:0 auto;background:#f5f6f6;border-radius:4px 4px 0 0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 10px 16px 0;text-align:left;width:624px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <h1 class="text-bold topp10 bottomp10 bottom0"
                                                        style="Margin:0;color:#1b2129;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:18px;font-weight:600;line-height:1.3;padding: 10px 0;text-align:left;word-wrap:normal">
                                                        Your earnings for the week
                                                        of ${earnings.getReportDate()?string("MMM d, yyyy")}</h1></th>
                                                <th class="expander"
                                                    style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0!important;text-align:left;visibility:hidden;width:0"></th>
                                            </tr>
                                        </table>
                                    </th>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <table class="container content float-center"
                       style="-moz-border-radius:0 0 2px 2px;-ms-border-radius:0 0 2px 2px;-o-border-radius:0 0 2px 2px;-webkit-border-radius:0 0 2px 2px;Margin:0 auto;background:#fff;border:1px solid #d8d8d8;border-collapse:collapse;border-radius:0 0 2px 2px;border-spacing:0;float:none;padding:0;text-align:center;vertical-align:top;width:640px">
                    <tbody>
                    <tr style="padding:0;text-align:left;vertical-align:top">
                        <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;hyphens:auto;line-height:19px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                            <table class="row"
                                   style="border-collapse:collapse;border-spacing:0;display:table;padding:0;position:relative;text-align:left;vertical-align:top;width:100%">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <th class="small-12 large-4 columns first last"
                                        style="Margin:0 auto;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0 16px;text-align:left;width:197px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <table class="spacer"
                                                           style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                                        <tbody>
                                                        <tr style="padding:0;text-align:left;vertical-align:top">
                                                            <td height="30px"
                                                                style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:30px;font-weight:400;hyphens:auto;line-height:30px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                                                                &#xA0;</td>
                                                        </tr>
                                                        </tbody>
                                                    </table>
                                                    <p class="uppercase text-center text-blue font13"
                                                       style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:13px;font-weight:400;line-height:19px;padding:0;text-align:center;text-transform:uppercase">
                                                        Total payout</p>

                                                    <p class="uppercase text-center font50 line-height50"
                                                       style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:50px;font-weight:400;line-height:50px;padding:0;text-align:center;text-transform:uppercase">
                                                    <@formatMoney earnings.getTotalEarnings()/>
                                                    </p>
                                                    <table class="spacer"
                                                           style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                                        <tbody>
                                                        <tr style="padding:0;text-align:left;vertical-align:top">
                                                            <td height="30px"
                                                                style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:30px;font-weight:400;hyphens:auto;line-height:30px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                                                                &#xA0;</td>
                                                        </tr>
                                                        </tbody>
                                                    </table>
                                                </th>
                                            </tr>
                                        </table>
                                    </th>
                                </tr>
                                </tbody>
                            </table>
                            <table class="row"
                                   style="border-collapse:collapse;border-spacing:0;display:table;padding:0;position:relative;text-align:left;vertical-align:top;width:100%">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <th class="right-border bottom10sm small-12 large-4 columns first"
                                        style="Margin:0 auto;border-right:1px solid #c7c7c7;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0 8px 0 16px;text-align:left;width:197px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <p class="text-center text-dark font36 line-height36"
                                                       style="Margin:0;color:#070d16;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:36px;font-weight:400;line-height:36px;padding:0;text-align:center">
                                                        ${earnings.getDriver().getRating()?string("0.##")}
                                                    </p>

                                                    <p class="text-center text-grey text-light uppercase"
                                                       style="Margin:0;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:center;text-transform:uppercase">
                                                        Current rating</p></th>
                                            </tr>
                                        </table>
                                    </th>
                                    <th class="right-border bottom10sm small-12 large-4 columns"
                                        style="Margin:0 auto;border-right:1px solid #c7c7c7;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0 8px;text-align:left;width:197px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <p class="text-center text-dark font36 line-height36"
                                                       style="Margin:0;color:#070d16;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:36px;font-weight:400;line-height:36px;padding:0;text-align:center">
                                                    ${earnings.getHoursOnline()}</p>

                                                    <p class="text-center text-grey uppercase"
                                                       style="margin: 0;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:center;text-transform:uppercase">
                                                        Hours Online</p></th>
                                            </tr>
                                        </table>
                                    </th>
                                    <th class="small-12 large-4 columns last"
                                        style="Margin:0 auto;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0 16px 0 8px;text-align:left;width:197px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <p class="text-center text-dark font36 line-height36"
                                                       style="Margin:0;color:#070d16;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:36px;font-weight:400;line-height:36px;padding:0;text-align:center">
                                                    ${earnings.getTotalRides()}</p>

                                                    <p class="text-center text-grey uppercase"
                                                       style="Margin:0;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:center;text-transform:uppercase">
                                                        Trips</p></th>
                                            </tr>
                                        </table>
                                    </th>
                                </tr>
                                </tbody>
                            </table>
                            <table class="spacer"
                                   style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <td height="60px"
                                        style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:60px;font-weight:400;hyphens:auto;line-height:60px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                                        &#xA0;</td>
                                </tr>
                                </tbody>
                            </table>
                            <table class="row"
                                   style="border-collapse:collapse;border-spacing:0;display:table;padding:0;position:relative;text-align:left;vertical-align:top;width:100%">

                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <th class="small-12 large-12 columns first last"
                                        style="Margin:0 auto;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0 16px;text-align:left;width:624px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <table class="earnings-table"
                                                           style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                                        <thead style="border-bottom:2px solid #6ec8f9">
                                                        <tr style="padding:0;text-align:left;vertical-align:top">
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Day</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Trips</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Base fare</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Distance fare</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Time fare</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Total fare</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">${city.appName}
                                                                    fee</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Tips</span>
                                                            </th>
                                                            <th style="Margin:0;color:#6ec8f9;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:11px;font-weight:400;line-height:19px;max-width:50px;padding: 0 0 10px;text-align:center;text-transform:uppercase">
                                                                <span style="display:inline-block">Driver earning</span>
                                                            </th>
                                                        </tr>
                                                        </thead>
                                                    <#list earnings.getDailyEarnings() as dailyEarningEntry>
                                                        <#assign dailyEarning = dailyEarningEntry.value>
                                                        <tbody>
                                                        <tr style="padding:0;text-align:left;vertical-align:top">
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block">${dailyEarningEntry.key?string("MMM d")}</span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block">${dailyEarning.getRideCount()}</span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getBaseFare()/></span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getDistanceFare()/></span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getTimeFare()/></span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getTotalFare()/></span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getRideAustinFee()/></span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getTips()/></span>
                                                            </td>
                                                            <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-bottom:1px solid #d1d1d1;border-collapse:collapse!important;color:#8B8D8F;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;hyphens:auto;line-height:19px;max-width:50px;padding:10px 0;text-align:center;vertical-align:middle;word-wrap:break-word">
                                                                <span style="display:inline-block"><@formatMoney dailyEarning.getEarning()/></span>
                                                            </td>
                                                        </tr>
                                                        </tbody>
                                                    </#list>
                                                    </table>
                                                </th>
                                                <th class="expander"
                                                    style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0!important;text-align:left;visibility:hidden;width:0"></th>
                                            </tr>
                                        </table>
                                    </th>
                                </tr>
                                </tbody>
                            </table>
                            <table class="spacer"
                                   style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <td height="30px"
                                        style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:30px;font-weight:400;hyphens:auto;line-height:30px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                                        &#xA0;</td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <table class="container footer float-center"
                       style="background:#000;border-collapse:collapse;border-spacing:0;float:none;margin:25px 0;padding:0;text-align:center;vertical-align:top;width:640px">
                    <tbody>
                    <tr style="padding:0;text-align:left;vertical-align:top">
                        <td style="-moz-hyphens:auto;-webkit-hyphens:auto;Margin:0;border-collapse:collapse!important;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;hyphens:auto;line-height:19px;padding:0;text-align:left;vertical-align:top;word-wrap:break-word">
                            <table class="row collapse"
                                   style="border-collapse:collapse;border-spacing:0;display:table;padding:0;position:relative;text-align:left;vertical-align:top;width:100%">
                                <tbody>
                                <tr style="padding:0;text-align:left;vertical-align:top">
                                    <th class="small-12 large-12 columns first last"
                                        style="Margin:0 auto;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding: 0;text-align:left;width:648px">
                                        <table style="border-collapse:collapse;border-spacing:0;padding:0;text-align:left;vertical-align:top;width:100%">
                                            <tr style="padding:0;text-align:left;vertical-align:top">
                                                <th style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0;text-align:left">
                                                    <center data-parsed="" style="min-width:592px;width:100%"><img
                                                            src="${city.logoUrl}"
                                                            align="center" class="float-center"
                                                            style="-ms-interpolation-mode:bicubic;Margin:0 auto;clear:both;display:block;float:none;max-width:120px;outline:0;text-align:center;text-decoration:none;width:auto">
                                                    </center>
                                                </th>
                                                <th class="expander"
                                                    style="Margin:0;color:#0a0a0a;font-family:'Open Sans',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;line-height:19px;padding:0!important;text-align:left;visibility:hidden;width:0"></th>
                                            </tr>
                                        </table>
                                    </th>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </center>
        </td>
    </tr>
</table><!-- prevent Gmail on iOS font size manipulation -->
<div style="display:none;white-space:nowrap;font:15px courier;line-height:0">&#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0;
    &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0;
    &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0; &#xA0;</div>
</body>
</html>
