/*global Highcharts, console */

function getChart(target) {
    "use strict";

    var chartDom = document.getElementById(target);
    return Highcharts.charts[Highcharts.attr(chartDom, "data-highcharts-chart")];
}

function makeChart(target, properties) {
    "use strict";

    // Create Chart
    properties.chart.renderTo = target;
    Highcharts.chart(properties);
}

function switchStacking() {
    "use strict";

    if (!event.srcElement.classList.contains("is-selected")) {

        var target = event.srcElement.getAttribute("data-base-id");

        // Swap is-info is-selected
        var normalButton = document.getElementById("btn-" + target + "-normal");
        var percentButton = document.getElementById("btn-" + target + "-percent");

        var switchValue = normalButton.classList.contains("is-selected");
        if (switchValue) {
            normalButton.classList.remove("is-info", "is-selected");
            percentButton.classList.add("is-info", "is-selected");
        } else {
            normalButton.classList.add("is-info", "is-selected");
            percentButton.classList.remove("is-info", "is-selected");
        }
        var chart = getChart(target + "-graph");

        chart.update({
            plotOptions: {
                column: {
                    stacking: switchValue ? "percent" : "normal"
                }
            },
            yAxis: [
                {
                    min: 0,
                    title: {
                        text: "Solar Usage"
                    },
                    labels: {
                        format: switchValue ? "{value} %" : "{value} kW"
                    }
                },
                {
                    min: 0,
                    title: {
                        text: "Grid Usage"
                    },
                    labels: {
                        format: "{value} kW"
                    },
                    opposite: true
                }]
        });
    }
}

function refreshTarget(target, refreshUrl, responseType, updateFunction) {
    "use strict";

    var request = new XMLHttpRequest();
    request.open("GET", refreshUrl, true);
    request.responseType = responseType;
    request.onload = function () {
        request.onerror = function () {
            console.log("Refresh Error for " + target);
        };
        if (request.status === 200) {
            updateFunction(target, request.response);
        }
    };
    request.send();
}

function updateElement(target, response) {
    "use strict";

    var element = document.getElementById(target);
    if (element !== null) {
        element.innerHTML = response + " W";
    }
}

function updateGauge(target, response) {
    "use strict";

    var chart = getChart(target);
    var point = chart.series[0].points[0];
    point.update(Number(response));
    updateElement(target + "-label", response);
}

function updatePvc(target, response) {
    "use strict";

    var chart = getChart(target);

    chart.series[0].setData(response.production);
    chart.series[1].setData(response.consumption);
    chart.series[2].setData(response.gridImport);
}

function updateStatusList(target, response) {
    "use strict";

    document.getElementById(target).outerHTML = response;
}

function makeRefreshChart(target, properties, refreshUrl, interval, updateFunction) {
    "use strict";

    makeChart(target, properties);

    // Set initial values
    refreshTarget(target, refreshUrl, "json", updateFunction);

    // Refresh every interval
    setInterval(function () {
        refreshTarget(target, refreshUrl, "json", updateFunction);
    }, interval);
}

function makeStatusList(target, refreshUrl, interval) {
    "use strict";

    // Refresh every interval
    setInterval(function () {
        refreshTarget(target, refreshUrl,"", updateStatusList);
    }, interval);
}
