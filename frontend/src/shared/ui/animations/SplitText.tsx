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
  enableGradient = false,
  use3D = false,
  bounceEffect = false,
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

  const getItemVariants = () => {
    if (use3D) {
      return {
        hidden: {
          opacity: 0,
          y: 50,
          scale: 0.5,
          rotateX: -90,
          rotateY: 45,
          z: -100,
        },
        visible: {
          opacity: 1,
          y: 0,
          scale: 1,
          rotateX: 0,
          rotateY: 0,
          z: 0,
        },
      };
    }

    if (bounceEffect) {
      return {
        hidden: {
          opacity: 0,
          y: 100,
          scale: 0,
        },
        visible: {
          opacity: 1,
          y: 0,
          scale: 1,
        },
      };
    }

    return {
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
  };

  const getItemTransition = () => {
    if (bounceEffect) {
      return {
        type: 'spring' as const,
        stiffness: 400,
        damping: 8,
        mass: 0.8,
        duration: duration,
      };
    }

    if (use3D) {
      return {
        type: 'spring' as const,
        stiffness: 200,
        damping: 15,
        mass: 1,
        duration: duration,
      };
    }

    return {
      type: 'spring' as const,
      stiffness: 100,
      damping: 10,
      duration: duration,
    };
  };

  const itemVariants = getItemVariants();
  const itemTransition = getItemTransition();

  return (
    <motion.div
      ref={ref}
      className={cn('inline-block', className)}
      variants={containerVariants}
      initial="hidden"
      animate={controls}
      style={{ 
        perspective: use3D ? '1000px' : undefined,
        transformStyle: use3D ? 'preserve-3d' : undefined,
      }}
      {...props}
    >
      {splitElements.map((element, index) => (
        <motion.span
          key={index}
          className={cn(
            'inline-block',
            enableGradient && 'bg-gradient-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent',
            splitType === 'words' && index > 0 && 'ml-[0.25em]',
            splitType === 'chars' && element === ' ' && 'w-[0.25em]',
            use3D && 'transform-gpu',
            'select-none'
          )}
          variants={itemVariants}
          transition={itemTransition}
          style={{
            display: element === ' ' ? 'inline-block' : 'inline-block',
            transformOrigin: 'center bottom',
            backfaceVisibility: use3D ? 'hidden' : undefined,
          }}
          whileHover={
            !triggerOnScroll ? {
              scale: 1.1,
              rotateZ: Math.random() * 10 - 5,
              transition: { duration: 0.2 }
            } : undefined
          }
        >
          {element === ' ' ? '\u00A0' : element}
        </motion.span>
      ))}
    </motion.div>
  );
};

export default SplitText;
export type { SplitTextProps }; 