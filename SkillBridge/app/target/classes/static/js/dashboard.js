document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('statusChart');
    if (!canvas) return;

    fetch('/api/analytics/summary')
        .then(res => res.json())
        .then(data => {
            const labels = Object.keys(data.applicationsByStatus);
            const values = Object.values(data.applicationsByStatus);

            new Chart(canvas, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Applications',
                        data: values,
                        backgroundColor: ['#64748b', '#f59e0b', '#dc2626', '#16a34a']
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
                }
            });
        })
        .catch(err => console.error('Failed to load analytics summary:', err));
});
