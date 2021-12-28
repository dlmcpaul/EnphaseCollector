/*global Highcharts, console */

const productionColors = ["#DF5353", "#DDDF0D", "#55BF3B"];
const consumptionColors = ["#55BF3B", "#DDDF0D", "#DF5353"];

function getChart(target) {
    "use strict";

    const chartDom = document.getElementById(target);
    return Highcharts.charts[Highcharts.attr(chartDom, "data-highcharts-chart")];
}

function makeChart(target, properties) {
    "use strict";

    // Create Chart
    properties.chart.renderTo = target;
    return Highcharts.chart(properties);
}

function switchStacking(event) {
    "use strict";

    if (!event.target.classList.contains("is-selected")) {

        const target = event.target.getAttribute("data-base-id");

        // Swap is-info is-selected
        const normalButton = document.getElementById("btn-" + target + "-normal");
        const percentButton = document.getElementById("btn-" + target + "-percent");

        const switchValue = normalButton.classList.contains("is-selected");
        if (switchValue) {
            normalButton.classList.remove("is-info", "is-selected");
            percentButton.classList.add("is-info", "is-selected");
        } else {
            normalButton.classList.add("is-info", "is-selected");
            percentButton.classList.remove("is-info", "is-selected");
        }
        const chart = getChart(target + "-graph");

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
                        format: switchValue ? "{value} %" : "{value} kWh"
                    }
                },
                {
                    min: 0,
                    title: {
                        text: "Grid Usage"
                    },
                    labels: {
                        format: "{value} kWh"
                    },
                    opposite: true
                }]
        });
    }
}

function refreshTarget(target, refreshUrl, responseType, updateFunction) {
    "use strict";

    const request = new XMLHttpRequest();
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

    const element = document.getElementById(target);
    if (element !== null) {
        element.innerHTML = response + " W";
    }
}

function calculateBand(maximum, divisor) {
    "use strict";

    return Math.round((maximum * 1000) / divisor) / 1000;
}

function calculateMaximum(current, value) {
    "use strict";

    if (current > value) {
        return current;
    }
    return Math.round(value * 1000 ) / 1000;
}

function getColor(index, type) {
    "use strict";

    if (type === "production") {
        return productionColors[index];
    }

    return consumptionColors[index];
}

function updatePlotBands(target, chart, guageValue) {
    "use strict";

    const maximum = calculateMaximum(chart.yAxis[0].max, guageValue);

    // Only update if new maximum is greater than old
    if (maximum > chart.yAxis[0].max) {
        chart.update({
            yAxis: {
                min: 0,
                max: maximum,
                plotBands: [{
                    from: 0,
                    to: calculateBand(maximum, 4),
                    color: getColor(0, target)
                }, {
                    from: calculateBand(maximum, 4),
                    to: calculateBand(maximum, 2),
                    color: getColor(1, target)
                }, {
                    from: calculateBand(maximum, 2),
                    to: maximum,
                    color: getColor(2, target)
                }]
            }
        });
    }
}

function updatePoint(chart, guageValue) {
    "use strict";

    chart.series[0].points[0].update(guageValue);
}

function updateGauge(target, response) {
    "use strict";

    const chart = getChart(target);

    // Recalculate Bands based on response
    updatePlotBands(target, chart, Number(response));
    updatePoint(chart, Number(response));
    updateElement(target + "-label", response);
}

function updatePvc(target, response) {
    "use strict";

    const chart = getChart(target);

    chart.series[0].setData(response.production);
    chart.series[1].setData(response.consumption);
    chart.series[2].setData(response.gridImport);
    chart.series[3].setData(response.excess);

    chart.update({
        xAxis: {
            plotBands: response.plotBands
        }
    });
}

function updateStatusList(target, response) {
    "use strict";

    document.getElementById(target).outerHTML = response;
}

function makeRefreshChart(target, properties, refreshUrl, interval, updateFunction) {
    "use strict";

    const chart = makeChart(target, properties);

    // Set initial values
    refreshTarget(target, refreshUrl, "json", updateFunction);

    // Refresh every interval
    setInterval(function () {
        refreshTarget(target, refreshUrl, "json", updateFunction);
    }, interval);

    return chart;
}

function makeStatusList(target, refreshUrl, interval) {
    "use strict";

    // Refresh every interval
    setInterval(function () {
        refreshTarget(target, refreshUrl,"", updateStatusList);
    }, interval);
}
