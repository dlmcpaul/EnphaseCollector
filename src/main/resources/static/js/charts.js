/*global Highcharts, console */

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
            chart.series[1].setData(request.response.gridImport);
            chart.series[2].setData(request.response.solarConsumption);
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
        console.log("Statuss List Error");
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
