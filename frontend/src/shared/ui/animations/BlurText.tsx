import React, { useRef, useEffect } from 'react';
import { motion, useAnimation, useInView } from 'framer-motion';
import { cn } from '../../utils/cn';
import type { BlurTextProps } from './types';

const BlurText: React.FC<BlurTextProps> = ({
  text,
  className,
  delay = 0,
  duration = 0.8,
  direction = 'top',
  distance = 20,
  blur = 10,
  triggerOnScroll = true,
  onComplete,
  ...props
}) => {
  const ref = useRef<HTMLDivElement>(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });
  const controls = useAnimation();

  useEffect(() => {
    if ((triggerOnScroll && isInView) || !triggerOnScroll) {
      controls.start('visible');
    }
  }, [controls, isInView, triggerOnScroll]);

  const getDirectionVariables = () => {
    switch (direction) {
      case 'top':
        return { x: 0, y: -distance };
      case 'bottom':
        return { x: 0, y: distance };
      case 'left':
        return { x: -distance, y: 0 };
      case 'right':
        return { x: distance, y: 0 };
      default:
        return { x: 0, y: -distance };
    }
  };

  const { x, y } = getDirectionVariables();

  const transition = {
    duration,
    delay: delay / 1000,
    ease: [0.25, 0.1, 0.25, 1] as const,
  };

  const variants = {
    hidden: {
      opacity: 0,
      filter: `blur(${blur}px)`,
      x,
      y,
    },
    visible: {
      opacity: 1,
      filter: 'blur(0px)',
      x: 0,
      y: 0,
      transition: {
        ...transition,
        onComplete,
      },
    },
  };

  return (
    <motion.div
      ref={ref}
      className={cn('inline-block', className)}
      variants={variants}
      initial="hidden"
      animate={controls}

      {...props}
    >
      {text}
    </motion.div>
  );
};

export { BlurText };
export type { BlurTextProps }; 