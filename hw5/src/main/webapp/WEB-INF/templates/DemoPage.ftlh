<#-- @ftlvariable name="message" type="java.lang.String" -->
<#import "./common.ftlh" as c>

<@c.page>
    <h1>Demo</h1>
    <p>${message}</p>
    <p>У этой страницы нет action, а текст выше приходит из DefaultActionable, который реализуется DemoBasePage.</p>
</@c.page>