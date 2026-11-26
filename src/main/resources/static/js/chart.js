// Shared Chart.js look & feel. Load AFTER the Chart.js library and BEFORE any page script that draws charts.
(function () {
    if (!window.Chart) {
        return;
    }

    Chart.defaults.font.family = getComputedStyle(document.body).fontFamily; // same font as Bootstrap
    Chart.defaults.color = '#475467';
    Chart.defaults.maintainAspectRatio = false;  // let the .chart-box CSS height decide the size
    Chart.defaults.plugins.legend.position = 'bottom';
    Chart.defaults.plugins.legend.labels.usePointStyle = true;

    window.EmsCharts = {
        colors: {
            blue: '#2459b3',
            green: '#12b76a',
            orange: '#f79009',
            red: '#d92d20',
            purple: '#7a5af8'
        },
        // Used for pie/doughnut slices; repeats if there are more departments than colors
        palette: ['#2459b3', '#12b76a', '#f79009', '#7a5af8', '#d92d20', '#0ba5ec', '#ee46bc', '#667085', '#15b79e', '#fd853a'],

        /** Total of all numbers in all given arrays (used to detect "no data"). */
        total: function () {
            var sum = 0;
            for (var i = 0; i < arguments.length; i++) {
                arguments[i].forEach(function (n) { sum += n; });
            }
            return sum;
        },

        /** Hides the canvas and shows the ".chart-empty" message in the same card when there is no data. */
        showEmpty: function (canvasId, isEmpty) {
            var canvas = document.getElementById(canvasId);
            var body = canvas.closest('.card-body');
            body.querySelector('.chart-box').classList.toggle('d-none', isEmpty);
            body.querySelector('.chart-empty').classList.toggle('d-none', !isEmpty);
        }
    };
})();