/*global Highcharts, makeRefreshChart, updateGauge, updatePvc, makeChart, makeStatusList, switchToTab, switchToHistoryTab, getAnswers, switchStacking */

function initHighCharts(timezone) {
    "use strict";

    //Disable use of UTC
    Highcharts.setOptions({
        time: {
          timezone: timezone
        },
        global: {
            useUTC: false
        }
    });
}

function initLiveCharts(contextPath, refreshInterval, exportLimit) {
    "use strict";

    const consumptionProperties = {
        chart: {
            renderTo: "consumption",
            type: "gauge"
        },
        title: {
            text: null
        },
        credits: {
            enabled: false
        },
        pane: {
            startAngle: -150,
            endAngle: 150,
            background: [{
                backgroundColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, "#FFF"],
                        [1, "#333"]
                    ]
                },
                borderWidth: 0,
                outerRadius: "109%"
            }, {
                backgroundColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, "#333"],
                        [1, "#FFF"]
                    ]
                },
                borderWidth: 1,
                outerRadius: "107%"
            }, {
                // default background
            }, {
                backgroundColor: "#DDD",
                borderWidth: 0,
                outerRadius: "105%",
                innerRadius: "103%"
            }]
        },
        yAxis: {
            min: 0,
            max: 6000,

            minorTickInterval: "auto",
            minorTickWidth: 1,
            minorTickLength: 10,
            minorTickPosition: "inside",
            minorTickColor: "#666",

            tickPixelInterval: 30,
            tickWidth: 2,
            tickPosition: "inside",
            tickLength: 10,
            tickColor: "#666",
            labels: {
                step: 2,
                rotation: "auto"
            },
            title: {
                text: "W"
            },
            plotBands: [{
                id: "low",
                from: 0,
                to: 1500,
                color: "#55BF3B" // green
            }, {
                id: "middle",
                from: 1500,
                to: 3000,
                color: "#DDDF0D" // yellow
            }, {
                id: "high",
                from: 3000,
                to: 6000,
                color: "#DF5353" // red
            }]
        },
        series: [{
            name: "consumption",
            data: [0],
            tooltip: {
                valueSuffix: " W"
            }
        }]
    };

    const productionProperties = {
        chart: {
            renderTo: "production",
            type: "gauge"
        },
        credits: {
            enabled: false
        },
        title: {
            text: null
        },
        pane: {
            startAngle: -150,
            endAngle: 150,
            background: [{
                backgroundColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, "#FFF"],
                        [1, "#333"]
                    ]
                },
                borderWidth: 0,
                outerRadius: "109%"
            }, {
                backgroundColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, "#333"],
                        [1, "#FFF"]
                    ]
                },
                borderWidth: 1,
                outerRadius: "107%"
            }, {
                // default background
            }, {
                backgroundColor: "#DDD",
                borderWidth: 0,
                outerRadius: "105%",
                innerRadius: "103%"
            }]
        },
        yAxis: {
            min: 0,
            max: 5000,

            minorTickInterval: "auto",
            minorTickWidth: 1,
            minorTickLength: 10,
            minorTickPosition: "inside",
            minorTickColor: "#666",

            tickPixelInterval: 30,
            tickWidth: 2,
            tickPosition: "inside",
            tickLength: 10,
            tickColor: "#666",
            labels: {
                step: 2,
                rotation: "auto"
            },
            title: {
                text: "W"
            },
            plotBands: [{
                id: "low",
                from: 0,
                to: 1250,
                color: "#DF5353" // red
            }, {
                id: "middle",
                from: 1250,
                to: 2500,
                color: "#DDDF0D" // yellow
            }, {
                id: "high",
                from: 2500,
                to: 5000,
                color: "#55BF3B" // green
            }]
        },
        series: [{
            name: "production",
            data: [0],
            tooltip: {
                valueSuffix: " W"
            }
        }]
    };

    const now = new Date();

    const pvcProperties = {
        chart: {
            renderTo: "pvc"
        },
        credits: {
            enabled: false
        },
        title: {
            text: "Production vs Consumption"
        },
        xAxis: {
            title: {
                text: "Today"
            },
            type: "datetime", //For time series, x-axis labels will be time
            labels: {
                format: "{value:%H}"
            },
            minPadding: 0.05,
            maxPadding: 0.05
        },
        yAxis: [
            {
                title: {
                    text: "Watts"
                },
                labels: {
                    format: "{value}W",
                    style: {
                        color: Highcharts.getOptions().colors[1]
                    }
                }
            }],
        legend: {
            layout: "vertical",
            align: "left",
            x: 120,
            verticalAlign: "top",
            y: 50,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendColor.backgroundColor) || "rgba(255,255,255,0.25)"
        },
        series: [
            {
                name: "production",
                type: "line",
                color: "#55BF3B",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "consumption",
                type: "line",
                color: "#DDDF0D",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "grid",
                type: "area",
                color: "#DF5353",
                fillOpacity: 0.2,
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "excess",
                type: "area",
                color: "#5353FF",
                fillOpacity: 0.2,
                data: [ [now.getTime(), 0] ]
            }
        ]
    };

    makeRefreshChart("production", productionProperties, contextPath + "/production", refreshInterval, updateGauge);
    makeRefreshChart("consumption", consumptionProperties, contextPath + "/consumption", refreshInterval, updateGauge);
    const pvcChart = makeRefreshChart("pvc", pvcProperties, contextPath + "/pvc", refreshInterval, updatePvc);
    if (exportLimit > 0) {
        pvcChart.update({
            yAxis: {
                plotLines: [{
                    color: 'red', // Color value
                    dashStyle: 'longdashdot', // Style of the plot line. Default to solid
                    value: exportLimit, // Value of where the line will appear
                    width: 2 // Width of the line
                }]
            }
        });
    }
    makeStatusList("statusComponent", contextPath + "/refreshStats", refreshInterval);
}

