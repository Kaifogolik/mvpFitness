import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../utils/cn';
import type { GlitchTextProps } from './types';

const GlitchText: React.FC<GlitchTextProps> = ({
  text,
  className,
  delay = 0,
  duration = 1,
  intensity = 'medium',
  colors = ['#8B5CF6', '#F97316'],
  enableOnHover = false,
  autoPlay = false,
  speed = 2,
  onComplete,
  ...props
}) => {
  const [isGlitching, setIsGlitching] = useState(autoPlay);

  useEffect(() => {
    if (autoPlay) {
      const timer = setTimeout(() => {
        setIsGlitching(true);
        if (onComplete) {
          setTimeout(onComplete, duration * 1000);
        }
      }, delay);
      return () => clearTimeout(timer);
    }
  }, [autoPlay, delay, duration, onComplete]);

  const handleMouseEnter = () => {
    if (enableOnHover) {
      setIsGlitching(true);
    }
  };

  const handleMouseLeave = () => {
    if (enableOnHover) {
      setIsGlitching(false);
    }
  };

  const getGlitchIntensity = () => {
    switch (intensity) {
      case 'low':
        return {
          translateX: 2,
          translateY: 1,
          skew: 1,
          opacity: 0.8,
        };
      case 'high':
        return {
          translateX: 6,
          translateY: 4,
          skew: 3,
          opacity: 0.9,
        };
      default: // medium
        return {
          translateX: 4,
          translateY: 2,
          skew: 2,
          opacity: 0.85,
        };
    }
  };

  const glitchParams = getGlitchIntensity();
  const [primaryColor, secondaryColor] = colors;

  return (
    <motion.div
      className={cn('relative inline-block select-none', className)}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      {...props}
    >
      {/* Main Text */}
      <motion.span
        className="relative z-30"
        animate={
          isGlitching
            ? {
                x: [0, glitchParams.translateX, -glitchParams.translateX, 0],
                y: [0, glitchParams.translateY, -glitchParams.translateY, 0],
                skewX: [0, glitchParams.skew, -glitchParams.skew, 0],
              }
            : {}
        }
        transition={{
          duration: duration / speed,
          repeat: autoPlay ? Infinity : 1,
          repeatDelay: 1,
          ease: 'easeInOut',
        }}
      >
        {text}
      </motion.span>

      {/* Glitch Layer 1 - Primary Color */}
      <motion.span
        className="absolute top-0 left-0 z-10"
        style={{
          color: primaryColor,
          textShadow: `2px 0 ${primaryColor}`,
          clipPath: isGlitching ? 'polygon(0 0, 100% 0, 100% 45%, 0 45%)' : 'none',
        }}
        animate={
          isGlitching
            ? {
                x: [0, -glitchParams.translateX, glitchParams.translateX, 0],
                opacity: [0, glitchParams.opacity, 0, glitchParams.opacity, 0],
              }
            : { opacity: 0 }
        }
        transition={{
          duration: duration / speed,
          repeat: autoPlay ? Infinity : 1,
          repeatDelay: 1,
          ease: 'linear',
        }}
      >
        {text}
      </motion.span>

      {/* Glitch Layer 2 - Secondary Color */}
      <motion.span
        className="absolute top-0 left-0 z-20"
        style={{
          color: secondaryColor,
          textShadow: `-2px 0 ${secondaryColor}`,
          clipPath: isGlitching ? 'polygon(0 55%, 100% 55%, 100% 100%, 0 100%)' : 'none',
        }}
        animate={
          isGlitching
            ? {
                x: [0, glitchParams.translateX, -glitchParams.translateX, 0],
                opacity: [0, glitchParams.opacity, 0, glitchParams.opacity, 0],
              }
            : { opacity: 0 }
        }
        transition={{
          duration: duration / speed,
          repeat: autoPlay ? Infinity : 1,
          repeatDelay: 1,
          ease: 'linear',
          delay: 0.1,
        }}
      >
        {text}
      </motion.span>

      {/* Digital Noise Overlay */}
      {isGlitching && (
        <motion.div
          className="absolute inset-0 z-40 pointer-events-none"
          style={{
            background: `
              linear-gradient(90deg, transparent 0%, ${primaryColor}20 25%, transparent 50%, ${secondaryColor}20 75%, transparent 100%),
              repeating-linear-gradient(0deg, transparent, transparent 2px, ${primaryColor}40 2px, ${primaryColor}40 4px)
            `,
            mixBlendMode: 'multiply',
          }}
          animate={{
            opacity: [0, 0.8, 0, 0.6, 0],
            scaleX: [1, 1.02, 0.98, 1.01, 1],
          }}
          transition={{
            duration: duration / (speed * 2),
            repeat: autoPlay ? Infinity : 2,
            ease: 'linear',
          }}
        />
      )}

      {/* Static Noise Lines */}
      {isGlitching && intensity !== 'low' && (
        <>
          {[...Array(3)].map((_, i) => (
            <motion.div
              key={`noise-${i}`}
              className="absolute z-50 pointer-events-none"
              style={{
                height: '1px',
                width: '100%',
                background: i % 2 === 0 ? primaryColor : secondaryColor,
                top: `${20 + i * 30}%`,
                left: 0,
              }}
              animate={{
                scaleX: [0, 1, 0],
                opacity: [0, 1, 0],
              }}
              transition={{
                duration: 0.1,
                repeat: autoPlay ? Infinity : 1,
                repeatDelay: Math.random() * 0.5,
                delay: i * 0.05,
              }}
            />
          ))}
        </>
      )}

      {/* Scanline Effect */}
      {isGlitching && intensity === 'high' && (
        <motion.div
          className="absolute inset-0 z-60 pointer-events-none"
          style={{
            background: `repeating-linear-gradient(
              0deg,
              transparent,
              transparent 2px,
              ${primaryColor}10 2px,
              ${primaryColor}10 4px
            )`,
          }}
          animate={{
            y: ['-100%', '100%'],
            opacity: [0, 0.5, 0],
          }}
          transition={{
            duration: duration / speed,
            repeat: autoPlay ? Infinity : 1,
            ease: 'linear',
          }}
        />
      )}
    </motion.div>
  );
};

export default GlitchText;
export type { GlitchTextProps }; 