import React, { useState, useEffect } from 'react';
import { cn } from '../../utils/cn';
import type { GlitchTextProps } from './types';

const GlitchText: React.FC<GlitchTextProps> = ({
  text,
  className,
  enableOnHover = false,
  glitchIntensity = 'medium',
  colors = {
    primary: '#8B5CF6',
    secondary: '#F97316',
  },
  autoPlay = false,
  playDuration = 3000,
  ...props
}) => {
  const [isGlitching, setIsGlitching] = useState(autoPlay);

  useEffect(() => {
    if (autoPlay) {
      const interval = setInterval(() => {
        setIsGlitching(true);
        setTimeout(() => setIsGlitching(false), 500);
      }, playDuration);

      return () => clearInterval(interval);
    }
    
    return () => {}; // Explicit return for non-autoPlay case
  }, [autoPlay, playDuration]);

  const getIntensityClass = () => {
    const intensityMap = {
      low: 'animate-glitch-low',
      medium: 'animate-glitch-medium',
      high: 'animate-glitch-high',
    };
    return intensityMap[glitchIntensity];
  };

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

  return (
    <span
      className={cn(
        'relative inline-block overflow-hidden',
        enableOnHover && 'cursor-pointer',
        className
      )}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      {...props}
    >
      <span
        className={cn(
          'relative z-10',
          isGlitching && getIntensityClass()
        )}
        style={{
          '--glitch-color-1': colors.primary,
          '--glitch-color-2': colors.secondary,
        } as React.CSSProperties}
      >
        {text}
      </span>
      
      {isGlitching && (
        <>
          {/* Shadow 1 */}
          <span
            className="absolute top-0 left-0 w-full h-full opacity-80 animate-glitch-shadow-1"
            style={{
              color: colors.primary,
              transform: 'translate(-2px, 0)',
              zIndex: -1,
            }}
            aria-hidden="true"
          >
            {text}
          </span>
          
          {/* Shadow 2 */}
          <span
            className="absolute top-0 left-0 w-full h-full opacity-80 animate-glitch-shadow-2"
            style={{
              color: colors.secondary,
              transform: 'translate(2px, 0)',
              zIndex: -2,
            }}
            aria-hidden="true"
          >
            {text}
          </span>
        </>
      )}
      
      
    </span>
  );
};

export { GlitchText };
export type { GlitchTextProps }; 