function initHistoryCharts() {
    "use strict";

    const now = new Date();

    const weeklyProperties = {
        chart: {
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            column: {
                stacking: "normal"
            }
        },
        tooltip: {
            pointFormat: "{series.name}: <b>{point.y:.2f}</b> kWh<br/>",
            shared: true
        },
        title: {
            text: ""
        },
        xAxis: {
            title: {
                text: "Last 7 Days"
            },
            type: "datetime", //For time series, x-axis labels will be time
            labels: {
                format: "{value:%a}"
            },
            minPadding: 0.05,
            maxPadding: 0.05
        },
        yAxis: [
            {
                min: 0,
                title: {
                    text: "Solar Generation"
                },
                labels: {
                    format: "{value} kWh"
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
            }],
        series: [
            {
                name: "export",
                type: "column",
                yAxis: 0,
                color: "#0000FF",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "solar",
                type: "column",
                yAxis: 0,
                color: "#55BF3B",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "grid",
                type: "line",
                yAxis: 1,
                color: "#FF0000",
                data: [ [now.getTime(), 0] ]
            }
        ]
    };
    const monthlyProperties = {
        chart: {
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            column: {
                stacking: "normal"
            }
        },
        tooltip: {
            pointFormat: "{series.name}: <b>{point.y:.2f}</b> kWh<br/>",
            shared: true
        },
        title: {
            text: ""
        },
        xAxis: {
            title: {
                text: "Last 4 Weeks"
            },
            type: "datetime", //For time series, x-axis labels will be time
            labels: {
                format: "{value:%e %b}"
            },
            minPadding: 0.05,
            maxPadding: 0.05
        },
        yAxis: [
            {
                min: 0,
                title: {
                    text: "Solar Generation"
                },
                labels: {
                    format: "{value} kWh"
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
            }],
        series: [
            {
                name: "export",
                type: "column",
                yAxis: 0,
                color: "#0000FF",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "solar",
                type: "column",
                yAxis: 0,
                color: "#55BF3B",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "grid",
                type: "line",
                yAxis: 1,
                color: "#FF0000",
                data: [ [now.getTime(), 0] ]
            }
        ]
    };
    const quarterlyProperties = {
        chart: {
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            column: {
                stacking: "normal"
            }
        },
        tooltip: {
            pointFormat: "{series.name}: <b>{point.y:.2f}</b> kWh<br/>",
            shared: true
        },
        title: {
            text: ""
        },
        xAxis: {
            title: {
                text: "Last Quarter"
            },
            type: "datetime", //For time series, x-axis labels will be time
            labels: {
                format: "{value: %b}"
            },
            minPadding: 0.05,
            maxPadding: 0.05
        },
        yAxis: [
            {
                min: 0,
                title: {
                    text: "Solar Generation"
                },
                labels: {
                    format: "{value} kWh"
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
            }],
        series: [
            {
                name: "export",
                type: "column",
                yAxis: 0,
                color: "#0000FF",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "solar",
                type: "column",
                yAxis: 0,
                color: "#55BF3B",
                data: [ [now.getTime(), 0] ]
            },
            {
                name: "grid",
                type: "line",
                yAxis: 1,
                color: "#FF0000",
                data: [ [now.getTime(), 0] ]
            }
        ]
    };

    makeChart("weekly-graph", weeklyProperties);
    makeChart("monthly-graph", monthlyProperties);
    makeChart("quarterly-graph", quarterlyProperties);
}

function setupCharts(timeZone, contextPath, refreshInterval, exportLimit) {
    "use strict";
    initHighCharts(timeZone);
    initLiveCharts(contextPath, refreshInterval, exportLimit);
    initHistoryCharts();
}

function setupClickEvents(contextPath) {
    "use strict";
    document.getElementById("live-event")
        .addEventListener("click", function () {
            switchToTab('main-tab', 'live');
        }, false);
    document.getElementById("weekly-event")
        .addEventListener("click", function () {
            switchToHistoryTab('main-tab', 'weekly', contextPath + '/history?duration=7Days');
        }, false);
    document.getElementById("monthly-event")
        .addEventListener("click", function () {
            switchToHistoryTab('main-tab', 'monthly', contextPath + '/history?duration=4Weeks');
        }, false);
    document.getElementById("quarterly-event")
        .addEventListener("click", function () {
            switchToHistoryTab('main-tab', 'quarterly', contextPath + '/history?duration=3Months');
        }, false);
    document.getElementById("qna-event")
        .addEventListener("click", function () {
            switchToTab('main-tab', 'qna');
        }, false);

    let names = ["weekly", "monthly", "quarterly"];
    for (let i = 0; i < names.length; i++) {
        document.getElementById("btn-" + names[i] + "-normal").addEventListener("click", (ev) => switchStacking(ev), false);
        document.getElementById("btn-" + names[i] + "-percent").addEventListener("click", (ev) => switchStacking(ev), false);
    }
}