document.addEventListener('DOMContentLoaded', () => {
    console.log('eSkooly Frontend Initialized');
    
    // Simulate some stat updates or fetch from backend if needed
    // For now, let's just animate the numbers
    animateValue("stat-students", 0, 1250, 2000);
    animateValue("stat-teachers", 0, 85, 2000);
    animateValue("stat-classes", 0, 42, 2000);
});

function animateValue(id, start, end, duration) {
    if (start === end) return;
    const range = end - start;
    let current = start;
    const increment = end > start ? 1 : -1;
    const stepTime = Math.abs(Math.floor(duration / range));
    const obj = document.getElementById(id);
    if (!obj) return;
    
    const timer = setInterval(function() {
        current += increment;
        obj.innerHTML = current + '+';
        if (current == end) {
            clearInterval(timer);
        }
    }, stepTime);
}
