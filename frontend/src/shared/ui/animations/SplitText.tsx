import React, { useRef, useEffect, useState } from 'react';
import { motion, useAnimation, useInView } from 'framer-motion';
import { cn } from '../../utils/cn';
import type { SplitTextProps } from './types';

const SplitText: React.FC<SplitTextProps> = ({
  text,
  className,
  delay = 0,
  duration = 0.5,
  splitType = 'chars',
  stagger = 0.05,
  triggerOnScroll = true,
  onComplete,
  ...props
}) => {
  const ref = useRef<HTMLDivElement>(null);
  const isInView = useInView(ref, { once: true, amount: 0.3 });
  const controls = useAnimation();
  const [splitElements, setSplitElements] = useState<string[]>([]);

  useEffect(() => {
    let elements: string[] = [];
    
    switch (splitType) {
      case 'chars':
        elements = text.split('');
        break;
      case 'words':
        elements = text.split(' ');
        break;
      case 'lines':
        elements = text.split('\n');
        break;
      default:
        elements = text.split('');
    }
    
    setSplitElements(elements);
  }, [text, splitType]);

  useEffect(() => {
    if ((triggerOnScroll && isInView) || !triggerOnScroll) {
      controls.start('visible');
    }
  }, [controls, isInView, triggerOnScroll]);

  const containerVariants = {
    hidden: {},
    visible: {
      transition: {
        staggerChildren: stagger,
        delayChildren: delay / 1000,
        onComplete,
      },
    },
  };

  const itemVariants = {
    hidden: {
      opacity: 0,
      y: 20,
      scale: 0.8,
      rotateX: -90,
    },
    visible: {
      opacity: 1,
      y: 0,
      scale: 1,
      rotateX: 0,
    },
  };

  const itemTransition = {
    type: 'spring' as const,
    stiffness: 100,
    damping: 10,
    duration: duration,
  };

  return (
    <motion.div
      ref={ref}
      className={cn('inline-block', className)}
      variants={containerVariants}
      initial="hidden"
      animate={controls}
      style={{ perspective: '1000px' }}
      {...props}
    >
      {splitElements.map((element, index) => (
        <motion.span
          key={index}
          variants={itemVariants}
          transition={itemTransition}
          className="inline-block origin-bottom"
          style={{
            display: splitType === 'lines' ? 'block' : 'inline-block',
            transformOrigin: 'bottom center',
          }}
        >
          {element === ' ' ? '\u00A0' : element}
          {splitType === 'words' && index < splitElements.length - 1 && '\u00A0'}
        </motion.span>
      ))}
    </motion.div>
  );
};

export { SplitText };
export type { SplitTextProps }; 