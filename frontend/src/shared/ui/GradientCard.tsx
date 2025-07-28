import React from 'react'
import { motion } from 'framer-motion'
import { cn } from '../utils/cn'

interface GradientCardProps {
  children: React.ReactNode
  className?: string
  gradient?: 'primary' | 'secondary' | 'success' | 'custom'
  customGradient?: string
  onClick?: () => void
  hover?: boolean
  glow?: boolean
  padding?: 'none' | 'sm' | 'md' | 'lg' | 'xl'
  borderRadius?: 'md' | 'lg' | 'xl' | '2xl' | '3xl'
}

const GradientCard = React.forwardRef<HTMLDivElement, GradientCardProps>(
  ({ 
    children, 
    className, 
    gradient = 'primary',
    customGradient,
    onClick, 
    hover = true,
    glow = false,
    padding = 'md',
    borderRadius = 'xl',
    ...props 
  }, ref) => {
    const gradients = {
      primary: 'bg-gradient-to-br from-primary-500 via-primary-600 to-secondary-500',
      secondary: 'bg-gradient-to-br from-secondary-400 via-secondary-500 to-secondary-600',
      success: 'bg-gradient-to-br from-success-400 via-success-500 to-success-600',
      custom: customGradient || 'bg-gradient-to-br from-primary-500 to-secondary-500'
    }

    const paddings = {
      none: '',
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8',
      xl: 'p-12'
    }

    const radiuses = {
      md: 'rounded-md',
      lg: 'rounded-lg',
      xl: 'rounded-xl',
      '2xl': 'rounded-2xl',
      '3xl': 'rounded-3xl'
    }

    const hoverAnimation = hover ? {
      scale: 1.02,
      y: -4,
      boxShadow: glow ? 
        '0 25px 50px -12px rgba(168, 85, 247, 0.5), 0 0 30px rgba(249, 115, 22, 0.3)' :
        '0 25px 50px -12px rgba(0, 0, 0, 0.25)'
    } : {}

    const tapAnimation = onClick ? { scale: 0.98 } : {}

    return (
      <motion.div
        ref={ref}
        className={cn(
          'relative overflow-hidden text-white',
          gradients[gradient],
          radiuses[borderRadius],
          paddings[padding],
          glow && 'shadow-glow-lg',
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
        {/* Animated background pattern */}
        <motion.div
          className="absolute inset-0 opacity-20"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Ccircle cx='9' cy='9' r='3'/%3E%3Ccircle cx='51' cy='51' r='3'/%3E%3Ccircle cx='21' cy='21' r='2'/%3E%3Ccircle cx='39' cy='39' r='2'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`
          }}
          animate={{
            backgroundPosition: ['0% 0%', '100% 100%'],
          }}
          transition={{
            duration: 20,
            repeat: Infinity,
            repeatType: 'reverse',
            ease: 'linear'
          }}
        />

        {/* Shimmer effect */}
        <motion.div
          className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent -translate-x-full"
          animate={{
            translateX: ['0%', '200%'],
          }}
          transition={{
            duration: 2,
            repeat: Infinity,
            repeatDelay: 3,
            ease: 'easeInOut'
          }}
        />

        {/* Content */}
        <motion.div
          className="relative z-10"
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

GradientCard.displayName = 'GradientCard'

export { GradientCard, type GradientCardProps } 