import React, { useEffect, useState } from 'react';
import { cn } from '../../utils/cn';
import type { ShinyTextProps } from './types';

const ShinyText: React.FC<ShinyTextProps> = ({
  text,
  className,
  speed = 3,
  direction = 'left-to-right',
  shimmerWidth = 100,
  color,
  autoPlay = true,
  playOnHover = false,
  ...props
}) => {
  const [isShimmering, setIsShimmering] = useState(autoPlay);

  useEffect(() => {
    if (autoPlay) {
      setIsShimmering(true);
    }
  }, [autoPlay]);

  const handleMouseEnter = () => {
    if (playOnHover) {
      setIsShimmering(true);
    }
  };

  const handleMouseLeave = () => {
    if (playOnHover) {
      setIsShimmering(false);
    }
  };

  const shimmerGradient = color 
    ? `linear-gradient(90deg, transparent, ${color}, transparent)`
    : 'linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent)';

  const animationDirection = direction === 'left-to-right' ? 'shimmer-left-to-right' : 'shimmer-right-to-left';

  return (
    <span
      className={cn(
        'relative inline-block overflow-hidden',
        playOnHover && 'cursor-pointer',
        className
      )}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      {...props}
    >
      <span className="relative z-10">
        {text}
      </span>
      
      {isShimmering && (
        <span
          className={cn(
            'absolute inset-0 z-20 pointer-events-none',
            `animate-${animationDirection}`
          )}
          style={{
            background: shimmerGradient,
            width: `${shimmerWidth}%`,
            animationDuration: `${speed}s`,
            animationIterationCount: autoPlay ? 'infinite' : '1',
            animationFillMode: 'forwards',
          }}
        />
      )}
      
      <style>{`
        @keyframes shimmer-left-to-right {
          0% {
            transform: translateX(-100%);
          }
          100% {
            transform: translateX(200%);
          }
        }
        
        @keyframes shimmer-right-to-left {
          0% {
            transform: translateX(200%);
          }
          100% {
            transform: translateX(-100%);
          }
        }
        
        .animate-shimmer-left-to-right {
          animation: shimmer-left-to-right linear;
        }
        
        .animate-shimmer-right-to-left {
          animation: shimmer-right-to-left linear;
        }
      `}</style>
    </span>
  );
};

export { ShinyText };
export type { ShinyTextProps }; 