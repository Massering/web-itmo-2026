// Всплывающее уведомление в правом нижнем углу.
window.ajax = function (options, onSuccess, onError) {
    const settings = Object.assign({
        type: "POST",
        dataType: "json",
        error: function (xhr) {
            notify("Request failed: " + (xhr.statusText || xhr.status), true);
        }
    }, options);

    settings.success = function (response) {
        if (response && response.redirect) {
            location.href = response.redirect;
            return;
        }
        if (response && response.error) {
            if (onError) { onError(response.error); } else { notify(response.error, true); }
            return;
        }
        if (onSuccess) { onSuccess(response || {}); }
    };

    $.ajax(settings);
};
