import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../utils/cn';

interface BackgroundProps {
  variant?: 'default' | 'dark' | 'minimal' | 'animated' | 'glass' | 'morphism';
  opacity?: number;
  animate?: boolean;
  overlayChildren?: boolean;
  className?: string;
  children?: React.ReactNode;
}

const Background: React.FC<BackgroundProps> = ({
  variant = 'default',
  opacity = 1,
  animate = true,
  overlayChildren = false,
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
        return 'bg-gradient-to-br from-primary-500 via-secondary-500 to-primary-600 bg-gradient-animated';
      case 'glass':
        return 'bg-gradient-to-br from-neutral-50/80 via-white/90 to-primary-50/80 dark:from-neutral-950/80 dark:via-neutral-900/90 dark:to-neutral-800/80 backdrop-blur-xl';
      case 'morphism':
        return 'bg-gradient-to-br from-neutral-100 via-neutral-50 to-neutral-100 dark:from-neutral-800 dark:via-neutral-900 dark:to-neutral-800';
      default:
        return 'bg-gradient-to-br from-neutral-50 via-white to-primary-50 dark:from-neutral-950 dark:via-neutral-900 dark:to-neutral-800';
    }
  };

  return (
    <div 
      className={cn(
        'relative min-h-screen overflow-hidden',
        getBackgroundVariant(),
        className
      )}
      style={{ opacity }}
    >
      {/* Animated Abstract Forms - только с design tokens */}
      {animate && (
        <>
          {/* Primary Orb */}
          <motion.div
            className="absolute -top-20 -left-20 w-96 h-96 rounded-full opacity-30 bg-primary-500/30 dark:bg-primary-400/20"
            style={{
              filter: 'blur(40px)',
            }}
            animate={{
              x: [0, 50, 0],
              y: [0, 30, 0],
              scale: [1, 1.1, 1],
            }}
            transition={{
              duration: 8,
              repeat: Infinity,
              ease: 'easeInOut'
            }}
          />

          {/* Secondary Orb */}
          <motion.div
            className="absolute -bottom-20 -right-20 w-80 h-80 rounded-full opacity-25 bg-secondary-500/30 dark:bg-secondary-400/20"
            style={{
              filter: 'blur(35px)',
            }}
            animate={{
              x: [0, -40, 0],
              y: [0, -25, 0],
              scale: [1, 1.2, 1],
            }}
            transition={{
              duration: 10,
              repeat: Infinity,
              ease: 'easeInOut',
              delay: 2
            }}
          />

          {/* Floating Elements */}
          <motion.div
            className="absolute top-1/4 right-1/4 w-32 h-32 rounded-full opacity-20 bg-gradient-to-r from-primary-400/20 to-secondary-400/20"
            style={{
              filter: 'blur(20px)',
            }}
            animate={{
              x: [0, 20, -10, 0],
              y: [0, -15, 10, 0],
              rotate: [0, 180, 360],
            }}
            transition={{
              duration: 12,
              repeat: Infinity,
              ease: 'linear'
            }}
          />

          {/* Mesh Pattern Overlay - используем CSS custom properties */}
          <div 
            className="absolute inset-0 opacity-10 dark:opacity-5"
            style={{
              backgroundImage: `
                linear-gradient(90deg, rgb(139 92 246 / 0.1) 1px, transparent 1px),
                linear-gradient(180deg, rgb(139 92 246 / 0.1) 1px, transparent 1px)
              `,
              backgroundSize: '40px 40px',
            }}
          />

          {/* Dynamic Gradient Overlay */}
          <motion.div
            className="absolute inset-0 opacity-20 bg-gradient-to-br from-primary-500/10 via-transparent to-secondary-500/10"
            animate={{
              backgroundPosition: ['0% 0%', '100% 100%', '0% 0%'],
            }}
            transition={{
              duration: 20,
              repeat: Infinity,
              ease: 'linear'
            }}
          />
        </>
      )}

      {/* Soft Morphism Effects */}
      {variant === 'morphism' && (
        <>
          <div className="absolute inset-0 bg-gradient-to-br from-white/50 via-transparent to-primary-100/30 dark:from-neutral-800/50 dark:to-neutral-700/30" />
          <div className="absolute inset-0 shadow-neumorphism-inset" style={{ mixBlendMode: 'multiply' }} />
        </>
      )}

      {/* Glass Effects */}
      {variant === 'glass' && (
        <div className="absolute inset-0 bg-white/5 backdrop-blur-sm border border-white/10" />
      )}

      {/* Content Container */}
      <div 
        className={cn(
          'relative z-10',
          overlayChildren && 'bg-white/5 backdrop-blur-sm dark:bg-neutral-900/5'
        )}
      >
        {children}
      </div>

      {/* Dark Mode Enhancements */}
      <div className="hidden dark:block">
        {animate && (
          <>
            {/* Neon Glow Effects for Dark Mode */}
            <motion.div
              className="absolute top-1/2 left-1/2 w-64 h-64 rounded-full opacity-20 bg-primary-500/40"
              style={{
                filter: 'blur(60px)',
                transform: 'translate(-50%, -50%)',
              }}
              animate={{
                scale: [1, 1.3, 1],
                opacity: [0.2, 0.4, 0.2],
              }}
              transition={{
                duration: 6,
                repeat: Infinity,
                ease: 'easeInOut'
              }}
            />

            {/* Accent Lights */}
            <motion.div
              className="absolute top-1/4 left-3/4 w-16 h-16 rounded-full opacity-30 bg-secondary-500/60"
              style={{
                filter: 'blur(15px)',
              }}
              animate={{
                opacity: [0.3, 0.8, 0.3],
              }}
              transition={{
                duration: 4,
                repeat: Infinity,
                ease: 'easeInOut',
                delay: 1
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