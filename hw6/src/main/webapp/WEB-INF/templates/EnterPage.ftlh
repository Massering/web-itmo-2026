<#-- @ftlvariable name="login" type="java.lang.String" -->
<#-- @ftlvariable name="error" type="java.lang.String" -->
<#import "commons.ftlh" as c>

<@c.page>
    <div class="enter form-box">
        <div class="header">Enter</div>
        <div class="body">
            <form id="enter-form">
                <div class="field">
                    <div class="name">
                        <label for="loginOrEmail">Login or email</label>
                    </div>
                    <div class="value">
                        <input id="loginOrEmail" name="loginOrEmail"/>
                    </div>
                </div>
                <div class="field">
                    <div class="name">
                        <label for="password">Password</label>
                    </div>
                    <div class="value">
                        <input id="password" name="password" type="password"/>
                    </div>
                </div>
                <div class="error" id="enter-error"></div>
                <div class="button-field">
                    <input type="submit" value="Enter">
                </div>
            </form>
        </div>
    </div>

    <script>
        $(function () {
            $("#enter-form").on("submit", function (e) {
                e.preventDefault();
                $("#enter-error").text("");

                ajax({
                    url: window.location.pathname,
                    data: {
                        action: "enter",
                        loginOrEmail: $("#loginOrEmail").val(),
                        password: $("#password").val()
                    }
                }, null, function (message) {
                    $("#enter-error").text(message);
                });
            });
        });
    </script>
</@c.page>
