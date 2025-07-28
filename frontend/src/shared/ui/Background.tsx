import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../utils/cn';

interface BackgroundProps {
  variant?: 'default' | 'dark' | 'minimal' | 'animated';
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
        return 'bg-gradient-to-br from-neutral-950 via-purple-950 to-neutral-900';
      case 'minimal':
        return 'bg-gradient-to-br from-neutral-50 to-neutral-100';
      case 'animated':
        return 'bg-gradient-to-br from-primary-500 via-secondary-500 to-primary-600 bg-gradient-animated';
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
      {/* Animated Abstract Forms */}
      {animate && (
        <>
          {/* Primary Orb */}
          <motion.div
            className="absolute -top-20 -left-20 w-96 h-96 rounded-full opacity-30"
            style={{
              background: 'radial-gradient(circle, rgba(139, 92, 246, 0.3) 0%, transparent 70%)',
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
            className="absolute -bottom-20 -right-20 w-80 h-80 rounded-full opacity-25"
            style={{
              background: 'radial-gradient(circle, rgba(249, 115, 22, 0.3) 0%, transparent 70%)',
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
            className="absolute top-1/4 right-1/4 w-32 h-32 rounded-full opacity-20"
            style={{
              background: 'linear-gradient(45deg, rgba(139, 92, 246, 0.2), rgba(249, 115, 22, 0.2))',
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

          {/* Mesh Pattern Overlay */}
          <div 
            className="absolute inset-0 opacity-10"
            style={{
              backgroundImage: `
                linear-gradient(90deg, rgba(139, 92, 246, 0.1) 1px, transparent 1px),
                linear-gradient(180deg, rgba(139, 92, 246, 0.1) 1px, transparent 1px)
              `,
              backgroundSize: '40px 40px',
            }}
          />

          {/* Subtle Gradient Overlay */}
          <motion.div
            className="absolute inset-0 opacity-20"
            style={{
              background: 'linear-gradient(135deg, rgba(139, 92, 246, 0.1) 0%, transparent 50%, rgba(249, 115, 22, 0.1) 100%)',
            }}
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

      {/* Content Container */}
      <div 
        className={cn(
          'relative z-10',
          overlayChildren && 'bg-white/5 backdrop-blur-sm'
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
              className="absolute top-1/2 left-1/2 w-64 h-64 rounded-full opacity-20"
              style={{
                background: 'radial-gradient(circle, rgba(139, 92, 246, 0.4) 0%, transparent 60%)',
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
              className="absolute top-1/4 left-3/4 w-16 h-16 rounded-full opacity-30"
              style={{
                background: 'radial-gradient(circle, rgba(249, 115, 22, 0.6) 0%, transparent 50%)',
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