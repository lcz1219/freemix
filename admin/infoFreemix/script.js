// 平滑滚动效果
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({
            behavior: 'smooth'
        });
    });
});

// 简单的下载按钮点击事件
// document.querySelectorAll('.download-button').forEach(button => {
//     button.addEventListener('click', function(e) {
//         e.preventDefault();
//         alert('感谢您的下载兴趣！实际下载链接将在产品发布时提供。');
//     });
// });

// 添加视差滚动效果
window.addEventListener('scroll', function() {
    const scrollPosition = window.scrollY;
    const heroSection = document.querySelector('.hero');
    
    if (heroSection) {
        heroSection.style.backgroundPositionY = scrollPosition * 0.5 + 'px';
    }
});

// 添加动画效果到元素当它们进入视窗时
const observerOptions = {
    root: null,
    rootMargin: '0px',
    threshold: 0.1
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('animated');
        }
    });
}, observerOptions);

document.querySelectorAll('.feature-card, .platform-card, .screenshot-card').forEach(card => {
    observer.observe(card);
});

// 添加动画类到CSS
const style = document.createElement('style');
style.innerHTML = `
    .feature-card, .platform-card, .screenshot-card {
        opacity: 0;
        transform: translateY(20px);
        transition: opacity 0.6s ease, transform 0.6s ease;
    }
    
    .feature-card.animated, .platform-card.animated, .screenshot-card.animated {
        opacity: 1;
        transform: translateY(0);
    }
`;
document.head.appendChild(style);