import React from 'react'
import { motion, type MotionProps } from 'framer-motion'
import { cn } from '../utils/cn'

interface MotionSlideInProps extends MotionProps {
  children: React.ReactNode
  direction?: 'left' | 'right' | 'up' | 'down'
  distance?: number
  delay?: number
  duration?: number
  className?: string
  once?: boolean
  threshold?: number
}

const MotionSlideIn: React.FC<MotionSlideInProps> = ({
  children,
  direction = 'up',
  distance = 30,
  delay = 0,
  duration = 0.6,
  className,
  once = true,
  threshold = 0.1,
  ...motionProps
}) => {
  const getInitialPosition = () => {
    switch (direction) {
      case 'left':
        return { x: -distance, opacity: 0 }
      case 'right':
        return { x: distance, opacity: 0 }
      case 'up':
        return { y: distance, opacity: 0 }
      case 'down':
        return { y: -distance, opacity: 0 }
      default:
        return { y: distance, opacity: 0 }
    }
  }

  const getFinalPosition = () => {
    return { x: 0, y: 0, opacity: 1 }
  }

  return (
    <motion.div
      className={cn(className)}
      initial={getInitialPosition()}
      whileInView={getFinalPosition()}
      viewport={{ once, amount: threshold }}
      transition={{
        type: "spring",
        stiffness: 100,
        damping: 15,
        delay,
        duration,
        opacity: { duration: duration * 0.8 }
      }}
      {...motionProps}
    >
      {children}
    </motion.div>
  )
}

// HOC wrapper function
export const withSlideIn = <P extends object>(
  Component: React.ComponentType<P>,
  slideProps?: Partial<MotionSlideInProps>
) => {
  const SlideInComponent = React.forwardRef<any, P & Partial<MotionSlideInProps>>(
    ({ className, ...props }, ref) => {
      const { direction, distance, delay, duration, once, threshold, ...componentProps } = props as any
      
      return (
        <MotionSlideIn
          direction={direction || slideProps?.direction}
          distance={distance || slideProps?.distance}
          delay={delay || slideProps?.delay}
          duration={duration || slideProps?.duration}
          once={once ?? slideProps?.once}
          threshold={threshold || slideProps?.threshold}
          className={className}
        >
          <Component ref={ref} {...(componentProps as P)} />
        </MotionSlideIn>
      )
    }
  )

  SlideInComponent.displayName = `withSlideIn(${Component.displayName || Component.name})`
  return SlideInComponent
}

// Utility variants for common use cases
export const SlideInLeft: React.FC<Omit<MotionSlideInProps, 'direction'>> = (props) => (
  <MotionSlideIn direction="left" {...props} />
)

export const SlideInRight: React.FC<Omit<MotionSlideInProps, 'direction'>> = (props) => (
  <MotionSlideIn direction="right" {...props} />
)

export const SlideInUp: React.FC<Omit<MotionSlideInProps, 'direction'>> = (props) => (
  <MotionSlideIn direction="up" {...props} />
)

export const SlideInDown: React.FC<Omit<MotionSlideInProps, 'direction'>> = (props) => (
  <MotionSlideIn direction="down" {...props} />
)

// Stagger children animation
export const StaggerContainer: React.FC<{
  children: React.ReactNode
  stagger?: number
  className?: string
}> = ({ children, stagger = 0.1, className }) => {
  return (
    <motion.div
      className={cn(className)}
      initial="hidden"
      whileInView="visible"
      viewport={{ once: true, amount: 0.1 }}
      variants={{
        hidden: {},
        visible: {
          transition: {
            staggerChildren: stagger
          }
        }
      }}
    >
      {React.Children.map(children, (child, index) => (
        <motion.div
          key={index}
          variants={{
            hidden: { y: 30, opacity: 0 },
            visible: { 
              y: 0, 
              opacity: 1,
              transition: {
                type: "spring",
                stiffness: 100,
                damping: 15
              }
            }
          }}
        >
          {child}
        </motion.div>
      ))}
    </motion.div>
  )
}

export { MotionSlideIn }
export type { MotionSlideInProps } 