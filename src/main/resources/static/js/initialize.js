/*global Highcharts, makeRefreshChart, updateGauge, updatePvc, makeChart, makeStatusList*/

function initHighCharts() {
    "use strict";

    //Disable use of UTC
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
}

function initLiveCharts(contextPath, refreshInterval) {
    "use strict";

    var consumptionProperties = {
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
                from: 0,
                to: 1500,
                color: "#55BF3B" // green
            }, {
                from: 1500,
                to: 3000,
                color: "#DDDF0D" // yellow
            }, {
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

    var productionProperties = {
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
                from: 0,
                to: 1000,
                color: "#DF5353" // red
            }, {
                from: 1000,
                to: 2000,
                color: "#DDDF0D" // yellow
            }, {
                from: 2000,
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

    var now = new Date();

    var pvcProperties = {
        chart: {
            renderTo: "pvc",
            height: "30%"
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
        yAxis: {
            labels: {
                format: "{value}W",
                style: {
                    color: Highcharts.getOptions().colors[1]
                }
            },
            title: {
                text: "Watts"
            }
        },
        legend: {
            layout: "vertical",
            align: "left",
            x: 120,
            verticalAlign: "top",
            y: 50,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || "rgba(255,255,255,0.25)"
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
            }
        ]
    };

    makeRefreshChart("production", productionProperties, contextPath + "/production", refreshInterval, updateGauge);
    makeRefreshChart("consumption", consumptionProperties, contextPath + "/consumption", refreshInterval, updateGauge);
    makeRefreshChart("pvc", pvcProperties, contextPath + "/pvc", refreshInterval, updatePvc);
    makeStatusList("statusList", contextPath + "/refreshStats", refreshInterval);
}

function initHistoryCharts() {
    "use strict";

    var now = new Date();

    var weeklyProperties = {
        chart: {
            height: "30%"
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
                    text: "Solar Usage"
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
    var monthlyProperties = {
        chart: {
            height: "30%"
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
                    text: "Solar Usage"
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
    var quarterlyProperties = {
        chart: {
            height: "30%"
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
                    text: "Solar Usage"
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