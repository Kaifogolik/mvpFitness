import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../utils/cn';

interface BackgroundProps {
  variant?: 'default' | 'dark' | 'minimal' | 'animated' | 'glass' | 'morphism' | 'fitness' | 'neon';
  opacity?: number;
  animate?: boolean;
  overlayChildren?: boolean;
  particleCount?: number;
  className?: string;
  children?: React.ReactNode;
}

const Background: React.FC<BackgroundProps> = ({
  variant = 'default',
  opacity = 1,
  animate = true,
  overlayChildren = false,
  particleCount = 5,
  className,
  children
}) => {
  const getBackgroundVariant = () => {
    switch (variant) {
      case 'dark':
        return 'bg-gradient-to-br from-neutral-950 via-neutral-900 to-neutral-800';
      case 'minimal':
        return 'bg-gradient-to-br from-neutral-50 to-neutral-100 dark:from-neutral-900 dark:to-neutral-800';
      case 'animated':
        return 'bg-gradient-to-br from-primary-500 via-secondary-500 to-primary-600 animate-gradient-shift';
      case 'glass':
        return 'bg-gradient-to-br from-white/80 via-primary-50/90 to-secondary-50/80 dark:from-neutral-950/80 dark:via-neutral-900/90 dark:to-neutral-800/80 backdrop-blur-xl';
      case 'morphism':
        return 'bg-gradient-to-br from-neutral-100 via-neutral-50 to-neutral-100 dark:from-neutral-800 dark:via-neutral-900 dark:to-neutral-800';
      case 'fitness':
        return 'bg-gradient-to-br from-primary-900 via-primary-800 to-secondary-900 dark:from-primary-950 dark:via-primary-900 dark:to-secondary-950';
      case 'neon':
        return 'bg-gradient-to-br from-neutral-950 via-primary-950/50 to-secondary-950/50';
      default:
        return 'bg-gradient-to-br from-neutral-50 via-white to-primary-50 dark:from-neutral-950 dark:via-neutral-900 dark:to-neutral-800';
    }
  };

  // Генерируем частицы
  const particles = Array.from({ length: particleCount }, (_, i) => ({
    id: i,
    size: Math.random() * 100 + 50,
    delay: Math.random() * 5,
    duration: Math.random() * 10 + 15,
    x: Math.random() * 100,
    y: Math.random() * 100,
  }));

  return (
    <div 
      className={cn(
        'relative min-h-screen overflow-hidden',
        getBackgroundVariant(),
        className
      )}
      style={{ opacity }}
    >
      {/* Революционные floating particles */}
      {animate && particles.map((particle) => (
        <motion.div
          key={particle.id}
          className={cn(
            'absolute rounded-full opacity-20',
            variant === 'fitness' ? 'bg-gradient-to-r from-primary-400/30 to-secondary-400/30' :
            variant === 'neon' ? 'bg-gradient-to-r from-primary-500/40 to-secondary-500/40' :
            'bg-gradient-to-r from-primary-400/20 to-secondary-400/20'
          )}
          style={{
            width: particle.size,
            height: particle.size,
            left: `${particle.x}%`,
            top: `${particle.y}%`,
            filter: 'blur(15px)',
          }}
          animate={{
            x: [0, 100, -50, 0],
            y: [0, -80, 120, 0],
            scale: [1, 1.3, 0.8, 1],
            rotate: [0, 180, 360],
          }}
          transition={{
            duration: particle.duration,
            delay: particle.delay,
            repeat: Infinity,
            ease: 'easeInOut',
          }}
        />
      ))}

      {/* Главные орбы с живыми эффектами */}
      {animate && (
        <>
          {/* Primary Orb - революционные эффекты */}
          <motion.div
            className={cn(
              'absolute -top-32 -left-32 w-96 h-96 opacity-30 animate-morph',
              variant === 'fitness' ? 'bg-primary-500/40' : 'bg-primary-500/30'
            )}
            style={{
              filter: 'blur(60px)',
              background: variant === 'neon' 
                ? 'radial-gradient(circle, rgba(168, 85, 247, 0.6) 0%, rgba(249, 115, 22, 0.4) 70%, transparent 100%)'
                : undefined
            }}
            animate={{
              x: [0, 80, -40, 0],
              y: [0, 60, -30, 0],
              scale: [1, 1.4, 0.9, 1],
            }}
            transition={{
              duration: 20,
              repeat: Infinity,
              ease: 'easeInOut'
            }}
          />

          {/* Secondary Orb - пульсирующий эффект */}
          <motion.div
            className={cn(
              'absolute -bottom-32 -right-32 w-80 h-80 opacity-25 animate-pulse-glow',
              variant === 'fitness' ? 'bg-secondary-500/40' : 'bg-secondary-500/30'
            )}
            style={{
              filter: 'blur(50px)',
              background: variant === 'neon'
                ? 'radial-gradient(circle, rgba(249, 115, 22, 0.7) 0%, rgba(168, 85, 247, 0.3) 70%, transparent 100%)'
                : undefined
            }}
            animate={{
              x: [0, -60, 40, 0],
              y: [0, -40, 60, 0],
              scale: [1, 1.5, 1.1, 1],
            }}
            transition={{
              duration: 25,
              repeat: Infinity,
              ease: 'easeInOut',
              delay: 3
            }}
          />

          {/* Средние floating элементы */}
          <motion.div
            className="absolute top-1/3 right-1/3 w-40 h-40 rounded-full opacity-20 bg-gradient-to-r from-primary-400/30 to-secondary-400/30 animate-orb-float"
            style={{ filter: 'blur(25px)' }}
            animate={{
              x: [0, 60, -30, 0],
              y: [0, -40, 80, 0],
              rotate: [0, 120, 240, 360],
            }}
            transition={{
              duration: 18,
              repeat: Infinity,
              ease: 'linear'
            }}
          />

          {/* Mesh Pattern с живыми эффектами */}
          <motion.div 
            className="absolute inset-0 opacity-[0.03] dark:opacity-[0.08]"
            style={{
              backgroundImage: `
                linear-gradient(90deg, rgb(139 92 246 / 0.4) 1px, transparent 1px),
                linear-gradient(180deg, rgb(249 115 22 / 0.4) 1px, transparent 1px)
              `,
              backgroundSize: '60px 60px',
            }}
            animate={{
              backgroundPosition: ['0px 0px', '60px 60px', '0px 0px'],
            }}
            transition={{
              duration: 30,
              repeat: Infinity,
              ease: 'linear'
            }}
          />

          {/* Динамический Gradient Overlay */}
          <motion.div
            className="absolute inset-0 opacity-30 bg-gradient-to-br from-primary-500/20 via-transparent to-secondary-500/20"
            animate={{
              backgroundPosition: ['0% 0%', '100% 100%', '0% 0%'],
              backgroundSize: ['100% 100%', '200% 200%', '100% 100%'],
            }}
            transition={{
              duration: 40,
              repeat: Infinity,
              ease: 'linear'
            }}
          />
        </>
      )}

      {/* Fitness-специфичные эффекты */}
      {variant === 'fitness' && animate && (
        <>
          {/* Энергетические волны */}
          <motion.div
            className="absolute inset-0 opacity-10"
            style={{
              background: 'radial-gradient(circle at 30% 70%, rgba(168, 85, 247, 0.3) 0%, transparent 50%), radial-gradient(circle at 70% 30%, rgba(249, 115, 22, 0.3) 0%, transparent 50%)',
            }}
            animate={{
              scale: [1, 1.2, 1],
              rotate: [0, 90, 180, 270, 360],
            }}
            transition={{
              duration: 50,
              repeat: Infinity,
              ease: 'linear'
            }}
          />

          {/* Sparkle эффекты */}
          {[...Array(8)].map((_, i) => (
            <motion.div
              key={`sparkle-${i}`}
              className="absolute w-2 h-2 bg-white rounded-full opacity-60 animate-sparkle"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
              }}
              animate={{
                scale: [0, 1, 0],
                opacity: [0, 1, 0],
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                delay: i * 0.3,
                ease: 'easeInOut'
              }}
            />
          ))}
        </>
      )}

      {/* Neon режим - киберпанк эффекты */}
      {variant === 'neon' && animate && (
        <>
          {/* Неоновые линии */}
          <motion.div
            className="absolute inset-0 opacity-20"
            style={{
              background: `
                linear-gradient(90deg, transparent 48%, rgba(168, 85, 247, 0.5) 49%, rgba(168, 85, 247, 0.5) 51%, transparent 52%),
                linear-gradient(0deg, transparent 48%, rgba(249, 115, 22, 0.5) 49%, rgba(249, 115, 22, 0.5) 51%, transparent 52%)
              `,
              backgroundSize: '100px 100px',
            }}
            animate={{
              backgroundPosition: ['0px 0px', '100px 100px'],
            }}
            transition={{
              duration: 10,
              repeat: Infinity,
              ease: 'linear'
            }}
          />

          {/* Пульсирующий центральный glow */}
          <motion.div
            className="absolute top-1/2 left-1/2 w-96 h-96 rounded-full opacity-30 bg-primary-500/50"
            style={{
              filter: 'blur(80px)',
              transform: 'translate(-50%, -50%)',
            }}
            animate={{
              scale: [1, 1.8, 1],
              opacity: [0.3, 0.7, 0.3],
            }}
            transition={{
              duration: 4,
              repeat: Infinity,
              ease: 'easeInOut'
            }}
          />
        </>
      )}

      {/* Glassmorphism эффекты */}
      {variant === 'glass' && (
        <>
          <div className="absolute inset-0 bg-white/10 backdrop-blur-sm border border-white/20 dark:border-white/10" />
          <motion.div
            className="absolute inset-0 bg-gradient-to-br from-white/5 to-transparent"
            animate={{
              opacity: [0.05, 0.15, 0.05],
            }}
            transition={{
              duration: 8,
              repeat: Infinity,
              ease: 'easeInOut'
            }}
          />
        </>
      )}

      {/* Soft Morphism Effects */}
      {variant === 'morphism' && (
        <>
          <div className="absolute inset-0 bg-gradient-to-br from-white/50 via-transparent to-primary-100/30 dark:from-neutral-800/50 dark:to-neutral-700/30" />
          <div className="absolute inset-0 shadow-neumorphism-inset" style={{ mixBlendMode: 'soft-light' }} />
        </>
      )}

      {/* Content Container с адаптивным overlay */}
      <div 
        className={cn(
          'relative z-10',
          overlayChildren && variant === 'glass' && 'bg-white/5 backdrop-blur-sm dark:bg-neutral-900/5',
          overlayChildren && variant === 'morphism' && 'bg-white/10 dark:bg-neutral-800/10',
          overlayChildren && variant === 'neon' && 'bg-neutral-950/20 backdrop-blur-sm'
        )}
      >
        {children}
      </div>

      {/* Dark Mode Enhancements */}
      <div className="hidden dark:block">
        {animate && (
          <>
            {/* Enhanced Neon Glow для Dark Mode */}
            <motion.div
              className="absolute top-1/2 left-1/2 w-80 h-80 rounded-full opacity-20 bg-primary-500/60"
              style={{
                filter: 'blur(100px)',
                transform: 'translate(-50%, -50%)',
              }}
              animate={{
                scale: [1, 1.5, 1],
                opacity: [0.2, 0.6, 0.2],
              }}
              transition={{
                duration: 8,
                repeat: Infinity,
                ease: 'easeInOut'
              }}
            />

            {/* Accent Lights - улучшенные */}
            <motion.div
              className="absolute top-1/4 left-3/4 w-24 h-24 rounded-full opacity-40 bg-secondary-500/80 animate-drift"
              style={{ filter: 'blur(20px)' }}
              animate={{
                opacity: [0.4, 0.9, 0.4],
                scale: [1, 1.3, 1],
              }}
              transition={{
                duration: 6,
                repeat: Infinity,
                ease: 'easeInOut',
                delay: 2
              }}
            />

            {/* Дополнительные accent points */}
            <motion.div
              className="absolute bottom-1/4 right-1/4 w-16 h-16 rounded-full opacity-30 bg-primary-400/60"
              style={{ filter: 'blur(15px)' }}
              animate={{
                opacity: [0.3, 0.7, 0.3],
                x: [0, 20, 0],
                y: [0, -15, 0],
              }}
              transition={{
                duration: 10,
                repeat: Infinity,
                ease: 'easeInOut',
                delay: 4
              }}
            />
          </>
        )}
      </div>
    </div>
  );
};

export default Background;
export type { BackgroundProps }; 