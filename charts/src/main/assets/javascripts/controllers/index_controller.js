import {Controller} from "stimulus";

import Highcharts from 'highcharts';

export default class extends Controller {
    connect() {
        var labels = charts.records.map(r => {
            var suffix = '';
            var changeInPosition = r[2]
            if (changeInPosition !== null && changeInPosition !== 0) {
                suffix += ' ';
                suffix += changeInPosition > 0 ? '▲' : '▼';
                suffix += changeInPosition;
            }
            return r[0] + suffix;
        });
        var data = charts.records.map(r => r[1]);
        var myChart = Highcharts.chart('container', {
            chart: {
                type: 'bar'
            },
            title: false,
            plotOptions: {
                series: {
                    animation: false
                },
                bar: {
                    pointWidth: 40
                }
            },
            series: [{
                showInLegend: false,
                data: data
            }],
            xAxis: {
                categories: labels,
                labels: {
                    style: {
                        color: '#000'
                    },
                    align: 'left',
                    x: 5
                }
            },
            yAxis: {
                allowDecimals: false,
                title: false
            },
            tooltip: {
                formatter: function () {
                    return this.x;
                }
            }
        });
    }

    greet() {
        console.log("Hello, Stimulus!", this.element)
    }
}