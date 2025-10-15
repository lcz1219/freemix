// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            window.scrollTo({
                top: target.offsetTop - 80,
                behavior: 'smooth'
            });
        }
    });
});

// Header scroll effect
window.addEventListener('scroll', function() {
    const header = document.querySelector('header');
    if (window.scrollY > 50) {
        header.classList.add('scrolled');
    } else {
        header.classList.remove('scrolled');
    }
});

// Parallax effect for hero section
document.addEventListener('mousemove', function(e) {
    const hero = document.querySelector('.hero');
    const x = (window.innerWidth - e.pageX) / 100;
    const y = (window.innerHeight - e.pageY) / 100;
    hero.style.backgroundPosition = `${x}px ${y}px`;
});

// Animation on scroll
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

// Add CSS for animations
const style = document.createElement('style');
style.textContent = `
    .feature-card, .platform-card, .screenshot-card {
        opacity: 0;
        transform: translateY(50px) scale(0.9);
        transition: opacity 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275), 
                    transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
    
    .feature-card.animated, .platform-card.animated, .screenshot-card.animated {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
    
    /* Staggered animations */
    .feature-card:nth-child(1), .platform-card:nth-child(1), .screenshot-card:nth-child(1) {
        transition-delay: 0.1s;
    }
    
    .feature-card:nth-child(2), .platform-card:nth-child(2), .screenshot-card:nth-child(2) {
        transition-delay: 0.2s;
    }
    
    .feature-card:nth-child(3), .platform-card:nth-child(3), .screenshot-card:nth-child(3) {
        transition-delay: 0.3s;
    }
    
    .feature-card:nth-child(4), .platform-card:nth-child(4), .screenshot-card:nth-child(4) {
        transition-delay: 0.4s;
    }
    
    .feature-card:nth-child(5), .platform-card:nth-child(5), .screenshot-card:nth-child(5) {
        transition-delay: 0.5s;
    }
    
    .feature-card:nth-child(6), .platform-card:nth-child(6), .screenshot-card:nth-child(6) {
        transition-delay: 0.6s;
    }
`;
document.head.appendChild(style);

// Enhanced CTA button ripple effect
document.querySelectorAll('.cta-button').forEach(button => {
    button.addEventListener('click', function(e) {
        // Remove any existing ripples
        const existingRipples = this.querySelectorAll('.ripple');
        existingRipples.forEach(ripple => ripple.remove());
        
        // Create ripple
        const ripple = document.createElement('span');
        ripple.classList.add('ripple');
        this.appendChild(ripple);
        
        // Position ripple
        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        
        // Remove ripple after animation
        setTimeout(() => {
            ripple.remove();
        }, 600);
    });
});

// Add CSS for ripple effect
const rippleStyle = document.createElement('style');
rippleStyle.textContent = `
    .cta-button {
        position: relative;
        overflow: hidden;
        transform: translate3d(0, 0, 0);
    }
    
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.7);
        transform: scale(0);
        animation: ripple 0.6s linear;
        pointer-events: none;
    }
    
    @keyframes ripple {
        to {
            transform: scale(2.5);
            opacity: 0;
        }
    }
`;
document.head.appendChild(rippleStyle);

// Background particle effect
document.addEventListener('DOMContentLoaded', function() {
    const hero = document.querySelector('.hero');
    const particlesContainer = document.createElement('div');
    particlesContainer.classList.add('particles');
    hero.appendChild(particlesContainer);
    
    // Create particles
    for (let i = 0; i < 30; i++) {
        const particle = document.createElement('div');
        particle.classList.add('particle');
        particle.style.left = Math.random() * 100 + '%';
        particle.style.top = Math.random() * 100 + '%';
        particle.style.animationDelay = Math.random() * 5 + 's';
        particle.style.width = Math.random() * 10 + 2 + 'px';
        particle.style.height = particle.style.width;
        particlesContainer.appendChild(particle);
    }
    
    // Add CSS for particles
    const particleStyle = document.createElement('style');
    particleStyle.textContent = `
        .particles {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 0;
        }
        
        .particle {
            position: absolute;
            background: rgba(255, 255, 255, 0.15);
            border-radius: 50%;
            animation: float 15s infinite ease-in-out;
        }
        
        @keyframes float {
            0%, 100% {
                transform: translate(0, 0);
            }
            25% {
                transform: translate(20px, 20px);
            }
            50% {
                transform: translate(-20px, 20px);
            }
            75% {
                transform: translate(-20px, -20px);
            }
        }
    `;
    document.head.appendChild(particleStyle);
});