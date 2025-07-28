import React, { useRef, useEffect } from 'react';
import { motion, useAnimation, useInView } from 'framer-motion';
import { cn } from '../../utils/cn';
import type { ShinyTextProps } from './types';

const ShinyText: React.FC<ShinyTextProps> = ({
  text,
  className,
  delay = 0,
  duration = 2,
  shimmerDirection = 'right',
  shimmerWidth = '100px',
  shimmerColor = 'rgba(255, 255, 255, 0.8)',
  playOnHover = false,
  autoPlay = true,
  speed = 3,
  onComplete,
  ...props
}) => {
  const ref = useRef<HTMLDivElement>(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });
  const controls = useAnimation();

  useEffect(() => {
    if (autoPlay && isInView) {
      const timer = setTimeout(() => {
        playShimmer();
      }, delay);
      return () => clearTimeout(timer);
    }
  }, [isInView, delay, autoPlay]);

  const playShimmer = () => {
    controls.start('animate');
  };

  const handleMouseEnter = () => {
    if (playOnHover) {
      playShimmer();
    }
  };

  const getShimmerVariants = () => {
    const distance = '200%';
    
    switch (shimmerDirection) {
      case 'left':
        return {
          initial: { x: distance },
          animate: { x: `-${distance}` },
        };
      case 'top':
        return {
          initial: { y: distance },
          animate: { y: `-${distance}` },
        };
      case 'bottom':
        return {
          initial: { y: `-${distance}` },
          animate: { y: distance },
        };
      default: // right
        return {
          initial: { x: `-${distance}` },
          animate: { x: distance },
        };
    }
  };

  const shimmerVariants = getShimmerVariants();

  const isVertical = shimmerDirection === 'top' || shimmerDirection === 'bottom';

  return (
    <motion.div
      ref={ref}
      className={cn(
        'relative inline-block overflow-hidden',
        'bg-gradient-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent',
        className
      )}
      onMouseEnter={handleMouseEnter}
      {...props}
    >
      <span className="relative z-10">{text}</span>
      
      {/* Shimmer Effect */}
      <motion.div
        className="absolute inset-0 z-20"
        style={{
          background: isVertical
            ? `linear-gradient(to ${shimmerDirection === 'top' ? 'bottom' : 'top'}, transparent 30%, ${shimmerColor} 50%, transparent 70%)`
            : `linear-gradient(to ${shimmerDirection === 'left' ? 'left' : 'right'}, transparent 30%, ${shimmerColor} 50%, transparent 70%)`,
          [isVertical ? 'height' : 'width']: shimmerWidth,
          mixBlendMode: 'overlay',
        }}
        variants={{
          initial: shimmerVariants.initial,
          animate: {
            ...shimmerVariants.animate,
            transition: {
              duration: duration / speed,
              ease: 'easeInOut',
              repeat: autoPlay ? Infinity : 0,
              repeatDelay: 3,
              onComplete,
            },
          },
        }}
        initial="initial"
        animate={controls}
      />

      {/* Enhanced Gradient Text Base */}
      <motion.span
        className="absolute inset-0 bg-gradient-to-r from-primary-600 via-secondary-500 to-primary-600 bg-clip-text text-transparent opacity-90"
        style={{
          backgroundSize: '200% auto',
        }}
        animate={{
          backgroundPosition: ['0% center', '200% center', '0% center'],
        }}
        transition={{
          duration: 8,
          repeat: Infinity,
          ease: 'linear',
        }}
      >
        {text}
      </motion.span>

      {/* Glow Effect */}
      <motion.div
        className="absolute inset-0 opacity-30 blur-sm"
        style={{
          background: 'linear-gradient(45deg, rgba(168, 85, 247, 0.5), rgba(249, 115, 22, 0.5))',
          clipPath: 'text',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
        }}
        animate={{
          scale: [1, 1.02, 1],
          opacity: [0.3, 0.6, 0.3],
        }}
        transition={{
          duration: 2,
          repeat: Infinity,
          ease: 'easeInOut',
        }}
      >
        {text}
      </motion.div>
    </motion.div>
  );
};

export default ShinyText;
export type { ShinyTextProps }; 