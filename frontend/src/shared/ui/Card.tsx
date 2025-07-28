import React from 'react'
import { motion } from 'framer-motion'
import { cn } from '../utils/cn'

interface CardProps {
  variant?: 'default' | 'glass' | 'primary' | 'neumorphism' | 'interactive'
  children: React.ReactNode
  className?: string
  onClick?: () => void
  hover?: boolean
  padding?: 'none' | 'sm' | 'md' | 'lg' | 'xl'
}

const Card = React.forwardRef<HTMLDivElement, CardProps>(
  ({ 
    variant = 'default', 
    children, 
    className, 
    onClick, 
    hover = true,
    padding = 'md',
    ...props 
  }, ref) => {
    const variants = {
      default: 'card',
      glass: 'card-glass',
      primary: 'card-primary',
      neumorphism: 'card-neumorphism',
      interactive: 'card-interactive'
    }

    const paddings = {
      none: '',
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8',
      xl: 'p-12'
    }

    const hoverAnimation = hover && variant !== 'interactive' ? {
      scale: 1.02,
      y: -4,
      boxShadow: variant === 'glass' ? '0 25px 50px -12px rgba(0, 0, 0, 0.25)' :
                  variant === 'primary' ? '0 0 30px rgba(168, 85, 247, 0.4)' :
                  '0 25px 50px -12px rgba(0, 0, 0, 0.25)'
    } : undefined

    const tapAnimation = onClick ? { scale: 0.98 } : undefined

    return (
      <motion.div
        ref={ref}
        className={cn(
          variants[variant],
          paddings[padding],
          onClick && 'cursor-pointer',
          className
        )}
        onClick={onClick}
        whileHover={hoverAnimation}
        whileTap={tapAnimation}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ 
          type: "spring", 
          stiffness: 300, 
          damping: 20,
          opacity: { duration: 0.3 },
          y: { duration: 0.3 }
        }}
        {...props}
      >
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.1, duration: 0.4 }}
        >
          {children}
        </motion.div>
      </motion.div>
    )
  }
)

Card.displayName = 'Card'

export { Card, type CardProps } 