<#-- @ftlvariable name="login" type="java.lang.String" -->
<#-- @ftlvariable name="error" type="java.lang.String" -->
<#import "commons.ftlh" as c>

<@c.page>
    <div class="registration form-box">
        <div class="header">Register</div>
        <div class="body">
            <form id="register-form">
                <div class="field">
                    <div class="name">
                        <label for="login">Login</label>
                    </div>
                    <div class="value">
                        <input id="login" name="login" value="${login!}"/>
                    </div>
                </div>
                <div class="field">
                    <div class="name">
                        <label for="email">Email</label>
                    </div>
                    <div class="value">
                        <input id="email" name="email" type="text"/>
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
                <div class="field">
                    <div class="name">
                        <label for="passwordConfirmation">Confirm password</label>
                    </div>
                    <div class="value">
                        <input id="passwordConfirmation" name="passwordConfirmation" type="password"/>
                    </div>
                </div>
                <div class="error" id="register-error"></div>
                <div class="button-field">
                    <input type="submit" value="Register">
                </div>
            </form>
        </div>
    </div>

    <script>
        $(function () {
            $("#register-form").on("submit", function (e) {
                e.preventDefault();
                $("#register-error").text("");

                ajax({
                    url: window.location.pathname,
                    data: {
                        action: "register",
                        login: $("#login").val(),
                        email: $("#email").val(),
                        password: $("#password").val(),
                        passwordConfirmation: $("#passwordConfirmation").val()
                    }
                }, function () {
                    // успех без redirect — по факту не случится: сервер всегда шлёт redirect
                }, function (message) {
                    $("#register-error").text(message);
                });
            });
        });
    </script>
</@c.page>
