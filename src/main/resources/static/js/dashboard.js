// Dashboard graphs: fetch JSON from GET /dashboard/chart-data and draw 3 Chart.js charts.
(function () {
    var root = document.getElementById('dashboardCharts');
    if (!root || !window.Chart || !window.EmsCharts) {
        return;
    }
    var c = EmsCharts.colors;

    fetch(root.dataset.chartUrl, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
        .then(function (response) {
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            return response.json();
        })
        .then(function (data) {
            drawAttendanceTrend(data.attendanceTrend);
            drawLeaveTrend(data.leaveTrend);
            drawDepartmentDistribution(data.departmentDistribution);
        })
        .catch(function (error) {
            console.error('Dashboard charts failed to load', error);
            document.getElementById('chartError').classList.remove('d-none');
        });

    /** Line chart: Present / Half Day / Absent per day. */
    function drawAttendanceTrend(trend) {
        if (EmsCharts.total(trend.present, trend.halfDay, trend.absent) === 0) {
            EmsCharts.showEmpty('attendanceChart', true);
            return;
        }
        new Chart(document.getElementById('attendanceChart'), {
            type: 'line',
            data: {
                labels: trend.labels,
                datasets: [
                    line('Present', trend.present, c.green),
                    line('Half Day', trend.halfDay, c.orange),
                    line('Absent', trend.absent, c.red)
                ]
            },
            options: {
                interaction: { mode: 'index', intersect: false }, // tooltip shows all 3 values for the hovered day
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    /** Stacked bar chart: Approved / Pending / Rejected leave requests per month. */
    function drawLeaveTrend(trend) {
        if (EmsCharts.total(trend.approved, trend.pending, trend.rejected) === 0) {
            EmsCharts.showEmpty('leaveChart', true);
            return;
        }
        new Chart(document.getElementById('leaveChart'), {
            type: 'bar',
            data: {
                labels: trend.labels,
                datasets: [
                    bar('Approved', trend.approved, c.green),
                    bar('Pending', trend.pending, c.orange),
                    bar('Rejected', trend.rejected, c.red)
                ]
            },
            options: {
                interaction: { mode: 'index', intersect: false },
                scales: {
                    x: { stacked: true },
                    y: { stacked: true, beginAtZero: true, ticks: { precision: 0 } }
                }
            }
        });
    }

    /** Doughnut chart: number of employees in each department. */
    function drawDepartmentDistribution(dist) {
        if (EmsCharts.total(dist.counts) === 0) {
            EmsCharts.showEmpty('departmentChart', true);
            return;
        }
        new Chart(document.getElementById('departmentChart'), {
            type: 'doughnut',
            data: {
                labels: dist.labels,
                datasets: [{
                    data: dist.counts,
                    backgroundColor: dist.labels.map(function (_, i) { return EmsCharts.palette[i % EmsCharts.palette.length]; }),
                    borderWidth: 2
                }]
            },
            options: { cutout: '60%' }
        });
    }

    function line(label, values, color) {
        return {
            label: label,
            data: values,
            borderColor: color,
            backgroundColor: color + '26', // same color, ~15% opacity, for the filled area
            fill: true,
            cubicInterpolationMode: 'monotone', // smooth curve that never overshoots below 0
            pointRadius: 4
        };
    }

    function bar(label, values, color) {
        return { label: label, data: values, backgroundColor: color, borderRadius: 4, maxBarThickness: 40 };
    }
})();