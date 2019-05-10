function initLiveCharts(contextPath, refreshInterval) {
    var consumptionProperties = {
        chart: {
            renderTo: 'consumption',
            type: 'gauge'
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
                        [0, '#FFF'],
                        [1, '#333']
                    ]
                },
                borderWidth: 0,
                outerRadius: '109%'
            }, {
                backgroundColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, '#333'],
                        [1, '#FFF']
                    ]
                },
                borderWidth: 1,
                outerRadius: '107%'
            }, {
                // default background
            }, {
                backgroundColor: '#DDD',
                borderWidth: 0,
                outerRadius: '105%',
                innerRadius: '103%'
            }]
        },
        yAxis: {
            min: 0,
            max: 6000,

            minorTickInterval: 'auto',
            minorTickWidth: 1,
            minorTickLength: 10,
            minorTickPosition: 'inside',
            minorTickColor: '#666',

            tickPixelInterval: 30,
            tickWidth: 2,
            tickPosition: 'inside',
            tickLength: 10,
            tickColor: '#666',
            labels: {
                step: 2,
                rotation: 'auto'
            },
            title: {
                text: 'W'
            },
            plotBands: [{
                from: 0,
                to: 1500,
                color: '#55BF3B' // green
            }, {
                from: 1500,
                to: 3000,
                color: '#DDDF0D' // yellow
            }, {
                from: 3000,
                to: 6000,
                color: '#DF5353' // red
            }]
        },
        series: [{
            name: 'consumption',
            data: [0],
            tooltip: {
                valueSuffix: ' W'
            }
        }]
    };

    var productionProperties = {
        chart: {
            renderTo: 'production',
            type: 'gauge'
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
                        [0, '#FFF'],
                        [1, '#333']
                    ]
                },
                borderWidth: 0,
                outerRadius: '109%'
            }, {
                backgroundColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, '#333'],
                        [1, '#FFF']
                    ]
                },
                borderWidth: 1,
                outerRadius: '107%'
            }, {
                // default background
            }, {
                backgroundColor: '#DDD',
                borderWidth: 0,
                outerRadius: '105%',
                innerRadius: '103%'
            }]
        },
        yAxis: {
            min: 0,
            max: 5000,

            minorTickInterval: 'auto',
            minorTickWidth: 1,
            minorTickLength: 10,
            minorTickPosition: 'inside',
            minorTickColor: '#666',

            tickPixelInterval: 30,
            tickWidth: 2,
            tickPosition: 'inside',
            tickLength: 10,
            tickColor: '#666',
            labels: {
                step: 2,
                rotation: 'auto'
            },
            title: {
                text: 'W'
            },
            plotBands: [{
                from: 0,
                to: 1000,
                color: '#DF5353' // red
            }, {
                from: 1000,
                to: 2000,
                color: '#DDDF0D' // yellow
            }, {
                from: 2000,
                to: 5000,
                color: '#55BF3B' // green
            }]
        },
        series: [{
            name: 'production',
            data: [0],
            tooltip: {
                valueSuffix: ' W'
            }
        }]
    };

    var now = new Date();

    //Disable use of UTC
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    var pvcProperties = {
        chart: {
            renderTo: 'pvc',
            height: '30%'
        },
        credits: {
            enabled: false
        },
        title: {
            text: 'Production vs Consumption'
        },
        xAxis: {
            title: {
                text: 'Today'
            },
            type: 'datetime', //For time series, x-axis labels will be time
            labels: {
                //You can format the label according to your need
                format: '{value:%H}'
            },
            minPadding: 0.05,
            maxPadding: 0.05
        },
        yAxis: {
            labels: {
                format: '{value}W',
                style: {
                    color: Highcharts.getOptions().colors[1]
                }
            },
            title: {
                text: 'Watts'
            }
        },
        legend: {
            layout: 'vertical',
            align: 'left',
            x: 120,
            verticalAlign: 'top',
            y: 50,
            floating: true,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || 'rgba(255,255,255,0.25)'
        },
        series: [
            {
                type: 'line',
                name: 'production',
                color: '#55BF3B',
                data: [ [now.getTime(), 0] ]
            },
            {
                type: 'line',
                name: 'consumption',
                color: '#DDDF0D',
                data: [ [now.getTime(), 0] ]
            },
            {
                type: 'area',
                name: 'grid',
                color: '#DF5353',
                fillOpacity: 0.2,
                data: [ [now.getTime(), 0] ]
            }]
    };

    makeGuageChart("production", productionProperties, contextPath + '/production', refreshInterval);
    makeGuageChart("consumption", consumptionProperties, contextPath + '/consumption', refreshInterval);
    makePvcChart("pvc", pvcProperties, contextPath + '/pvc', refreshInterval);
    makeStatusList("statusList", contextPath + '/refreshStats', refreshInterval)
}
function initHistoryCharts(contextPath, refreshInterval) {
    var now = new Date();

    //Disable use of UTC
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

    var weeklyProperties = {
        chart: {
            renderTo: 'weekly',
            height: '30%'
        },
        credits: {
            enabled: false
        },
        title: {
            text: 'Last Weeks Production'
        },
        xAxis: {
            title: {
                text: 'Last Week'
            },
            type: 'datetime', //For time series, x-axis labels will be time
            labels: {
                //You can format the label according to your need
                format: '{value:%d}'
            },
            minPadding: 0.05,
            maxPadding: 0.05
        },
        yAxis: {
            labels: {
                format: '{value}kW',
                style: {
                    color: Highcharts.getOptions().colors[1]
                }
            },
            title: {
                text: 'KiloWatts'
            }
        },
        series: [
            {
                type: 'column',
                name: 'production',
                color: '#55BF3B',
                data: [ [now.getTime(), 0] ]
            }]
    };
    makeWeeklyChart("weekly", weeklyProperties, contextPath + '/weekly', refreshInterval);
}