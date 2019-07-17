/*global Highcharts, console */

function getChart(target) {
    "use strict";

    var chartDom = document.getElementById(target);
    return Highcharts.charts[Highcharts.attr(chartDom, "data-highcharts-chart")];
}

function switchStacking(target) {
    "use strict";

    if (!event.srcElement.classList.contains("is-selected")) {

        // Swap is-info is-selected
        var normalButton = document.getElementById(target + "NormalButton");
        var percentButton = document.getElementById(target + "PercentButton");

        var switchValue = normalButton.classList.contains("is-selected");
        if (switchValue) {
            normalButton.classList.remove("is-info", "is-selected");
            percentButton.classList.add("is-info", "is-selected");
        } else {
            normalButton.classList.add("is-info", "is-selected");
            percentButton.classList.remove("is-info", "is-selected");
        }
        var chart = getChart(target + "_graph");

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

function refreshGaugeChart(chart, url, target) {
    "use strict";

    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.onload = function () {
        request.onerror = function () {
            console.log("Gauge Chart Error");
            return;
        };
        if (request.status === 200) {
            var point = chart.series[0].points[0];
            point.update(Number(request.response));

            var element = document.getElementById(target + "_label");
            if (element !== null) {
                element.innerHTML = request.response + " W";
            }
        }
    };
    request.send();
}

function makeGaugeChart(target, properties, refreshUrl, interval) {
    "use strict";

    // Create Chart
    properties.chart.renderTo = target;
    var chart = Highcharts.chart(properties);

    // Set initial values
    refreshGaugeChart(chart, refreshUrl, target);

    // Refresh every interval
    setInterval(function () {
        refreshGaugeChart(chart, refreshUrl, target);
    }, interval);
}

function refreshPvcChart(chart, url) {
    "use strict";

    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.responseType = "json";
    request.onerror = function () {
        console.log("PvC Chart Error");
        return;
    };
    request.onload = function () {
        if (request.status === 200) {
            chart.series[0].setData(request.response.production);
            chart.series[1].setData(request.response.consumption);
            chart.series[2].setData(request.response.gridImport);
        }
    };
    request.send();
}

function makePvcChart(target, properties, refreshUrl, interval) {
    "use strict";

    // Create Chart
    properties.chart.renderTo = target;
    var chart = Highcharts.chart(properties);

    // Set initial values
    refreshPvcChart(chart, refreshUrl);

    // Refresh every interval
    setInterval(function () {
        refreshPvcChart(chart, refreshUrl);
    }, interval);
}

function refreshHistoryChart(chart, url) {
    "use strict";

    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.responseType = "json";
    request.onerror = function () {
        console.log("History Chart Error");
        return;
    };
    request.onload = function () {
        if (request.status === 200) {
            chart.series[0].setData(request.response.gridExport);
            chart.series[1].setData(request.response.solarConsumption);
            chart.series[2].setData(request.response.gridImport);
        }
    };
    request.send();
}

function makeHistoryChart(target, properties) {
    "use strict";

    // Create Chart
    properties.chart.renderTo = target;
    Highcharts.chart(properties);
}

function refreshStatusList(target, url) {
    "use strict";

    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.onerror = function () {
        console.log("Status List Error");
        return;
    };
    request.onload = function () {
        if (request.status === 200) {
            document.getElementById(target).outerHTML = request.response;
        }
    };
    request.send();
}

function makeStatusList(target, refreshUrl, interval) {
    "use strict";

    // Refresh every interval
    setInterval(function () {
        refreshStatusList(target, refreshUrl);
    }, interval);
}
