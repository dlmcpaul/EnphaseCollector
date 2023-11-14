/*global console */

function getAnswers(target, event) {
    "use strict";

    event.preventDefault();

    const form = document.getElementById(target + "-form");

    // All these attributes and classes will be removed when we replace the outerHTML
    if (form.hasAttribute('data-submitting')) { return; }
    form.setAttribute('data-submitting', '');

    const button = document.getElementById(target + "-button");
    button.classList.add('is-loading');

    const request = new XMLHttpRequest();
    request.open("POST", form.action, true);
    request.responseType = "";
    request.onload = function () {
        request.onerror = function () {
            console.log("Refresh Error for " + target);
            button.innerText = "Error";
        };
        if (request.status === 200) {
            document.getElementById(target + "-data").outerHTML = request.response;
            document.getElementById("bill-form")
                .addEventListener("submit", (ev) => getAnswers('bill', ev), false);
        } else {
            console.log("Refresh Error for " + target);
            button.innerText = "Error";
            button.classList.remove('is-loading');
            form.removeAttribute('data-submitting');
        }
    };
    request.send(new FormData(form));
}

function setupQnA() {
    "use strict";

    document.getElementById("bill-form")
        .addEventListener("submit", (ev) => getAnswers('bill', ev), false);
}