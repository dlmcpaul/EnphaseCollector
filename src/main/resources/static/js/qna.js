/*global console */

function getAnswers(target) {
    "use strict";

    const form = document.getElementById(target + "-form");

    const request = new XMLHttpRequest();
    request.open("POST", form.action, true);
    request.responseType = "";
    request.onload = function () {
        request.onerror = function () {
            console.log("Refresh Error for " + target);
        };
        if (request.status === 200) {
            document.getElementById(target + "-data").outerHTML = request.response;
        }
    };
    request.send(new FormData(form));

    return false;
}
