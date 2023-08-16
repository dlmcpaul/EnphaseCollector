/*global console */

function getAnswers(target, event) {
    "use strict";

    event.preventDefault();

    const form = document.getElementById(target + "-form");

    const button = document.getElementById(target + "-button");
    button.innerText = "Calculating";

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
        }
    };
    request.send(new FormData(form));
}

function setupQnA() {
    "use strict";

    document.getElementById("bill-form")
        .addEventListener("submit", (ev) => getAnswers('bill', ev), false);
}